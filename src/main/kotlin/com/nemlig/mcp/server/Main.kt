package com.nemlig.mcp.server

import com.nemlig.mcp.client.NemligClient
import com.nemlig.mcp.config.ConfigLoader
import com.nemlig.mcp.tools.NemligTools
import io.modelcontextprotocol.kotlin.sdk.server.Server
import io.modelcontextprotocol.kotlin.sdk.server.ServerOptions
import io.modelcontextprotocol.kotlin.sdk.server.StdioServerTransport
import io.modelcontextprotocol.kotlin.sdk.types.CallToolResult
import io.modelcontextprotocol.kotlin.sdk.types.Implementation
import io.modelcontextprotocol.kotlin.sdk.types.ServerCapabilities
import io.modelcontextprotocol.kotlin.sdk.types.TextContent
import io.modelcontextprotocol.kotlin.sdk.types.ToolSchema
import kotlinx.coroutines.Job
import kotlinx.coroutines.runBlocking
import kotlinx.io.asSink
import kotlinx.io.asSource
import kotlinx.io.buffered
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import kotlinx.serialization.json.putJsonObject
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

fun main() = runBlocking {
    val config = ConfigLoader.load()
    logger.info { "Starting Nemlig MCP Server v${config.server.version}..." }

    // Initialize API client and authenticate
    val client = NemligClient(config.nemlig)
    if (!config.nemlig.username.isNullOrBlank() && !config.nemlig.password.isNullOrBlank()) {
        client.authenticate().fold(
            onSuccess = { logger.info { "Authentication successful" } },
            onFailure = { logger.warn { "Authentication failed: ${it.message}" } }
        )
    } else {
        logger.warn { "No credentials provided - some features may not work" }
    }

    val tools = NemligTools(client)

    // Create MCP server
    val server = Server(
        Implementation(
            name = config.server.name,
            version = config.server.version,
        ),
        ServerOptions(
            capabilities = ServerCapabilities(
                tools = ServerCapabilities.Tools(listChanged = true),
            ),
        ),
    )

    // Register tools
    server.addTool(
        name = "search_products",
        description = "Search for products in the Nemlig catalog. Returns a list of products matching the search query with prices, availability, and basic information.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("query") {
                    put("type", "string")
                    put("description", "Search query for products (e.g., 'milk', 'organic vegetables', 'gluten free bread')")
                }
                putJsonObject("limit") {
                    put("type", "number")
                    put("description", "Maximum number of results to return (default: 20)")
                    put("default", 20)
                }
                putJsonObject("page") {
                    put("type", "number")
                    put("description", "Page number for pagination (default: 1)")
                    put("default", 1)
                }
            },
            required = listOf("query"),
        ),
    ) { request ->
        val result = tools.searchProducts(request.arguments ?: JsonObject(emptyMap()))
        CallToolResult(content = listOf(TextContent(result.toString())))
    }

    server.addTool(
        name = "get_product_details",
        description = "Get detailed information about a specific product including full description, nutritional information, pricing, availability, and images.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("productId") {
                    put("type", "string")
                    put("description", "The unique identifier of the product")
                }
            },
            required = listOf("productId"),
        ),
    ) { request ->
        val result = tools.getProductDetails(request.arguments ?: JsonObject(emptyMap()))
        CallToolResult(content = listOf(TextContent(result.toString())))
    }

    server.addTool(
        name = "view_cart",
        description = "View the current shopping cart contents including all items, quantities, individual prices, and total price.",
    ) { request ->
        val result = tools.viewCart(request.arguments ?: JsonObject(emptyMap()))
        CallToolResult(content = listOf(TextContent(result.toString())))
    }

    server.addTool(
        name = "add_to_cart",
        description = "Add a product to the shopping cart with a specified quantity. Use this after finding products via search or getting product details.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("productId") {
                    put("type", "string")
                    put("description", "The unique identifier of the product to add")
                }
                putJsonObject("quantity") {
                    put("type", "number")
                    put("description", "Number of units to add (default: 1)")
                    put("default", 1)
                    put("minimum", 1)
                }
            },
            required = listOf("productId"),
        ),
    ) { request ->
        val result = tools.addToCart(request.arguments ?: JsonObject(emptyMap()))
        CallToolResult(content = listOf(TextContent(result.toString())))
    }

    server.addTool(
        name = "remove_from_cart",
        description = "Remove a product from the shopping cart completely.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("productId") {
                    put("type", "string")
                    put("description", "The unique identifier of the product to remove")
                }
            },
            required = listOf("productId"),
        ),
    ) { request ->
        val result = tools.removeFromCart(request.arguments ?: JsonObject(emptyMap()))
        CallToolResult(content = listOf(TextContent(result.toString())))
    }

    server.addTool(
        name = "get_order_history",
        description = "Retrieve past orders including order dates, items purchased, quantities, prices, and delivery information. Useful for reordering or analyzing shopping patterns.",
        inputSchema = ToolSchema(
            properties = buildJsonObject {
                putJsonObject("limit") {
                    put("type", "number")
                    put("description", "Maximum number of orders to return (default: 10)")
                    put("default", 10)
                }
            },
        ),
    ) { request ->
        val result = tools.getOrderHistory(request.arguments ?: JsonObject(emptyMap()))
        CallToolResult(content = listOf(TextContent(result.toString())))
    }

    logger.info { "Registered ${6} tools, starting stdio transport..." }

    // Start stdio transport
    val transport = StdioServerTransport(
        inputStream = System.`in`.asSource().buffered(),
        outputStream = System.out.asSink().buffered(),
    )

    server.createSession(transport)
    val done = Job()
    server.onClose {
        done.complete()
    }
    done.join()
}
