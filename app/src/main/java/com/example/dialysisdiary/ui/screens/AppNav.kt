package com.example.dialysisdiary.ui.screens

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

enum class Screen { Home, Calendar, Profile, DailyLog, SessionDetail }

// Global theme state: change this and every screen recomposes
object AppTheme {
    var dark by mutableStateOf(true)
}

internal val UiBg: Color get() = if (AppTheme.dark) Color(0xFF0B1326) else Color(0xFFF8F9FF)
internal val UiCard: Color get() = if (AppTheme.dark) Color(0xFF131B2E) else Color(0xFFFFFFFF)
internal val UiCardHigh: Color get() = if (AppTheme.dark) Color(0xFF222A3D) else Color(0xFFDCE9FF)
internal val UiOutline: Color get() = if (AppTheme.dark) Color(0xFF3D494C) else Color(0xFFBCC9CD)
internal val UiPrimary: Color get() = if (AppTheme.dark) Color(0xFF4CD7F6) else Color(0xFF00687A)
internal val UiOnPrimary: Color get() = if (AppTheme.dark) Color(0xFF003640) else Color(0xFFFFFFFF)
internal val UiText: Color get() = if (AppTheme.dark) Color(0xFFDAE2FD) else Color(0xFF0D1C2E)
internal val UiTextDim: Color get() = if (AppTheme.dark) Color(0xFFBCC9CD) else Color(0xFF3F484C)
internal val UiGreen: Color get() = if (AppTheme.dark) Color(0xFF4ADE80) else Color(0xFF16A34A)

@Composable
fun ThemeToggleButton() {
    Switch(
        checked = AppTheme.dark,
        onCheckedChange = { AppTheme.dark = it },
        thumbContent = { Text(if (AppTheme.dark) "🌙" else "☀️", fontSize = 12.sp) },
        colors = SwitchDefaults.colors(
            checkedThumbColor = UiPrimary, checkedTrackColor = UiCardHigh, checkedBorderColor = UiOutline,
            uncheckedThumbColor = UiPrimary, uncheckedTrackColor = UiCardHigh, uncheckedBorderColor = UiOutline
        )
    )
}

@Composable
fun AppBottomBar(selected: Screen, onSelect: (Screen) -> Unit) {
    val colors = NavigationBarItemDefaults.colors(
        selectedIconColor = UiPrimary, selectedTextColor = UiPrimary, indicatorColor = UiCardHigh,
        unselectedIconColor = UiTextDim, unselectedTextColor = UiTextDim
    )
    NavigationBar(containerColor = UiCard) {
        NavigationBarItem(selected == Screen.Home, { onSelect(Screen.Home) }, { Icon(Icons.Default.Home, null) }, label = { Text("Home") }, colors = colors)
        NavigationBarItem(selected == Screen.Calendar, { onSelect(Screen.Calendar) }, { Icon(Icons.Default.DateRange, null) }, label = { Text("Calendar") }, colors = colors)
        NavigationBarItem(selected == Screen.Profile, { onSelect(Screen.Profile) }, { Icon(Icons.Default.Person, null) }, label = { Text("Profile") }, colors = colors)
    }
}