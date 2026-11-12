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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.dialysisdiary.data.local.AppDatabase
import com.example.dialysisdiary.data.local.DialysisSessionEntity
import com.example.dialysisdiary.data.repository.DialysisSessionRepository
import com.example.dialysisdiary.ui.screens.*
import com.example.dialysisdiary.ui.theme.DialysisDiaryTheme
import com.example.dialysisdiary.ui.viewmodel.DialysisSessionViewModel
import com.example.dialysisdiary.ui.viewmodel.DialysisSessionViewModelFactory

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

                val database = remember { AppDatabase.getDatabase(applicationContext) }
                val repository = remember { DialysisSessionRepository(database.dialysisSessionDao()) }
                val sessionViewModel: DialysisSessionViewModel = viewModel(
                    factory = DialysisSessionViewModelFactory(repository)
                )

                var screen by remember { mutableStateOf(Screen.Home) }
                var selectedSessionForDetail by remember { mutableStateOf<DialysisSessionEntity?>(null) }
                BackHandler(enabled = screen != Screen.Home) { screen = Screen.Home }

                when (screen) {
                    Screen.Home -> HomeScreen(
                        viewModel = sessionViewModel,
                        onCheckIn = { screen = Screen.DailyLog },
                        onNavigate = { screen = it },
                        onSessionClick = {
                            selectedSessionForDetail = it
                            screen = Screen.SessionDetail
                        }
                    )
                    Screen.Calendar -> CalendarScreen(
                        viewModel = sessionViewModel,
                        onNavigate = { screen = it }
                    )
                    Screen.Profile -> ProfileScreen(
                        onNavigate = { screen = it }
                    )
                    Screen.DailyLog -> DailyLogScreen(
                        viewModel = sessionViewModel,
                        onNavigate = { screen = it },
                        onSaved = { screen = Screen.Home }
                    )
                    Screen.SessionDetail -> SessionDetailScreen(
                        session = selectedSessionForDetail,
                        viewModel = sessionViewModel,
                        onBack = { screen = Screen.Home },
                        onNavigate = { screen = it }
                    )
                }
            }
        }
    }
}