package dev.p4oc.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import dev.p4oc.domain.model.ServerConfig
import dev.p4oc.domain.repository.SettingsRepository
import dev.p4oc.domain.repository.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : SettingsRepository {

    private object Keys {
        val SERVER_HOST = stringPreferencesKey("server_host")
        val SERVER_PORT = intPreferencesKey("server_port")
        val SERVER_USERNAME = stringPreferencesKey("server_username")
        val SERVER_PASSWORD = stringPreferencesKey("server_password")
        val SERVER_FULL_URL = stringPreferencesKey("server_full_url")
        val SERVER_USE_URL = booleanPreferencesKey("server_use_url")
        val THEME = stringPreferencesKey("theme")
        val CONNECTION_TIMEOUT = intPreferencesKey("connection_timeout")
    }

    override val serverConfig: Flow<ServerConfig?> = context.dataStore.data.map { prefs ->
        val host = prefs[Keys.SERVER_HOST]
        val useUrl = prefs[Keys.SERVER_USE_URL] ?: false
        val fullUrl = prefs[Keys.SERVER_FULL_URL] ?: ""
        
        if (host.isNullOrBlank() && fullUrl.isBlank()) null
        else ServerConfig(
            host = host ?: "",
            port = prefs[Keys.SERVER_PORT] ?: 4096,
            username = prefs[Keys.SERVER_USERNAME] ?: "",
            password = prefs[Keys.SERVER_PASSWORD] ?: "",
            fullUrl = fullUrl,
            useUrl = useUrl
        )
    }

    override val theme: Flow<ThemeMode> = context.dataStore.data.map { prefs ->
        val themeName = prefs[Keys.THEME] ?: ThemeMode.SYSTEM.name
        try {
            ThemeMode.valueOf(themeName)
        } catch (e: Exception) {
            ThemeMode.SYSTEM
        }
    }

    override val connectionTimeout: Flow<Int> = context.dataStore.data.map { prefs ->
        prefs[Keys.CONNECTION_TIMEOUT] ?: 30
    }

    override suspend fun saveServerConfig(config: ServerConfig) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SERVER_HOST] = config.host
            prefs[Keys.SERVER_PORT] = config.port
            prefs[Keys.SERVER_USERNAME] = config.username
            prefs[Keys.SERVER_PASSWORD] = config.password
            prefs[Keys.SERVER_FULL_URL] = config.fullUrl
            prefs[Keys.SERVER_USE_URL] = config.useUrl
        }
    }

    override suspend fun saveTheme(theme: ThemeMode) {
        context.dataStore.edit { prefs ->
            prefs[Keys.THEME] = theme.name
        }
    }

    override suspend fun saveConnectionTimeout(timeout: Int) {
        context.dataStore.edit { prefs ->
            prefs[Keys.CONNECTION_TIMEOUT] = timeout
        }
    }

    override suspend fun clearServerConfig() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.SERVER_HOST)
            prefs.remove(Keys.SERVER_PORT)
            prefs.remove(Keys.SERVER_USERNAME)
            prefs.remove(Keys.SERVER_PASSWORD)
            prefs.remove(Keys.SERVER_FULL_URL)
            prefs.remove(Keys.SERVER_USE_URL)
        }
    }

    override suspend fun saveUrlOnly(url: String) {
        context.dataStore.edit { prefs ->
            prefs[Keys.SERVER_FULL_URL] = url
            prefs[Keys.SERVER_USE_URL] = true
        }
    }
}
