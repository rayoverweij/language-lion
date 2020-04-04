package com.android.example.thelanguagelion.ui.notebook

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class NotebookViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is the Notebook."
    }
    val text: LiveData<String> = _text
}