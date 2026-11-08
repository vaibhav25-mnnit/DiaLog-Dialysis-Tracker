package com.example.dialysisdiary.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Amber = Color(0xFFFFB95F)
private val Cyan2 = Color(0xFF5DE6FF)
private const val DRY_WEIGHT = 65.5   // TODO: load from backend / profile

private data class MedItem(val name: String, val given: Boolean = false)

private fun adjust(value: String, delta: Double): String {
    val base = value.toDoubleOrNull() ?: 65.0
    return String.format(Locale.US, "%.1f", (base + delta).coerceIn(30.0, 150.0))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyLogScreen(onNavigate: (Screen) -> Unit, onSaved: () -> Unit) {
    val context = LocalContext.current
    val today = remember { SimpleDateFormat("EEEE, MMM d, yyyy", Locale.getDefault()).format(Date()) }

    var preWeight by rememberSaveable { mutableStateOf("") }
    var preSys by rememberSaveable { mutableStateOf("") }
    var preDia by rememberSaveable { mutableStateOf("") }
    var preHr by rememberSaveable { mutableStateOf("") }

    var ufTarget by rememberSaveable { mutableStateOf("") }
    var ufAchieved by rememberSaveable { mutableStateOf("") }

    var postWeight by rememberSaveable { mutableStateOf("") }
    var postSys by rememberSaveable { mutableStateOf("") }
    var postDia by rememberSaveable { mutableStateOf("") }
    var postHr by rememberSaveable { mutableStateOf("") }

    var severity by rememberSaveable { mutableStateOf("Mild") }
    var notes by rememberSaveable { mutableStateOf("") }

    val meds = remember {
        mutableStateListOf(
            MedItem("Heparin Bolus"),
            MedItem("Epoetin Alfa (Epogen)"),
            MedItem("Venofer (Iron Sucrose)"),
            MedItem("Calcitriol (Vitamin D)")
        )
    }
    var newMed by remember { mutableStateOf("") }

    val symptomOptions = listOf(
        "❄️ Cold / Chills", "🤢 Nausea", "🤮 Vomiting", "⚡ Muscle Cramps", "💫 Dizziness",
        "🤕 Headache", "😴 Fatigue", "⚠️ Chest Heaviness", "✨ No Distress"
    )
    val symptoms = remember { mutableStateListOf<String>() }
    val hasSymptoms = symptoms.any { !it.contains("No Distress") }

    val gain = preWeight.toDoubleOrNull()?.minus(DRY_WEIGHT)
    val netLoss = if (preWeight.toDoubleOrNull() != null && postWeight.toDoubleOrNull() != null)
        preWeight.toDouble() - postWeight.toDouble() else null
    val ufProgress = run {
        val t = ufTarget.toDoubleOrNull()
        val a = ufAchieved.toDoubleOrNull()
        if (t != null && a != null && t > 0) (a / t).coerceIn(0.0, 1.0).toFloat() else 0f
    }

    Scaffold(containerColor = UiBg, bottomBar = { AppBottomBar(Screen.DailyLog, onNavigate) }) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Column {
                Text("NEPHROLOG", color = UiPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Text("Daily Check-in", color = UiText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(today, color = UiTextDim, fontSize = 14.sp)
            }
            ThemeToggleButton()

            // 1. Pre-dialysis
            SectionCard("1. Pre-Dialysis Vitals", UiPrimary) {
                Text("Weight (kg)  •  Dry target: $DRY_WEIGHT kg", color = UiTextDim, fontSize = 13.sp)
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepButton("−") { preWeight = adjust(preWeight, -0.1) }
                    Field("Weight", preWeight, { preWeight = it }, Modifier.weight(1f), decimal = true, center = true)
                    StepButton("+") { preWeight = adjust(preWeight, 0.1) }
                }
                if (gain != null) {
                    val high = gain > 2.0
                    InfoPill(
                        text = String.format(Locale.US, "%+.1f kg vs dry weight", gain),
                        color = if (high) Amber else UiPrimary,
                        warn = high
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Field("BP Sys", preSys, { preSys = it }, Modifier.weight(1f))
                    Field("BP Dia", preDia, { preDia = it }, Modifier.weight(1f))
                    Field("Pulse", preHr, { preHr = it }, Modifier.weight(1f))
                }
            }

            // 2. UF
            SectionCard("2. Fluid Ultrafiltration (UF)", Cyan2) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Field("Target UF (L)", ufTarget, { ufTarget = it }, Modifier.weight(1f), decimal = true)
                    Field("Achieved UF (L)", ufAchieved, { ufAchieved = it }, Modifier.weight(1f), decimal = true)
                }
                LinearProgressIndicator(
                    progress = { ufProgress },
                    modifier = Modifier.fillMaxWidth().height(10.dp),
                    color = UiPrimary,
                    trackColor = UiCardHigh
                )
                Text("${(ufProgress * 100).toInt()}% of target achieved", color = UiTextDim, fontSize = 13.sp)
            }

            // 3. Post-dialysis
            SectionCard("3. Post-Dialysis Vitals", Cyan2) {
                Field("Weight (kg)", postWeight, { postWeight = it }, Modifier.fillMaxWidth(), decimal = true)
                if (netLoss != null) {
                    InfoPill(String.format(Locale.US, "Net loss: %.1f kg", netLoss), UiPrimary, warn = false)
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Field("BP Sys", postSys, { postSys = it }, Modifier.weight(1f))
                    Field("BP Dia", postDia, { postDia = it }, Modifier.weight(1f))
                    Field("Pulse", postHr, { postHr = it }, Modifier.weight(1f))
                }
            }

            // 4. Medications
            val givenCount = meds.count { it.given }
            SectionCard("4. Current Medication", Cyan2, trailing = "$givenCount of ${meds.size} given") {
                meds.forEachIndexed { i, med ->
                    Row(
                        Modifier.fillMaxWidth()
                            .background(UiCard, RoundedCornerShape(10.dp))
                            .border(1.dp, UiOutline.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(horizontal = 8.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = med.given,
                            onCheckedChange = { meds[i] = med.copy(given = it) },
                            colors = CheckboxDefaults.colors(
                                checkedColor = UiPrimary, checkmarkColor = UiOnPrimary, uncheckedColor = UiTextDim
                            )
                        )
                        Text(med.name, color = UiText, fontWeight = FontWeight.SemiBold)
                    }
                }
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Field("Add medicine", newMed, { newMed = it }, Modifier.weight(1f), keyboard = KeyboardType.Text)
                    FilledIconButton(
                        onClick = {
                            if (newMed.isNotBlank()) { meds.add(MedItem(newMed.trim())); newMed = "" }
                        },
                        colors = IconButtonDefaults.filledIconButtonColors(containerColor = UiPrimary, contentColor = UiOnPrimary)
                    ) { Icon(Icons.Default.Add, contentDescription = "Add medicine") }
                }
            }

            // 5. Symptoms
            SectionCard("5. Symptoms Experienced", Amber, trailing = "Tap to select") {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    symptomOptions.forEach { s ->
                        FilterChip(
                            selected = s in symptoms,
                            onClick = {
                                if (s in symptoms) symptoms.remove(s)
                                else {
                                    if (s.contains("No Distress")) symptoms.clear()
                                    else symptoms.removeAll { it.contains("No Distress") }
                                    symptoms.add(s)
                                }
                            },
                            label = { Text(s) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = UiCard, labelColor = UiText,
                                selectedContainerColor = UiPrimary, selectedLabelColor = UiOnPrimary
                            )
                        )
                    }
                }
                if (hasSymptoms) {
                    Text("Severity", color = UiTextDim, fontSize = 13.sp)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Mild", "Moderate", "Severe").forEach { level ->
                            val selected = severity == level
                            Button(
                                onClick = { severity = level },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (selected) Amber else UiCardHigh,
                                    contentColor = if (selected) Color(0xFF472A00) else UiTextDim
                                )
                            ) { Text(level, fontWeight = FontWeight.SemiBold) }
                        }
                    }
                }
            }

            // 6. Remarks
            SectionCard("6. Remarks / Extra Injections", UiPrimary) {
                Field(
                    "e.g. Saline 150 mL given, injection for nausea…", notes, { notes = it },
                    Modifier.fillMaxWidth().height(130.dp), keyboard = KeyboardType.Text, singleLine = false
                )
            }

            // Actions
            Button(
                onClick = {
                    if (preWeight.isBlank() || preSys.isBlank() || preDia.isBlank()) {
                        Toast.makeText(context, "Enter pre-dialysis weight and BP", Toast.LENGTH_SHORT).show()
                    } else {
                        // TODO: send all these values to your backend
                        Toast.makeText(context, "Session saved (demo)", Toast.LENGTH_SHORT).show()
                        onSaved()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UiPrimary, contentColor = UiOnPrimary)
            ) { Text("Save & Finalize Session Log", fontWeight = FontWeight.Bold) }

            OutlinedButton(
                onClick = { Toast.makeText(context, "Draft saved (demo)", Toast.LENGTH_SHORT).show() },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) { Text("Save Incomplete Draft", color = UiText) }

            Spacer(Modifier.height(8.dp))
        }
    }
}

@Composable
private fun SectionCard(
    title: String, dot: Color, trailing: String? = null,
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        Modifier.fillMaxWidth()
            .background(UiCard, shape)
            .border(1.dp, UiOutline.copy(alpha = 0.4f), shape)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(Modifier.size(10.dp).background(dot, CircleShape))
            Text(
                title, color = UiText, fontSize = 17.sp, fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(start = 8.dp).weight(1f)
            )
            if (trailing != null) Text(trailing, color = UiTextDim, fontSize = 12.sp)
        }
        content()
    }
}

@Composable
private fun InfoPill(text: String, color: Color, warn: Boolean) {
    Row(
        Modifier.fillMaxWidth()
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(10.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(10.dp))
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (warn) {
            Icon(Icons.Default.Warning, null, tint = color, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(6.dp))
        }
        Text(text, color = color, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
    }
}

@Composable
private fun StepButton(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.size(52.dp),
        contentPadding = PaddingValues(0.dp),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = UiCardHigh, contentColor = UiText)
    ) { Text(label, fontSize = 22.sp, fontWeight = FontWeight.Bold) }
}

@Composable
private fun Field(
    label: String,
    value: String,
    onChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    decimal: Boolean = false,
    keyboard: KeyboardType = if (decimal) KeyboardType.Decimal else KeyboardType.Number,
    singleLine: Boolean = true,
    center: Boolean = false
) {
    OutlinedTextField(
        value = value,
        onValueChange = onChange,
        modifier = modifier,
        label = { Text(label) },
        singleLine = singleLine,
        textStyle = TextStyle(fontSize = 18.sp, textAlign = if (center) TextAlign.Center else TextAlign.Start),
        keyboardOptions = KeyboardOptions(keyboardType = keyboard),
        shape = RoundedCornerShape(10.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = UiText, unfocusedTextColor = UiText,
            focusedBorderColor = UiPrimary, unfocusedBorderColor = UiOutline,
            focusedLabelColor = UiPrimary, unfocusedLabelColor = UiTextDim,
            cursorColor = UiPrimary
        )
    )
}