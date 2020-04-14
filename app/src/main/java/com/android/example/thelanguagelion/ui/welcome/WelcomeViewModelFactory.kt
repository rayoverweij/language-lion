package com.android.example.thelanguagelion.ui.welcome

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.example.thelanguagelion.database.StudentDatabaseDao

class WelcomeViewModelFactory(private val dataSource: StudentDatabaseDao, private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(WelcomeViewModel::class.java)) {
            return WelcomeViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}