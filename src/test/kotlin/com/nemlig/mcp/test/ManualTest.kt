package com.nemlig.mcp.test

import com.nemlig.mcp.client.NemligClient
import com.nemlig.mcp.config.ConfigLoader
import com.nemlig.mcp.tools.Result
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
    when (authResult) {
        is Result.Success -> println("✅ Authentication successful\n")
        is Result.Failure -> {
            println("❌ Authentication failed: ${authResult.exception.message}\n")
            println("Cannot continue without authentication")
            return@runBlocking
        }
    }

    // Test 2: Search Products
    println("Test 2: Searching for 'mælk' (milk)")
    when (val searchResult = client.searchProducts("mælk", limit = 5)) {
        is Result.Success -> {
            println("✅ Search successful")
            println("   Query: ${searchResult.value.query}")
            println("   Total results: ${searchResult.value.totalResults}")
            println("   Products returned: ${searchResult.value.products.size}\n")
        }
        is Result.Failure -> println("❌ Search failed: ${searchResult.exception.message}\n")
    }

    // Test 3: Get Orders
    println("Test 3: Getting order history")
    when (val ordersResult = client.getOrders(limit = 5)) {
        is Result.Success -> {
            println("✅ Orders retrieved")
            println("   Orders count: ${ordersResult.value.size}\n")
        }
        is Result.Failure -> println("❌ Get orders failed: ${ordersResult.exception.message}\n")
    }

    println("=== Tests Complete ===")
}
