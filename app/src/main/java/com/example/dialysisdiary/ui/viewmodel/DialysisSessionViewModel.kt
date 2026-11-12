package com.example.dialysisdiary.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.dialysisdiary.data.local.DialysisSessionEntity
import com.example.dialysisdiary.data.repository.DialysisSessionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

enum class CalendarViewMode {
    MONTH, WEEK, DAY
}

class DialysisSessionViewModel(private val repository: DialysisSessionRepository) : ViewModel() {

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    val sessions: StateFlow<List<DialysisSessionEntity>> = repository.allSessions
        .onEach { _isLoading.value = false }
        .catch { _isLoading.value = false }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _selectedDateMillis = MutableStateFlow(getStartOfDay(System.currentTimeMillis()))
    val selectedDateMillis: StateFlow<Long> = _selectedDateMillis.asStateFlow()

    private val _viewMode = MutableStateFlow(CalendarViewMode.MONTH)
    val viewMode: StateFlow<CalendarViewMode> = _viewMode.asStateFlow()

    fun setViewMode(mode: CalendarViewMode) {
        _viewMode.value = mode
    }

    fun selectDate(millis: Long) {
        _selectedDateMillis.value = getStartOfDay(millis)
    }

    fun navigateToToday() {
        _selectedDateMillis.value = getStartOfDay(System.currentTimeMillis())
    }

    fun navigatePrevious() {
        val cal = Calendar.getInstance().apply { timeInMillis = _selectedDateMillis.value }
        when (_viewMode.value) {
            CalendarViewMode.MONTH -> cal.add(Calendar.MONTH, -1)
            CalendarViewMode.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, -1)
            CalendarViewMode.DAY -> cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        _selectedDateMillis.value = getStartOfDay(cal.timeInMillis)
    }

    fun navigateNext() {
        val cal = Calendar.getInstance().apply { timeInMillis = _selectedDateMillis.value }
        when (_viewMode.value) {
            CalendarViewMode.MONTH -> cal.add(Calendar.MONTH, 1)
            CalendarViewMode.WEEK -> cal.add(Calendar.WEEK_OF_YEAR, 1)
            CalendarViewMode.DAY -> cal.add(Calendar.DAY_OF_YEAR, 1)
        }
        _selectedDateMillis.value = getStartOfDay(cal.timeInMillis)
    }

    fun saveSession(
        id: Long = 0,
        title: String,
        dateMillis: Long,
        startTimeMillis: Long,
        endTimeMillis: Long,
        location: String,
        notes: String,
        status: String,
        onResult: (Boolean, String?) -> Unit
    ) {
        val trimmedTitle = title.trim()
        if (trimmedTitle.isBlank()) {
            onResult(false, "Session title is required.")
            return
        }
        if (endTimeMillis <= startTimeMillis) {
            onResult(false, "End time must be after start time.")
            return
        }

        viewModelScope.launch {
            try {
                val session = DialysisSessionEntity(
                    id = id,
                    title = trimmedTitle,
                    dateMillis = getStartOfDay(dateMillis),
                    startTimeMillis = startTimeMillis,
                    endTimeMillis = endTimeMillis,
                    location = location.trim(),
                    notes = notes.trim(),
                    status = status
                )
                if (id == 0L) {
                    repository.insertSession(session)
                } else {
                    repository.updateSession(session)
                }
                onResult(true, null)
            } catch (e: Exception) {
                onResult(false, e.localizedMessage ?: "Failed to save session")
            }
        }
    }

    fun deleteSession(session: DialysisSessionEntity, onResult: ((Boolean) -> Unit)? = null) {
        viewModelScope.launch {
            try {
                repository.deleteSession(session)
                onResult?.invoke(true)
            } catch (e: Exception) {
                onResult?.invoke(false)
            }
        }
    }

    companion object {
        fun getStartOfDay(timeInMillis: Long): Long {
            val cal = Calendar.getInstance().apply {
                this.timeInMillis = timeInMillis
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            return cal.timeInMillis
        }

        fun isSameDay(millis1: Long, millis2: Long): Boolean {
            return getStartOfDay(millis1) == getStartOfDay(millis2)
        }

        fun formatMonthYear(timeInMillis: Long): String {
            return SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(Date(timeInMillis))
        }

        fun formatDateShort(timeInMillis: Long): String {
            return SimpleDateFormat("EEE, MMM d", Locale.getDefault()).format(Date(timeInMillis))
        }

        fun formatTime(timeInMillis: Long): String {
            return SimpleDateFormat("h:mm a", Locale.getDefault()).format(Date(timeInMillis))
        }
    }
}

class DialysisSessionViewModelFactory(
    private val repository: DialysisSessionRepository
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(DialysisSessionViewModel::class.java)) {
            return DialysisSessionViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
