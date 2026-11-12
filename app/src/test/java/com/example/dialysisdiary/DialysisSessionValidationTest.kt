package com.example.dialysisdiary

import com.example.dialysisdiary.data.local.DialysisSessionEntity
import com.example.dialysisdiary.ui.viewmodel.DialysisSessionViewModel
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class DialysisSessionValidationTest {

    @Test
    fun getStartOfDay_resetsHoursMinutesSecondsMillis() {
        val cal = Calendar.getInstance().apply {
            set(2025, Calendar.OCTOBER, 15, 14, 30, 45)
            set(Calendar.MILLISECOND, 500)
        }
        val startOfDay = DialysisSessionViewModel.getStartOfDay(cal.timeInMillis)

        val checkCal = Calendar.getInstance().apply { timeInMillis = startOfDay }
        assertEquals(2025, checkCal.get(Calendar.YEAR))
        assertEquals(Calendar.OCTOBER, checkCal.get(Calendar.MONTH))
        assertEquals(15, checkCal.get(Calendar.DAY_OF_MONTH))
        assertEquals(0, checkCal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, checkCal.get(Calendar.MINUTE))
        assertEquals(0, checkCal.get(Calendar.SECOND))
        assertEquals(0, checkCal.get(Calendar.MILLISECOND))
    }

    @Test
    fun isSameDay_returnsTrueForTimestampsOnSameDay() {
        val morning = Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 20, 8, 0, 0) }.timeInMillis
        val evening = Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 20, 20, 0, 0) }.timeInMillis

        assertTrue(DialysisSessionViewModel.isSameDay(morning, evening))
    }

    @Test
    fun isSameDay_returnsFalseForDifferentDays() {
        val day1 = Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 20, 8, 0, 0) }.timeInMillis
        val day2 = Calendar.getInstance().apply { set(2025, Calendar.OCTOBER, 21, 8, 0, 0) }.timeInMillis

        assertFalse(DialysisSessionViewModel.isSameDay(day1, day2))
    }

    @Test
    fun dialysisSessionEntity_defaultStatusIsScheduled() {
        val session = DialysisSessionEntity(
            title = "Dialysis Session",
            dateMillis = 1000L,
            startTimeMillis = 1000L,
            endTimeMillis = 5000L
        )

        assertEquals(DialysisSessionEntity.STATUS_SCHEDULED, session.status)
        assertEquals("", session.location)
        assertEquals("", session.notes)
    }

    @Test
    fun dialysisSessionEntity_supportsAllStatuses() {
        val scheduled = DialysisSessionEntity(title = "S1", dateMillis = 0, startTimeMillis = 0, endTimeMillis = 1, status = DialysisSessionEntity.STATUS_SCHEDULED)
        val completed = DialysisSessionEntity(title = "S2", dateMillis = 0, startTimeMillis = 0, endTimeMillis = 1, status = DialysisSessionEntity.STATUS_COMPLETED)
        val cancelled = DialysisSessionEntity(title = "S3", dateMillis = 0, startTimeMillis = 0, endTimeMillis = 1, status = DialysisSessionEntity.STATUS_CANCELLED)

        assertEquals("Scheduled", scheduled.status)
        assertEquals("Completed", completed.status)
        assertEquals("Cancelled", cancelled.status)
    }
}
