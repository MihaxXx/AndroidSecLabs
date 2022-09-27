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
import com.example.android.firebaseui_login_sample.databinding.FragmentSomeProtectedBinding
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.net.URL

class SomeProtectedFragment : Fragment() {

    companion object {
        const val TAG = "SomeProtectedFragment"
    }

    // Get a reference to the ViewModel scoped to this Fragment.
    private val viewModel by viewModels<LoginViewModel>()
    private lateinit var binding: FragmentSomeProtectedBinding
    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment.
        binding = DataBindingUtil.inflate<FragmentSomeProtectedBinding>(
            inflater, R.layout.fragment_some_protected, container, false
        )
        val currentUser = FirebaseAuth.getInstance().currentUser

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
}
