package com.example.expense.core.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "kaasu_prefs")

class PreferencesManager(private val context: Context) {

    companion object {
        private val CURRENCY_KEY = stringPreferencesKey("currency_code")
        private val THEME_KEY = stringPreferencesKey("theme_mode")
        const val DEFAULT_CURRENCY = "NZD"
        const val THEME_SYSTEM = "SYSTEM"
        const val THEME_LIGHT = "LIGHT"
        const val THEME_DARK = "DARK"
    }

    val currencyCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CURRENCY_KEY] ?: DEFAULT_CURRENCY
    }

    val themeMode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[THEME_KEY] ?: THEME_SYSTEM
    }

    suspend fun setCurrencyCode(code: String) {
        context.dataStore.edit { prefs ->
            prefs[CURRENCY_KEY] = code
        }
    }

    suspend fun setThemeMode(mode: String) {
        context.dataStore.edit { prefs ->
            prefs[THEME_KEY] = mode
        }
    }
}
