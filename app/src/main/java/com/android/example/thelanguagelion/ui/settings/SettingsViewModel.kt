package com.android.example.thelanguagelion.ui.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import com.android.example.thelanguagelion.database.Profile
import com.android.example.thelanguagelion.database.Sememe
import com.android.example.thelanguagelion.database.Sentence
import com.android.example.thelanguagelion.database.StudentDatabaseDao
import com.android.example.thelanguagelion.resetDatabase
import kotlinx.coroutines.*
import java.util.*

class SettingsViewModel(val database: StudentDatabaseDao, application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var sememes: List<Sememe>
    private lateinit var sentences: List<Sentence>
    private lateinit var profiles: List<Profile>


    fun resetProgress() {
        uiScope.launch {
            resetDatabase(database, context)
        }
    }

    fun logData() {
        uiScope.launch {
            sememes = getAllSememes()
            sentences = getAllSentences()
            profiles = getAllProfiles()
            Log.i("Data", "Profiles: $profiles")
            Log.i("Data", "Sentences: $sentences")
            Log.i("Data", "Sememes: $sememes")
        }
    }

    fun switchProfileOne() {
        uiScope.launch {
            setProfileOne()
        }
    }



    private suspend fun setProfileOne() {
        withContext(Dispatchers.IO) {
            val primQueue = LinkedList(listOf(
                "S0041007", "S0041018", "S0041011", "S0041008", "S0041017", "S0041016", "S0041010", "S0031003", "S0041009",
                "S0021009", "S0041012", "S0041019", "S0021012", "S0041013", "S0041020", "S0050002", "S0041014", "S0041021",
                "S0050002", "S0030001", "S0051000", "S0051001", "S0031004", "S0040000", "S0031005", "S0040001", "S0031006",
                "S0040002", "S0031007", "S0040003", "S0031008", "S0040004", "S0031012", "S0021001", "S0021003", "S0031013",
                "S0021005", "S0010000", "S0021007", "S0010001", "S0021008", "S0010002", "S0021010", "S0010003", "S0021013",
                "S0010004", "S0010005", "S0010006", "S0010007", "S0010008", "S0031009", "S0010009", "S0010010", "S0042000",
                "S0010011", "S0010012", "S0042001", "S0010013", "S0010014", "S0042002", "S0010015"))
            val secQueue = LinkedList(listOf(
                "S0041000", "S0041015", "S0041001", "S0041002", "S0041003", "S0041005", "S0021000", "S0050001", "S0031001",
                "S0041006", "S0021004", "S0031002", "S0021006", "S0030000", "S0050000", "S0021002"))
            database.update(Profile(0, primaryQueue = primQueue, secondaryQueue = secQueue))

            val learnedGrammars = listOf("G001", "G002", "G003", "G004", "G005", "G007")
            for(lg in learnedGrammars) {
                database.update(Sentence(lg, 1))
            }
            val unlearnedGrammars = listOf("G006", "G008", "G009", "G010")
            for (ug in unlearnedGrammars) {
                database.update(Sentence(ug, 0))
            }

            database.update(Sememe("S0021000", 1, 8))
            database.update(Sememe("S0021002", 1, 8))
            database.update(Sememe("S0021004", 1, 8))
            database.update(Sememe("S0021006", 1, 8))
            database.update(Sememe("S0030000", 1, 8))
            database.update(Sememe("S0031001", 1, 8))
            database.update(Sememe("S0031002", 1, 8))
            database.update(Sememe("S0031003", 1, 5))
            database.update(Sememe("S0041000", 1, 8))
            database.update(Sememe("S0041001", 1, 8))
            database.update(Sememe("S0041002", 1, 8))
            database.update(Sememe("S0041003", 1, 8))
            database.update(Sememe("S0041005", 1, 8))
            database.update(Sememe("S0041006", 1, 8))
            database.update(Sememe("S0041007", 1, 7))
            database.update(Sememe("S0041008", 1, 7))
            database.update(Sememe("S0041009", 1, 6))
            database.update(Sememe("S0041010", 1, 5))
            database.update(Sememe("S0041011", 1, 4))
            database.update(Sememe("S0041015", 1, 8))
            database.update(Sememe("S0041016", 1, 6))
            database.update(Sememe("S0041017", 1, 5))
            database.update(Sememe("S0041018", 1, 1))
            database.update(Sememe("S0050000", 1, 8))
            database.update(Sememe("S0050001", 1, 8))

            val unlearnedSememes = listOf(
                "S0010000", "S0010001", "S0010002", "S0010003", "S0010004", "S0010005", "S0010006", "S0010007", "S0010008", "S0010009",
                "S0010010", "S0010011", "S0010012", "S0010013", "S0010014", "S0010015", "S0021001", "S0021003", "S0021005", "S0021007",
                "S0021008", "S0021009", "S0021010", "S0021012", "S0021013", "S0030001", "S0031004", "S0031005", "S0031006", "S0031007",
                "S0031008", "S0031009", "S0031010", "S0031011", "S0031012", "S0031013", "S0040000", "S0040001", "S0040002", "S0040003",
                "S0040004", "S0041012", "S0041013", "S0041014", "S0041019", "S0041020", "S0041021", "S0042000", "S0042001", "S0042002",
                "S0050002", "S0051000", "S0051001")
            for (us in unlearnedSememes) {
                database.update(Sememe(us, 0, 0))
            }
        }
    }



    private suspend fun getAllSememes(): List<Sememe> {
        return withContext(Dispatchers.IO) {
            val sememes = database.getAllSememes()
            sememes
        }
    }

    private suspend fun getAllSentences(): List<Sentence> {
        return withContext(Dispatchers.IO) {
            val sentences = database.getAllSentences()
            sentences
        }
    }

    private suspend fun getAllProfiles(): List<Profile> {
        return withContext(Dispatchers.IO) {
            val profiles = database.getAllProfiles()
            profiles
        }
    }


    init {
        uiScope.launch {
            sememes = getAllSememes()
            sentences = getAllSentences()
            profiles = getAllProfiles()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}