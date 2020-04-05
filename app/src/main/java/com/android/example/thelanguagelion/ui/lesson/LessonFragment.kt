package com.android.example.thelanguagelion.ui.lesson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.example.thelanguagelion.R
import com.android.example.thelanguagelion.databinding.FragmentLessonBinding
import com.google.android.material.bottomnavigation.BottomNavigationView


class LessonFragment : Fragment() {
    private lateinit var binding: FragmentLessonBinding
    private lateinit var viewModel: LessonViewModel

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_lesson, container, false)
        viewModel = ViewModelProviders.of(this).get(LessonViewModel::class.java)

        binding.lessonViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        binding.buttonCheck.setOnClickListener {
            viewModel.onCheck(binding.editAnswer.text.toString())
            binding.editAnswer.setText("")
        }

        return binding.root
    }

    private fun lessonFinished() {
    }
}