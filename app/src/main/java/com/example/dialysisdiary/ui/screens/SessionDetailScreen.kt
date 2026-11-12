package com.example.dialysisdiary.ui.screens

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dialysisdiary.data.local.DialysisSessionEntity
import com.example.dialysisdiary.ui.viewmodel.DialysisSessionViewModel
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun SessionDetailScreen(
    session: DialysisSessionEntity?,
    viewModel: DialysisSessionViewModel?,
    onBack: () -> Unit,
    onNavigate: (Screen) -> Unit
) {
    val context = LocalContext.current
    var showDeleteConfirm by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    // Mutable local copy so edits can reflect immediately without leaving screen
    var currentSession by remember { mutableStateOf(session) }

    if (currentSession == null) {
        Box(
            modifier = Modifier.fillMaxSize().background(UiBg),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Session not found", color = UiText, fontSize = 18.sp)
                Spacer(Modifier.height(8.dp))
                Button(onClick = onBack) { Text("Go Back") }
            }
        }
        return
    }

    val sess = currentSession!!

    val statusColor = when (sess.status) {
        DialysisSessionEntity.STATUS_COMPLETED -> UiGreen
        DialysisSessionEntity.STATUS_CANCELLED -> Color(0xFFEF4444)
        else -> UiPrimary
    }

    val durationMinutes = ((sess.endTimeMillis - sess.startTimeMillis) / (1000 * 60)).coerceAtLeast(0)
    val durationText = "${durationMinutes / 60}h ${String.format(Locale.US, "%02d", durationMinutes % 60)}m"

    Scaffold(
        containerColor = UiBg,
        bottomBar = { AppBottomBar(Screen.Home, onNavigate) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Top Navigation Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .background(UiCard, CircleShape)
                        .border(1.dp, UiOutline.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = UiText)
                }
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text("Dialysis Session", color = UiPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Session Details", color = UiText, fontSize = 22.sp, fontWeight = FontWeight.Bold)
                }
                // Edit button
                IconButton(
                    onClick = { showEditDialog = true },
                    modifier = Modifier
                        .background(UiCard, CircleShape)
                        .border(1.dp, UiOutline.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit Session", tint = UiPrimary)
                }
                Spacer(Modifier.width(8.dp))
                // Delete button
                IconButton(
                    onClick = { showDeleteConfirm = true },
                    modifier = Modifier
                        .background(UiCard, CircleShape)
                        .border(1.dp, UiOutline.copy(alpha = 0.4f), CircleShape)
                ) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete Session", tint = Color(0xFFEF4444))
                }
            }

            // Status Card
            DetailCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = statusColor, modifier = Modifier.size(32.dp))
                    Spacer(Modifier.width(12.dp))
                    Column(Modifier.weight(1f)) {
                        Text(sess.title, color = UiText, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                        Text(
                            DialysisSessionViewModel.formatDateShort(sess.dateMillis),
                            color = UiTextDim,
                            fontSize = 13.sp
                        )
                    }
                    Surface(
                        color = statusColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, statusColor.copy(alpha = 0.4f))
                    ) {
                        Text(
                            text = sess.status,
                            color = statusColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Schedule & Time Info Card
            DetailCard {
                Text("Schedule & Timing", color = UiText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoTile(
                        label = "DATE",
                        value = DialysisSessionViewModel.formatDateShort(sess.dateMillis),
                        modifier = Modifier.weight(1f)
                    )
                    InfoTile(
                        label = "DURATION",
                        value = durationText,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(10.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    InfoTile(
                        label = "START TIME",
                        value = DialysisSessionViewModel.formatTime(sess.startTimeMillis),
                        modifier = Modifier.weight(1f)
                    )
                    InfoTile(
                        label = "END TIME",
                        value = DialysisSessionViewModel.formatTime(sess.endTimeMillis),
                        modifier = Modifier.weight(1f)
                    )
                }

                if (sess.location.isNotBlank()) {
                    Spacer(Modifier.height(10.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(UiCardHigh, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = UiPrimary, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Column {
                            Text("TREATMENT LOCATION", color = UiTextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            Text(sess.location, color = UiText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            // Clinical Log & Notes Card
            DetailCard {
                Text("Clinical Record & Notes", color = UiText, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(Modifier.height(8.dp))

                if (sess.notes.isNotBlank()) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(UiCardHigh, RoundedCornerShape(10.dp))
                            .padding(14.dp)
                    ) {
                        Text(
                            text = sess.notes,
                            color = UiText,
                            fontSize = 14.sp,
                            lineHeight = 22.sp
                        )
                    }
                } else {
                    Text(
                        "No additional vitals or clinical notes were recorded for this session.",
                        color = UiTextDim,
                        fontSize = 13.sp
                    )
                }
            }

            // Edit Session button
            Button(
                onClick = { showEditDialog = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = UiPrimary, contentColor = UiOnPrimary)
            ) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Edit Session", fontWeight = FontWeight.Bold)
            }

            // Back button
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.fillMaxWidth().height(48.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("Back to Home", color = UiText, fontWeight = FontWeight.SemiBold)
            }
        }
    }

    // Delete Confirmation Dialog
    if (showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = { showDeleteConfirm = false },
            containerColor = UiCard,
            title = { Text("Delete Session", color = UiText, fontWeight = FontWeight.Bold) },
            text = { Text("Are you sure you want to delete this dialysis session record? This action cannot be undone.", color = UiTextDim) },
            confirmButton = {
                Button(
                    onClick = {
                        viewModel?.deleteSession(sess) { success ->
                            if (success) {
                                Toast.makeText(context, "Session deleted", Toast.LENGTH_SHORT).show()
                                showDeleteConfirm = false
                                onBack()
                            } else {
                                Toast.makeText(context, "Failed to delete session", Toast.LENGTH_SHORT).show()
                            }
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444), contentColor = Color.White)
                ) { Text("Delete") }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteConfirm = false }) { Text("Cancel", color = UiTextDim) }
            }
        )
    }

    // Edit Session Dialog
    if (showEditDialog) {
        EditSessionDialog(
            session = sess,
            onDismiss = { showEditDialog = false },
            onSave = { updatedTitle, updatedDateMillis, updatedLocation, updatedNotes, updatedStatus ->
                val startCal = Calendar.getInstance().apply {
                    timeInMillis = updatedDateMillis
                    set(Calendar.HOUR_OF_DAY, Calendar.getInstance().also { it.timeInMillis = sess.startTimeMillis }.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, Calendar.getInstance().also { it.timeInMillis = sess.startTimeMillis }.get(Calendar.MINUTE))
                }
                val endCal = Calendar.getInstance().apply {
                    timeInMillis = updatedDateMillis
                    set(Calendar.HOUR_OF_DAY, Calendar.getInstance().also { it.timeInMillis = sess.endTimeMillis }.get(Calendar.HOUR_OF_DAY))
                    set(Calendar.MINUTE, Calendar.getInstance().also { it.timeInMillis = sess.endTimeMillis }.get(Calendar.MINUTE))
                }
                viewModel?.saveSession(
                    id = sess.id,
                    title = updatedTitle,
                    dateMillis = updatedDateMillis,
                    startTimeMillis = startCal.timeInMillis,
                    endTimeMillis = endCal.timeInMillis,
                    location = updatedLocation,
                    notes = updatedNotes,
                    status = updatedStatus
                ) { success, error ->
                    if (success) {
                        // Update local display state
                        currentSession = sess.copy(
                            title = updatedTitle,
                            dateMillis = updatedDateMillis,
                            startTimeMillis = startCal.timeInMillis,
                            endTimeMillis = endCal.timeInMillis,
                            location = updatedLocation,
                            notes = updatedNotes,
                            status = updatedStatus
                        )
                        Toast.makeText(context, "Session updated!", Toast.LENGTH_SHORT).show()
                        showEditDialog = false
                    } else {
                        Toast.makeText(context, error ?: "Failed to update session", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        )
    }
}

@Composable
private fun EditSessionDialog(
    session: DialysisSessionEntity,
    onDismiss: () -> Unit,
    onSave: (title: String, dateMillis: Long, location: String, notes: String, status: String) -> Unit
) {
    val context = LocalContext.current
    val todayStart = remember { DialysisSessionViewModel.getStartOfDay(System.currentTimeMillis()) }

    var title by remember { mutableStateOf(session.title) }
    var selectedDateMillis by remember { mutableStateOf(session.dateMillis) }
    var location by remember { mutableStateOf(session.location) }
    var notes by remember { mutableStateOf(session.notes) }

    val isPastDate = remember(selectedDateMillis) {
        DialysisSessionViewModel.getStartOfDay(selectedDateMillis) < todayStart
    }
    var status by remember {
        mutableStateOf(
            if (isPastDate && session.status == DialysisSessionEntity.STATUS_SCHEDULED)
                DialysisSessionEntity.STATUS_COMPLETED
            else session.status
        )
    }

    // Auto-force status to Completed when past date is selected
    LaunchedEffect(isPastDate) {
        if (isPastDate && status == DialysisSessionEntity.STATUS_SCHEDULED) {
            status = DialysisSessionEntity.STATUS_COMPLETED
        }
    }

    val formattedDate = remember(selectedDateMillis) {
        SimpleDateFormat("EEE, MMM d, yyyy", Locale.getDefault()).format(Date(selectedDateMillis))
    }

    val datePicker = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val cal = Calendar.getInstance().apply {
                set(year, month, dayOfMonth, 0, 0, 0)
                set(Calendar.MILLISECOND, 0)
            }
            selectedDateMillis = cal.timeInMillis
        },
        Calendar.getInstance().also { it.timeInMillis = selectedDateMillis }.get(Calendar.YEAR),
        Calendar.getInstance().also { it.timeInMillis = selectedDateMillis }.get(Calendar.MONTH),
        Calendar.getInstance().also { it.timeInMillis = selectedDateMillis }.get(Calendar.DAY_OF_MONTH)
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UiCard,
        title = {
            Text("Edit Session", color = UiText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Title
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

                // Date Picker
                OutlinedButton(
                    onClick = { datePicker.show() },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp)
                ) {
                    Icon(Icons.Default.DateRange, contentDescription = null, tint = UiPrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("Date: $formattedDate", color = UiText)
                }

                if (isPastDate) {
                    Surface(
                        color = Color(0xFFFFB95F).copy(alpha = 0.15f),
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFFB95F).copy(alpha = 0.5f))
                    ) {
                        Text(
                            "• Past date: status locked to Completed",
                            color = Color(0xFFFFB95F),
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }

                // Status chips
                Text("Status", color = UiTextDim, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    listOf(
                        DialysisSessionEntity.STATUS_SCHEDULED,
                        DialysisSessionEntity.STATUS_COMPLETED,
                        DialysisSessionEntity.STATUS_CANCELLED
                    ).forEach { st ->
                        val isScheduled = st == DialysisSessionEntity.STATUS_SCHEDULED
                        val locked = isPastDate && isScheduled
                        FilterChip(
                            selected = status == st,
                            onClick = { if (!locked) status = st },
                            enabled = !locked,
                            label = { Text(st, fontSize = 11.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = UiCardHigh,
                                labelColor = UiTextDim,
                                selectedContainerColor = when (st) {
                                    DialysisSessionEntity.STATUS_COMPLETED -> UiGreen
                                    DialysisSessionEntity.STATUS_CANCELLED -> Color(0xFFEF4444)
                                    else -> UiPrimary
                                },
                                selectedLabelColor = Color.White
                            )
                        )
                    }
                }

                // Location
                OutlinedTextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location (Optional)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = UiText, unfocusedTextColor = UiText,
                        focusedBorderColor = UiPrimary, unfocusedBorderColor = UiOutline
                    )
                )

                // Notes
                OutlinedTextField(
                    value = notes,
                    onValueChange = { notes = it },
                    label = { Text("Clinical Notes") },
                    modifier = Modifier.fillMaxWidth().height(120.dp),
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
                onClick = { onSave(title, selectedDateMillis, location, notes, status) },
                colors = ButtonDefaults.buttonColors(containerColor = UiPrimary, contentColor = UiOnPrimary)
            ) { Text("Save Changes") }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel", color = UiTextDim) }
        }
    )
}

@Composable
private fun DetailCard(content: @Composable ColumnScope.() -> Unit) {
    val shape = RoundedCornerShape(14.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(UiCard, shape)
            .border(1.dp, UiOutline.copy(alpha = 0.4f), shape)
            .padding(16.dp),
        content = content
    )
}

@Composable
private fun InfoTile(label: String, value: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .background(UiCardHigh, RoundedCornerShape(10.dp))
            .padding(12.dp)
    ) {
        Text(label, color = UiTextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = UiText, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
    }
}
