package com.android.example.thelanguagelion.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "Welcome back, Rayo!\nKeep up the good work :)"
    }
    val text: LiveData<String> = _text
}