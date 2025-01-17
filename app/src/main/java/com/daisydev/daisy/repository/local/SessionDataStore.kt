package com.daisydev.daisy.repository.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.daisydev.daisy.models.Session
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Class responsible for saving and retrieving the user's session data
 * in a DataStore.
 * @param context Application context.
 */
class SessionDataStore @Inject constructor(context: Context) {
    private var dataStore: DataStore<Preferences> = context.sessionDataStore

    // We create a companion object to define the preference keys
    companion object {
        private val Context.sessionDataStore: DataStore<Preferences>
                by preferencesDataStore("session_data_store")

        // keys
        val ID = stringPreferencesKey("id")
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
    }

    // Function to get the user's session data
    suspend fun getSession(): Session {
        return try {
            val preferences = dataStore.data.first()
            val id = preferences[ID] ?: ""
            val name = preferences[NAME] ?: ""
            val email = preferences[EMAIL] ?: ""
            Session(id, name, email)
        } catch (e: Exception) {
            Session("", "", "")
        }
    }

    // Function to save the user's session data
    suspend fun saveSession(session: Session) {
        dataStore.edit { preferences ->
            preferences[ID] = session.id
            preferences[NAME] = session.name
            preferences[EMAIL] = session.email
        }
    }

    // Function to clear the user's session data
    suspend fun clearSession() {
        dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}