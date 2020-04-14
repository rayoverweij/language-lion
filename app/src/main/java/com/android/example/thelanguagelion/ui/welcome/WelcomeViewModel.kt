package com.android.example.thelanguagelion.ui.welcome

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.example.thelanguagelion.database.Profile
import com.android.example.thelanguagelion.database.StudentDatabaseDao
import kotlinx.coroutines.*

class WelcomeViewModel(val database: StudentDatabaseDao, application: Application) : AndroidViewModel(application) {
    private var viewModelJob = Job()
    private val uiScope = CoroutineScope(Dispatchers.Main + viewModelJob)

    private var profiles = MutableLiveData<List<Profile>?>()

    private var _eventMovingOn = MutableLiveData<Boolean>()
    val eventMovingOn: LiveData<Boolean>
        get() = _eventMovingOn


    init {
        Log.i("Welcome", "Welcome!")
        checkForExistingProfiles()
        Log.i("Welcome", "Profiles: ${profiles.value.toString()}")
        if(profiles.value != null) {
            onMovingOn()
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelJob.cancel()
    }


    private fun checkForExistingProfiles() {
        uiScope.launch {
            profiles.value = getProfiles()
        }
    }

    fun startApp(username: String) {
        uiScope.launch {
            newProfile(username)
        }
        onMovingOn()
    }


    private suspend fun getProfiles(): List<Profile>? {
        return withContext(Dispatchers.IO) {
            val profiles = database.getAllProfiles()
            profiles
        }
    }

    private suspend fun newProfile(username: String) {
        withContext(Dispatchers.IO) {
            database.insert(Profile(username = username))
        }
    }


    fun onMovingOn() {
        _eventMovingOn.value = true
    }

    fun onMovingOnFinish() {
        _eventMovingOn.value = false
    }
}
