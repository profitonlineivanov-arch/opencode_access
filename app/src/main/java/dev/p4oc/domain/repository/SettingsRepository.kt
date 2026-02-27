package dev.p4oc.domain.repository

import dev.p4oc.domain.model.ServerConfig
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val serverConfig: Flow<ServerConfig?>
    val theme: Flow<ThemeMode>
    val connectionTimeout: Flow<Int>

    suspend fun saveServerConfig(config: ServerConfig)
    suspend fun saveTheme(theme: ThemeMode)
    suspend fun saveConnectionTimeout(timeout: Int)
    suspend fun clearServerConfig()
    suspend fun saveUrlOnly(url: String)
}

enum class ThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}
