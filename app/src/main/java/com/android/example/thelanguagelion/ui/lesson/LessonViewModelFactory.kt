package com.android.example.thelanguagelion.ui.lesson

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.example.thelanguagelion.database.StudentDatabaseDao

class LessonViewModelFactory(private val dataSource: StudentDatabaseDao, private val application: Application, private val time: Int) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(LessonViewModel::class.java)) {
            return LessonViewModel(dataSource, application, time) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}