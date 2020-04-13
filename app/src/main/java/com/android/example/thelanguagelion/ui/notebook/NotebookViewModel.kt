package com.android.example.thelanguagelion.ui.notebook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.android.example.thelanguagelion.database.SememeDatabaseDao

class NotebookViewModel(val database: SememeDatabaseDao, application: Application) : AndroidViewModel(application) {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the Notebook."
    }
    val text: LiveData<String> = _text
}