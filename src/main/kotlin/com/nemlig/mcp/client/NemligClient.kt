package com.nemlig.mcp.client

import com.nemlig.mcp.config.NemligConfig
import com.nemlig.mcp.models.*
import kotlinx.serialization.json.*
import mu.KotlinLogging
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrl
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

    // Cookie jar for session management (like Python's requests.Session())
    private val cookieJar = object : CookieJar {
        private val cookieStore = mutableMapOf<String, List<Cookie>>()

        override fun loadForRequest(url: HttpUrl): List<Cookie> {
            return cookieStore[url.host] ?: emptyList()
        }

        override fun saveFromResponse(url: HttpUrl, cookies: List<Cookie>) {
            val existing = cookieStore[url.host].orEmpty().associateBy { it.name }.toMutableMap()
            for (cookie in cookies) {
                existing[cookie.name] = cookie
            }
            cookieStore[url.host] = existing.values.toList()
        }
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .readTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .writeTimeout(config.timeout, TimeUnit.MILLISECONDS)
        .cookieJar(cookieJar)  // Enable cookie-based session management
        .addInterceptor(LoggingInterceptor())
        .addInterceptor(RateLimitInterceptor(config.rateLimit))
        .build()

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        isLenient = true
    }

    /**
     * Authenticate with Nemlig API
     * Endpoint discovered from: https://github.com/schourode/nemlig
     * Uses cookie-based authentication (session cookies stored in CookieJar)
     */
    suspend fun authenticate(): Result<String> {
        logger.info { "Authenticating with Nemlig API..." }

        if (config.username.isNullOrBlank() || config.password.isNullOrBlank()) {
            return Result.failure(IllegalStateException("Username and password must be configured"))
        }

        return try {
            // Actual endpoint: POST /login/login
            // Request format from reverse-engineered Python implementation
            val requestBody = """
                {
                    "Username": "${config.username}",
                    "Password": "${config.password}",
                    "AppInstalled": false,
                    "AutoLogin": false,
                    "CheckForExistingProducts": true,
                    "DoMerge": true
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("${config.apiUrl}/login/login")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .addHeader("Accept", "application/json")
                .build()

            logger.debug { "Sending login request to ${request.url}" }
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                logger.debug { "Login response: $responseBody" }
                logger.info { "Authentication successful - session cookies stored" }
                // Authentication is cookie-based, cookies are automatically stored in CookieJar
                Result.success("authenticated")
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                logger.error { "Authentication failed: ${response.code} - $errorBody" }
                Result.failure(IOException("Authentication failed: ${response.code} - ${response.message}"))
            }
        } catch (e: Exception) {
            logger.error(e) { "Authentication error" }
            Result.failure(e)
        }
    }

    /**
     * Search for products
     * Endpoint discovered from: https://github.com/schourode/nemlig
     * GET /s/0/1/0/Search/Search?query={query}&take={limit}
     */
    suspend fun searchProducts(
        query: String,
        limit: Int = 20,
        page: Int = 1
    ): Result<SearchResult> {
        logger.info { "Searching products: query='$query', limit=$limit, page=$page" }

        return try {
            // Actual endpoint: GET /s/0/1/0/Search/Search
            val url = HttpUrl.Builder()
                .scheme("https")
                .host("www.nemlig.com")
                .addPathSegment("webapi")
                .addPathSegment("s")
                .addPathSegment("0")
                .addPathSegment("1")
                .addPathSegment("0")
                .addPathSegment("Search")
                .addPathSegment("Search")
                .addQueryParameter("query", query)
                .addQueryParameter("take", limit.toString())
                .build()

            val request = buildAuthenticatedRequest(url)
            logger.debug { "Search request: ${request.url}" }
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val jsonRoot = json.parseToJsonElement(responseBody).jsonObject
                val productsObj = jsonRoot["Products"]?.jsonObject
                val productsArray = productsObj?.get("Products")?.jsonArray ?: JsonArray(emptyList())
                val numFound = productsObj?.get("NumFound")?.jsonPrimitive?.intOrNull ?: 0

                val products = productsArray.map { element ->
                    val obj = element.jsonObject
                    val availability = obj["Availability"]?.jsonObject
                    Product(
                        id = obj["Id"]?.jsonPrimitive?.content ?: "",
                        name = obj["Name"]?.jsonPrimitive?.content ?: "",
                        price = obj["Price"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                        unit = obj["UnitPriceLabel"]?.jsonPrimitive?.contentOrNull,
                        brand = obj["Brand"]?.jsonPrimitive?.contentOrNull,
                        category = obj["Category"]?.jsonPrimitive?.contentOrNull,
                        imageUrl = obj["PrimaryImage"]?.jsonPrimitive?.contentOrNull,
                        description = obj["Description"]?.jsonPrimitive?.contentOrNull,
                        inStock = availability?.get("IsAvailableInStock")?.jsonPrimitive?.booleanOrNull ?: true
                    )
                }

                val result = SearchResult(
                    query = query,
                    products = products,
                    totalResults = numFound,
                    page = page,
                    pageSize = limit
                )
                logger.info { "Search completed: ${result.totalResults} results" }
                Result.success(result)
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                logger.error { "Search failed: ${response.code} - $errorBody" }
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
            val request = buildAuthenticatedRequest(url.toHttpUrl())
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
     * Endpoint discovered from: https://github.com/schourode/nemlig
     * POST /basket/AddToBasket
     */
    suspend fun addToCart(productId: String, quantity: Int): Result<Cart> {
        logger.info { "Adding to cart: productId=$productId, quantity=$quantity" }

        return try {
            // Actual endpoint: POST /basket/AddToBasket
            val requestBody = """
                {
                    "productId": "$productId",
                    "quantity": $quantity
                }
            """.trimIndent()

            val request = Request.Builder()
                .url("${config.apiUrl}/basket/AddToBasket")
                .post(requestBody.toRequestBody("application/json".toMediaType()))
                .addHeader("Accept", "application/json")
                .build()

            logger.debug { "Add to cart request: ${request.url}" }
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                logger.debug { "Add to cart response: $responseBody" }
                logger.info { "Successfully added $quantity item(s) to cart" }

                // TODO: Parse actual cart response and return updated cart
                Result.success(Cart(emptyList(), 0.0, 0))
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                logger.error { "Add to cart failed: ${response.code} - $errorBody" }
                Result.failure(IOException("Add to cart failed: ${response.message}"))
            }
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
     * Endpoint discovered from: https://github.com/schourode/nemlig
     * GET /order/GetBasicOrderHistory?skip={skip}&take={limit}
     */
    suspend fun getOrders(limit: Int = 10): Result<List<Order>> {
        logger.info { "Getting order history: limit=$limit" }

        return try {
            // Actual endpoint: GET /order/GetBasicOrderHistory
            val url = HttpUrl.Builder()
                .scheme("https")
                .host("www.nemlig.com")
                .addPathSegment("webapi")
                .addPathSegment("order")
                .addPathSegment("GetBasicOrderHistory")
                .addQueryParameter("skip", "0")
                .addQueryParameter("take", limit.toString())
                .build()

            val request = buildAuthenticatedRequest(url)
            logger.debug { "Get orders request: ${request.url}" }
            val response = client.newCall(request).execute()

            if (response.isSuccessful) {
                val responseBody = response.body?.string() ?: ""
                val jsonRoot = json.parseToJsonElement(responseBody).jsonObject
                val ordersArray = jsonRoot["Orders"]?.jsonArray ?: JsonArray(emptyList())

                val orders = ordersArray.map { element ->
                    val obj = element.jsonObject
                    val deliveryTime = obj["DeliveryTime"]?.jsonObject
                    val timeStr = if (deliveryTime != null) {
                        val start = deliveryTime["Start"]?.jsonPrimitive?.contentOrNull ?: ""
                        val end = deliveryTime["End"]?.jsonPrimitive?.contentOrNull ?: ""
                        "$start - $end"
                    } else null

                    Order(
                        id = obj["Id"]?.jsonPrimitive?.content ?: "",
                        orderNumber = obj["OrderNumber"]?.jsonPrimitive?.content ?: "",
                        date = obj["OrderDate"]?.jsonPrimitive?.content ?: "",
                        status = OrderStatus.fromCode(obj["Status"]?.jsonPrimitive?.intOrNull ?: 0),
                        totalPrice = obj["Total"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                        subTotal = obj["SubTotal"]?.jsonPrimitive?.doubleOrNull ?: 0.0,
                        deliveryAddress = obj["DeliveryAddress"]?.jsonPrimitive?.contentOrNull,
                        deliveryTime = timeStr,
                        isEditable = obj["IsEditable"]?.jsonPrimitive?.booleanOrNull ?: false,
                        isCancellable = obj["IsCancellable"]?.jsonPrimitive?.booleanOrNull ?: false
                    )
                }

                logger.info { "Retrieved ${orders.size} orders" }
                Result.success(orders)
            } else {
                val errorBody = response.body?.string() ?: "Unknown error"
                logger.error { "Get orders failed: ${response.code} - $errorBody" }
                Result.failure(IOException("Get orders failed: ${response.message}"))
            }
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
     * Authentication is handled via cookies stored in CookieJar after login
     */
    private fun buildAuthenticatedRequest(url: HttpUrl): Request {
        return Request.Builder()
            .url(url)
            .addHeader("Accept", "application/json")
            .build()
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
