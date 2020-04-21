package com.android.example.thelanguagelion.ui.notebook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.example.thelanguagelion.database.Sememe
import com.android.example.thelanguagelion.database.StudentDatabaseDao
import com.android.example.thelanguagelion.getFileFromAssets
import kotlinx.coroutines.*
import simplenlg.framework.SemElement
import simplenlg.semantics.Semanticon

class NotebookViewModel(val database: StudentDatabaseDao, application: Application) : AndroidViewModel(application) {
    private val context = getApplication<Application>().applicationContext

    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private lateinit var allLearned: List<Sememe>
    private var semanticon: Semanticon

    private var _sememes = MutableLiveData<List<SemElement>>()
    val sememes: LiveData<List<SemElement>>
        get() = _sememes


    init {
        val semanticonPath = getFileFromAssets("semanticon.xml", context).absolutePath
        semanticon = Semanticon(semanticonPath)

        uiScope.launch {
            allLearned = getAllLearned()
            val semList = mutableListOf<SemElement>()

            allLearned.map {
                semList.add(semanticon.getSemById(it.sememeId))
            }

            _sememes.value = semList
        }
    }

    private suspend fun getAllLearned(): List<Sememe> {
        return withContext(Dispatchers.IO) {
            database.getAllLearned()
        }
    }
}