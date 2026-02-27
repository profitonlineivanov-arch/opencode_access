package dev.p4oc.domain.model

data class ServerConfig(
    val host: String,
    val port: Int,
    val username: String = "",
    val password: String = "",
    val useUrl: Boolean = false,
    val fullUrl: String = ""
) {
    val baseUrl: String
        get() = if (useUrl && fullUrl.isNotBlank()) {
            fullUrl.removeSuffix("/")
        } else {
            "http://$host:$port"
        }
    
    val isConfigured: Boolean
        get() = if (useUrl) {
            fullUrl.isNotBlank() && (fullUrl.startsWith("http://") || fullUrl.startsWith("https://"))
        } else {
            host.isNotBlank() && port > 0
        }
}
