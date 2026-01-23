package com.nemlig.mcp.tools

import com.nemlig.mcp.client.NemligClient
import kotlinx.serialization.json.*
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

/**
 * MCP Tools for Nemlig operations
 */
class NemligTools(private val client: NemligClient) {

    /**
     * Search for products in the Nemlig catalog
     */
    suspend fun searchProducts(args: JsonObject): JsonElement {
        logger.info { "Tool called: search_products with args: $args" }

        val query = args["query"]?.jsonPrimitive?.content
            ?: return buildError("Missing required parameter: query")

        val limit = args["limit"]?.jsonPrimitive?.intOrNull ?: 20
        val page = args["page"]?.jsonPrimitive?.intOrNull ?: 1

        return when (val result = client.searchProducts(query, limit, page)) {
            is Result.Success -> {
                buildJsonObject {
                    put("success", true)
                    put("query", query)
                    putJsonArray("products") {
                        result.value.products.forEach { product ->
                            addJsonObject {
                                put("id", product.id)
                                put("name", product.name)
                                put("price", product.price)
                                product.unit?.let { put("unit", it) }
                                product.brand?.let { put("brand", it) }
                                product.category?.let { put("category", it) }
                                put("inStock", product.inStock)
                            }
                        }
                    }
                    put("totalResults", result.value.totalResults)
                    put("page", page)
                }
            }
            is Result.Failure -> buildError(result.exception.message ?: "Search failed")
        }
    }

    /**
     * Get detailed information about a specific product
     */
    suspend fun getProductDetails(args: JsonObject): JsonElement {
        logger.info { "Tool called: get_product_details with args: $args" }

        val productId = args["productId"]?.jsonPrimitive?.content
            ?: return buildError("Missing required parameter: productId")

        return when (val result = client.getProduct(productId)) {
            is Result.Success -> {
                val product = result.value
                buildJsonObject {
                    put("success", true)
                    putJsonObject("product") {
                        put("id", product.id)
                        put("name", product.name)
                        put("price", product.price)
                        product.unit?.let { put("unit", it) }
                        product.brand?.let { put("brand", it) }
                        product.category?.let { put("category", it) }
                        product.imageUrl?.let { put("imageUrl", it) }
                        product.description?.let { put("description", it) }
                        put("inStock", product.inStock)

                        product.nutritionalInfo?.let { nutrition ->
                            putJsonObject("nutrition") {
                                nutrition.energyKcal?.let { put("energyKcal", it) }
                                nutrition.fat?.let { put("fat", it) }
                                nutrition.carbohydrates?.let { put("carbohydrates", it) }
                                nutrition.protein?.let { put("protein", it) }
                                nutrition.sugar?.let { put("sugar", it) }
                            }
                        }
                    }
                }
            }
            is Result.Failure -> buildError(result.exception.message ?: "Failed to get product")
        }
    }

    /**
     * View the current shopping cart
     */
    suspend fun viewCart(args: JsonObject): JsonElement {
        logger.info { "Tool called: view_cart" }

        return when (val result = client.getCart()) {
            is Result.Success -> {
                val cart = result.value
                buildJsonObject {
                    put("success", true)
                    putJsonObject("cart") {
                        put("itemCount", cart.itemCount)
                        put("totalPrice", cart.totalPrice)
                        putJsonArray("items") {
                            cart.items.forEach { item ->
                                addJsonObject {
                                    put("productId", item.productId)
                                    put("productName", item.productName)
                                    put("quantity", item.quantity)
                                    put("pricePerUnit", item.pricePerUnit)
                                    put("totalPrice", item.totalPrice)
                                }
                            }
                        }
                    }
                }
            }
            is Result.Failure -> buildError(result.exception.message ?: "Failed to get cart")
        }
    }

    /**
     * Add a product to the shopping cart
     */
    suspend fun addToCart(args: JsonObject): JsonElement {
        logger.info { "Tool called: add_to_cart with args: $args" }

        val productId = args["productId"]?.jsonPrimitive?.content
            ?: return buildError("Missing required parameter: productId")

        val quantity = args["quantity"]?.jsonPrimitive?.intOrNull ?: 1

        if (quantity < 1) {
            return buildError("Quantity must be at least 1")
        }

        return when (val result = client.addToCart(productId, quantity)) {
            is Result.Success -> {
                buildJsonObject {
                    put("success", true)
                    put("message", "Added $quantity item(s) to cart")
                    put("productId", productId)
                    put("quantity", quantity)
                }
            }
            is Result.Failure -> buildError(result.exception.message ?: "Failed to add to cart")
        }
    }

    /**
     * Remove a product from the shopping cart
     */
    suspend fun removeFromCart(args: JsonObject): JsonElement {
        logger.info { "Tool called: remove_from_cart with args: $args" }

        val productId = args["productId"]?.jsonPrimitive?.content
            ?: return buildError("Missing required parameter: productId")

        return when (val result = client.removeFromCart(productId)) {
            is Result.Success -> {
                buildJsonObject {
                    put("success", true)
                    put("message", "Removed item from cart")
                    put("productId", productId)
                }
            }
            is Result.Failure -> buildError(result.exception.message ?: "Failed to remove from cart")
        }
    }

    /**
     * View order history
     */
    suspend fun getOrderHistory(args: JsonObject): JsonElement {
        logger.info { "Tool called: get_order_history with args: $args" }

        val limit = args["limit"]?.jsonPrimitive?.intOrNull ?: 10

        return when (val result = client.getOrders(limit)) {
            is Result.Success -> {
                buildJsonObject {
                    put("success", true)
                    putJsonArray("orders") {
                        result.value.forEach { order ->
                            addJsonObject {
                                put("id", order.id)
                                put("date", order.date)
                                put("status", order.status.name)
                                put("totalPrice", order.totalPrice)
                                putJsonArray("items") {
                                    order.items.forEach { item ->
                                        addJsonObject {
                                            put("productName", item.productName)
                                            put("quantity", item.quantity)
                                            put("price", item.price)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
            is Result.Failure -> buildError(result.exception.message ?: "Failed to get orders")
        }
    }

    /**
     * Get available delivery time slots
     */
    suspend fun getDeliverySlots(args: JsonObject): JsonElement {
        logger.info { "Tool called: get_delivery_slots" }

        return when (val result = client.getDeliverySlots()) {
            is Result.Success -> {
                buildJsonObject {
                    put("success", true)
                    putJsonArray("slots") {
                        result.value.forEach { slot ->
                            addJsonObject {
                                put("id", slot.id)
                                put("date", slot.date)
                                put("timeFrom", slot.timeFrom)
                                put("timeTo", slot.timeTo)
                                put("available", slot.available)
                                put("price", slot.price)
                            }
                        }
                    }
                }
            }
            is Result.Failure -> buildError(result.exception.message ?: "Failed to get delivery slots")
        }
    }

    /**
     * Build an error response
     */
    private fun buildError(message: String): JsonObject {
        return buildJsonObject {
            put("success", false)
            put("error", message)
        }
    }
}

/**
 * Result wrapper for API responses
 */
sealed class Result<out T> {
    data class Success<T>(val value: T) : Result<T>()
    data class Failure(val exception: Exception) : Result<Nothing>()
}

/**
 * Extension to convert Kotlin Result to custom Result
 */
fun <T> kotlin.Result<T>.toResult(): Result<T> {
    return fold(
        onSuccess = { Result.Success(it) },
        onFailure = { Result.Failure(it as? Exception ?: Exception(it.message)) }
    )
}
