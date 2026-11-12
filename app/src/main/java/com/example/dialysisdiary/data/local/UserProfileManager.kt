package com.example.dialysisdiary.data.local

import android.content.Context
import java.util.Locale

data class UserProfile(
    val name: String = "Vaibhav",
    val dryWeight: Double = 65.5,
    val height: Double = 170.0
) {
    val bmi: Double
        get() = if (height > 0) dryWeight / ((height / 100.0) * (height / 100.0)) else 0.0

    val bmiCategory: String
        get() = when {
            bmi < 18.5 -> "Underweight"
            bmi < 25.0 -> "Healthy Weight"
            bmi < 30.0 -> "Overweight"
            else -> "Obese"
        }
}

object UserProfileManager {
    private const val PREFS_NAME = "user_profile_prefs"
    private const val KEY_NAME = "key_name"
    private const val KEY_DRY_WEIGHT = "key_dry_weight"
    private const val KEY_HEIGHT = "key_height"

    fun getProfile(context: Context): UserProfile {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val name = prefs.getString(KEY_NAME, "Vaibhav") ?: "Vaibhav"
        val dryWeight = prefs.getFloat(KEY_DRY_WEIGHT, 65.5f).toDouble()
        val height = prefs.getFloat(KEY_HEIGHT, 170.0f).toDouble()
        return UserProfile(name = name, dryWeight = dryWeight, height = height)
    }

    fun saveProfile(context: Context, profile: UserProfile) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_NAME, profile.name)
            .putFloat(KEY_DRY_WEIGHT, profile.dryWeight.toFloat())
            .putFloat(KEY_HEIGHT, profile.height.toFloat())
            .apply()
    }
}
