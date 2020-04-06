package com.android.example.thelanguagelion.ui.lesson

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import simplenlg.framework.NLGFactory
import simplenlg.framework.SemElement
import simplenlg.realiser.Realiser
import simplenlg.lexicon.english.XMLLexicon as englishXMLLexicon
import simplenlg.lexicon.dutch.XMLLexicon as dutchXMLLexicon
import simplenlg.semantics.Semanticon
import java.io.File
import java.io.IOException

class LessonViewModel(application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private var semanticon : Semanticon
    private var englishLexicon : englishXMLLexicon
    private var dutchLexicon : dutchXMLLexicon
    private var englishFactory : NLGFactory
    private var dutchFactory : NLGFactory
    private var realiser : Realiser

    private lateinit var wordList: MutableList<SemElement>
    private lateinit var currSem: SemElement

    companion object {
        private const val DONE = 0L
        private const val ONE_SECOND = 1000L
        private const val ONE_MINUTE = 60000L

    }

    private var _exercise = MutableLiveData<String>()
    val exercise: LiveData<String>
        get() = _exercise

    private var _answer = MutableLiveData<String>()
    val answer: LiveData<String>
        get() = _answer

    private var _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score


    init {
        Log.i("LessonViewModel", "Lesson started!")
        _exercise.value = "loading..."
        _answer.value = ""
        _score.value = 0

        val semanticonPath = getFileFromAssets("semanticon.xml").absolutePath
        val englishLexPath = getFileFromAssets("english-lexicon.xml").absolutePath
        val dutchLexPath = getFileFromAssets("dutch-lexicon.xml").absolutePath

        Log.i("LessonViewModel", semanticonPath)

        semanticon = Semanticon(semanticonPath)
        englishLexicon = englishXMLLexicon(englishLexPath)
        dutchLexicon = dutchXMLLexicon(dutchLexPath)
        englishFactory = NLGFactory(englishLexicon)
        dutchFactory = NLGFactory(dutchLexicon)
        realiser = Realiser()

        Log.i("LessonViewModel", "SimpleNLG initialized!")

        createList()
        nextExercise()
    }


    private fun createList() {
        val sems_food_items = semanticon.getSemsInCategory("food_item")
        Log.i("LessonViewModel", sems_food_items.toString())

        wordList = sems_food_items.toMutableList()
        wordList.shuffle()
    }

    private fun nextExercise() {
        if(!wordList.isEmpty()) {
            currSem = wordList.removeAt(0)
            _exercise.value = currSem.english[0]
            _answer.value = currSem.dutch[0]
        } else {
            _exercise.value = "Done!"
        }
    }

    fun onCheck(answer: String) {
        Log.i("LessonViewModel", "Exercise: ${_answer.value} - answer: $answer - equals: ${_answer.value == answer}")
        if(_answer.value == answer) {
            _score.value = (score.value)?.plus(1)
        }
        nextExercise()
    }

    @Throws(IOException::class)
    fun getFileFromAssets(fileName: String): File = File(context.cacheDir, fileName)
        .also {
            it.outputStream().use { cache ->
                context.assets.open(fileName).use {
                    it.copyTo(cache)
                }
            }
        }
}