package com.example.dialysisdiary.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LocationOn
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
import com.example.dialysisdiary.data.local.DialysisSessionEntity
import com.example.dialysisdiary.data.local.UserProfileManager
import com.example.dialysisdiary.ui.viewmodel.DialysisSessionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val Amber = Color(0xFFFFB95F)
private val Cyan2 = Color(0xFF5DE6FF)

private data class MedItem(val name: String, var given: Boolean = false)

private fun adjust(value: String, delta: Double): String {
    val base = value.toDoubleOrNull() ?: 65.0
    return String.format(Locale.US, "%.1f", (base + delta).coerceIn(30.0, 150.0))
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun DailyLogScreen(
    viewModel: DialysisSessionViewModel? = null,
    onNavigate: (Screen) -> Unit,
    onSaved: () -> Unit
) {
    val context = LocalContext.current
    val userProfile = remember { UserProfileManager.getProfile(context) }
    val dryWeight = userProfile.dryWeight

    // Date selection state
    var selectedDateMillis by rememberSaveable { mutableStateOf(System.currentTimeMillis()) }
    val formattedDate = remember(selectedDateMillis) {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
    }
    val todayStart = remember { DialysisSessionViewModel.getStartOfDay(System.currentTimeMillis()) }
    val isPastDate = remember(selectedDateMillis) {
        DialysisSessionViewModel.getStartOfDay(selectedDateMillis) < todayStart
    }

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
    var dialysisCenter by rememberSaveable { mutableStateOf("") }

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

    val gain = preWeight.toDoubleOrNull()?.minus(dryWeight)
    val netLoss = if (preWeight.toDoubleOrNull() != null && postWeight.toDoubleOrNull() != null)
        preWeight.toDouble() - postWeight.toDouble() else null
    val ufProgress = run {
        val t = ufTarget.toDoubleOrNull()
        val a = ufAchieved.toDoubleOrNull()
        if (t != null && a != null && t > 0) (a / t).coerceIn(0.0, 1.0).toFloat() else 0f
    }

    Scaffold(containerColor = UiBg, bottomBar = { AppBottomBar(Screen.Home, onNavigate) }) { padding ->
        Column(
            Modifier.padding(padding).verticalScroll(rememberScrollState()).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("DiaLog", color = UiPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Daily Check-in", color = UiText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                }
            }

            // Date Selector Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        val cal = Calendar.getInstance().apply { timeInMillis = selectedDateMillis }
                        DatePickerDialog(
                            context,
                            { _, year, month, dayOfMonth ->
                                val newCal = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                                }
                                selectedDateMillis = newCal.timeInMillis
                            },
                            cal.get(Calendar.YEAR),
                            cal.get(Calendar.MONTH),
                            cal.get(Calendar.DAY_OF_MONTH)
                        ).show()
                    },
                shape = RoundedCornerShape(12.dp),
                color = UiCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, UiPrimary.copy(alpha = 0.5f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.DateRange, contentDescription = "Select Date", tint = UiPrimary, modifier = Modifier.size(24.dp))
                        Spacer(Modifier.width(12.dp))
                        Column(Modifier.weight(1f)) {
                            Text("SESSION DATE (TAP TO CHANGE)", color = UiTextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(formattedDate, color = UiText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                        Text("Change", color = UiPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (isPastDate) {
                        Spacer(Modifier.height(6.dp))
                        Text(
                            "• Past date selected: Session will be saved with Completed status.",
                            color = Amber,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }

            // Center field (before section 1)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = UiCard,
                border = androidx.compose.foundation.BorderStroke(1.dp, UiOutline.copy(alpha = 0.4f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = UiPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(Modifier.width(10.dp))
                    OutlinedTextField(
                        value = dialysisCenter,
                        onValueChange = { dialysisCenter = it },
                        label = { Text("Dialysis Center / Hospital") },
                        placeholder = { Text("e.g. City Dialysis Clinic", color = UiTextDim) },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = UiText, unfocusedTextColor = UiText,
                            focusedBorderColor = UiPrimary, unfocusedBorderColor = UiOutline.copy(alpha = 0f),
                            unfocusedContainerColor = UiCard, focusedContainerColor = UiCard,
                            focusedLabelColor = UiPrimary, unfocusedLabelColor = UiTextDim
                        ),
                        shape = RoundedCornerShape(10.dp)
                    )
                }
            }

            // 1. Pre-dialysis
            SectionCard("1. Pre-Dialysis Vitals", UiPrimary) {
                Text(
                    String.format(Locale.US, "Weight (kg)  •  Dry target: %.1f kg", dryWeight),
                    color = UiTextDim,
                    fontSize = 13.sp
                )
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
                    Field("BP Sys", preSys, { preSys = it }, Modifier.weight(1f), center = true)
                    Field("BP Dia", preDia, { preDia = it }, Modifier.weight(1f), center = true)
                    Field("Pulse", preHr, { preHr = it }, Modifier.weight(1f), center = true)
                }
            }

            // 2. Fluid removal
            SectionCard("2. Fluid Ultrafiltration (UF)", Cyan2) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Field("Target UF (L)", ufTarget, { ufTarget = it }, Modifier.weight(1f), decimal = true)
                    Field("Achieved UF (L)", ufAchieved, { ufAchieved = it }, Modifier.weight(1f), decimal = true)
                }
                LinearProgressIndicator(
                    progress = { ufProgress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = Cyan2, trackColor = UiCardHigh
                )
                Text(
                    "${(ufProgress * 100).toInt()}% of target achieved",
                    color = UiTextDim, fontSize = 12.sp
                )
            }

            // 3. Post-dialysis
            SectionCard("3. Post-Dialysis Vitals", Cyan2) {
                Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    StepButton("−") { postWeight = adjust(postWeight, -0.1) }
                    Field("Weight", postWeight, { postWeight = it }, Modifier.weight(1f), decimal = true, center = true)
                    StepButton("+") { postWeight = adjust(postWeight, 0.1) }
                }
                if (netLoss != null) {
                    InfoPill(
                        text = String.format(Locale.US, "-%.1f kg net fluid removed", netLoss),
                        color = UiGreen,
                        warn = false
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Field("BP Sys", postSys, { postSys = it }, Modifier.weight(1f), center = true)
                    Field("BP Dia", postDia, { postDia = it }, Modifier.weight(1f), center = true)
                    Field("Pulse", postHr, { postHr = it }, Modifier.weight(1f), center = true)
                }
            }

            // 4. Medications
            SectionCard("4. Intradialytic Medications", Amber) {
                meds.forEachIndexed { i, m ->
                    Row(
                        Modifier.fillMaxWidth().clickable { meds[i] = m.copy(given = !m.given) },
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Checkbox(
                            checked = m.given,
                            onCheckedChange = { meds[i] = m.copy(given = it) },
                            colors = CheckboxDefaults.colors(checkedColor = Amber)
                        )
                        Text(m.name, color = UiText, fontSize = 14.sp)
                    }
                }
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Field("Add custom med…", newMed, { newMed = it }, Modifier.weight(1f), keyboard = KeyboardType.Text)
                    Button(
                        onClick = {
                            if (newMed.isNotBlank()) {
                                meds.add(MedItem(newMed.trim(), true))
                                newMed = ""
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = Amber, contentColor = Color.Black)
                    ) { Text("Add") }
                }
            }

            // 5. Symptoms
            SectionCard("5. Symptoms & Tolerance", UiGreen) {
                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    symptomOptions.forEach { opt ->
                        val sel = symptoms.contains(opt)
                        FilterChip(
                            selected = sel,
                            onClick = {
                                if (opt.contains("No Distress")) {
                                    symptoms.clear()
                                    if (!sel) symptoms.add(opt)
                                } else {
                                    symptoms.removeIf { it.contains("No Distress") }
                                    if (sel) symptoms.remove(opt) else symptoms.add(opt)
                                }
                            },
                            label = { Text(opt, fontSize = 12.sp) }
                        )
                    }
                }
                if (hasSymptoms) {
                    Text("Severity", color = UiTextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Mild", "Moderate", "Severe").forEach { lvl ->
                            FilterChip(
                                selected = severity == lvl,
                                onClick = { severity = lvl },
                                label = { Text(lvl) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = if (lvl == "Severe") Color(0xFFEF4444) else UiPrimary,
                                    selectedLabelColor = Color.White
                                )
                            )
                        }
                    }
                }
            }

            // 6. Notes
            SectionCard("6. Clinical Notes", UiTextDim) {
                Field(
                    "e.g. Saline 150 mL given, injection for nausea, machine flow rate…", notes, { notes = it },
                    Modifier.fillMaxWidth().height(120.dp), keyboard = KeyboardType.Text, singleLine = false
                )
            }

            // Actions
            Button(
                onClick = {
                    if (preWeight.isBlank() || preSys.isBlank() || preDia.isBlank()) {
                        Toast.makeText(context, "Please enter pre-dialysis weight and blood pressure", Toast.LENGTH_SHORT).show()
                    } else {
                        val startCal = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            set(Calendar.HOUR_OF_DAY, 9)
                            set(Calendar.MINUTE, 0)
                        }
                        val endCal = (startCal.clone() as Calendar).apply {
                            add(Calendar.HOUR_OF_DAY, 4)
                        }

                        val notesBuilder = StringBuilder()
                        notesBuilder.appendLine("Pre-Dialysis Vitals:")
                        notesBuilder.appendLine("• Weight: $preWeight kg (${if (gain != null) String.format(Locale.US, "%+.1f kg vs dry weight", gain) else ""})")
                        notesBuilder.appendLine("• BP: $preSys/$preDia mmHg, Pulse: ${if (preHr.isNotBlank()) "$preHr bpm" else "N/A"}")
                        notesBuilder.appendLine()

                        if (ufTarget.isNotBlank() || ufAchieved.isNotBlank()) {
                            notesBuilder.appendLine("Fluid Ultrafiltration (UF):")
                            notesBuilder.appendLine("• Target: ${ufTarget.ifBlank { "--" }} L, Achieved: ${ufAchieved.ifBlank { "--" }} L (${(ufProgress * 100).toInt()}%)")
                            notesBuilder.appendLine()
                        }

                        if (postWeight.isNotBlank() || postSys.isNotBlank() || postDia.isNotBlank()) {
                            notesBuilder.appendLine("Post-Dialysis Vitals:")
                            notesBuilder.appendLine("• Weight: ${postWeight.ifBlank { "--" }} kg (${if (netLoss != null) String.format(Locale.US, "-%.1f kg net loss", netLoss) else ""})")
                            notesBuilder.appendLine("• BP: ${postSys.ifBlank { "--" }}/${postDia.ifBlank { "--" }} mmHg, Pulse: ${if (postHr.isNotBlank()) "$postHr bpm" else "N/A"}")
                            notesBuilder.appendLine()
                        }

                        val checkedMeds = meds.filter { it.given }.map { it.name }
                        if (checkedMeds.isNotEmpty()) {
                            notesBuilder.appendLine("Medications Given:")
                            notesBuilder.appendLine("• " + checkedMeds.joinToString(", "))
                            notesBuilder.appendLine()
                        }

                        if (hasSymptoms) {
                            notesBuilder.appendLine("Symptoms Reported ($severity):")
                            notesBuilder.appendLine("• " + symptoms.joinToString(", "))
                            notesBuilder.appendLine()
                        }

                        if (notes.isNotBlank()) {
                            notesBuilder.appendLine("Session Notes:")
                            notesBuilder.appendLine(notes.trim())
                        }

                        if (viewModel != null) {
                            viewModel.saveSession(
                                title = "Dialysis Treatment",
                                dateMillis = selectedDateMillis,
                                startTimeMillis = startCal.timeInMillis,
                                endTimeMillis = endCal.timeInMillis,
                                location = dialysisCenter.trim().ifBlank { "Dialysis Clinic" },
                                notes = notesBuilder.toString().trim(),
                                status = DialysisSessionEntity.STATUS_COMPLETED
                            ) { success, error ->
                                if (success) {
                                    Toast.makeText(context, "Session saved and added to Calendar!", Toast.LENGTH_SHORT).show()
                                    onSaved()
                                } else {
                                    Toast.makeText(context, error ?: "Failed to save session", Toast.LENGTH_SHORT).show()
                                }
                            }
                        } else {
                            Toast.makeText(context, "Session saved", Toast.LENGTH_SHORT).show()
                            onSaved()
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UiPrimary, contentColor = UiOnPrimary)
            ) { Text("Save & Finalize Session Log", fontWeight = FontWeight.Bold, fontSize = 16.sp) }

            if (!isPastDate) {
                OutlinedButton(
                    onClick = {
                        val startCal = Calendar.getInstance().apply {
                            timeInMillis = selectedDateMillis
                            set(Calendar.HOUR_OF_DAY, 9)
                            set(Calendar.MINUTE, 0)
                        }
                        val endCal = (startCal.clone() as Calendar).apply {
                            add(Calendar.HOUR_OF_DAY, 4)
                        }
                        viewModel?.saveSession(
                            title = "Scheduled Dialysis",
                            dateMillis = selectedDateMillis,
                            startTimeMillis = startCal.timeInMillis,
                            endTimeMillis = endCal.timeInMillis,
                            location = dialysisCenter.trim().ifBlank { "Dialysis Clinic" },
                            notes = if (notes.isNotBlank()) notes.trim() else "Draft check-in",
                            status = DialysisSessionEntity.STATUS_SCHEDULED
                        ) { success, _ ->
                            if (success) {
                                Toast.makeText(context, "Draft scheduled on calendar", Toast.LENGTH_SHORT).show()
                                onSaved()
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(12.dp)
                ) { Text("Save as Scheduled (Draft)", color = UiText) }
            }

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
