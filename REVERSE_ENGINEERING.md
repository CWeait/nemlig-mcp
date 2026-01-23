# Nemlig API Reverse Engineering Notes

This document tracks the discoveries from reverse-engineering Nemlig.com's internal API.

**Source:** Endpoints discovered from https://github.com/schourode/nemlig (Python implementation)

**Base URL:** `https://www.nemlig.com/webapi`

---

## Authentication

### Endpoint Discovery

**✅ DISCOVERED** from existing Python implementation

**Details:**
```
Endpoint URL: https://www.nemlig.com/webapi/login/login
Method: POST
Content-Type: application/json

Request Body:
{
  "Username": "your.email@example.com",
  "Password": "your_password",
  "AppInstalled": false,
  "AutoLogin": false,
  "CheckForExistingProducts": true,
  "DoMerge": true
}

Authentication Method: Cookie-based (session cookies)
Response: Sets session cookies via Set-Cookie header
```

**How subsequent requests authenticate:**
```
Authentication: HTTP Cookies (automatic via CookieJar)
No explicit Authorization header needed
Session cookies are automatically sent with each request
```

### Implementation Status
- [x] Endpoint discovered
- [x] Request format documented
- [x] Response format documented (cookie-based)
- [x] Token storage method identified (CookieJar)
- [x] Code updated in NemligClient.kt (line ~56)
- [ ] Tested successfully with real credentials

---

## Product Search

### Endpoint Discovery

**✅ DISCOVERED** from existing Python implementation

**Details:**
```
Endpoint URL: https://www.nemlig.com/webapi/s/0/1/0/Search/Search
Method: GET
Query Parameters:
  - query: search term (string)
  - take: number of results (int, default 10)

Example:
GET /webapi/s/0/1/0/Search/Search?query=mælk&take=20

Response Body:
[Response format needs to be documented from actual API call]
```

### Implementation Status
- [x] Endpoint discovered
- [x] Parameters documented
- [ ] Response format documented (needs actual API response)
- [x] Code updated in NemligClient.kt (line ~103)
- [ ] Response parsing implemented
- [ ] Tested successfully

**TODO:**
- Test with real credentials to capture actual response format
- Update Product model to match response structure
- Implement JSON parsing of search results

---

## Add to Cart

### Endpoint Discovery

**✅ DISCOVERED** from existing Python implementation

**Details:**
```
Endpoint URL: https://www.nemlig.com/webapi/basket/AddToBasket
Method: POST
Content-Type: application/json

Request Body:
{
  "productId": "12345",
  "quantity": 2
}

Response Body:
[Response format needs to be documented from actual API call]
```

### Implementation Status
- [x] Endpoint discovered
- [x] Request format documented
- [ ] Response format documented (needs actual API response)
- [x] Code updated in NemligClient.kt (line ~198)
- [ ] Response parsing implemented
- [ ] Tested successfully

**TODO:**
- Test with real credentials and valid product ID
- Capture actual response format
- Update Cart model if needed

---

## View Cart

### Endpoint Discovery

**❓ NOT YET DISCOVERED**

**Steps to discover:**
1. After logging in and adding items, view your cart
2. Look for API call that loads cart data
3. Check Network tab for GET or POST request

**Likely patterns:**
```
GET /webapi/basket/GetBasket
GET /webapi/cart/view
GET /webapi/order/basket
```

### Implementation Status
- [ ] Endpoint discovered
- [ ] Response format documented
- [ ] Code updated
- [ ] Tested successfully

---

## Order History

### Endpoint Discovery

**✅ DISCOVERED** from existing Python implementation

**Details:**
```
Endpoint URL: https://www.nemlig.com/webapi/order/GetBasicOrderHistory
Method: GET
Query Parameters:
  - skip: number to skip (int, default 0)
  - take: number to retrieve (int, default 10)

Example:
GET /webapi/order/GetBasicOrderHistory?skip=0&take=10

Additional endpoint for order details:
GET /webapi/order/GetOrderHistory?orderNumber=ORDER_NUMBER

Response Body:
[Response format needs to be documented from actual API call]
```

### Implementation Status
- [x] Endpoint discovered
- [x] Parameters documented
- [ ] Response format documented (needs actual API response)
- [x] Code updated in NemligClient.kt (line ~229)
- [ ] Response parsing implemented
- [ ] Tested successfully

**TODO:**
- Test with real credentials to capture actual response format
- Update Order model to match response structure
- Implement JSON parsing of order history

---

## Delivery Slots

### Endpoint Discovery

**❓ NOT YET DISCOVERED**

**Steps to discover:**
1. During checkout, view available delivery times
2. Look for API call that loads time slots
3. Check Network tab

**Likely patterns:**
```
GET /webapi/delivery/slots
GET /webapi/delivery/GetAvailableSlots
GET /webapi/checkout/timeslots
```

### Implementation Status
- [ ] Endpoint discovered
- [ ] Response format documented
- [ ] Code updated
- [ ] Tested successfully

---

## Summary of Implementation

### ✅ Endpoints Implemented (with actual URLs)
1. **Authentication** - `/login/login` - Cookie-based auth
2. **Product Search** - `/s/0/1/0/Search/Search` - Needs response parsing
3. **Add to Cart** - `/basket/AddToBasket` - Needs response parsing
4. **Order History** - `/order/GetBasicOrderHistory` - Needs response parsing

### ❓ Endpoints Still Needed
5. **View Cart** - Unknown endpoint
6. **Remove from Cart** - Unknown endpoint
7. **Delivery Slots** - Unknown endpoint
8. **Get Product Details** - Unknown endpoint

---

## Next Steps

### 1. Test Authentication (Priority 1)
```bash
# Create .env file with your credentials
cp .env.example .env
nano .env  # Add your Nemlig credentials

# Build and run to test authentication
./gradlew build
./gradlew run
```

Check logs for:
```
INFO  - Authenticating with Nemlig API...
INFO  - Authentication successful - session cookies stored
```

### 2. Capture Response Formats (Priority 2)

Once authentication works, test each endpoint:

```bash
# Set LOG_LEVEL=DEBUG in .env to see full responses
echo "LOG_LEVEL=DEBUG" >> .env

# Run and trigger tools through MCP to see responses
./gradlew run
```

For each endpoint, document the actual JSON response structure in this file.

### 3. Update Data Models (Priority 3)

Based on actual responses, update:
- `src/main/kotlin/com/nemlig/mcp/models/Models.kt`
- Add @Serializable annotations
- Match field names to API response

### 4. Implement JSON Parsing (Priority 4)

Update each method in `NemligClient.kt` to parse the actual JSON response:
```kotlin
val searchResponse = json.decodeFromString<SearchResponse>(responseBody)
// Transform to SearchResult model
```

---

## API Rate Limiting

**Discovered:** Python implementation uses 1 second delay between requests

**Our Implementation:**
- RateLimitInterceptor in NemligClient.kt
- Default: 1 request per second
- Configurable via RateLimitConfig

---

## Tips for Further Discovery

1. **Use Browser DevTools** - Essential for finding new endpoints
2. **Check Python code** - https://github.com/schourode/nemlig may have more endpoints
3. **Look for patterns** - Nemlig seems to use descriptive endpoint names
4. **Test incrementally** - Get one endpoint working before moving to next
5. **Log everything** - Use DEBUG log level to see full requests/responses

---

**Last Updated:** 2026-01-23
**Status:** 4 of 8 endpoints discovered and implemented, testing pending
