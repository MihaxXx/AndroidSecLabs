package com.example.inventory

import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.inventory.databinding.FragmentItemDetailBinding
import com.example.inventory.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    lateinit var sharedPreferences:SharedPreferences
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        sharedPreferences = (activity as MainActivity).sharedPreferences
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (sharedPreferences.getString("defaultProviderName","") != null)
        {
            binding.apply {
                defaultProviderName.setText(sharedPreferences.getString("defaultProviderName",""))
                defaultProviderEmail.setText(sharedPreferences.getString("defaultProviderEmail",""))
                defaultProviderPhone.setText(sharedPreferences.getString("defaultProviderPhone",""))
                switchSubstituteDefaultValues.isChecked = sharedPreferences.getBoolean("switchSubstituteDefaultValues",false)
                switchHideSensitiveData.isChecked = sharedPreferences.getBoolean("switchHideSensitiveData",false)
                switchForbidDataSharing.isChecked = sharedPreferences.getBoolean("switchForbidDataSharing",false)
                saveButton.setOnClickListener { save() }
            }
        }

    }
    private fun save(){
        sharedPreferences.edit()
            .putString("defaultProviderName", binding.defaultProviderName.text.toString())
            .putString("defaultProviderEmail", binding.defaultProviderEmail.text.toString())
            .putString("defaultProviderPhone", binding.defaultProviderPhone.text.toString())
            .putBoolean("switchSubstituteDefaultValues", binding.switchSubstituteDefaultValues.isChecked)
            .putBoolean("switchHideSensitiveData", binding.switchHideSensitiveData.isChecked)
            .putBoolean("switchForbidDataSharing", binding.switchForbidDataSharing.isChecked)
            .apply()
        findNavController().popBackStack()
    }
}