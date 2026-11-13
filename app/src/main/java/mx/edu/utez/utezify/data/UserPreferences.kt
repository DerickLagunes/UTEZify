package mx.edu.utez.utezify.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import mx.edu.utez.utezify.data.models.User

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_session")

class UserPreferences(private val context: Context) {

    companion object {
        private val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        private val USER_MATRICULA = stringPreferencesKey("user_matricula")
        private val USER_FULL_NAME = stringPreferencesKey("user_full_name")
        private val USER_ID = intPreferencesKey("user_id")
        private val LAST_OPEN_TIMESTAMP = longPreferencesKey("last_open_timestamp")
    }

    val isLoggedIn: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[IS_LOGGED_IN] ?: false
    }

    val userSession: Flow<User?> = context.dataStore.data.map { preferences ->
        val m = preferences[USER_MATRICULA]
        if (m.isNullOrBlank()) {
            null
        } else {
            User(
                id = preferences[USER_ID],
                fullName = preferences[USER_FULL_NAME] ?: "",
                matricula = m
            )
        }
    }

    val lastOpenTimestamp: Flow<Long> = context.dataStore.data.map { preferences ->
        preferences[LAST_OPEN_TIMESTAMP] ?: 0L
    }

    suspend fun getLastOpenTimestampSync(): Long {
        return context.dataStore.data.first()[LAST_OPEN_TIMESTAMP] ?: 0L
    }

    suspend fun updateLastOpenTimestamp(timestamp: Long = System.currentTimeMillis()) {
        context.dataStore.edit { preferences ->
            preferences[LAST_OPEN_TIMESTAMP] = timestamp
        }
    }

    suspend fun saveSession(user: User) {
        context.dataStore.edit { preferences ->
            preferences[IS_LOGGED_IN] = true
            preferences[USER_MATRICULA] = user.matricula
            preferences[USER_FULL_NAME] = user.fullName
            user.id?.let { preferences[USER_ID] = it }
        }
    }

    suspend fun clearSession() {
        context.dataStore.edit { preferences ->
            preferences.clear()
        }
    }
}
