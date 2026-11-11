package com.example.data.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "user_settings")

class SettingsDataStore(private val context: Context) {
    companion object {
        private val KEY_HIDE_AMOUNTS = booleanPreferencesKey("hide_amounts")
        private val KEY_LAST_EXPORT_DATE = longPreferencesKey("last_export_date")
        private val KEY_USER_NAME = stringPreferencesKey("user_name")
        private val KEY_THEME_MODE = stringPreferencesKey("theme_mode") // "SYSTEM", "LIGHT", "DARK"
        private val KEY_TOTAL_XP = longPreferencesKey("total_xp")
        private val KEY_RECORD_STREAK = intPreferencesKey("record_streak")
        private val KEY_USER_HEIGHT_CM = floatPreferencesKey("user_height_cm")
        private val KEY_WEIGHT_TARGET_KG = floatPreferencesKey("weight_target_kg")
    }

    val hideAmountsFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[KEY_HIDE_AMOUNTS] ?: false
    }

    val lastExportDateFlow: Flow<Long?> = context.dataStore.data.map { preferences ->
        preferences[KEY_LAST_EXPORT_DATE]
    }

    val userNameFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_NAME] ?: "Princy"
    }

    val themeModeFlow: Flow<String> = context.dataStore.data.map { preferences ->
        preferences[KEY_THEME_MODE] ?: "SYSTEM"
    }

    val totalXpFlow: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[KEY_TOTAL_XP] ?: 0L
    }

    val recordStreakFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[KEY_RECORD_STREAK] ?: 0
    }

    val userHeightCmFlow: Flow<Float?> = context.dataStore.data.map { preferences ->
        preferences[KEY_USER_HEIGHT_CM] ?: 175f
    }

    val weightTargetKgFlow: Flow<Double> = context.dataStore.data.map { preferences ->
        (preferences[KEY_WEIGHT_TARGET_KG] ?: 66.0f).toDouble()
    }

    suspend fun setHideAmounts(hide: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[KEY_HIDE_AMOUNTS] = hide
        }
    }

    suspend fun setLastExportDate(timestamp: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_LAST_EXPORT_DATE] = timestamp
        }
    }

    suspend fun setUserName(name: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_NAME] = name
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { preferences ->
            preferences[KEY_THEME_MODE] = mode
        }
    }

    suspend fun addXp(delta: Long) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_TOTAL_XP] ?: 0L
            val updated = (current + delta).coerceAtLeast(0L)
            preferences[KEY_TOTAL_XP] = updated
        }
    }

    suspend fun setTotalXp(xp: Long) {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOTAL_XP] = xp.coerceAtLeast(0L)
        }
    }

    suspend fun updateRecordStreak(streak: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[KEY_RECORD_STREAK] ?: 0
            if (streak > current) {
                preferences[KEY_RECORD_STREAK] = streak
            }
        }
    }

    suspend fun setUserHeightCm(heightCm: Float) {
        context.dataStore.edit { preferences ->
            preferences[KEY_USER_HEIGHT_CM] = heightCm
        }
    }

    suspend fun setWeightTargetKg(target: Double) {
        context.dataStore.edit { preferences ->
            preferences[KEY_WEIGHT_TARGET_KG] = target.toFloat()
        }
    }

    suspend fun resetAllSettings() {
        context.dataStore.edit { preferences ->
            preferences[KEY_TOTAL_XP] = 0L
            preferences[KEY_RECORD_STREAK] = 0
            preferences[KEY_HIDE_AMOUNTS] = false
            preferences.remove(KEY_LAST_EXPORT_DATE)
            preferences.remove(KEY_WEIGHT_TARGET_KG)
        }
    }
}
