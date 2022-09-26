package com.example.android.firebaseui_login_sample

import android.app.Activity
import android.content.Intent
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
import com.example.android.firebaseui_login_sample.databinding.FragmentLoginBinding
import com.example.android.firebaseui_login_sample.databinding.FragmentMainBinding
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth

class AccountSettingsFragment : Fragment() {

    companion object {
        const val TAG = "AccountSettingsFragment"
    }

    // Get a reference to the ViewModel scoped to this Fragment.
    private val viewModel by viewModels<LoginViewModel>()

    private lateinit var binding: FragmentAccountSettingsBinding

    private lateinit var navController: NavController

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment.
        binding = DataBindingUtil.inflate<FragmentAccountSettingsBinding>(
            inflater, R.layout.fragment_account_settings, container, false
        )
        val currentUser = FirebaseAuth.getInstance().currentUser
        binding.avatarImageView.setImageURI(currentUser?.photoUrl)
        binding.editTextTextPersonName.setText(currentUser?.displayName)
        binding.editTextTextEmailAddress.setText(currentUser?.email)
        binding.authMethodTextView.setText(currentUser?.providerId)
        binding.changeAvatarButton.setOnClickListener{ changeAvatar() }
        binding.saveAccountSettingsButton.setOnClickListener{ saveChanges() }
        binding.DeleteAccountButton.setOnClickListener {
            val user = FirebaseAuth.getInstance().currentUser!!

            user.delete()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "User account deleted.")
                    }
                }
        }
        //binding.authButton.setOnClickListener { launchSignInFlow() }

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
    private fun changeAvatar(){

    }

    private fun saveChanges(){

    }
}