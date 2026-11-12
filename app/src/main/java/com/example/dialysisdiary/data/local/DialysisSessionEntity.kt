package com.example.dialysisdiary.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "dialysis_sessions")
data class DialysisSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val dateMillis: Long,          // Start of day millis in local timezone
    val startTimeMillis: Long,     // Exact session start epoch millis
    val endTimeMillis: Long,       // Exact session end epoch millis
    val location: String = "",
    val notes: String = "",
    val status: String = STATUS_SCHEDULED  // "Scheduled", "Completed", "Cancelled"
) {
    companion object {
        const val STATUS_SCHEDULED = "Scheduled"
        const val STATUS_COMPLETED = "Completed"
        const val STATUS_CANCELLED = "Cancelled"
    }
}
