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
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.dialysisdiary.data.local.UserProfile
import com.example.dialysisdiary.data.local.UserProfileManager
import java.util.Locale

@Composable
fun ProfileScreen(onNavigate: (Screen) -> Unit) {
    val context = LocalContext.current
    var profile by remember { mutableStateOf(UserProfileManager.getProfile(context)) }
    var showEditDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = UiBg,
        bottomBar = { AppBottomBar(Screen.Profile, onNavigate) }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Header
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text("DiaLog", color = UiPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text("Profile", color = UiText, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
                ThemeToggleButton()
            }

            // User Card
            ProfileCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(UiPrimary.copy(alpha = 0.2f))
                            .border(2.dp, UiPrimary, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = profile.name.take(1).uppercase(),
                            color = UiPrimary,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(Modifier.width(16.dp))
                    Column(Modifier.weight(1f)) {
                        Text(profile.name, color = UiText, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                        Text("Dialysis Patient", color = UiTextDim, fontSize = 13.sp)
                    }
                    IconButton(
                        onClick = { showEditDialog = true },
                        modifier = Modifier
                            .background(UiCardHigh, CircleShape)
                            .size(40.dp)
                    ) {
                        Icon(Icons.Default.Edit, contentDescription = "Edit Profile", tint = UiPrimary, modifier = Modifier.size(18.dp))
                    }
                }
            }

            // Metrics Card (Dry weight & Height)
            ProfileCard {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Clinical Metrics",
                        color = UiText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    TextButton(onClick = { showEditDialog = true }) {
                        Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(14.dp), tint = UiPrimary)
                        Spacer(Modifier.width(4.dp))
                        Text("Edit", color = UiPrimary, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricBox(
                        label = "LATEST DRY WEIGHT",
                        value = String.format(Locale.US, "%.1f", profile.dryWeight),
                        unit = "kg",
                        caption = "Target post-treatment",
                        modifier = Modifier.weight(1f)
                    )
                    MetricBox(
                        label = "HEIGHT",
                        value = String.format(Locale.US, "%.1f", profile.height),
                        unit = "cm",
                        caption = "Baseline height",
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))

                // BMI Card
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(UiCardHigh, RoundedCornerShape(10.dp))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text("BODY MASS INDEX (BMI)", color = UiTextDim, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(2.dp))
                        Text(
                            String.format(Locale.US, "%.1f kg/m²", profile.bmi),
                            color = UiText,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Surface(
                        color = UiGreen.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = profile.bmiCategory,
                            color = UiGreen,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }
            }

            // Info Card about Dry Weight
            ProfileCard {
                Row(verticalAlignment = Alignment.Top) {
                    Icon(Icons.Default.Info, contentDescription = null, tint = UiPrimary, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Column {
                        Text("What is Dry Weight?", color = UiText, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Dry weight is your ideal weight without excess fluid build-up in your body. It is used during each session to calculate fluid removal (ultrafiltration target). Consult your nephrologist before updating your dry weight.",
                            color = UiTextDim,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }
        }
    }

    if (showEditDialog) {
        EditProfileDialog(
            profile = profile,
            onDismiss = { showEditDialog = false },
            onSave = { updated ->
                UserProfileManager.saveProfile(context, updated)
                profile = updated
                showEditDialog = false
                Toast.makeText(context, "Profile updated successfully", Toast.LENGTH_SHORT).show()
            }
        )
    }
}

@Composable
private fun ProfileCard(content: @Composable ColumnScope.() -> Unit) {
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
private fun MetricBox(
    label: String,
    value: String,
    unit: String,
    caption: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(UiCardHigh, RoundedCornerShape(10.dp))
            .padding(14.dp)
    ) {
        Text(label, color = UiTextDim, fontSize = 10.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(value, color = UiText, fontSize = 26.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(4.dp))
            Text(unit, color = UiPrimary, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.padding(bottom = 3.dp))
        }
        Spacer(Modifier.height(4.dp))
        Text(caption, color = UiTextDim, fontSize = 11.sp)
    }
}

@Composable
private fun EditProfileDialog(
    profile: UserProfile,
    onDismiss: () -> Unit,
    onSave: (UserProfile) -> Unit
) {
    var name by remember { mutableStateOf(profile.name) }
    var dryWeightText by remember { mutableStateOf(String.format(Locale.US, "%.1f", profile.dryWeight)) }
    var heightText by remember { mutableStateOf(String.format(Locale.US, "%.1f", profile.height)) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = UiCard,
        title = {
            Text("Edit Profile & Metrics", color = UiText, fontWeight = FontWeight.Bold, fontSize = 18.sp)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Patient Name") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = UiText, unfocusedTextColor = UiText,
                        focusedBorderColor = UiPrimary, unfocusedBorderColor = UiOutline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = dryWeightText,
                    onValueChange = { dryWeightText = it },
                    label = { Text("Latest Dry Weight (kg)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = UiText, unfocusedTextColor = UiText,
                        focusedBorderColor = UiPrimary, unfocusedBorderColor = UiOutline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                OutlinedTextField(
                    value = heightText,
                    onValueChange = { heightText = it },
                    label = { Text("Height (cm)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = UiText, unfocusedTextColor = UiText,
                        focusedBorderColor = UiPrimary, unfocusedBorderColor = UiOutline
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (errorMessage != null) {
                    Text(errorMessage!!, color = Color(0xFFEF4444), fontSize = 12.sp)
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val dryWeight = dryWeightText.toDoubleOrNull()
                    val height = heightText.toDoubleOrNull()
                    if (name.isBlank()) {
                        errorMessage = "Name cannot be empty"
                        return@Button
                    }
                    if (dryWeight == null || dryWeight <= 0.0) {
                        errorMessage = "Please enter a valid dry weight (kg)"
                        return@Button
                    }
                    if (height == null || height <= 0.0) {
                        errorMessage = "Please enter a valid height (cm)"
                        return@Button
                    }
                    errorMessage = null
                    onSave(profile.copy(name = name.trim(), dryWeight = dryWeight, height = height))
                },
                colors = ButtonDefaults.buttonColors(containerColor = UiPrimary, contentColor = UiOnPrimary)
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = UiTextDim)
            }
        }
    )
}
