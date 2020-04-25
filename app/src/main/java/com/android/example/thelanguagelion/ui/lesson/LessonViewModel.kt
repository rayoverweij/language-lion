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
import org.w3c.dom.Node
import simplenlg.features.Feature
import simplenlg.features.NumberAgreement
import simplenlg.framework.LexicalCategory
import simplenlg.framework.NLGFactory
import simplenlg.framework.SemElement
import simplenlg.phrasespec.NPPhraseSpec
import simplenlg.phrasespec.SPhraseSpec
import simplenlg.realiser.Realiser
import simplenlg.semantics.Semanticon
import java.io.File
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory
import com.android.example.thelanguagelion.database.Sentence as DataSentence
import simplenlg.lexicon.dutch.XMLLexicon as dutchXMLLexicon
import simplenlg.lexicon.english.XMLLexicon as englishXMLLexicon

class LessonViewModel(val database: StudentDatabaseDao, application: Application, time: Int) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var semanticon: Semanticon
    private var englishLexicon: englishXMLLexicon
    private var dutchLexicon: dutchXMLLexicon
    private var englishFactory: NLGFactory
    private var dutchFactory: NLGFactory
    private var realiser: Realiser

    private var grammar: MutableSet<Sentence>
    private var grammarById: MutableMap<String, Sentence>
    private var grammarByType: MutableMap<String, MutableList<Sentence>>

    private lateinit var sememes: List<Sememe>
    private lateinit var sentences: List<DataSentence>
    private lateinit var primQueue: LinkedList<String>
    private lateinit var secQueue: LinkedList<String>
    private lateinit var qHead: String
    private lateinit var currSem: SemElement
    private lateinit var currEntry: Sememe
    private lateinit var alsoTested: MutableList<Sememe>

    private var timerTime: Long
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

    private var _answer = MutableLiveData<List<String>>()
    val answer: LiveData<List<String>>
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
        _answer.value = listOf("")
        _feedback.value = ""
        _score.value = 0
        _lessonStatus.value = LessonStatus.INPROGRESS

        grammar = mutableSetOf()
        grammarById = mutableMapOf()
        grammarByType = mutableMapOf()

        val grammarPath = getFileFromAssets("grammar.xml", context).absolutePath
        val uri = File(grammarPath).toURI()

        try  {
            val factory = DocumentBuilderFactory.newInstance()
            val builder = factory.newDocumentBuilder()
            val doc = builder.parse(uri.toString())

            if(doc != null) {
                val lexRoot = doc.documentElement
                val sentenceNodes = lexRoot.childNodes
                for(i in 0 .. sentenceNodes.length) {
                    val sentenceNode = sentenceNodes.item(i)
                    if(sentenceNode?.nodeType == Node.ELEMENT_NODE) {
                        if(sentenceNode.nodeName != "sentence") continue

                        val featureNodes = sentenceNode.childNodes
                        val sentence = Sentence()

                        for(j in 0 .. featureNodes.length) {
                            val featureNode = featureNodes.item(j)
                            if(featureNode?.nodeType == Node.ELEMENT_NODE) {
                                val name = featureNode.nodeName.trim()
                                val value = featureNode.textContent?.trim()

                                if(name == "id") sentence.id = value
                                if(name == "subject") sentence.subject = value
                                if(name == "verb") sentence.verb = value
                                if(name == "complement") sentence.complement = value
                            }
                        }

                        grammar.add(sentence)

                        val id = sentence.id
                        if(id != null) {
                            if(grammarById.containsKey(id)) Log.e("Lesson", "Grammar error: ID $id occurs more than once")
                            grammarById[id] = sentence
                        }

                        val pOfSpeech = listOf(Sentence::subject, Sentence::verb, Sentence::complement)
                        for(p in pOfSpeech) {
                            val part = p.get(sentence)
                            if(part != null) {
                                val typeList = grammarByType[part] ?: mutableListOf()
                                typeList.add(sentence)
                                grammarByType[part] = typeList
                            }
                        }
                    }
                }
            }
        } catch (ex: Exception) {
            Log.e("Lesson", ex.toString())
        }

        val semanticonPath = getFileFromAssets("semanticon.xml", context).absolutePath
        val englishLexPath = getFileFromAssets("english-lexicon.xml", context).absolutePath
        val dutchLexPath = getFileFromAssets("dutch-lexicon.xml", context).absolutePath

        semanticon = Semanticon(semanticonPath)
        englishLexicon = englishXMLLexicon(englishLexPath)
        dutchLexicon = dutchXMLLexicon(dutchLexPath)
        englishFactory = NLGFactory(englishLexicon)
        dutchFactory = NLGFactory(dutchLexicon)
        realiser = Realiser()

        Log.i("Lesson", dutchLexicon.hasWord("eten").toString())
        Log.i("Lesson", dutchLexicon.hasWord("drinken").toString())

        timerTime = when(time) {
            1 -> ONE_MINUTE
            5 -> FIVE_MINUTES
            else -> TEN_MINUTES
        }

        timer = object : CountDownTimer(timerTime, ONE_SECOND) {
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
            sentences = getAllSentences()
            primQueue = getPrimQueue()
            secQueue = getSecQueue()
            alsoTested = mutableListOf()
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
                currEntry = currEntry(qHead)!!

                val r = Random()

                var dutchToEng = false
                if(currEntry.stage > 4) {
                    _task.value = "Translate to Dutch"
                } else {
                    _task.value = "Translate to English"
                    dutchToEng = true
                }

                val templates = mutableListOf<Sentence>()
                for (category in currSem.categories) {
                    Log.i("Lesson", category)
                    val t = getSentences(category)
                    if(t != null) {
                        templates.addAll(t)
                    }
                }

                val template = try {
                    templates.random()
                } catch (e: NoSuchElementException) {
                    when {
                        currSem.categories.contains("locale") -> grammarById["G008"]
                        currSem.categories.contains("food_item") -> grammarById["G002"]
                        currSem.categories.contains("drink_item") -> grammarById["G003"]
                        currSem.categories.contains("food_descriptor") -> grammarById["G004"]
                        currSem.categories.contains("readable") -> grammarById["G006"]
                        else -> getRandomSentence()
                    }
                }!!

                val subject = if(currSem.categories.contains(template.subject)) {
                    currSem
                } else {
                    val s = getRandomSem(template.subject!!)!!
                    alsoTested.add(findSememeFromSemElement(s)!!)
                    s
                }
                val defArtS = r.nextBoolean()
                val noDetS = subject.categories.contains("no_determiner")
                val pluralZij = subject.id == "S0021012"

                val verb = if(currSem.categories.contains(template.verb)) {
                    currSem
                } else {
                    val v = getRandomSem(template.verb!!)!!
                    alsoTested.add(findSememeFromSemElement(v)!!)
                    v
                }

                val complement: SemElement?; val defArtC: Boolean?; val noDetC: Boolean?
                if(template.complement != null) {
                    complement = if(currSem.categories.contains(template.complement)) {
                        currSem
                    } else {
                        val c = getRandomSem(template.complement!!)!!
                        alsoTested.add(findSememeFromSemElement(c)!!)
                        c
                    }
                    defArtC = r.nextBoolean()
                    noDetC = complement.categories.contains("no_determiner")
                } else {
                    complement = null; defArtC = null; noDetC = null
                }

                val interjection = if(currSem.categories.contains("interjection")) currSem else null

                val dutchClauses = buildClauses("Dutch", subject.dutch, verb.dutch, complement?.dutch, interjection?.dutch,
                    defArtS, defArtC, noDetS, noDetC, pluralZij, dutchFactory)

                val englishClauses = buildClauses("English", subject.english, verb.english, complement?.english, interjection?.english,
                    defArtS, defArtC, noDetS, noDetC, pluralZij, englishFactory)

                val dutchOutput = dutchClauses.map {
                    realiser.realiseSentence(it)
                }

                val englishOutput = englishClauses.map {
                    realiser.realiseSentence(it)
                }

                Log.i("Lesson", alsoTested.toString())

                if(dutchToEng) {
                    _exercise.value = dutchOutput.random()
                    _answer.value = englishOutput
                } else {
                    _exercise.value = englishOutput.random()
                    _answer.value = dutchOutput
                }
                _feedback.value = ""
                _lessonStatus.value = LessonStatus.INPROGRESS
            }

        } else onLessonFinish()
    }



    fun onCheck(answer: String) {
        val trimmedAnswers = _answer.value!!.map {
            it
                .replace(".", "")
                .replace(",", "")
                .toLowerCase(Locale.ROOT)
        }

        val trimmedAnswer = answer
            .trim()
            .replace(".", "")
            .replace(",", "")
            .replace("\\s+".toRegex(), " ")
            .toLowerCase(Locale.ROOT)

        Log.i("Lesson", "Exercise: $trimmedAnswers - answer: $trimmedAnswer - correct: ${trimmedAnswers.contains(trimmedAnswer)}")
        if(trimmedAnswers.contains(trimmedAnswer)) {
            correctAnswer()
        } else {
            primQueue.add(0, qHead)
            _lessonStatus.value = LessonStatus.INCORRECT
            _feedback.value = "Incorrect. The correct answer was \"${_answer.value!!.random()}\""
        }
    }

    fun dontKnow() {
        primQueue.add(0, qHead)
        _lessonStatus.value = LessonStatus.DONTKNOW
        _feedback.value = "The correct answer is \"${_answer.value!!.random()}\""
    }



    private fun correctAnswer() {
        when(currEntry.stage) {
            0 -> {
                currEntry.stage = 1
                currEntry.learned = 1
                primQueue.add(3, qHead)
            }
            in 1..6 -> {
                currEntry.stage++
                try {
                    primQueue.add(currEntry.stage + 2, qHead)
                } catch (ex: Exception) {
                    primQueue.add(qHead)
                }
            }
            7 -> {
                currEntry.stage++
                secQueue.add(qHead)
            }
            else -> {
                secQueue.add(qHead)
            }
        }

        for(entry in alsoTested) {
            when(entry.stage) {
                0 -> {
                    entry.stage = 1
                    entry.learned = 1
                }
                in 1..7 -> {
                    entry.stage++
                }
                else -> {
                    primQueue.remove(entry.sememeId)
                    secQueue.remove(entry.sememeId)
                    secQueue.add(entry.sememeId)
                }
            }
        }

        uiScope.launch {
            updateSememe(currEntry.sememeId, currEntry.learned, currEntry.stage)
            for(entry in alsoTested) {
                updateSememe(entry.sememeId, entry.learned, entry.stage)
            }

            when(currEntry.sememeId) {
                "S0041000" -> updateSentence("G002", 1)
                "S0041015" -> updateSentence("G003", 1)
                "S0050000" -> {
                    updateSentence("G004", 1)
                    updateSentence("G005", 1)
                    updateSentence("G007", 1)
                }
            }

            updateProfile(0, primQueue, secQueue)
            sememes = getAllSememes()
            sentences = getAllSentences()
            alsoTested.clear()
        }

        _score.value = (score.value)?.plus(1)
        _lessonStatus.value = LessonStatus.CORRECT
        _feedback.value = positiveFeedback.random()
    }



    private fun getRandomSem(type: String): SemElement? {
        val dataSems = semanticon.getSemsInCategory(type)
        val learnedSems = sememes.filter { it.learned == 1}
        val learnedSemsIds = learnedSems.map { it.sememeId }

        val learnedDataSems = dataSems.filter { sem ->
            learnedSemsIds.contains(sem.id)
        }

        return try {
            learnedDataSems.random()
        } catch (ex: Exception) {
            null
        }
    }

    private fun getRandomSentence(): Sentence? {
        val learnedSentences = sentences.filter { it.learned == 1 }
        val learnedSentencesIds = learnedSentences.map { it.sentenceId }

        val learnedDataSentences = grammar.filter { sentence ->
            learnedSentencesIds.contains(sentence.id)
        }

        return learnedDataSentences.random()
    }

    private fun getSentences(type: String): List<Sentence>? {
        Log.i("Lesson", "getting sentences of type $type")
        val dataSentences = grammarByType[type] ?: return null
        val learnedSentences = sentences.filter { it.learned == 1 }
        if(learnedSentences.isEmpty()) {
            return null
        }
        val learnedSentencesIds = learnedSentences.map { it.sentenceId }

        return dataSentences.filter { sentence ->
            learnedSentencesIds.contains(sentence.id)
        }
    }



    private fun findSememeFromSemElement(semElement: SemElement): Sememe? {
        return sememes.find {
            it.sememeId == semElement.id
        }
    }



    private fun buildClauses(language: String, subject: List<String>, verb: List<String>, complement: List<String>?, interjection: List<String>?,
                             defArtS: Boolean, defArtC: Boolean?, noDetS: Boolean, noDetC: Boolean?, pluralZij: Boolean, factory: NLGFactory)
            : List<SPhraseSpec> {
        val clauses = mutableListOf<SPhraseSpec>()

        for (s in subject) {
            val builtSubject = if(language == "Dutch") {
                createDutchNounPhrase(s, defArtS, noDetS)
            } else {
                createEnglishNounPhrase(s, defArtS, noDetS)
            }

            for (v in verb) {
                if(complement == null && interjection == null) {
                    val clause = factory.createClause()
                    clause.setSubject(builtSubject)
                    clause.setVerb(v)
                    if(pluralZij) clause.setFeature(Feature.NUMBER, NumberAgreement.PLURAL)
                    clauses.add(clause)
                } else if(complement != null && interjection == null) {
                    for (c in complement) {
                        val clause = factory.createClause()

                        clause.setSubject(builtSubject)
                        clause.setVerb(v)
                        if(pluralZij) clause.setFeature(Feature.NUMBER, NumberAgreement.PLURAL)

                        val builtComplement = if(language == "Dutch") {
                            createDutchNounPhrase(c, defArtC!!, noDetC!!)
                        } else createEnglishNounPhrase(c, defArtC!!, noDetC!!)
                        clause.setComplement(builtComplement)

                        clauses.add(clause)
                    }
                } else if(complement == null && interjection != null) {
                    for(i in interjection) {
                        val clause = factory.createClause()
                        clause.setSubject(builtSubject)
                        clause.setVerb(v)
                        if(pluralZij) clause.setFeature(Feature.NUMBER, NumberAgreement.PLURAL)
                        clause.addFrontModifier(i)
                        clauses.add(clause)
                    }
                } else {
                    for(c in complement!!) {
                        val builtComplement = if(language == "Dutch") {
                            createDutchNounPhrase(c, defArtC!!, noDetC!!)
                        } else createEnglishNounPhrase(c, defArtC!!, noDetC!!)

                        for(i in interjection!!) {
                            val clause = factory.createClause()
                            clause.setSubject(builtSubject)
                            clause.setVerb(v)
                            if(pluralZij) clause.setFeature(Feature.NUMBER, NumberAgreement.PLURAL)
                            clause.setComplement(builtComplement)
                            clause.addFrontModifier(i)
                            clauses.add(clause)
                        }
                    }
                }
            }
        }

        return clauses
    }

    private fun createDutchNounPhrase(head: String, defArt: Boolean, noDet: Boolean): NPPhraseSpec {
        val dutchNP = dutchFactory.createNounPhrase(head)
        Log.i("Lesson", "${dutchLexicon.getWord(head)}")
        if(dutchLexicon.getWord(head).category == LexicalCategory.NOUN) {
            if (defArt) {
                if (dutchLexicon.getWord(head).hasFeature("gender")) {
                    if (dutchLexicon.getWord(head).getFeature("gender").toString() == "NEUTER") {
                        dutchNP.setSpecifier("het")
                    } else {
                        dutchNP.setSpecifier("de")
                    }
                } else {
                    dutchNP.setSpecifier("de")
                }
            } else {
                if (!noDet) {
                    dutchNP.setSpecifier("een")
                }
            }
        }
        return dutchNP
    }

    private fun createEnglishNounPhrase(head: String, defArt: Boolean, noDet: Boolean): NPPhraseSpec {
        Log.i("Lesson", "${englishLexicon.getWord(head)}")
        val englishNP = englishFactory.createNounPhrase(head)
        if(englishLexicon.getWord(head).category == LexicalCategory.NOUN) {
            if (defArt) {
                englishNP.setSpecifier("the")
            } else {
                if (!noDet) {
                    englishNP.setSpecifier("a")
                }
            }
        }
        return englishNP
    }



    private suspend fun getAllSememes(): List<Sememe> {
        return withContext(Dispatchers.IO) {
            val sememes = database.getAllSememes()
            sememes
        }
    }

    private suspend fun getAllSentences(): List<DataSentence> {
        return withContext(Dispatchers.IO) {
            val sentences = database.getAllSentences()
            sentences
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

    private suspend fun updateSentence(id: String, learned: Int) {
        withContext(Dispatchers.IO) {
            database.update(DataSentence(id, learned))
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