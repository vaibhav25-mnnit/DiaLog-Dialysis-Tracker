package com.example.dialysisdiary.ui.screens

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.KeyboardArrowLeft
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dialysisdiary.data.local.DialysisSessionEntity
import com.example.dialysisdiary.ui.viewmodel.CalendarViewMode
import com.example.dialysisdiary.ui.viewmodel.DialysisSessionViewModel
import java.util.Calendar
import java.util.Date

@Composable
fun CalendarScreen(
    viewModel: DialysisSessionViewModel,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    val sessions by viewModel.sessions.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val selectedDateMillis by viewModel.selectedDateMillis.collectAsState()
    val viewMode by viewModel.viewMode.collectAsState()

    var showFormDialog by remember { mutableStateOf(false) }
    var sessionToEdit by remember { mutableStateOf<DialysisSessionEntity?>(null) }
    var sessionToView by remember { mutableStateOf<DialysisSessionEntity?>(null) }
    var sessionToDelete by remember { mutableStateOf<DialysisSessionEntity?>(null) }

    Scaffold(
        containerColor = UiBg,
        bottomBar = { AppBottomBar(Screen.Calendar, onNavigate) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Screen.DailyLog) },
                containerColor = UiPrimary,
                contentColor = UiOnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Session")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            // Header
            CalendarHeader(
                selectedDateMillis = selectedDateMillis,
                viewMode = viewMode,
                onPrevious = { viewModel.navigatePrevious() },
                onNext = { viewModel.navigateNext() },
                onToday = { viewModel.navigateToToday() },
                onViewModeChange = { viewModel.setViewMode(it) }
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = UiPrimary)
                }
            } else {
                Box(modifier = Modifier.weight(1f)) {
                    when (viewMode) {
                        CalendarViewMode.MONTH -> MonthCalendarView(
                            selectedDateMillis = selectedDateMillis,
                            sessions = sessions,
                            onDateSelected = { viewModel.selectDate(it) },
                            onSessionClick = { sessionToView = it }
                        )

                        CalendarViewMode.WEEK -> WeekCalendarView(
                            selectedDateMillis = selectedDateMillis,
                            sessions = sessions,
                            onDateSelected = { viewModel.selectDate(it) },
                            onSessionClick = { sessionToView = it }
                        )

                        CalendarViewMode.DAY -> DayCalendarView(
                            selectedDateMillis = selectedDateMillis,
                            sessions = sessions,
                            onSessionClick = { sessionToView = it }
                        )
                    }
                }
            }
        }
    }

    // Session Details Dialog
    sessionToView?.let { session ->
        SessionDetailsDialog(
            session = session,
            onDismiss = { sessionToView = null },
            onEdit = {
                sessionToEdit = session
                sessionToView = null
                showFormDialog = true
            },
            onDelete = {
                sessionToDelete = session
                sessionToView = null
            }
        )
    }

    // Add / Edit Session Form Dialog
    if (showFormDialog) {
        SessionFormDialog(
            initialSession = sessionToEdit,
            defaultDateMillis = selectedDateMillis,
            onDismiss = { showFormDialog = false },
            onSave = { id, title, dateMillis, startTimeMillis, endTimeMillis, location, notes, status ->
                viewModel.saveSession(
                    id = id,
                    title = title,
                    dateMillis = dateMillis,
                    startTimeMillis = startTimeMillis,
                    endTimeMillis = endTimeMillis,
                    location = location,
                    notes = notes,
                    status = status
                ) { success, errorMsg ->
                    if (success) {
                        Toast.makeText(context, "Session saved successfully", Toast.LENGTH_SHORT).show()
                        showFormDialog = false
                    } else {
                        Toast.makeText(context, errorMsg ?: "Error saving session", Toast.LENGTH_LONG).show()
                    }
                }
            }
        )
    }

    // Delete Confirmation Dialog
    sessionToDelete?.let { session ->
        AlertDialog(
            onDismissRequest = { sessionToDelete = null },
            containerColor = UiCard,
            title = { Text("Delete Session", color = UiText, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete '${session.title}'? This action cannot be undone.", color = UiTextDim) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteSession(session) { success ->
                            if (success) {
                                Toast.makeText(context, "Session deleted", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Failed to delete session", Toast.LENGTH_SHORT).show()
                            }
                        }
                        sessionToDelete = null
                    }
                ) {
                    Text("Delete", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { sessionToDelete = null }) {
                    Text("Cancel", color = UiTextDim)
                }
            }
        )
    }
}

@Composable
private fun CalendarHeader(
    selectedDateMillis: Long,
    viewMode: CalendarViewMode,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onToday: () -> Unit,
    onViewModeChange: (CalendarViewMode) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UiCard)
            .padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text("DiaLog", color = UiPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                Text(
                    text = DialysisSessionViewModel.formatMonthYear(selectedDateMillis),
                    color = UiText,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            ThemeToggleButton()
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // View Mode Selector
            Row(
                modifier = Modifier
                    .background(UiCardHigh, RoundedCornerShape(10.dp))
                    .padding(2.dp)
            ) {
                CalendarViewMode.entries.forEach { mode ->
                    val selected = mode == viewMode
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (selected) UiPrimary else Color.Transparent)
                            .clickable { onViewModeChange(mode) }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = mode.name.lowercase().replaceFirstChar { it.uppercase() },
                            color = if (selected) UiOnPrimary else UiTextDim,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            // Navigation Controls
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedButton(
                    onClick = onToday,
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = UiText)
                ) {
                    Text("Today", fontSize = 12.sp)
                }

                IconButton(onClick = onPrevious, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.KeyboardArrowLeft, contentDescription = "Previous", tint = UiText)
                }

                IconButton(onClick = onNext, modifier = Modifier.size(36.dp)) {
                    Icon(Icons.Default.KeyboardArrowRight, contentDescription = "Next", tint = UiText)
                }
            }
        }
    }
}

@Composable
private fun MonthCalendarView(
    selectedDateMillis: Long,
    sessions: List<DialysisSessionEntity>,
    onDateSelected: (Long) -> Unit,
    onSessionClick: (DialysisSessionEntity) -> Unit
) {
    val calendar = remember(selectedDateMillis) {
        Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.DAY_OF_MONTH, 1)
        }
    }

    val monthDays = remember(selectedDateMillis) {
        val days = mutableListOf<CalendarDayInfo>()
        val cal = calendar.clone() as Calendar

        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0 for Sunday
        cal.add(Calendar.DAY_OF_MONTH, -firstDayOfWeek)

        for (i in 0 until 42) {
            val dayStart = DialysisSessionViewModel.getStartOfDay(cal.timeInMillis)
            val isCurrentMonth = cal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH)
            val isToday = DialysisSessionViewModel.isSameDay(dayStart, System.currentTimeMillis())
            days.add(
                CalendarDayInfo(
                    dayNumber = cal.get(Calendar.DAY_OF_MONTH),
                    dateMillis = dayStart,
                    isCurrentMonth = isCurrentMonth,
                    isToday = isToday
                )
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
        }
        days
    }

    val selectedDaySessions = remember(selectedDateMillis, sessions) {
        sessions.filter { DialysisSessionViewModel.isSameDay(it.dateMillis, selectedDateMillis) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Weekday Header
        Row(modifier = Modifier.fillMaxWidth()) {
            listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat").forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = UiTextDim,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        // Grid of Days
        monthDays.chunked(7).forEach { week ->
            Row(modifier = Modifier.fillMaxWidth()) {
                week.forEach { dayInfo ->
                    val isSelected = DialysisSessionViewModel.isSameDay(dayInfo.dateMillis, selectedDateMillis)
                    val daySessions = sessions.filter { DialysisSessionViewModel.isSameDay(it.dateMillis, dayInfo.dateMillis) }

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(68.dp)
                            .padding(2.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(
                                when {
                                    isSelected -> UiPrimary.copy(alpha = 0.25f)
                                    dayInfo.isToday -> UiCardHigh
                                    dayInfo.isCurrentMonth -> UiCard
                                    else -> UiBg.copy(alpha = 0.5f)
                                }
                            )
                            .border(
                                width = if (isSelected) 1.5.dp else 0.5.dp,
                                color = if (isSelected) UiPrimary else UiOutline.copy(alpha = 0.3f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onDateSelected(dayInfo.dateMillis) }
                            .padding(4.dp)
                    ) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = dayInfo.dayNumber.toString(),
                                color = when {
                                    dayInfo.isToday -> UiPrimary
                                    dayInfo.isCurrentMonth -> UiText
                                    else -> UiTextDim.copy(alpha = 0.5f)
                                },
                                fontSize = 13.sp,
                                fontWeight = if (dayInfo.isToday || isSelected) FontWeight.Bold else FontWeight.Normal
                            )

                            Spacer(Modifier.height(4.dp))

                            // Session Badges / Dots
                            if (daySessions.isNotEmpty()) {
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(2.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally
                                ) {
                                    daySessions.take(2).forEach { session ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(14.dp)
                                                .clip(RoundedCornerShape(3.dp))
                                                .background(getStatusColor(session.status))
                                                .padding(horizontal = 2.dp),
                                            contentAlignment = Alignment.CenterStart
                                        ) {
                                            Text(
                                                text = session.title,
                                                color = Color.White,
                                                fontSize = 9.sp,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                    if (daySessions.size > 2) {
                                        Text(
                                            text = "+${daySessions.size - 2} more",
                                            color = UiPrimary,
                                            fontSize = 9.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Selected Date Session List Section
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Sessions for ${DialysisSessionViewModel.formatDateShort(selectedDateMillis)}",
            color = UiText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        if (selectedDaySessions.isEmpty()) {
            Text(
                text = "No dialysis sessions scheduled for this date.",
                color = UiTextDim,
                fontSize = 14.sp
            )
        } else {
            selectedDaySessions.forEach { session ->
                SessionCardItem(session = session, onClick = { onSessionClick(session) })
            }
        }
    }
}

@Composable
private fun WeekCalendarView(
    selectedDateMillis: Long,
    sessions: List<DialysisSessionEntity>,
    onDateSelected: (Long) -> Unit,
    onSessionClick: (DialysisSessionEntity) -> Unit
) {
    val weekDays = remember(selectedDateMillis) {
        val cal = Calendar.getInstance().apply {
            timeInMillis = selectedDateMillis
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)
        }
        List(7) {
            val start = DialysisSessionViewModel.getStartOfDay(cal.timeInMillis)
            val info = CalendarDayInfo(
                dayNumber = cal.get(Calendar.DAY_OF_MONTH),
                dateMillis = start,
                isCurrentMonth = true,
                isToday = DialysisSessionViewModel.isSameDay(start, System.currentTimeMillis())
            )
            cal.add(Calendar.DAY_OF_MONTH, 1)
            info
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Week Header (7 days)
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            val dayLabels = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
            weekDays.forEachIndexed { idx, dayInfo ->
                val isSelected = DialysisSessionViewModel.isSameDay(dayInfo.dateMillis, selectedDateMillis)
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) UiPrimary else UiCard)
                        .clickable { onDateSelected(dayInfo.dateMillis) }
                        .padding(vertical = 8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = dayLabels[idx],
                        color = if (isSelected) UiOnPrimary else UiTextDim,
                        fontSize = 11.sp
                    )
                    Text(
                        text = dayInfo.dayNumber.toString(),
                        color = if (isSelected) UiOnPrimary else UiText,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Sessions for week days
        Text(
            text = "Week Agenda",
            color = UiText,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        weekDays.forEach { dayInfo ->
            val daySessions = sessions.filter { DialysisSessionViewModel.isSameDay(it.dateMillis, dayInfo.dateMillis) }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(UiCard, RoundedCornerShape(12.dp))
                    .padding(12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = DialysisSessionViewModel.formatDateShort(dayInfo.dateMillis),
                        color = UiPrimary,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f)
                    )
                    if (dayInfo.isToday) {
                        Box(
                            modifier = Modifier
                                .background(UiPrimary.copy(alpha = 0.2f), RoundedCornerShape(6.dp))
                                .padding(horizontal = 6.dp, vertical = 2.dp)
                        ) {
                            Text("Today", color = UiPrimary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                if (daySessions.isEmpty()) {
                    Text("No sessions", color = UiTextDim, fontSize = 12.sp)
                } else {
                    daySessions.forEach { session ->
                        SessionCardItem(session = session, onClick = { onSessionClick(session) })
                    }
                }
            }
        }
    }
}

@Composable
private fun DayCalendarView(
    selectedDateMillis: Long,
    sessions: List<DialysisSessionEntity>,
    onSessionClick: (DialysisSessionEntity) -> Unit
) {
    val daySessions = remember(selectedDateMillis, sessions) {
        sessions.filter { DialysisSessionViewModel.isSameDay(it.dateMillis, selectedDateMillis) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = DialysisSessionViewModel.formatDateShort(selectedDateMillis),
                color = UiText,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "${daySessions.size} Session(s)",
                color = UiTextDim,
                fontSize = 14.sp
            )
        }

        if (daySessions.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(UiCard, RoundedCornerShape(12.dp)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No dialysis sessions scheduled for this day.\nTap '+' to create one.",
                    color = UiTextDim,
                    textAlign = TextAlign.Center,
                    fontSize = 14.sp
                )
            }
        } else {
            daySessions.forEach { session ->
                SessionCardItem(session = session, onClick = { onSessionClick(session) })
            }
        }
    }
}

@Composable
private fun SessionCardItem(
    session: DialysisSessionEntity,
    onClick: () -> Unit
) {
    val statusColor = getStatusColor(session.status)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(UiCard)
            .border(1.dp, UiOutline.copy(alpha = 0.3f), RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(5.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(statusColor)
        )

        Spacer(Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.title,
                    color = UiText,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = session.status)
            }

            Spacer(Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.DateRange, contentDescription = null, tint = UiTextDim, modifier = Modifier.size(14.dp))
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "${DialysisSessionViewModel.formatTime(session.startTimeMillis)} - ${DialysisSessionViewModel.formatTime(session.endTimeMillis)}",
                    color = UiTextDim,
                    fontSize = 13.sp
                )
            }

            if (session.location.isNotBlank()) {
                Spacer(Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = UiTextDim, modifier = Modifier.size(14.dp))
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = session.location,
                        color = UiTextDim,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusChip(status: String) {
    val color = getStatusColor(status)
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = 0.15f))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            text = status,
            color = color,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun SessionDetailsDialog(
    session: DialysisSessionEntity,
    onDismiss: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UiCard,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = session.title,
                    color = UiText,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    modifier = Modifier.weight(1f)
                )
                StatusChip(status = session.status)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                DetailRow(icon = Icons.Default.DateRange, label = "Date & Time", value = "${DialysisSessionViewModel.formatDateShort(session.dateMillis)}, ${DialysisSessionViewModel.formatTime(session.startTimeMillis)} - ${DialysisSessionViewModel.formatTime(session.endTimeMillis)}")
                if (session.location.isNotBlank()) {
                    DetailRow(icon = Icons.Default.LocationOn, label = "Location", value = session.location)
                }
                if (session.notes.isNotBlank()) {
                    Text("Notes", color = UiTextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(session.notes, color = UiText, fontSize = 14.sp)
                }
            }
        },
        confirmButton = {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                IconButton(onClick = onDelete) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444))
                }
                Button(
                    onClick = onEdit,
                    colors = ButtonDefaults.buttonColors(containerColor = UiPrimary, contentColor = UiOnPrimary)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("Edit")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Close", color = UiTextDim)
            }
        }
    )
}

@Composable
private fun DetailRow(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = UiPrimary, modifier = Modifier.size(18.dp))
        Spacer(Modifier.width(8.dp))
        Column {
            Text(label, color = UiTextDim, fontSize = 11.sp)
            Text(value, color = UiText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun SessionFormDialog(
    initialSession: DialysisSessionEntity?,
    defaultDateMillis: Long,
    onDismiss: () -> Unit,
    onSave: (Long, String, Long, Long, Long, String, String, String) -> Unit
) {
    val context = LocalContext.current
    val isEdit = initialSession != null

    var title by remember { mutableStateOf(initialSession?.title ?: "Dialysis Treatment") }

    val calendarDate = remember {
        Calendar.getInstance().apply {
            timeInMillis = initialSession?.dateMillis ?: defaultDateMillis
        }
    }
    var selectedDateMillis by remember { mutableStateOf(calendarDate.timeInMillis) }

    val calendarStart = remember {
        Calendar.getInstance().apply {
            if (initialSession != null) {
                timeInMillis = initialSession.startTimeMillis
            } else {
                timeInMillis = selectedDateMillis
                set(Calendar.HOUR_OF_DAY, 8)
                set(Calendar.MINUTE, 0)
            }
        }
    }
    var startTimeMillis by remember { mutableStateOf(calendarStart.timeInMillis) }

    val calendarEnd = remember {
        Calendar.getInstance().apply {
            if (initialSession != null) {
                timeInMillis = initialSession.endTimeMillis
            } else {
                timeInMillis = selectedDateMillis
                set(Calendar.HOUR_OF_DAY, 12)
                set(Calendar.MINUTE, 0)
            }
        }
    }
    var endTimeMillis by remember { mutableStateOf(calendarEnd.timeInMillis) }

    var location by remember { mutableStateOf(initialSession?.location ?: "") }
    var notes by remember { mutableStateOf(initialSession?.notes ?: "") }
    var status by remember { mutableStateOf(initialSession?.status ?: DialysisSessionEntity.STATUS_SCHEDULED) }

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDateMillis = cal.timeInMillis

            // Update start and end time dates
            val sCal = Calendar.getInstance().apply {
                timeInMillis = startTimeMillis
                set(year, month, dayOfMonth)
            }
            startTimeMillis = sCal.timeInMillis

            val eCal = Calendar.getInstance().apply {
                timeInMillis = endTimeMillis
                set(year, month, dayOfMonth)
            }
            endTimeMillis = eCal.timeInMillis
        },
        calendarDate.get(Calendar.YEAR),
        calendarDate.get(Calendar.MONTH),
        calendarDate.get(Calendar.DAY_OF_MONTH)
    )

    val startTimePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            val sCal = Calendar.getInstance().apply {
                timeInMillis = startTimeMillis
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            startTimeMillis = sCal.timeInMillis
        },
        calendarStart.get(Calendar.HOUR_OF_DAY),
        calendarStart.get(Calendar.MINUTE),
        false
    )

    val endTimePicker = TimePickerDialog(
        context,
        { _, hour, minute ->
            val eCal = Calendar.getInstance().apply {
                timeInMillis = endTimeMillis
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
            }
            endTimeMillis = eCal.timeInMillis
        },
        calendarEnd.get(Calendar.HOUR_OF_DAY),
        calendarEnd.get(Calendar.MINUTE),
        false
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UiCard,
        title = {
            Text(
                text = if (isEdit) "Edit Dialysis Session" else "New Dialysis Session",
                color = UiText,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Session Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = UiText, unfocusedTextColor = UiText,
                        focusedBorderColor = UiPrimary, unfocusedBorderColor = UiOutline
                    )
                )

                // Date Picker Button
                OutlinedButton(
                    onClick = { datePicker.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Text("Date: ${DialysisSessionViewModel.formatDateShort(selectedDateMillis)}", color = UiText)
                }

                // Time Range Picker Row
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = { startTimePicker.show() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("Start: ${DialysisSessionViewModel.formatTime(startTimeMillis)}", color = UiText, fontSize = 12.sp)
                    }

                    OutlinedButton(
                        onClick = { endTimePicker.show() },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("End: ${DialysisSessionViewModel.formatTime(endTimeMillis)}", color = UiText, fontSize = 12.sp)
                    }
                }

                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Dialysis Center / Location (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = UiText, unfocusedTextColor = UiText,
                        focusedBorderColor = UiPrimary, unfocusedBorderColor = UiOutline
                    )
                )

                val isPastInForm = DialysisSessionViewModel.getStartOfDay(selectedDateMillis) < DialysisSessionViewModel.getStartOfDay(System.currentTimeMillis())
                // Auto-enforce Completed for past dates
                LaunchedEffect(isPastInForm) {
                    if (isPastInForm && status == DialysisSessionEntity.STATUS_SCHEDULED) {
                        status = DialysisSessionEntity.STATUS_COMPLETED
                    }
                }
                Text("Status", color = UiTextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                if (isPastInForm) {
                    Text("• Past date — Scheduled status not available", color = Color(0xFFFFB95F), fontSize = 11.sp)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        DialysisSessionEntity.STATUS_SCHEDULED,
                        DialysisSessionEntity.STATUS_COMPLETED,
                        DialysisSessionEntity.STATUS_CANCELLED
                    ).forEach { st ->
                        val isScheduledChip = st == DialysisSessionEntity.STATUS_SCHEDULED
                        val locked = isPastInForm && isScheduledChip
                        val selected = status == st
                        FilterChip(
                            selected = selected,
                            onClick = { if (!locked) status = st },
                            enabled = !locked,
                            label = { Text(st, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = UiCardHigh, labelColor = UiTextDim,
                                selectedContainerColor = getStatusColor(st), selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Notes (Optional)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = UiText, unfocusedTextColor = UiText,
                        focusedBorderColor = UiPrimary, unfocusedBorderColor = UiOutline
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onSave(
                        initialSession?.id ?: 0L,
                        title,
                        selectedDateMillis,
                        startTimeMillis,
                        endTimeMillis,
                        location,
                        notes,
                        status
                    )
                },
                colors = ButtonDefaults.buttonColors(containerColor = UiPrimary, contentColor = UiOnPrimary)
            ) {
                Text(if (isEdit) "Update" else "Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UiTextDim)
            }
        }
    )
}

private fun getStatusColor(status: String): Color {
    return when (status) {
        DialysisSessionEntity.STATUS_COMPLETED -> Color(0xFF16A34A)
        DialysisSessionEntity.STATUS_CANCELLED -> Color(0xFFEF4444)
        else -> Color(0xFF0284C7)
    }
}

private data class CalendarDayInfo(
    val dayNumber: Int,
    val dateMillis: Long,
    val isCurrentMonth: Boolean,
    val isToday: Boolean
)
