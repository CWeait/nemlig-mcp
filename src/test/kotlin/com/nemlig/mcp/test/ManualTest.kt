package com.nemlig.mcp.test

import com.nemlig.mcp.client.NemligClient
import com.nemlig.mcp.config.ConfigLoader
import kotlinx.coroutines.runBlocking

/**
 * Simple test script to verify API endpoints work
 */
fun main() = runBlocking {
    println("=== Nemlig MCP Test ===\n")

    // Load configuration
    val config = ConfigLoader.load()
    val client = NemligClient(config.nemlig)

    // Test 1: Authentication
    println("Test 1: Authentication")
    val authResult = client.authenticate()
    authResult.fold(
        onSuccess = { println("✅ Authentication successful\n") },
        onFailure = { println("❌ Authentication failed: ${it.message}\n") }
    )

    if (authResult.isFailure) {
        println("Cannot continue without authentication")
        return@runBlocking
    }

    // Test 2: Search Products
    println("Test 2: Searching for 'mælk' (milk)")
    var firstProductId: String? = null
    client.searchProducts("mælk", limit = 5).fold(
        onSuccess = {
            println("✅ Search successful")
            println("   Query: ${it.query}")
            println("   Total results: ${it.totalResults}")
            println("   Products returned: ${it.products.size}")
            if (it.products.isNotEmpty()) {
                firstProductId = it.products.first().id
                it.products.forEach { p ->
                    println("   - [${p.id}] ${p.name} (${p.price} kr) - ${p.brand}")
                }
            }
            println()
        },
        onFailure = { println("❌ Search failed: ${it.message}\n") }
    )

    // Test 3: Get Orders
    println("Test 3: Getting order history")
    var firstOrderId: String? = null
    var firstOrderNumber: String? = null
    client.getOrders(limit = 3).fold(
        onSuccess = {
            println("✅ Orders retrieved: ${it.size}")
            it.forEach { o ->
                println("   - #${o.orderNumber} (id=${o.id}) ${o.date} - ${o.totalPrice} kr (${o.status})")
            }
            if (it.isNotEmpty()) {
                firstOrderId = it.first().id
                firstOrderNumber = it.first().orderNumber
            }
            println()
        },
        onFailure = { println("❌ Get orders failed: ${it.message}\n") }
    )

    // Test 3b: Get order details with line items
    if (firstOrderId != null) {
        println("Test 3b: Get order details (id=$firstOrderId)")
        client.getOrderDetails(firstOrderId!!).fold(
            onSuccess = { detail ->
                println("✅ Order detail retrieved")
                println("   Order #${detail.orderNumber} - ${detail.orderDate}")
                println("   Total: ${detail.total} kr (sub: ${detail.subTotal} kr)")
                println("   Shipping: ${detail.shippingPrice} kr, Packaging: ${detail.packagingPrice} kr")
                println("   Products: ${detail.numberOfProducts}, Lines: ${detail.lines.size}")
                if (detail.totalProductDiscount > 0) println("   Discount: ${detail.totalProductDiscount} kr")
                if (detail.couponDiscount != 0.0) println("   Coupon discount: ${detail.couponDiscount} kr")
                detail.lines.take(5).forEach { line ->
                    val discount = if (line.discountAmount > 0) " (discount: ${line.discountAmount} kr)" else ""
                    println("   - ${line.productName} x${line.quantity} = ${line.amount} kr$discount")
                }
                if (detail.lines.size > 5) println("   ... and ${detail.lines.size - 5} more lines")
                if (detail.couponLines.isNotEmpty()) {
                    println("   Coupons: ${detail.couponLines.joinToString { "${it.type}: ${it.name}" }}")
                }
                require(detail.lines.isNotEmpty()) { "Expected at least one order line!" }
                println("   ✅ Verified: ${detail.lines.size} line items present")
                println()
            },
            onFailure = { println("❌ Get order details failed: ${it.message}\n") }
        )
    }

    // Test 4: Get Product Details
    val productId = firstProductId ?: "103368"
    println("Test 4: Get product details (id=$productId)")
    client.getProduct(productId).fold(
        onSuccess = {
            println("✅ Product retrieved")
            println("   Name: ${it.name}")
            println("   Price: ${it.price}")
            println("   Brand: ${it.brand}")
            println("   InStock: ${it.inStock}")
            println()
        },
        onFailure = { println("❌ Get product failed: ${it.message}\n") }
    )

    // Test 5: Get Cart
    println("Test 5: Get cart")
    client.getCart().fold(
        onSuccess = {
            println("✅ Cart retrieved")
            println("   Items: ${it.itemCount}")
            println("   Total: ${it.totalPrice} kr")
            it.items.forEach { item ->
                println("   - ${item.productName} x${item.quantity} (${item.totalPrice} kr)")
            }
            println()
        },
        onFailure = { println("❌ Get cart failed: ${it.message}\n") }
    )

    // Test 6: Add to Cart
    println("Test 6: Add to cart (id=$productId, qty=1)")
    client.addToCart(productId, 1).fold(
        onSuccess = {
            println("✅ Added to cart")
            println("   Items: ${it.itemCount}")
            println("   Total: ${it.totalPrice} kr")
            println()
        },
        onFailure = { println("❌ Add to cart failed: ${it.message}\n") }
    )

    // Test 7: Remove from Cart
    println("Test 7: Remove from cart (id=$productId)")
    client.removeFromCart(productId).fold(
        onSuccess = {
            println("✅ Removed from cart")
            println("   Items: ${it.itemCount}")
            println("   Total: ${it.totalPrice} kr")
            println()
        },
        onFailure = { println("❌ Remove from cart failed: ${it.message}\n") }
    )

    println("=== Tests Complete ===")
}
