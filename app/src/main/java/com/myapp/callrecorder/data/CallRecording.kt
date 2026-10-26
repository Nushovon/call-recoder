package com.myapp.callrecorder.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * One row per recorded call.
 * number    -> the phone number involved in the call
 * timestamp -> epoch millis when the call started (shown as date + time in UI)
 * durationSec -> length of the recording in seconds
 * filePath  -> absolute path to the saved .m4a file on disk
 * direction -> "INCOMING" or "OUTGOING"
 */
@Entity(tableName = "call_recordings")
data class CallRecording(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val number: String,
    val timestamp: Long,
    val durationSec: Int,
    val filePath: String,
    val direction: String
)
