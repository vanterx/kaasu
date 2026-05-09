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
        const val DEFAULT_CURRENCY = "NZD"
    }

    val currencyCode: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[CURRENCY_KEY] ?: DEFAULT_CURRENCY
    }

    suspend fun setCurrencyCode(code: String) {
        context.dataStore.edit { prefs ->
            prefs[CURRENCY_KEY] = code
        }
    }
}
