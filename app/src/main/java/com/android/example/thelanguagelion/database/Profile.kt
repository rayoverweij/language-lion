package com.android.example.thelanguagelion.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.*

@Entity(tableName = "profile_table")
data class Profile(
    @PrimaryKey(autoGenerate = false)
    var profileId: Int = 0,

    @ColumnInfo(name = "username")
    var username: String = "",

    @ColumnInfo(name = "primaryQueue")
    var primaryQueue: LinkedList<String> = LinkedList(),

    @ColumnInfo(name = "secondaryQueue")
    var secondaryQueue: LinkedList<String> = LinkedList()
)