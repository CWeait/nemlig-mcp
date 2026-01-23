package com.nemlig.mcp.config

/**
 * Application configuration data class
 */
data class Config(
    val nemlig: NemligConfig,
    val server: ServerConfig
)

/**
 * Nemlig API configuration
 */
data class NemligConfig(
    val apiUrl: String = "https://www.nemlig.com/webapi",
    val username: String? = null,
    val password: String? = null,
    val timeout: Long = 30000L,
    val rateLimit: RateLimitConfig = RateLimitConfig()
)

/**
 * Rate limiting configuration
 * Default: 1 request per second (matching Python implementation's rate limiting)
 */
data class RateLimitConfig(
    val requestsPerSecond: Int = 1,
    val burstSize: Int = 2
)

/**
 * MCP Server configuration
 */
data class ServerConfig(
    val name: String = "nemlig-mcp",
    val version: String = "1.0.0",
    val logLevel: String = "INFO"
)

/**
 * Configuration loader utility
 */
object ConfigLoader {
    fun load(): Config {
        // Load from environment variables with defaults
        return Config(
            nemlig = NemligConfig(
                apiUrl = System.getenv("NEMLIG_API_URL")
                    ?: "https://www.nemlig.com/webapi",
                username = System.getenv("NEMLIG_USERNAME"),
                password = System.getenv("NEMLIG_PASSWORD"),
                timeout = System.getenv("NEMLIG_TIMEOUT")?.toLongOrNull() ?: 30000L
            ),
            server = ServerConfig(
                name = System.getenv("SERVER_NAME") ?: "nemlig-mcp",
                version = System.getenv("SERVER_VERSION") ?: "1.0.0",
                logLevel = System.getenv("LOG_LEVEL") ?: "INFO"
            )
        )
    }
}
