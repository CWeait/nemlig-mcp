package com.nemlig.mcp.client

import com.nemlig.mcp.config.NemligConfig
import com.nemlig.mcp.models.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

/**
 * Client for interacting with the Nemlig API
 *
 * This client handles:
 * - Authentication
 * - Product search and retrieval
 * - Cart management
 * - Order operations
 * - Rate limiting
 */
class NemligClient(private val config: NemligConfig) {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .readTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .writeTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .addInterceptor(LoggingInterceptor())
        .addInterceptor(RateLimitInterceptor(config.rateLimit))
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    private var authToken: String? = null

    /**
     * Authenticate with Nemlig API
     */
    suspend fun authenticate(): Result<String> {
        logger.info { "Authenticating with Nemlig API..." }

        if (config.username.isNullOrBlank() || config.password.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Username and password must be configured"))
        }

        // TODO: Implement actual authentication flow
        // This is a placeholder - you'll need to reverse-engineer the actual auth endpoint
        return try {
            val requestBody = """
                {
                    "username": "${config.username}",
                    "password": "${config.password}"
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("${config.apiUrl}/auth/login")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .build()

            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val token = response.body?.string() ?: ""
                authToken = token
                logger.info { "Authentication successful" }
                Result.success(token)
            } else {
                logger.error { "Authentication failed: ${response.code} ${response.message}" }
                Result.failure(IOException("Authentication failed: ${response.message}"))
            }
        } catch (e: Exception) {
            logger.error(e) { "Authentication error" }
            Result.failure(e)
        }
    }

    /**
     * Search for products
     */
    suspend fun searchProducts(
        query: String,
        limit: Int = 20,
        page: Int = 1
    ): Result<SearchResult> {
        logger.info { "Searching products: query='$query', limit=$limit, page=$page" }

        // TODO: Implement actual product search endpoint
        // This is a placeholder - you'll need to reverse-engineer the actual endpoint
        return try {
            val url = HttpUrl.Builder()
                .scheme("https")
                .host(config.apiUrl.removePrefix("https://"))
                .addPathSegment("search")
                .addQueryParameter("q", query)
                .addQueryParameter("limit", limit.toString())
                .addQueryParameter("page", page.toString())
                .build()

            val request = buildAuthenticatedRequest(url)
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                // For now, return mock data
                val mockResult = SearchResult(
                    query = query,
                    products = emptyList(),
                    totalResults = 0,
                    page = page,
                    pageSize = limit
                )
                logger.info { "Search completed: ${mockResult.totalResults} results" }
                Result.success(mockResult)
            } else {
                Result.failure(IOException("Search failed: ${response.message}"))
            }
        } catch (e: Exception) {
            logger.error(e) { "Search error" }
            Result.failure(e)
        }
    }

    /**
     * Get product details by ID
     */
    suspend fun getProduct(productId: String): Result<Product> {
        logger.info { "Getting product details: $productId" }

        // TODO: Implement actual product details endpoint
        return try {
            val url = "${config.apiUrl}/products/$productId"
            val request = buildAuthenticatedRequest(HttpUrl.parse(url)!!)
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                // For now, return mock data
                val mockProduct = Product(
                    id = productId,
                    name = "Mock Product",
                    price = 0.0,
                    inStock = true
                )
                Result.success(mockProduct)
            } else {
                Result.failure(IOException("Failed to get product: ${response.message}"))
            }
        } catch (e: Exception) {
            logger.error(e) { "Get product error" }
            Result.failure(e)
        }
    }

    /**
     * Get current shopping cart
     */
    suspend fun getCart(): Result<Cart> {
        logger.info { "Getting cart" }

        // TODO: Implement actual cart endpoint
        return try {
            val mockCart = Cart(
                items = emptyList(),
                totalPrice = 0.0,
                itemCount = 0
            )
            Result.success(mockCart)
        } catch (e: Exception) {
            logger.error(e) { "Get cart error" }
            Result.failure(e)
        }
    }

    /**
     * Add item to cart
     */
    suspend fun addToCart(productId: String, quantity: Int): Result<Cart> {
        logger.info { "Adding to cart: productId=$productId, quantity=$quantity" }

        // TODO: Implement actual add to cart endpoint
        return try {
            Result.success(Cart(emptyList(), 0.0, 0))
        } catch (e: Exception) {
            logger.error(e) { "Add to cart error" }
            Result.failure(e)
        }
    }

    /**
     * Remove item from cart
     */
    suspend fun removeFromCart(productId: String): Result<Cart> {
        logger.info { "Removing from cart: $productId" }

        // TODO: Implement actual remove from cart endpoint
        return try {
            Result.success(Cart(emptyList(), 0.0, 0))
        } catch (e: Exception) {
            logger.error(e) { "Remove from cart error" }
            Result.failure(e)
        }
    }

    /**
     * Get order history
     */
    suspend fun getOrders(limit: Int = 10): Result<List<Order>> {
        logger.info { "Getting order history: limit=$limit" }

        // TODO: Implement actual orders endpoint
        return try {
            Result.success(emptyList())
        } catch (e: Exception) {
            logger.error(e) { "Get orders error" }
            Result.failure(e)
        }
    }

    /**
     * Get available delivery slots
     */
    suspend fun getDeliverySlots(): Result<List<DeliverySlot>> {
        logger.info { "Getting delivery slots" }

        // TODO: Implement actual delivery slots endpoint
        return try {
            Result.success(emptyList())
        } catch (e: Exception) {
            logger.error(e) { "Get delivery slots error" }
            Result.failure(e)
        }
    }

    /**
     * Build an authenticated request
     */
    private fun buildAuthenticatedRequest(url: HttpUrl): Request {
        val builder = Request.Builder().url(url)

        authToken?.let {
            builder.addHeader("Authorization", "Bearer $it")
        }

        return builder.build()
    }

    /**
     * Logging interceptor for debugging
     */
    private class LoggingInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val request = chain.request()
            logger.debug { "Request: ${request.method} ${request.url}" }

            val response = chain.proceed(request)
            logger.debug { "Response: ${response.code} ${request.url}" }

            return response
        }
    }

    /**
     * Rate limiting interceptor
     */
    private class RateLimitInterceptor(private val config: com.nemlig.mcp.config.RateLimitConfig) : Interceptor {
        private var lastRequestTime = 0L
        private val minTimeBetweenRequests = 1000L / config.requestsPerSecond

        override fun intercept(chain: Interceptor.Chain): Response {
            synchronized(this) {
                val now = System.currentTimeMillis()
                val timeSinceLastRequest = now - lastRequestTime

                if (timeSinceLastRequest < minTimeBetweenRequests) {
                    val sleepTime = minTimeBetweenRequests - timeSinceLastRequest
                    Thread.sleep(sleepTime)
                }

                lastRequestTime = System.currentTimeMillis()
            }

            return chain.proceed(chain.request())
        }
    }
}
