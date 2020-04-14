package com.android.example.thelanguagelion.database

import androidx.room.TypeConverter
import com.google.gson.Gson
import java.util.*

class Converters {
    @TypeConverter
    fun listToJson(value: LinkedList<String>?) = Gson().toJson(value)

    @TypeConverter
    fun jsonToList(value: String) = LinkedList(Gson().fromJson(value, Array<String>::class.java).toList())
}