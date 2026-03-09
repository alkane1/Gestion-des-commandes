package com.example.gestiondescommandes.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.userPrefsDataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

data class UserPreferences(
    val darkMode: Boolean,
    val config: ContainerConfig
)

class UserPreferencesStore(private val context: Context) {

    private object Keys {
        val darkMode = booleanPreferencesKey("dark_mode")
        val maxWeightKg = doublePreferencesKey("config_max_weight_kg")
        val maxVolumeM3 = doublePreferencesKey("config_max_volume_m3")
        val minFillThreshold = doublePreferencesKey("config_min_fill_threshold")
        val containerCount = intPreferencesKey("config_container_count")
    }

    val preferencesFlow: Flow<UserPreferences> = context.userPrefsDataStore.data
        .catch { exception ->
            if (exception is IOException) emit(emptyPreferences()) else throw exception
        }
        .map { prefs ->
            val defaultConfig = ContainerConfig()
            UserPreferences(
                darkMode = prefs[Keys.darkMode] ?: false,
                config = ContainerConfig(
                    maxWeightKg = prefs[Keys.maxWeightKg] ?: defaultConfig.maxWeightKg,
                    maxVolumeM3 = prefs[Keys.maxVolumeM3] ?: defaultConfig.maxVolumeM3,
                    minFillThreshold = prefs[Keys.minFillThreshold] ?: defaultConfig.minFillThreshold,
                    containerCount = prefs[Keys.containerCount] ?: defaultConfig.containerCount
                )
            )
        }

    suspend fun setDarkMode(enabled: Boolean) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.darkMode] = enabled
        }
    }

    suspend fun setConfig(config: ContainerConfig) {
        context.userPrefsDataStore.edit { prefs ->
            prefs[Keys.maxWeightKg] = config.maxWeightKg
            prefs[Keys.maxVolumeM3] = config.maxVolumeM3
            prefs[Keys.minFillThreshold] = config.minFillThreshold
            prefs[Keys.containerCount] = config.containerCount
        }
    }
}
