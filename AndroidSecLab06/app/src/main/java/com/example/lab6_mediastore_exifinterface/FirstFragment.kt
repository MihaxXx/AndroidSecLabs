package com.example.lab6_mediastore_exifinterface

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.lab6_mediastore_exifinterface.data.toStringPretty
import com.example.lab6_mediastore_exifinterface.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    private lateinit var mainActivity: MainActivity

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity = activity as MainActivity
        binding.buttonFirst.setOnClickListener {
            val exifData = mainActivity.getExifData()
            val bundle = Bundle()
            bundle.putParcelable("ExifTags", exifData)
            findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
        }
        binding.buttonUploadImg.setOnClickListener {
            mainActivity.onUploadClick()
        }
        restoreData()
    }

    private fun restoreData() {
        val uri = mainActivity.getImageUri()
        if (uri != null) {
            binding.imageView.setImageURI(uri)
        }
        binding.exifTagsLabel.text = mainActivity.getExifData().toStringPretty()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}