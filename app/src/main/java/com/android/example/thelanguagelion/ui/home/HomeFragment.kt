package com.android.example.thelanguagelion.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.android.example.thelanguagelion.R
import com.android.example.thelanguagelion.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding
    private lateinit var viewModel: HomeViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        viewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)

        binding.homeViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.buttonPractice.setOnClickListener {
            binding.textHome.visibility = View.GONE
            binding.buttonPractice.visibility = View.GONE
            binding.loadingSpinner.visibility = View.VISIBLE
            findNavController().navigate(HomeFragmentDirections.actionNavigationHomeToNavigationLesson())
        }

        return binding.root
    }
}
