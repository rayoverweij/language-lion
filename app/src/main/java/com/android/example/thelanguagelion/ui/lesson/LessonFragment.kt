package com.android.example.thelanguagelion.ui.lesson

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.NavHostFragment
import com.android.example.thelanguagelion.R
import com.android.example.thelanguagelion.database.StudentDatabase
import com.android.example.thelanguagelion.databinding.FragmentLessonBinding


class LessonFragment : Fragment() {
    private lateinit var binding: FragmentLessonBinding
    private lateinit var viewModel: LessonViewModel
    private lateinit var viewModelFactory: LessonViewModelFactory

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_lesson, container, false)
        val application = requireNotNull(this.activity).application
        val dataSource = StudentDatabase.getInstance(application).studentDatabaseDao
        viewModelFactory = LessonViewModelFactory(dataSource, application)
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(LessonViewModel::class.java)

        binding.lessonViewModel = viewModel
        binding.lifecycleOwner = viewLifecycleOwner

        // Button click listeners
        binding.buttonCheck.setOnClickListener {
            checkAnswer()
        }

        binding.buttonDontknow.setOnClickListener {
            viewModel.dontKnow()
        }

        binding.buttonContinue.setOnClickListener {
            viewModel.nextExercise()
        }

        // Also check the answer if the enter key is pressed
        binding.editAnswer.setOnEditorActionListener { _, actionId, _ ->
            return@setOnEditorActionListener when (actionId) {
                EditorInfo.IME_ACTION_SEND -> {
                    checkAnswer()
                    true
                }
                else -> false
            }
        }

        // Change the UI based on the status of the current question
        viewModel.lessonStatus.observe(viewLifecycleOwner, Observer<LessonStatus> { lessonStatus ->
            when (lessonStatus) {
                LessonStatus.INPROGRESS -> {
                    activity!!.window.statusBarColor = resources.getColor(R.color.colorPrimaryDark)
                    binding.buttonCheck.visibility = View.VISIBLE
                    binding.buttonDontknow.visibility = View.VISIBLE
                    binding.buttonContinue.visibility = View.GONE
                    //binding.editAnswer.isEnabled = true
                    //binding.editAnswer.isFocusable = true
                }
                else -> {
                    binding.buttonCheck.visibility = View.GONE
                    binding.buttonDontknow.visibility = View.GONE
                    binding.buttonContinue.visibility = View.VISIBLE
                    //binding.editAnswer.isEnabled = false
                    //binding.editAnswer.isFocusable = false

                    if(lessonStatus == LessonStatus.CORRECT) {
                        activity!!.window.statusBarColor = resources.getColor(R.color.colorCorrect)
                        binding.buttonContinue.setBackgroundColor(resources.getColor(R.color.colorCorrect))
                    } else {
                        activity!!.window.statusBarColor = resources.getColor(R.color.colorMistake)
                        binding.buttonContinue.setBackgroundColor(resources.getColor(R.color.colorMistake))
                    }
                }
            }
        })

        viewModel.eventLessonFinish.observe(viewLifecycleOwner, Observer<Boolean> { hasFinished ->
            if (hasFinished) lessonFinished()
        })

        return binding.root
    }

    private fun checkAnswer() {
        viewModel.onCheck(binding.editAnswer.text.toString())
        val inputMethodManager = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(view!!.windowToken, 0)
        binding.editAnswer.setText("")
    }

    private fun lessonFinished() {
        val action = LessonFragmentDirections.actionNavigationLessonToNavigationScore()
        action.score = viewModel.score.value ?: 0
        NavHostFragment.findNavController(this).navigate(action)
        viewModel.onLessonFinishComplete()
    }
}