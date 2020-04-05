package com.android.example.thelanguagelion.ui.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {

    private val _text = MutableLiveData<String>()
    val text: LiveData<String>
        get() = _text


    init {
        _text.value = "Welcome back, Rayo!\nKeep up the good work :)"
    }
}