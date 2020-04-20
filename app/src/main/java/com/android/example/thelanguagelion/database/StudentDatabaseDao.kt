package com.android.example.thelanguagelion.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

@Dao
interface StudentDatabaseDao {
    @Insert
    fun insert(sememe: Sememe)

    @Insert
    fun insert(sentence: Sentence)

    @Insert
    fun insert(profile: Profile)


    @Update
    fun update(sememe: Sememe)

    @Update
    fun update(sentence: Sentence)

    @Update
    fun update(profile: Profile)


    @Query("SELECT * FROM sememe_table WHERE sememeId = :key")
    fun getSememe(key: String): Sememe?

    @Query("SELECT * FROM sememe_table")
    fun getAllSememes(): List<Sememe>

    @Query("SELECT * FROM sememe_table WHERE learned = 1")
    fun getAllLearned(): LiveData<List<Sememe>>

    @Query("DELETE FROM sememe_table")
    fun clearSememes()


    @Query("SELECT * FROM sentence_table WHERE sentenceId = :key")
    fun getSentence(key: String): Sentence?

    @Query("SELECT * FROM sentence_table")
    fun getAllSentences(): List<Sentence>

    @Query("DELETE FROM sentence_table")
    fun clearSentences()


    @Query("SELECT * FROM profile_table")
    fun getAllProfiles(): List<Profile>

    @Query("DELETE FROM profile_table")
    fun clearProfiles()
}