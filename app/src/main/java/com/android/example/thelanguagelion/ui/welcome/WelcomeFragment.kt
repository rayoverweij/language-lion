package com.android.example.thelanguagelion.ui.welcome

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController

import com.android.example.thelanguagelion.R
import com.android.example.thelanguagelion.database.StudentDatabase
import com.android.example.thelanguagelion.databinding.FragmentWelcomeBinding

class WelcomeFragment : Fragment() {
    private lateinit var binding: FragmentWelcomeBinding
    private lateinit var viewModel: WelcomeViewModel
    private lateinit var viewModelFactory: WelcomeViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_welcome, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = StudentDatabase.getInstance(application).studentDatabaseDao
        viewModelFactory = WelcomeViewModelFactory(dataSource, application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(WelcomeViewModel::class.java)

        binding.welcomeViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.buttonWelcome.setOnClickListener {
            viewModel.startApp(binding.editName.text.toString())
        }

        viewModel.eventMovingOn.observe(viewLifecycleOwner, Observer<Boolean> { hasMovedOn ->
            if(hasMovedOn) movedOn()
        })

        return binding.root
    }

    private fun movedOn() {
        NavHostFragment.findNavController(this).navigate(WelcomeFragmentDirections.actionNavigationWelcomeToNavigationHome())
        viewModel.onMovingOnFinish()
    }
}
