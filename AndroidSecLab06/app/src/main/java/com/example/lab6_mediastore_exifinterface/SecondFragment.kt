package com.example.lab6_mediastore_exifinterface

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.lab6_mediastore_exifinterface.data.ExifData
import com.example.lab6_mediastore_exifinterface.data.Geo
import com.example.lab6_mediastore_exifinterface.data.getGeo
import com.example.lab6_mediastore_exifinterface.databinding.FragmentSecondBinding
import com.google.android.material.snackbar.Snackbar

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private lateinit var mainActivity: MainActivity

    private var _binding: FragmentSecondBinding? = null
    private var exifData: ExifData? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentSecondBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        binding.buttonSecond.setOnClickListener {
            updateExifData(it)
            findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
        }

        exifData = mainActivity.getExifData()
        if (exifData == null) {
            return
        }

        if (exifData!!.date != null) {
            binding.editDate.setText(exifData!!.date)
        }
        if (exifData!!.model != null) {
            binding.editModel.setText(exifData!!.model)
        }
        if (exifData!!.device != null) {
            binding.editDevice.setText(exifData!!.device)
        }

        val geo = exifData!!.getGeo() ?: return
        binding.editLatitude.setText(String.format("%.3f", geo.latitude))
        binding.editLongitude.setText(String.format("%.3f", geo.longitude))
    }

    private fun updateExifData(view: View) {
        val newExifData = exifData ?: return
        newExifData.date = binding.editDate.text.toString()
        newExifData.model = binding.editModel.text.toString()
        newExifData.device = binding.editDevice.text.toString()
        var newGeo: Geo? = null
        if (binding.editLatitude.text.isNotEmpty() && binding.editLongitude.text.isNotEmpty()) {
            val lat = binding.editLatitude.text.toString().toDoubleOrNull()
            val long = binding.editLongitude.text.toString().toDoubleOrNull()
            if (lat == null || long == null) {
                Snackbar.make(view, "Invalid latitude and/or longitude", Snackbar.LENGTH_LONG)
                    .show()
                return
            }
            newGeo = Geo(lat, long)
        }
        mainActivity.updateExifData(newExifData, newGeo)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}