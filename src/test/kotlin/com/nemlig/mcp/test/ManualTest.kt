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
    val searchResult = client.searchProducts("mælk", limit = 5)
    searchResult.fold(
        onSuccess = {
            println("✅ Search successful")
            println("   Query: ${it.query}")
            println("   Total results: ${it.totalResults}")
            println("   Products returned: ${it.products.size}\n")
        },
        onFailure = { println("❌ Search failed: ${it.message}\n") }
    )

    // Test 3: Get Orders
    println("Test 3: Getting order history")
    val ordersResult = client.getOrders(limit = 5)
    ordersResult.fold(
        onSuccess = {
            println("✅ Orders retrieved")
            println("   Orders count: ${it.size}\n")
        },
        onFailure = { println("❌ Get orders failed: ${it.message}\n") }
    )

    println("=== Tests Complete ===")
}
