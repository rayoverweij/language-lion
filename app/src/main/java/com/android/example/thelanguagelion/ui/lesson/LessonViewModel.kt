package com.android.example.thelanguagelion.ui.lesson

import android.app.Application
import android.os.CountDownTimer
import android.text.format.DateUtils
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import com.android.example.thelanguagelion.database.Profile
import com.android.example.thelanguagelion.database.Sememe
import com.android.example.thelanguagelion.database.StudentDatabaseDao
import com.android.example.thelanguagelion.getFileFromAssets
import kotlinx.coroutines.*
import simplenlg.framework.NLGFactory
import simplenlg.framework.SemElement
import simplenlg.realiser.Realiser
import simplenlg.semantics.Semanticon
import java.util.*
import simplenlg.lexicon.dutch.XMLLexicon as dutchXMLLexicon
import simplenlg.lexicon.english.XMLLexicon as englishXMLLexicon

class LessonViewModel(val database: StudentDatabaseDao, application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var semanticon : Semanticon
    private var englishLexicon : englishXMLLexicon
    private var dutchLexicon : dutchXMLLexicon
    private var englishFactory : NLGFactory
    private var dutchFactory : NLGFactory
    private var realiser : Realiser

    private lateinit var sememes: List<Sememe>
    private lateinit var primQueue: LinkedList<String>
    private lateinit var secQueue: LinkedList<String>
    private lateinit var qHead: String
    private lateinit var currSem: SemElement
    private lateinit var currEntry: Sememe

    private val timer: CountDownTimer
    private val positiveFeedback = listOf("Correct!", "Excellent!", "Good job!", "Well done!", "Nice!")

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

    private var _feedback = MutableLiveData<String>()
    val feedback: LiveData<String>
        get() = _feedback

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
        _feedback.value = ""
        _score.value = 0
        _lessonStatus.value = LessonStatus.INPROGRESS

        val semanticonPath = getFileFromAssets("semanticon.xml", context).absolutePath
        val englishLexPath = getFileFromAssets("english-lexicon.xml", context).absolutePath
        val dutchLexPath = getFileFromAssets("dutch-lexicon.xml", context).absolutePath

        semanticon = Semanticon(semanticonPath)
        englishLexicon = englishXMLLexicon(englishLexPath)
        dutchLexicon = dutchXMLLexicon(dutchLexPath)
        englishFactory = NLGFactory(englishLexicon)
        dutchFactory = NLGFactory(dutchLexicon)
        realiser = Realiser()

        timer = object : CountDownTimer(FIVE_MINUTES, ONE_SECOND) {
            override fun onTick(millisUntilFinished: Long) {
                _currentTime.value = millisUntilFinished / ONE_SECOND
            }

            override fun onFinish() {
                _currentTime.value = DONE
                onLessonFinish()
            }
        }

        uiScope.launch {
            sememes = getAllSememes()
            primQueue = getPrimQueue()
            secQueue = getSecQueue()
            nextExercise()
            timer.start()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timer.cancel()
    }


    fun nextExercise() {
        if(primQueue.isNotEmpty()) {
            uiScope.launch {
                qHead = primQueue.pop()
                Log.i("Lesson", qHead)
                currSem = semanticon.getSemById(qHead)
                Log.i("Lesson", currSem.toString())
                currEntry = currEntry(qHead)!!
                Log.i("Lesson", currEntry.stage.toString())

                var dutchToEng = false
                if(currEntry.stage > 4) {
                    _task.value = "Translate to Dutch"
                } else {
                    _task.value = "Translate to English"
                    dutchToEng = true
                }

                val dutchClause = dutchFactory.createClause()
                val englishClause = englishFactory.createClause()

                val dutchWord = currSem.dutch.random()
                val englishWord = currSem.english.random()

                if(currSem.categories.contains("verb_transitive") || currSem.categories.contains("verb_intransitive")) {
                    val subj = getRandomPP()

                    dutchClause.setSubject(subj.dutch.random())
                    englishClause.setSubject(subj.english.random())

                    dutchClause.setVerb(dutchWord)
                    englishClause.setVerb(englishWord)
                } else if(currSem.categories.contains("food_item") || currSem.categories.contains("drink_item")) {
                    val subj = getRandomPP()

                    dutchClause.setSubject(subj.dutch.random())
                    englishClause.setSubject(subj.english.random())

                    if(currSem.categories.contains("food_item")) {
                        dutchClause.setVerb(semanticon.getSem("eat").dutch.random())
                        englishClause.setVerb(semanticon.getSem("eat").english.random())
                    } else {
                        dutchClause.setVerb(semanticon.getSem("drink").dutch.random())
                        englishClause.setVerb(semanticon.getSem("drink").english.random())
                    }

                    val r = Random()

                    val dutchComplement = dutchFactory.createNounPhrase(dutchWord)
                    val englishComplement = englishFactory.createNounPhrase(englishWord)
                    if(r.nextBoolean()) {
                        if(dutchLexicon.getWord(dutchWord).hasFeature("gender")) {
                            if(dutchLexicon.getWord(dutchWord).getFeature("gender").toString() == "NEUTER") {
                                dutchComplement.setSpecifier("het")
                            } else {
                                dutchComplement.setSpecifier("de")
                            }
                        } else {
                            dutchComplement.setSpecifier("de")
                        }

                        englishComplement.setSpecifier("the")
                    } else {
                        if(!currSem.categories.contains("no_determiner")) {
                            dutchComplement.setSpecifier("een")
                            englishComplement.setSpecifier("a")
                        }
                    }

                    dutchClause.setComplement(dutchComplement)
                    englishClause.setComplement(englishComplement)
                }

                val dutchOutput = realiser.realiseSentence(dutchClause)
                val englishOutput = realiser.realiseSentence(englishClause)

                if(dutchToEng) {
                    _exercise.value = dutchOutput
                    _answer.value = englishOutput
                } else {
                    _exercise.value = englishOutput
                    _answer.value = dutchOutput
                }
                _feedback.value = ""
                _lessonStatus.value = LessonStatus.INPROGRESS
            }

        } else onLessonFinish()
    }

    fun onCheck(answer: String) {
        Log.i("Lesson", "Exercise: ${_answer.value} - answer: $answer - equals: ${_answer.value == answer}")
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
        when(currEntry.stage) {
            0 -> {
                currEntry.stage = 1
                currEntry.learned = 1
                primQueue.add(3, qHead)
            }
            in 1 .. 6 -> {
                currEntry.stage++
                primQueue.add(currEntry.stage + 2, qHead)
            }
            7 -> {
                currEntry.stage++
                secQueue.add(qHead)
            }
            else -> {
                secQueue.add(qHead)
            }
        }

        uiScope.launch {
            updateSememe(currEntry.sememeId, currEntry.learned, currEntry.stage)
        }

        _score.value = (score.value)?.plus(1)
        _lessonStatus.value = LessonStatus.CORRECT
        _feedback.value = positiveFeedback.random()
    }

    private fun incorrectAnswer() {
        primQueue.add(0, qHead)
        _lessonStatus.value = LessonStatus.INCORRECT
        _feedback.value = "Incorrect. The correct answer was \"${_answer.value}\""
    }


    private fun getRandomPP(): SemElement {
        val persPronouns = semanticon.getSemsInCategory("pers_pron")
        val learnedSems = sememes.filter { it.learned == 1}
        val learnedSemsIds = learnedSems.map { it.sememeId }

        val learnedPPs = persPronouns.filter { pp ->
            learnedSemsIds.contains(pp.id)
        }

        return learnedPPs.random()
    }


    private suspend fun getAllSememes(): List<Sememe> {
        return withContext(Dispatchers.IO) {
            val sememes = database.getAllSememes()
            sememes
        }
    }

    private suspend fun getPrimQueue(): LinkedList<String> {
        return withContext(Dispatchers.IO) {
            val profile = database.getAllProfiles()[0]
            profile.primaryQueue
        }
    }

    private suspend fun getSecQueue(): LinkedList<String> {
        return withContext(Dispatchers.IO) {
            val profile = database.getAllProfiles()[0]
            profile.secondaryQueue
        }
    }

    private suspend fun currEntry(id: String): Sememe? {
        return withContext(Dispatchers.IO) {
            val entry = database.getSememe(id)
            entry
        }
    }

    private suspend fun updateSememe(id: String, learned: Int, stage: Int) {
        withContext(Dispatchers.IO) {
            database.update(Sememe(id, learned, stage))
        }
    }

    private suspend fun updateProfile(id: Int, primQueue: LinkedList<String>, secQueue: LinkedList<String>) {
        withContext(Dispatchers.IO) {
            database.update(Profile(id, primaryQueue = primQueue, secondaryQueue = secQueue))
        }
    }


    fun onLessonFinish() {
        uiScope.launch {
            updateProfile(0, primQueue, secQueue)
        }
        _eventLessonFinish.value = true
    }

    fun onLessonFinishComplete() {
        _eventLessonFinish.value = false
    }
}