package com.nemlig.mcp.server

import com.nemlig.mcp.client.NemligClient
import com.nemlig.mcp.config.Config
import com.nemlig.mcp.tools.NemligTools
import kotlinx.serialization.json.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * Nemlig MCP Server implementation
 *
 * This server exposes Nemlig.com functionality as MCP tools that can be used by AI assistants.
 */
class NemligMcpServer(private val config: Config) {

    private val client = NemligClient(config.nemlig)
    private val tools = NemligTools(client)

    /**
     * List of available MCP tools
     */
    fun listTools(): List<Tool> = listOf(
        Tool(
            name = "search_products",
            description = "Search for products in the Nemlig catalog. Returns a list of products matching the search query with prices, availability, and basic information.",
            inputSchema = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
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
                }
                putJsonArray("required") {
                    add("query")
                }
            }
        ),

        Tool(
            name = "get_product_details",
            description = "Get detailed information about a specific product including full description, nutritional information, pricing, availability, and images.",
            inputSchema = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("productId") {
                        put("type", "string")
                        put("description", "The unique identifier of the product")
                    }
                }
                putJsonArray("required") {
                    add("productId")
                }
            }
        ),

        Tool(
            name = "view_cart",
            description = "View the current shopping cart contents including all items, quantities, individual prices, and total price.",
            inputSchema = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") { }
            }
        ),

        Tool(
            name = "add_to_cart",
            description = "Add a product to the shopping cart with a specified quantity. Use this after finding products via search or getting product details.",
            inputSchema = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
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
                }
                putJsonArray("required") {
                    add("productId")
                }
            }
        ),

        Tool(
            name = "remove_from_cart",
            description = "Remove a product from the shopping cart completely.",
            inputSchema = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("productId") {
                        put("type", "string")
                        put("description", "The unique identifier of the product to remove")
                    }
                }
                putJsonArray("required") {
                    add("productId")
                }
            }
        ),

        Tool(
            name = "get_order_history",
            description = "Retrieve past orders including order dates, items purchased, quantities, prices, and delivery information. Useful for reordering or analyzing shopping patterns.",
            inputSchema = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") {
                    putJsonObject("limit") {
                        put("type", "number")
                        put("description", "Maximum number of orders to return (default: 10)")
                        put("default", 10)
                    }
                }
            }
        ),

        Tool(
            name = "get_delivery_slots",
            description = "Get available delivery time slots for scheduling grocery deliveries. Shows dates, time ranges, availability, and delivery costs.",
            inputSchema = buildJsonObject {
                put("type", "object")
                putJsonObject("properties") { }
            }
        )
    )

    /**
     * Execute a tool call
     */
    suspend fun callTool(name: String, arguments: JsonObject): JsonElement {
        logger.info { "Executing tool: $name" }

        return try {
            when (name) {
                "search_products" -> tools.searchProducts(arguments)
                "get_product_details" -> tools.getProductDetails(arguments)
                "view_cart" -> tools.viewCart(arguments)
                "add_to_cart" -> tools.addToCart(arguments)
                "remove_from_cart" -> tools.removeFromCart(arguments)
                "get_order_history" -> tools.getOrderHistory(arguments)
                "get_delivery_slots" -> tools.getDeliverySlots(arguments)
                else -> buildJsonObject {
                    put("success", false)
                    put("error", "Unknown tool: $name")
                }
            }
        } catch (e: Exception) {
            logger.error(e) { "Error executing tool: $name" }
            buildJsonObject {
                put("success", false)
                put("error", e.message ?: "Unknown error")
            }
        }
    }

    /**
     * Get server information
     */
    fun getServerInfo(): ServerInfo {
        return ServerInfo(
            name = config.server.name,
            version = config.server.version
        )
    }

    /**
     * Initialize the server (authenticate, etc.)
     */
    suspend fun initialize() {
        logger.info { "Initializing Nemlig MCP Server..." }

        // Authenticate if credentials are provided
        if (!config.nemlig.username.isNullOrBlank() && !config.nemlig.password.isNullOrBlank()) {
            logger.info { "Authenticating with Nemlig API..." }
            client.authenticate().fold(
                onSuccess = { logger.info { "Authentication successful" } },
                onFailure = { logger.warn { "Authentication failed: ${it.message}" } }
            )
        } else {
            logger.warn { "No credentials provided - some features may not work" }
        }

        logger.info { "Nemlig MCP Server initialized successfully" }
    }
}

/**
 * MCP Tool definition
 */
data class Tool(
    val name: String,
    val description: String,
    val inputSchema: JsonObject
)

/**
 * Server information
 */
data class ServerInfo(
    val name: String,
    val version: String
)
