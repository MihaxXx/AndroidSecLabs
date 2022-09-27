package com.example.android.firebaseui_login_sample

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.observe
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.android.firebaseui_login_sample.databinding.FragmentAccountSettingsBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import android.provider.MediaStore
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL

class AccountSettingsFragment : Fragment() {

    companion object {
        const val TAG = "AccountSettingsFragment"
    }

    // Get a reference to the ViewModel scoped to this Fragment.
    private val viewModel by viewModels<LoginViewModel>()

    private lateinit var binding: FragmentAccountSettingsBinding

    private val pickImage = 100
    private var imageUri: Uri? = null

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment.
        binding = DataBindingUtil.inflate<FragmentAccountSettingsBinding>(
            inflater, R.layout.fragment_account_settings, container, false
        )
        val currentUser = FirebaseAuth.getInstance().currentUser
        if(currentUser?.photoUrl != null) {
            val url = URL(currentUser?.photoUrl.toString())
            //val bitmap = BitmapFactory.decodeStream( url.openConnection().getInputStream())
            //binding.avatarImageView.setImageBitmap( bitmap )

            val result: Deferred<Bitmap?> = lifecycleScope.async(Dispatchers.IO) {
                url.toBitmap
            }
            lifecycleScope.launch(Dispatchers.Main) {
                // show bitmap on image view when available
                binding.avatarImageView.setImageBitmap(result.await())
            }
        }
        binding.editTextTextPersonName.setText(currentUser?.displayName)
        binding.editTextTextEmailAddress.setText(currentUser?.email)
        binding.authMethodTextView.setText(currentUser?.providerData?.get(1)?.providerId ?:"" )
        binding.editTextAvatarURL.setText(currentUser?.photoUrl.toString())
        binding.saveAccountSettingsButton.setOnClickListener{ saveChanges() }
        binding.DeleteAccountButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser!!

            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User account deleted.")
                    }
                }
            AuthUI.getInstance().signOut(requireContext())
        }
        //binding.authButton.setOnClickListener { launchSignInFlow() }
        imageUri = null

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        navController = findNavController()
        viewModel.authenticationState.observe(viewLifecycleOwner, Observer { authenticationState ->
            when (authenticationState) {
                LoginViewModel.AuthenticationState.AUTHENTICATED -> Log.i(SettingsFragment.TAG, "Authenticated")
                // If the user is not logged in, they should not be able to set any preferences,
                // so navigate them to the login fragment
                LoginViewModel.AuthenticationState.UNAUTHENTICATED -> navController.navigate(
                    R.id.loginFragment
                )
                else -> Log.e(
                    SettingsFragment.TAG, "New $authenticationState state that doesn't require any UI change"
                )
            }
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK && requestCode == pickImage) {
            imageUri = data?.data
            binding.avatarImageView.setImageURI(imageUri)
        }
    }

    private fun saveChanges(){
        val user = FirebaseAuth.getInstance().currentUser

        //TODO: Update image
        if (user?.displayName != binding.editTextTextPersonName.text.toString() || user?.photoUrl.toString() != binding.editTextAvatarURL.text.toString()) {
            val profileUpdates = UserProfileChangeRequest.Builder().apply {
                if (user?.displayName != binding.editTextTextPersonName.text.toString())
                    setDisplayName(binding.editTextTextPersonName.text.toString())
                if (user?.photoUrl.toString() != binding.editTextAvatarURL.text.toString())
                    setPhotoUri(Uri.parse(binding.editTextAvatarURL.text.toString()))
            }.build()
            user!!.updateProfile(profileUpdates)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User profile updated.")
                    }
                }
        }
        if (user?.email != binding.editTextTextEmailAddress.text.toString().trim())
            user!!.updateEmail(binding.editTextTextEmailAddress.text.toString().trim())
    }
    private val URL.toBitmap: Bitmap?
        get() {
            return try {
                BitmapFactory.decodeStream(openStream())
            }catch (e: IOException){null}
        }
}
