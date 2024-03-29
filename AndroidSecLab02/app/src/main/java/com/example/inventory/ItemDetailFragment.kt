/*
 * Copyright (C) 2021 The Android Open Source Project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory


import android.app.Activity
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.security.crypto.EncryptedFile
import androidx.security.crypto.MasterKey
import com.example.inventory.data.Item
import com.example.inventory.data.getFormattedPrice
import com.example.inventory.databinding.FragmentItemDetailBinding
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.*

/**
 * [ItemDetailFragment] displays the details of the selected item.
 */
class ItemDetailFragment : Fragment() {
    private val viewModel : InventoryViewModel by activityViewModels {
        InventoryViewModelFactory(
            (activity?.application as InventoryApplication).database.itemDao()
        )
    }
    private val navigationArgs: ItemDetailFragmentArgs by navArgs()

    private var _binding: FragmentItemDetailBinding? = null
    private val binding get() = _binding!!

    lateinit var item: Item

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentItemDetailBinding.inflate(inflater, container, false)
        sharedPreferences = (activity as MainActivity).sharedPreferences
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val id = navigationArgs.itemId
        viewModel.retrieveItem(id).observe(this.viewLifecycleOwner){ selectedItem ->
            item = selectedItem
            bind(item)
        }
    }

    /**
     * Displays an alert dialog to get the user's confirmation before deleting the item.
     */
    private fun showConfirmationDialog() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(getString(android.R.string.dialog_alert_title))
            .setMessage(getString(R.string.delete_question))
            .setCancelable(false)
            .setNegativeButton(getString(R.string.no)) { _, _ -> }
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                deleteItem()
            }
            .show()
    }

    /**
     * Deletes the current item and navigates to the list fragment.
     */
    private fun deleteItem() {
        viewModel.deleteItem(item)
        findNavController().navigateUp()
    }

    private fun editItem() {
        val action = ItemDetailFragmentDirections.actionItemDetailFragmentToAddItemFragment(
            getString(R.string.edit_fragment_title),
            item.id
        )
        this.findNavController().navigate(action)
    }

    /**
     * Called when fragment is destroyed.
     */
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
    private fun blurString(s: String): String {
        return s.replaceRange(2 , s.length-3 , "*".repeat(s.length-5))
    }

    private fun bind(item: Item) {
        binding.apply {
            itemName.text = item.itemName
            itemPrice.text = item.getFormattedPrice()
            itemCount.text = item.quantityInStock.toString()
            itemSource.text = item.source
            if (sharedPreferences.getBoolean("switchHideSensitiveData",false)) {
                providerName.text = blurString(item.providerName)
                providerEmail.text = blurString(item.providerEmail)
                providerPhone.text = blurString(item.providerPhoneNumber)
            }
            else {
                providerName.text = item.providerName
                providerEmail.text = item.providerEmail
                providerPhone.text = item.providerPhoneNumber
            }
            shareItem.isEnabled = !sharedPreferences.getBoolean("switchForbidDataSharing",false)
            sellItem.isEnabled = viewModel.isStockAvailable(item)
            sellItem.setOnClickListener { viewModel.sellItem(item) }
            deleteItem.setOnClickListener { showConfirmationDialog() }
            editItem.setOnClickListener { editItem() }
            shareItem.setOnClickListener { share() }
            saveToFileItem.setOnClickListener { saveToFile() }
        }
    }
    /**
     * Emits a sample share [Intent].
     */
    private fun share() {
        val sharingIntent = Intent(Intent.ACTION_SEND)
        sharingIntent.type = "text/plain"
        val itemText = "Name: ${item.itemName}\n" +
                "Price: ${item.getFormattedPrice()}\n" +
                "${getString(R.string.quantity)} ${item.quantityInStock}\n" +
                "${getString(R.string.provider)} ${item.providerName}\n" +
                "${getString(R.string.provider_email)} ${item.providerEmail}\n" +
                "${getString(R.string.provider_phone)} ${item.providerPhoneNumber}\n"
        sharingIntent.putExtra(Intent.EXTRA_TEXT, itemText)
        startActivity(Intent.createChooser(sharingIntent, null))
    }

    private fun saveToFile(){
        val json = Json.encodeToString(item)
        fileContent = json
        createFile(item.itemName)
    }

    private fun createFile(filename:String) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, "${filename}.json")
        }
        startActivityForResult(intent, CREATE_FILE)
    }

    override fun onActivityResult(
        requestCode: Int, resultCode: Int, resultData: Intent?) {
        if (requestCode == CREATE_FILE
            && resultCode == Activity.RESULT_OK) {
            // The result data contains a URI for the document or directory that
            // the user selected.
            resultData?.data?.also { uri ->
                // Perform operations on the document using its URI.
                alterDocument(uri)
            }
        }
    }

    companion object {
        // Request code for creating a PDF document.
        const val CREATE_FILE = 1
    }

    private var fileContent: String = ""

    // Achtung!!! horrible crutch
    private fun alterDocument(uri: Uri) {
        try {
            //Write encrypted temp file to cache dir
            val file = File(requireContext().cacheDir, "secret_data")
            if (file.exists()) file.delete()
            writeBytesToEncryptedFile(file, fileContent.toByteArray())

            //Read encrypted temp file contents from cache dir
            val encryptedContents = readBytesFromFile(file)

            //Write encrypted temp file content to user chosen location
            writeBytesToUri(uri, encryptedContents)

            //Remove temp file from cache dir
            file.delete()

        } catch (e: FileNotFoundException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    private fun writeBytesToUri(uri: Uri, encryptedContents: ByteArray) {
        requireContext().applicationContext.contentResolver.openFileDescriptor(uri, "w")?.use {
            FileOutputStream(it.fileDescriptor).use {
                it.write(encryptedContents)
            }
        }
    }

    private fun writeBytesToEncryptedFile(file: File, bytes: ByteArray) {
        val mainKey = MasterKey.Builder(requireContext())
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        val encryptedFile = EncryptedFile.Builder(
            requireContext(),
            file,
            mainKey,
            EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB
        ).build()
        encryptedFile.openFileOutput().apply {
            write(bytes)
            flush()
            close()
        }
    }

    private fun readBytesFromFile(file: File) : ByteArray{
        val byteBuffer = ByteArrayOutputStream()
        requireContext().applicationContext.contentResolver.openInputStream(file.toUri())?.use { inputStream ->
            val bufferSize = 1024
            val buffer = ByteArray(bufferSize)

            var len: Int
            while (inputStream.read(buffer).also { len = it } != -1) {
                byteBuffer.write(buffer, 0, len)
            }
        }
        return byteBuffer.toByteArray()
    }

}
