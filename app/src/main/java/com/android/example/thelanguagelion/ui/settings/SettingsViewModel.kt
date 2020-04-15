package com.android.example.thelanguagelion.ui.settings

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.example.thelanguagelion.database.Profile
import com.android.example.thelanguagelion.database.Sememe
import com.android.example.thelanguagelion.database.StudentDatabaseDao
import com.android.example.thelanguagelion.getFileFromAssets
import kotlinx.coroutines.*
import org.w3c.dom.Node
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory

class SettingsViewModel(val database: StudentDatabaseDao, application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var sememes: List<Sememe>
    private lateinit var profiles: List<Profile>


    fun resetProgress() {
        uiScope.launch {
            resetDatabase()
        }
    }

    fun logData() {
        uiScope.launch {
            sememes = getAllSememes()
            profiles = getAllProfiles()
            Log.i("Data", "Profiles: $profiles")
            Log.i("Data", "Sememes: $sememes")
        }
    }



    private suspend fun resetDatabase() {
        withContext(Dispatchers.IO) {
            database.clearProfiles()
            database.clearSememes()

            val semanticonPath = getFileFromAssets("semanticon.xml", context).absolutePath
            val uri = File(semanticonPath).toURI()

            try {
                val factory = DocumentBuilderFactory.newInstance()
                val builder = factory.newDocumentBuilder()
                val doc = builder.parse(uri.toString())

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
            } catch (ex: Exception) {
                Log.e("Data", ex.toString())
            }

            database.insert(Profile(username = "Rayo"))
        }
    }

    private suspend fun getAllSememes(): List<Sememe> {
        return withContext(Dispatchers.IO) {
            val sememes = database.getAllSememes()
            sememes
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
            profiles = getAllProfiles()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}