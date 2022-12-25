package com.example.lab6_mediastore_exifinterface

import android.app.Activity
import android.app.RecoverableSecurityException
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.exifinterface.media.ExifInterface
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import com.example.lab6_mediastore_exifinterface.data.*
import com.example.lab6_mediastore_exifinterface.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import java.io.IOException
import java.util.*

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding


    lateinit var button: Button
    private val pickImage = 100
    private var imageUri: Uri? = null
    private var exifData: ExifData? = null

    private lateinit var newExifData: ExifData
    private var newGeo: Geo? = null

    fun getExifData(): ExifData? {
        return exifData
    }

    fun getImageUri(): Uri? {
        return imageUri
    }

    fun onUploadClick() {
        val gallery = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI)
        startActivityForResult(gallery, pickImage)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)


        button = findViewById(R.id.buttonUploadImg)
        button.setOnClickListener {
            onUploadClick()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            findViewById<ImageView>(R.id.imageView).setImageURI(imageUri)
            loadExifInfo()
        }
    }

    private fun loadExifInfo() {
        val tag = "loadExifInfo"
        if (imageUri == null) {
            Log.e(tag, "imageUri was null")
            return
        }
        try {
            this.contentResolver.openInputStream(imageUri!!).use { inputStream ->
                val exif = inputStream?.let { ExifInterface(it) }
                if (exif == null) {
                    Log.e(tag, "exif was null")
                    return
                }
                exifData = ExifData(
                    exif.getAttribute(ExifInterface.TAG_DATETIME),
                    exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE),
                    exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF),
                    exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE),
                    exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF),
                    exif.getAttribute(ExifInterface.TAG_MAKE),
                    exif.getAttribute(ExifInterface.TAG_MODEL),
                )
                Log.i(
                    tag,
                    "get location ${exifData?.latitude} ${exifData?.longitude} -> ${
                        exifData?.getGeo().toString()
                    }"
                )
                findViewById<TextView>(R.id.exifTagsLabel).text = exifData.toStringPretty()
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }
    }

    fun updateExifData(newExifData: ExifData, newGeo: Geo?) {
        this.newExifData = newExifData
        this.newGeo = newGeo
        try {
            writeExifData()
        } catch (securityException: SecurityException) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                throw securityException
            }
            val recoverableSecurityException =
                securityException as? RecoverableSecurityException ?: throw securityException
            val intentSender = recoverableSecurityException.userAction.actionIntent.intentSender
            requestUri.launch(IntentSenderRequest.Builder(intentSender).build())
        }
    }

    private var requestUri =
        registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
            if (result != null && result.resultCode == Activity.RESULT_OK) {
                writeExifData()
            }
        }


    private fun writeExifData() {
        val tag = "writeExifData"
        if (imageUri == null) {
            Log.e(tag, "imageUri was null")
            return
        }
        applicationContext.contentResolver.openFileDescriptor(imageUri!!, "rw", null)?.use {
            val exif = ExifInterface(it.fileDescriptor)

            if (newExifData.date != null) {
                exif.setAttribute(ExifInterface.TAG_DATETIME, newExifData.date)
            }
            if (newExifData.device != null) {
                exif.setAttribute(ExifInterface.TAG_MAKE, newExifData.device)
            }
            if (newExifData.model != null) {
                exif.setAttribute(ExifInterface.TAG_MODEL, newExifData.model)
            }
            if (newGeo != null) {
                Log.i(tag, "saving location ${newGeo.toString()}")
                exif.setLatLong(newGeo!!.latitude, newGeo!!.longitude)
            }
            lifecycleScope.launch {
                updating(exif)
            }
        }
    }

    private suspend fun updating(exif: ExifInterface) {
        exif.saveAttributes()
        delay(1000L)
        loadExifInfo()
    }

}