package com.example.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "endotrack_session")

class SessionPreferences(private val context: Context) {

    private object Keys {
        val LOGGED_IN_USER_ID = longPreferencesKey("logged_in_user_id")
        val HAS_SEEN_ONBOARDING = booleanPreferencesKey("has_seen_onboarding")
        val DEMO_SEEDED = booleanPreferencesKey("demo_seeded")
    }

    val loggedInUserIdFlow: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[Keys.LOGGED_IN_USER_ID]?.takeIf { it > 0L }
    }

    val hasSeenOnboardingFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.HAS_SEEN_ONBOARDING] ?: false
    }

    val isDemoSeededFlow: Flow<Boolean> = context.dataStore.data.map { prefs ->
        prefs[Keys.DEMO_SEEDED] ?: false
    }

    suspend fun setLoggedInUserId(userId: Long?) {
        context.dataStore.edit { prefs ->
            if (userId != null && userId > 0L) {
                prefs[Keys.LOGGED_IN_USER_ID] = userId
            } else {
                prefs.remove(Keys.LOGGED_IN_USER_ID)
            }
        }
    }

    suspend fun setHasSeenOnboarding(seen: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.HAS_SEEN_ONBOARDING] = seen
        }
    }

    suspend fun setDemoSeeded(seeded: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[Keys.DEMO_SEEDED] = seeded
        }
    }
}
