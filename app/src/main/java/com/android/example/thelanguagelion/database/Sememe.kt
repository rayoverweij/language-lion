package com.android.example.thelanguagelion.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sememe_table")
data class Sememe(
    @PrimaryKey(autoGenerate = false)
    var sememeId: String = "",

    @ColumnInfo(name = "learned")
    var learned: Int = 0,

    @ColumnInfo(name = "stage")
    var stage: Int = -1
)