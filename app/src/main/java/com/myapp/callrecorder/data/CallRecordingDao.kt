package com.myapp.callrecorder.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface CallRecordingDao {

    @Insert
    suspend fun insert(recording: CallRecording): Long

    // Newest recordings first - this feeds the list shown inside the app
    @Query("SELECT * FROM call_recordings ORDER BY timestamp DESC")
    suspend fun getAll(): List<CallRecording>

    @Query("DELETE FROM call_recordings WHERE id = :id")
    suspend fun deleteById(id: Long)
}
