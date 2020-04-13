package com.android.example.thelanguagelion.ui.lesson

import android.app.Application
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.android.example.thelanguagelion.database.SememeDatabaseDao
import simplenlg.framework.NLGFactory
import simplenlg.framework.SemElement
import simplenlg.realiser.Realiser
import simplenlg.semantics.Semanticon
import java.io.File
import java.io.IOException
import simplenlg.lexicon.dutch.XMLLexicon as dutchXMLLexicon
import simplenlg.lexicon.english.XMLLexicon as englishXMLLexicon

class LessonViewModel(val database: SememeDatabaseDao, application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private var semanticon : Semanticon
    private var englishLexicon : englishXMLLexicon
    private var dutchLexicon : dutchXMLLexicon
    private var englishFactory : NLGFactory
    private var dutchFactory : NLGFactory
    private var realiser : Realiser

    private lateinit var wordList: MutableList<SemElement>
    private lateinit var currSem: SemElement

    private val timer: CountDownTimer

    companion object {
        private const val DONE = 0L
        private const val ONE_SECOND = 1000L
        private const val ONE_MINUTE = 60000L
        private const val FIVE_MINUTES = 300000L
        private const val TEN_MINUTES = 600000L
    }

    private var _task = MutableLiveData<String>()
    val task: LiveData<String>
        get() = _task

    private var _exercise = MutableLiveData<String>()
    val exercise: LiveData<String>
        get() = _exercise

    private var _answer = MutableLiveData<String>()
    val answer: LiveData<String>
        get() = _answer

    private var _score = MutableLiveData<Int>()
    val score: LiveData<Int>
        get() = _score

    private var _currentTime = MutableLiveData<Long>()
    private val currentTime: LiveData<Long>
        get() = _currentTime

    private var _lessonStatus = MutableLiveData<LessonStatus>()
    val lessonStatus: LiveData<LessonStatus>
        get() = _lessonStatus

    private var _eventLessonFinish = MutableLiveData<Boolean>()
    val eventLessonFinish: LiveData<Boolean>
        get() = _eventLessonFinish

    val currentTimeString = Transformations.map(currentTime) { time ->
        DateUtils.formatElapsedTime(time)
    }


    init {
        _task.value = ""
        _exercise.value = ""
        _answer.value = ""
        _score.value = 0
        _lessonStatus.value = LessonStatus.INPROGRESS

        val semanticonPath = getFileFromAssets("semanticon.xml").absolutePath
        val englishLexPath = getFileFromAssets("english-lexicon.xml").absolutePath
        val dutchLexPath = getFileFromAssets("dutch-lexicon.xml").absolutePath

        semanticon = Semanticon(semanticonPath)
        englishLexicon = englishXMLLexicon(englishLexPath)
        dutchLexicon = dutchXMLLexicon(dutchLexPath)
        englishFactory = NLGFactory(englishLexicon)
        dutchFactory = NLGFactory(dutchLexicon)
        realiser = Realiser()

        timer = object : CountDownTimer(ONE_MINUTE, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = millisUntilFinished / ONE_SECOND
            }

            override fun onFinish() {
                _currentTime.value = DONE
                onLessonFinish()
            }
        }

        createList()
        nextExercise()

        timer.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }


    private fun createList() {
        val semsFoodItems = semanticon.getSemsInCategory("food_item")
        Log.i("LessonViewModel", semsFoodItems.toString())

        wordList = semsFoodItems.toMutableList()
        wordList.shuffle()
    }

    fun nextExercise() {
        if(wordList.isNotEmpty()) {
            currSem = wordList.removeAt(0)
            _task.value = "Translate to Dutch"
            _exercise.value = currSem.english[0]
            _answer.value = currSem.dutch[0]
            _lessonStatus.value = LessonStatus.INPROGRESS
        } else {
            onLessonFinish()
        }
    }

    fun onCheck(answer: String) {
        Log.i("LessonViewModel", "Exercise: ${_answer.value} - answer: $answer - equals: ${_answer.value == answer}")
        if(_answer.value == answer) {
            correctAnswer()
        } else {
            incorrectAnswer()
        }
    }

    fun dontKnow() {
        incorrectAnswer()
    }


    private fun correctAnswer() {
        _score.value = (score.value)?.plus(1)
        _lessonStatus.value = LessonStatus.CORRECT
    }

    private fun incorrectAnswer() {
        _lessonStatus.value = LessonStatus.INCORRECT
    }


    fun onLessonFinish() {
        _eventLessonFinish.value = true
    }

    fun onLessonFinishComplete() {
        _eventLessonFinish.value = false
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