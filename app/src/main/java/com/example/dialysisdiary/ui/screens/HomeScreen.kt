package com.example.dialysisdiary.ui.screens   // change to match your project

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dialysisdiary.ui.screens.AppBottomBar
import com.example.dialysisdiary.ui.screens.Screen

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
fun HomeScreen(onCheckIn: () -> Unit = {}, onNavigate: (Screen) -> Unit = {}) {
    Scaffold(containerColor = Bg, bottomBar = { AppBottomBar(Screen.Home, onNavigate) }) { padding ->
        LazyColumn(
            modifier = Modifier.padding(padding).fillMaxSize(),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item { Header() }
            item { WelcomeCard(onCheckIn) }
            item { CalendarCard() }
            item { Text("Recent Dialysis Sessions", color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold) }
            item { SessionCard("Yesterday, Oct 16", "4h 00m", "Minor cramping", "68.4 → 65.9 kg", "2.5 / 2.6 L", "142/88 → 124/76") }
            item { SessionCard("Monday, Oct 14", "4h 00m", "All vitals normal", "68.1 → 65.8 kg", "2.4 / 2.4 L", "138/84 → 120/78") }
        }
    }
}

@Composable
private fun AppCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(12.dp)
    Column(
        modifier = Modifier.fillMaxWidth()
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
            Text("NEPHROLOG", color = Primary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Text("Home", color = OnSurface, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
        }
        ThemeToggleButton()
    }
}

@Composable
private fun WelcomeCard(onCheckIn: () -> Unit) {
    AppCard {
        Text("Good morning, Vaibhav", color = OnSurface, fontSize = 22.sp, fontWeight = FontWeight.SemiBold)
        Text("Rest well today. Your goals are within recommended ranges.", color = OnSurfaceVar, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        Text("NEXT TREATMENT", color = OnSurfaceVar, fontSize = 11.sp)
        Text("Tomorrow, 8:30 AM", color = OnSurface, fontWeight = FontWeight.SemiBold)
        Text("Chair #4 • Metro Renal Center", color = OnSurfaceVar, fontSize = 14.sp)
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = onCheckIn,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Primary, contentColor = OnPrimary),
            shape = RoundedCornerShape(10.dp)
        ) { Text("Start Check-In", fontWeight = FontWeight.SemiBold) }
    }
}

@Composable
private fun CalendarCard() {
    val offset = 2                       // October starts on Tuesday
    val daysInMonth = 31
    val completed = setOf(2, 4, 7, 9, 11, 14, 16)
    val today = 17
    val cells: List<Int?> = List(offset) { null } + (1..daysInMonth).toList()

    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("October 2024", color = OnSurface, fontSize = 18.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
            Icon(Icons.Default.KeyboardArrowLeft, null, tint = OnSurfaceVar)
            Icon(Icons.Default.KeyboardArrowRight, null, tint = OnSurfaceVar)
        }
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth()) {
            listOf("S", "M", "T", "W", "T", "F", "S").forEach {
                Text(it, color = OnSurfaceVar, fontSize = 12.sp, textAlign = TextAlign.Center, modifier = Modifier.weight(1f))
            }
        }
        cells.chunked(7).forEach { week ->
            Row(Modifier.fillMaxWidth().height(44.dp)) {
                for (i in 0 until 7) {
                    val day = week.getOrNull(i)
                    Column(
                        Modifier.weight(1f).fillMaxHeight(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        if (day != null) {
                            Text(
                                day.toString(),
                                color = if (day == today) Primary else OnSurface,
                                fontWeight = if (day == today) FontWeight.Bold else FontWeight.Normal
                            )
                            Box(
                                Modifier.size(6.dp)
                                    .background(if (day in completed) Green else Color.Transparent, CircleShape)
                            )
                        }
                    }
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(Green, CircleShape))
            Text("  Completed session", color = OnSurfaceVar, fontSize = 13.sp)
        }
    }
}

@Composable
private fun SessionCard(date: String, duration: String, note: String, weight: String, uf: String, bp: String) {
    AppCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.CheckCircle, null, tint = Green)
            Column(Modifier.padding(start = 10.dp).weight(1f)) {
                Text(date, color = OnSurface, fontWeight = FontWeight.SemiBold)
                Text("$duration • $note", color = OnSurfaceVar, fontSize = 13.sp)
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            StatBox("Weight", weight, Modifier.weight(1f))
            StatBox("UF Target/Done", uf, Modifier.weight(1f))
            StatBox("BP Pre → Post", bp, Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier.background(CardHigh, RoundedCornerShape(8.dp)).padding(10.dp)) {
        Text(label, color = OnSurfaceVar, fontSize = 11.sp)
        Spacer(Modifier.height(4.dp))
        Text(value, color = OnSurface, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun BottomBar() {
    val colors = NavigationBarItemDefaults.colors(
        selectedIconColor = Primary, selectedTextColor = Primary, indicatorColor = CardHigh,
        unselectedIconColor = OnSurfaceVar, unselectedTextColor = OnSurfaceVar
    )
    NavigationBar(containerColor = CardBg) {
        NavigationBarItem(true, {}, { Icon(Icons.Default.Home, null) }, label = { Text("Home") }, colors = colors)
        NavigationBarItem(false, {}, { Icon(Icons.Default.DateRange, null) }, label = { Text("Daily Log") }, colors = colors)
        NavigationBarItem(false, {}, { Icon(Icons.Default.List, null) }, label = { Text("Vitals") }, colors = colors)
        NavigationBarItem(false, {}, { Icon(Icons.Default.Person, null) }, label = { Text("Care Team") }, colors = colors)
    }
}