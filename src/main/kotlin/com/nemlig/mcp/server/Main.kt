package com.nemlig.mcp.server

import com.nemlig.mcp.config.ConfigLoader
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import kotlin.system.exitProcess

private val logger = KotlinLogging.logger {}

/**
 * Main entry point for the Nemlig MCP Server
 *
 * This server runs as a stdio-based MCP server that can be configured in Claude Desktop
 * or other MCP-compatible clients.
 *
 * Usage:
 *   ./gradlew run
 *
 * Environment Variables:
 *   NEMLIG_USERNAME - Your Nemlig.com username/email
 *   NEMLIG_PASSWORD - Your Nemlig.com password
 *   NEMLIG_API_URL - API base URL (default: https://webapi.prod.knl.nemlig.it)
 *   LOG_LEVEL - Logging level (default: INFO)
 */
fun main() = runBlocking {
    try {
        logger.info { "Starting Nemlig MCP Server..." }

        // Load configuration
        val config = ConfigLoader.load()
        logger.info { "Configuration loaded: ${config.server.name} v${config.server.version}" }

        // Create and initialize server
        val server = NemligMcpServer(config)
        server.initialize()

        // TODO: Set up stdio transport with MCP SDK
        // This is a placeholder until we integrate with the actual Kotlin MCP SDK
        // The SDK will handle the stdio communication protocol

        logger.info { "Server ready - waiting for MCP requests via stdio..." }
        logger.info { "Available tools: ${server.listTools().joinToString(", ") { it.name }}" }

        // For now, print server info and exit
        val info = server.getServerInfo()
        println("Nemlig MCP Server")
        println("Name: ${info.name}")
        println("Version: ${info.version}")
        println("\nAvailable Tools:")
        server.listTools().forEach { tool ->
            println("  - ${tool.name}: ${tool.description}")
        }

        println("\n⚠️  Note: Full MCP stdio transport integration pending.")
        println("Once integrated, this server will communicate via stdio with MCP clients.")

        // Keep the process running
        println("\nServer is running. Press Ctrl+C to exit.")
        Thread.currentThread().join()

    } catch (e: Exception) {
        logger.error(e) { "Fatal error starting server" }
        exitProcess(1)
    }
}
