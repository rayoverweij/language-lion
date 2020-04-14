package com.android.example.thelanguagelion.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "profile_table")
data class Profile(
    @PrimaryKey(autoGenerate = true)
    var profileId: Int = 0,

    @ColumnInfo(name = "username")
    var username: String = "",

    @ColumnInfo(name = "primaryQueue")
    var primaryQueue: List<String> = mutableListOf(),

    @ColumnInfo(name = "secondaryQueue")
    var secondaryQueue: List<String> = mutableListOf()
)