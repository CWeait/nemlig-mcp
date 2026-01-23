# Authentication Implementation Example

## Before (Current Placeholder)

```kotlin
// In NemligClient.kt, line ~47
suspend fun authenticate(): Result<String> {
    logger.info { "Authenticating with Nemlig API..." }

    if (config.username.isNullOrBlank() || config.password.isNullOrBlank()) {
        return Result.failure(IllegalStateException("Username and password must be configured"))
    }

    // TODO: Implement actual authentication flow
    return try {
        val requestBody = """
            {
                "username": "${config.username}",
                "password": "${config.password}"
            }
        """.trimIndent()

        val request = Request.Builder()
            .url("${config.apiUrl}/auth/login")  // ← WRONG, just a guess
            .post(requestBody.toRequestBody("application/json".toMediaType()))
            .build()

        val response = client.newCall(request).execute()
        // ... rest of placeholder code
    }
}
```

## After (Example with Real Endpoint)

Let's say you discover:
- Endpoint: `https://webapi.prod.knl.nemlig.it/api/v2/customers/login`
- Request needs: `{"email": "user@example.com", "password": "pass", "remember": false}`
- Response contains: `{"accessToken": "...", "customerId": "...", "expiresAt": "2024-01-24T10:00:00Z"}`
- Future requests use: `Authorization: Bearer {accessToken}`

Then update to:

```kotlin
@Serializable
private data class LoginRequest(
    val email: String,
    val password: String,
    val remember: Boolean = false
)

@Serializable
private data class LoginResponse(
    val accessToken: String,
    val customerId: String,
    val expiresAt: String
)

suspend fun authenticate(): Result<String> {
    logger.info { "Authenticating with Nemlig API..." }

    if (config.username.isNullOrBlank() || config.password.isNullOrBlank()) {
        return Result.failure(IllegalStateException("Username and password must be configured"))
    }

    return try {
        // Create request body with discovered format
        val loginRequest = LoginRequest(
            email = config.username!!,
            password = config.password!!,
            remember = false
        )

        val requestBody = json.encodeToString(loginRequest)
            .toRequestBody("application/json".toMediaType())

        // Use the actual discovered endpoint
        val request = Request.Builder()
            .url("${config.apiUrl}/api/v2/customers/login")  // ← ACTUAL endpoint
            .post(requestBody)
            .addHeader("Accept", "application/json")
            // Add any other headers you discovered (User-Agent, etc.)
            .build()

        logger.debug { "Sending login request to ${request.url}" }
        val response = client.newCall(request).execute()

        if (response.isSuccessful) {
            val responseBody = response.body?.string()
                ?: return Result.failure(IOException("Empty response body"))

            logger.debug { "Login response: $responseBody" }

            // Parse the actual response format
            val loginResponse = json.decodeFromString<LoginResponse>(responseBody)

            // Store the token for future requests
            authToken = loginResponse.accessToken

            logger.info { "Authentication successful for customer ${loginResponse.customerId}" }
            logger.debug { "Token expires at: ${loginResponse.expiresAt}" }

            Result.success(loginResponse.accessToken)
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

// Update buildAuthenticatedRequest to use the token format you discovered
private fun buildAuthenticatedRequest(url: HttpUrl): Request {
    val builder = Request.Builder().url(url)

    authToken?.let {
        // Use the format you discovered (e.g., "Bearer {token}")
        builder.addHeader("Authorization", "Bearer $it")
    }

    return builder.build()
}
```

## Testing Your Implementation

After updating the code:

1. **Add your credentials to .env:**
```bash
NEMLIG_USERNAME=your.real.email@example.com
NEMLIG_PASSWORD=your_actual_password
```

2. **Build and run:**
```bash
./gradlew build
./gradlew run
```

3. **Check logs:**
Look for log output like:
```
INFO  - Authenticating with Nemlig API...
DEBUG - Sending login request to https://webapi.prod.knl.nemlig.it/api/v2/customers/login
DEBUG - Login response: {"accessToken":"...","customerId":"12345",...}
INFO  - Authentication successful for customer 12345
```

4. **If it fails:**
- Check the error in logs
- Verify endpoint URL matches exactly
- Check request body format matches what browser sends
- Verify headers match (especially Content-Type, Accept)
- Ensure credentials are correct

## Common Authentication Patterns

### Pattern 1: Bearer Token in Header
```kotlin
builder.addHeader("Authorization", "Bearer $it")
```

### Pattern 2: Custom Header
```kotlin
builder.addHeader("X-Auth-Token", it)
```

### Pattern 3: Cookie-based
```kotlin
// Token comes in Set-Cookie response header
// OkHttp CookieJar handles this automatically if configured
```

### Pattern 4: Token in Query Parameter
```kotlin
val url = url.newBuilder()
    .addQueryParameter("token", authToken)
    .build()
```

## What to Look For During Discovery

1. **Request URL** - The exact endpoint
2. **Request Method** - Usually POST for login
3. **Request Headers** - Content-Type, Accept, User-Agent, etc.
4. **Request Body** - Exact JSON structure
5. **Response Status** - Should be 200 or 201
6. **Response Body** - Where's the token? What else is returned?
7. **Token Format** - How is it used in subsequent requests?
8. **Token Lifetime** - How long is it valid?

## Debugging Tips

```kotlin
// Add more detailed logging
logger.debug { "Request URL: ${request.url}" }
logger.debug { "Request headers: ${request.headers}" }
logger.debug { "Request body: $requestBody" }
logger.debug { "Response code: ${response.code}" }
logger.debug { "Response headers: ${response.headers}" }
logger.debug { "Response body: $responseBody" }
```

Set LOG_LEVEL=DEBUG in .env to see all debug logs.
