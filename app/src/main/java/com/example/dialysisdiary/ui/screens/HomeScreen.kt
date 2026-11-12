package com.example.dialysisdiary.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dialysisdiary.data.local.DialysisSessionEntity
import com.example.dialysisdiary.data.local.UserProfileManager
import com.example.dialysisdiary.ui.viewmodel.DialysisSessionViewModel
import java.util.Calendar

private val Bg: Color get() = UiBg
private val CardBg: Color get() = UiCard
private val CardHigh: Color get() = UiCardHigh
private val Outline: Color get() = UiOutline
private val Primary: Color get() = UiPrimary
private val OnPrimary: Color get() = UiOnPrimary
private val OnSurface: Color get() = UiText
private val OnSurfaceVar: Color get() = UiTextDim
private val Green: Color get() = UiGreen

@Composable
fun HomeScreen(
    viewModel: DialysisSessionViewModel? = null,
    onCheckIn: () -> Unit = {},
    onNavigate: (Screen) -> Unit = {},
    onSessionClick: (DialysisSessionEntity) -> Unit = {}
) {
    val sessions = viewModel?.sessions?.collectAsState()?.value ?: emptyList()

    val nextSession = remember(sessions) {
        val now = System.currentTimeMillis()
        sessions.filter { it.startTimeMillis >= now && it.status == DialysisSessionEntity.STATUS_SCHEDULED }
            .minByOrNull { it.startTimeMillis }
    }

    val completedDates = remember(sessions) {
        sessions.filter { it.status == DialysisSessionEntity.STATUS_COMPLETED }
            .map { DialysisSessionViewModel.getStartOfDay(it.dateMillis) }
            .toSet()
    }

    val scheduledDates = remember(sessions) {
        sessions.filter { it.status == DialysisSessionEntity.STATUS_SCHEDULED }
            .map { DialysisSessionViewModel.getStartOfDay(it.dateMillis) }
            .toSet()
    }

    val recentSessions = remember(sessions) {
        sessions.sortedByDescending { it.dateMillis }.take(5)
    }

    Scaffold(
        containerColor = Bg,
        bottomBar = { AppBottomBar(Screen.Home, onNavigate) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigate(Screen.DailyLog) },
                containerColor = Primary,
                contentColor = OnPrimary
            ) {
                Icon(Icons.Default.Add, contentDescription = "New Daily Log")
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Header() }
            item { WelcomeCard(nextSession = nextSession, onCheckIn = onCheckIn, onOpenCalendar = { onNavigate(Screen.Calendar) }) }
            item { CalendarCard(completedDates = completedDates, scheduledDates = scheduledDates, onOpenCalendar = { onNavigate(Screen.Calendar) }) }
            item { Text("Recent Sessions", color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }

            if (sessions.isEmpty()) {
                item {
                    AppCard {
                        Text("No sessions recorded yet.", color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text("Tap '+' to log your first dialysis session.", color = OnSurfaceVar, fontSize = 13.sp)
                    }
                }
            } else {
                recentSessions.forEach { session ->
                    item {
                        SessionCardItem(session = session, onClick = { onSessionClick(session) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AppCard(modifier: Modifier = Modifier, content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = modifier.fillMaxWidth()
            .background(CardBg, shape)
            .border(1.dp, Outline.copy(alpha = 0.4f), shape)
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun Header() {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(
                "DiaLog",
                color = Primary,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.5).sp
            )
        }
    }
}

@Composable
private fun WelcomeCard(
    nextSession: DialysisSessionEntity?,
    onCheckIn: () -> Unit,
    onOpenCalendar: () -> Unit
) {
    val context = LocalContext.current
    val userProfile = remember { UserProfileManager.getProfile(context) }

    val inspiringQuotes = remember {
        listOf(
            "Every session is a step toward a stronger you. 💪",
            "Your resilience is your superpower. Keep going! 🌟",
            "Small steps every day lead to big changes. 🌊",
            "You are braver than you believe, stronger than you seem. 🦁",
            "Today is a new opportunity to take care of yourself. 🌱",
            "Your health journey matters — celebrate every win! 🏆",
            "Consistency is the key to a healthier tomorrow. 🔑",
            "You've got this. One day at a time. ☀️"
        )
    }
    val todayQuote = remember {
        val dayIndex = Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % inspiringQuotes.size
        inspiringQuotes[dayIndex]
    }

    AppCard {
        Text("Hi, ${userProfile.name} 👋", color = OnSurface, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Spacer(Modifier.height(6.dp))
        Surface(
            color = Primary.copy(alpha = 0.08f),
            shape = RoundedCornerShape(10.dp)
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("✦", color = Primary, fontSize = 14.sp, modifier = Modifier.padding(top = 1.dp, end = 8.dp))
                Text(
                    todayQuote,
                    color = OnSurface,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    lineHeight = 20.sp,
                    modifier = Modifier.weight(1f)
                )
            }
        }

//        if (nextSession != null) {
//            Text(
//                "${DialysisSessionViewModel.formatDateShort(nextSession.dateMillis)}, ${DialysisSessionViewModel.formatTime(nextSession.startTimeMillis)}",
//                color = OnSurface,
//                fontWeight = FontWeight.SemiBold
//            )
//            Text(
//                if (nextSession.location.isNotBlank()) nextSession.location else "Dialysis Session",
//                color = OnSurfaceVar,
//                fontSize = 14.sp
//            )
//        } else {
//            Text("No upcoming session scheduled", color = OnSurface, fontWeight = FontWeight.SemiBold)
//            Text("Tap to schedule a session in Calendar", color = OnSurfaceVar, fontSize = 13.sp)
//        }
//
//        Spacer(Modifier.height(12.dp))
//        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
//            Button(
//                onClick = onCheckIn,
//                modifier = Modifier.weight(1f).height(48.dp),
//                colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
//                shape = RoundedCornerShape(10.dp)
//            ) { Text("Start Check-In", fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
//
//            OutlinedButton(
//                onClick = onOpenCalendar,
//                modifier = Modifier.weight(1f).height(48.dp),
//                shape = RoundedCornerShape(10.dp)
//            ) { Text("View Calendar", color = OnSurface, fontWeight = FontWeight.SemiBold, fontSize = 13.sp) }
//        }
    }
}

@Composable
private fun CalendarCard(
    completedDates: Set<Long>,
    scheduledDates: Set<Long>,
    onOpenCalendar: () -> Unit
) {
    val cal = remember { Calendar.getInstance() }
    val monthTitle = remember { DialysisSessionViewModel.formatMonthYear(cal.timeInMillis) }
    val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    val today = cal.get(Calendar.DAY_OF_MONTH)

    val monthStartCal = (cal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, 1) }
    val offset = monthStartCal.get(Calendar.DAY_OF_WEEK) - 1
    val cells: List<Int?> = List(offset) { null } + (1..daysInMonth).toList()

    AppCard {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { onOpenCalendar() }
        ) {
            Text(monthTitle, color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Text("Full Calendar ›", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(it, color = OnSurfaceVar, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth().height(40.dp)) {
                for (i in 0 until 7) {
                    val day = week.getOrNull(i)
                    val cellDateMillis = remember(day) {
                        if (day != null) {
                            (monthStartCal.clone() as Calendar).apply { set(Calendar.DAY_OF_MONTH, day) }.timeInMillis
                        } else 0L
                    }
                    val dayStart = DialysisSessionViewModel.getStartOfDay(cellDateMillis)
                    val isCompleted = dayStart in completedDates
                    val isScheduled = dayStart in scheduledDates

                    Column(
                        Modifier.weight(1f).fillMaxHeight().clickable { onOpenCalendar() },
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (day != null) {
                            Text(
                                day.toString(),
                                color = if (day == today) Primary else OnSurface,
                                fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal,
                                fontSize = 13.sp
                            )
                            Box(
                                Modifier.size(6.dp)
                                    .background(
                                        when {
                                            isCompleted -> Green
                                            isScheduled -> Primary
                                            else -> Color.Transparent
                                        },
                                        CircleShape
                                    )
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(Green, CircleShape))
                Text(" Completed", color = OnSurfaceVar, fontSize = 12.sp)
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(Modifier.size(8.dp).background(Primary, CircleShape))
                Text(" Scheduled", color = OnSurfaceVar, fontSize = 12.sp)
            }
        }
    }
}

@Composable
private fun SessionCardItem(session: DialysisSessionEntity, onClick: () -> Unit) {
    val statusColor = when (session.status) {
        DialysisSessionEntity.STATUS_COMPLETED -> Green
        DialysisSessionEntity.STATUS_CANCELLED -> Color(0xFFEF4444)
        else -> Primary
    }

    val preWeight = remember(session.notes) { extractWeight(session.notes, "Pre-Dialysis") }
    val postWeight = remember(session.notes) { extractWeight(session.notes, "Post-Dialysis") }

    AppCard(modifier = Modifier.clickable(onClick = onClick)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = statusColor)
            Column(Modifier.padding(start = 10.dp).weight(1f)) {
                Text(
                    DialysisSessionViewModel.formatDateShort(session.dateMillis),
                    color = OnSurface,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    "${DialysisSessionViewModel.formatTime(session.startTimeMillis)} - ${DialysisSessionViewModel.formatTime(session.endTimeMillis)}",
                    color = OnSurfaceVar,
                    fontSize = 13.sp
                )
            }
            Surface(
                color = statusColor.copy(alpha = 0.15f),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text(
                    text = session.status,
                    color = statusColor,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox("Pre-Dialysis Weight", preWeight, Modifier.weight(1f))
            StatBox("Post-Dialysis Weight", postWeight, Modifier.weight(1f))
        }

        Spacer(Modifier.height(8.dp))
        Text(
            "Tap to view full record & location ›",
            color = Primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.align(Alignment.End)
        )
    }
}

private fun extractWeight(notes: String, section: String): String {
    val regex = Regex("""$section.*?Weight:\s*([0-9.]+)\s*kg""", setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL))
    val match = regex.find(notes)
    return if (match != null && match.groupValues.size > 1) "${match.groupValues[1]} kg" else "--"
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(CardHigh, RoundedCornerShape(8.dp)).padding(10.dp)) {
        Text(label, color = OnSurfaceVar, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = OnSurface, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}
