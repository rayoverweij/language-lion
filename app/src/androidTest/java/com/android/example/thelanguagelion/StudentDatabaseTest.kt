package com.android.example.thelanguagelion

import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.android.example.thelanguagelion.database.Sememe
import com.android.example.thelanguagelion.database.StudentDatabase
import com.android.example.thelanguagelion.database.StudentDatabaseDao
import org.junit.Assert.assertEquals
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException


/**
 * A simple test for database construction and querying separate from the app UI.
 */

@RunWith(AndroidJUnit4::class)
class StudentDatabaseTest {

    private lateinit var sleepDao: StudentDatabaseDao
    private lateinit var db: StudentDatabase

    @Before
    fun createDb() {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        // Using an in-memory database because the information stored here disappears when the process is killed
        db = Room.inMemoryDatabaseBuilder(context, StudentDatabase::class.java)
                // Allowing main thread queries, just for testing
                .allowMainThreadQueries()
                .build()
        sleepDao = db.studentDatabaseDao
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    @Throws(Exception::class)
    fun insertAndGetNight() {
        val sememe = Sememe()
        sleepDao.insert(sememe)
        val tonight = sleepDao.get(0)
        assertEquals(tonight?.stage, -1)
    }
}