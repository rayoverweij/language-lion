package com.android.example.thelanguagelion.ui.settings

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.example.thelanguagelion.database.Sememe
import com.android.example.thelanguagelion.database.SememeDatabaseDao
import kotlinx.coroutines.*

class SettingsViewModel(val database: SememeDatabaseDao, application: Application) : AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    fun resetProgress() {
        uiScope.launch {
            resetDatabase()
        }
    }

    private suspend fun resetDatabase() {
        withContext(Dispatchers.IO) {
            database.clear()

            database.insert(Sememe("S0010000"))
            database.insert(Sememe("S0010001"))
            database.insert(Sememe("S0010002"))
            database.insert(Sememe("S0010003"))
            database.insert(Sememe("S0010004"))
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }
}