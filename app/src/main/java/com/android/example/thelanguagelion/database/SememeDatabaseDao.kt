package com.android.example.thelanguagelion.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface SememeDatabaseDao {
    @Insert
    fun insert(sememe: Sememe)

    @Update
    fun update(sememe: Sememe)

    @Query("SELECT * FROM sememe_table WHERE sememeId = :key")
    fun get(key: Int): Sememe?

    @Query("DELETE FROM sememe_table")
    fun clear()

    @Query("SELECT * FROM sememe_table WHERE learned = 1")
    fun getAllLearned(): LiveData<List<Sememe>>
}