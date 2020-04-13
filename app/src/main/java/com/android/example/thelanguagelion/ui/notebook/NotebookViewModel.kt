package com.android.example.thelanguagelion.ui.notebook

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.android.example.thelanguagelion.database.SememeDatabaseDao

class NotebookViewModel(val database: SememeDatabaseDao, application: Application) : AndroidViewModel(application) {
    val sememes = database.getAll()
}