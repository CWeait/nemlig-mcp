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
        val env = loadEnvFile() + System.getenv()

        return Config(
            nemlig = NemligConfig(
                apiUrl = env["NEMLIG_API_URL"] ?: "https://www.nemlig.com/webapi",
                username = env["NEMLIG_USERNAME"],
                password = env["NEMLIG_PASSWORD"],
                timeout = env["NEMLIG_TIMEOUT"]?.toLongOrNull() ?: 30000L
            ),
            server = ServerConfig(
                name = env["SERVER_NAME"] ?: "nemlig-mcp",
                version = env["SERVER_VERSION"] ?: "1.0.0",
                logLevel = env["LOG_LEVEL"] ?: "INFO"
            )
        )
    }

    private fun loadEnvFile(): Map<String, String> {
        // Check current working directory first, then next to the JAR
        val candidates = listOf(
            java.io.File(".env"),
            java.io.File(
                ConfigLoader::class.java.protectionDomain.codeSource?.location?.toURI()?.let {
                    java.io.File(it).parentFile
                } ?: return emptyMap(),
                ".env"
            )
        )
        val envFile = candidates.firstOrNull { it.exists() } ?: return emptyMap()

        return envFile.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && it.contains('=') }
            .associate { line ->
                val key = line.substringBefore('=').trim()
                val value = line.substringAfter('=').trim().removeSurrounding("\"")
                key to value
            }
    }
}
