package com.android.example.thelanguagelion.ui.lesson

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class LessonViewModel : ViewModel() {
    private lateinit var wordList: MutableList<String>

    companion object {
        private const val DONE = 0L
        private const val ONE_SECOND = 1000L
        private const val ONE_MINUTE = 60000L
    }

    private var _exercise = MutableLiveData<String>()
    val exercise: LiveData<String>
        get() = _exercise

    private var _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score


    init {
        _exercise.value = ""
        _score.value = 0

        createList()
        nextExercise()
    }


    private fun createList() {
        wordList = mutableListOf(
            "queen",
            "hospital",
            "basketball",
            "cat",
            "change",
            "snail",
            "soup",
            "calendar",
            "sad",
            "desk",
            "guitar",
            "home",
            "railway",
            "zebra",
            "jelly",
            "car",
            "crow",
            "trade",
            "bag",
            "roll",
            "bubble"
        )
        wordList.shuffle()
    }

    private fun nextExercise() {
        if(!wordList.isEmpty()) {
            _exercise.value = wordList.removeAt(0)
        } else {
            _exercise.value = "Done!"
        }
    }

    fun onCheck(answer: String) {
        Log.i("LessonViewModel", "Exercise: ${_exercise.value} - answer: $answer - equals: ${_exercise.value == answer}")
        if(_exercise.value == answer) {
            _score.value = (score.value)?.plus(1)
        }
        nextExercise()
    }
}