package com.android.example.thelanguagelion.ui.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.example.thelanguagelion.database.Profile
import com.android.example.thelanguagelion.database.Sememe
import com.android.example.thelanguagelion.database.Sentence
import com.android.example.thelanguagelion.database.StudentDatabaseDao
import com.android.example.thelanguagelion.getFileFromAssets
import kotlinx.coroutines.*
import org.w3c.dom.Node
import java.io.File
import java.util.*
import javax.xml.parsers.DocumentBuilderFactory

class SettingsViewModel(val database: StudentDatabaseDao, application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var sememes: List<Sememe>
    private lateinit var sentences: List<Sentence>
    private lateinit var profiles: List<Profile>


    fun resetProgress() {
        uiScope.launch {
            resetDatabase()
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



    private suspend fun resetDatabase() {
        withContext(Dispatchers.IO) {
            database.clearProfiles()
            database.clearSememes()
            database.clearSentences()

            val semanticonPath = getFileFromAssets("semanticon.xml", context).absolutePath
            val semanticonUri = File(semanticonPath).toURI()

            val grammarPath = getFileFromAssets("grammar.xml", context).absolutePath
            val grammarUri = File(grammarPath).toURI()

            try {
                val factory = DocumentBuilderFactory.newInstance()
                val builder = factory.newDocumentBuilder()

                var doc = builder.parse(semanticonUri.toString())
                if(doc != null) {
                    val lexRoot = doc.documentElement
                    val semNodes = lexRoot.childNodes
                    for(i in 0 .. semNodes.length) {
                        val semNode = semNodes.item(i)
                        if(semNode?.nodeType == Node.ELEMENT_NODE) {
                            if(semNode.nodeName != "sem") continue
                            val semNodeAttrs = semNode.childNodes
                            for(j in 0 .. semNodeAttrs.length) {
                                val semNodeAttr = semNodeAttrs.item(j)
                                if(semNodeAttr?.nodeType == Node.ELEMENT_NODE) {
                                    val attrName = semNodeAttr.nodeName.trim()
                                    if(attrName == "id") {
                                        val value = semNodeAttr.textContent
                                        database.insert(Sememe(value))
                                    }
                                }
                            }
                        }
                    }
                }

                doc = builder.parse(grammarUri.toString())
                if(doc != null) {
                    val lexRoot = doc.documentElement
                    val sentenceNodes = lexRoot.childNodes
                    for(i in 0 .. sentenceNodes.length) {
                        val sentenceNode = sentenceNodes.item(i)
                        if(sentenceNode?.nodeType == Node.ELEMENT_NODE) {
                            if(sentenceNode.nodeName != "sentence") continue
                            val sentenceNodeAttrs = sentenceNode.childNodes
                            for(j in 0 .. sentenceNodeAttrs.length) {
                                val sentenceNodeAttr = sentenceNodeAttrs.item(j)
                                if(sentenceNodeAttr?.nodeType == Node.ELEMENT_NODE) {
                                    val attrName = sentenceNodeAttr.nodeName.trim()
                                    if(attrName == "id") {
                                        val value = sentenceNodeAttr.textContent
                                        database.insert(Sentence(value))
                                    }
                                }
                            }
                        }
                    }
                }

            } catch (ex: Exception) {
                Log.e("Data", ex.toString())
            }

            database.update(Sememe("S0021000", 1))
            database.update(Sentence("G001", 1))

            val primQueue = LinkedList(
                listOf(
                    "S0030000", "S0031001", "S0031002", "S0041000", "S0041001", "S0041015", "S0021002", "S0041002",
                    "S0041003", "S0041004", "S0041005", "S0041006", "S0021004", "S0021006", "S0041007", "S0041008",
                    "S0041016", "S0041009", "S0041017", "S0041010", "S0031003"
                )
            )
            database.insert(Profile(
                username = "Rayo",
                primaryQueue = primQueue
                ))
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