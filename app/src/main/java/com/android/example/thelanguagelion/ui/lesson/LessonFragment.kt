package com.android.example.thelanguagelion.ui.lesson

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.android.example.thelanguagelion.R
import com.android.example.thelanguagelion.databinding.FragmentLessonBinding


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

        // Also check the answer if the enter key is pressed
        binding.editAnswer.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    viewModel.onCheck(binding.editAnswer.text.toString())
                    binding.editAnswer.setText("")
                    true
                }
                else -> false
            }
        }

        return binding.root
    }

    private fun lessonFinished() {
    }
}