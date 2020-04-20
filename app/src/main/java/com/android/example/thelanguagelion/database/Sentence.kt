package com.android.example.thelanguagelion.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sentence_table")
data class Sentence(
    @PrimaryKey(autoGenerate = false)
    var sentenceId: String = "",

    @ColumnInfo(name = "learned")
    var learned: Int = 0
)