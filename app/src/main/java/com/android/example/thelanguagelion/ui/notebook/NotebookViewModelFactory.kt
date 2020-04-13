package com.android.example.thelanguagelion.ui.notebook

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.android.example.thelanguagelion.database.SememeDatabaseDao

class NotebookViewModelFactory(private val dataSource: SememeDatabaseDao, private val application: Application) : ViewModelProvider.Factory {
    @Suppress("unchecked_cast")
    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if(modelClass.isAssignableFrom(NotebookViewModel::class.java)) {
            return NotebookViewModel(dataSource, application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}