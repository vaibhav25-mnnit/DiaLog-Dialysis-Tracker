package com.example.dialysisdiary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.example.dialysisdiary.ui.screens.AppTheme
import com.example.dialysisdiary.ui.screens.DailyLogScreen
import com.example.dialysisdiary.ui.screens.PlaceholderScreen
import com.example.dialysisdiary.ui.screens.Screen
import com.example.dialysisdiary.ui.theme.DialysisDiaryTheme
import com.example.dialysisdiary.ui.screens.HomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DialysisDiaryTheme {
                val dark = AppTheme.dark
                DisposableEffect(dark) {
                    val transparent = android.graphics.Color.TRANSPARENT
                    val style = if (dark) SystemBarStyle.dark(transparent)
                    else SystemBarStyle.light(transparent, transparent)
                    this@MainActivity.enableEdgeToEdge(statusBarStyle = style, navigationBarStyle = style)
                    onDispose { }
                }

                var screen by remember { mutableStateOf(Screen.Home) }
                BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

                when (screen) {
                    Screen.Home -> HomeScreen(
                        onCheckIn = { screen = Screen.DailyLog },
                        onNavigate = { screen = it }
                    )
                    Screen.DailyLog -> DailyLogScreen(
                        onNavigate = { screen = it },
                        onSaved = { screen = Screen.Home }
                    )
                    Screen.Vitals -> PlaceholderScreen("Vitals & UF", Screen.Vitals) { screen = it }
                    Screen.CareTeam -> PlaceholderScreen("Care Team", Screen.CareTeam) { screen = it }
                }
            }
        }
    }
}