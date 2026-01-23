# Nemlig API Reverse Engineering Notes

This document tracks the discoveries from reverse-engineering Nemlig.com's internal API.

## Authentication

### Endpoint Discovery

**Steps to discover:**
1. Open https://www.nemlig.com in incognito browser
2. Open DevTools (F12) → Network tab → Filter by XHR/Fetch
3. Click login and enter credentials
4. Look for POST request during login

**What to document:**
```
Endpoint URL: [TO BE FILLED]
Method: POST
Content-Type: [TO BE FILLED]

Request Body:
[PASTE JSON HERE]

Response Body:
[PASTE JSON HERE]

Response Headers (especially Set-Cookie or Authorization):
[PASTE HERE]
```

**How subsequent requests authenticate:**
```
Header name: [e.g., Authorization, X-Auth-Token, Cookie]
Header value format: [e.g., "Bearer {token}", "Token {token}"]
```

### Implementation Status
- [ ] Endpoint discovered
- [ ] Request format documented
- [ ] Response format documented
- [ ] Token storage method identified
- [ ] Code updated in NemligClient.kt
- [ ] Tested successfully

---

## Product Search

### Endpoint Discovery

**Steps to discover:**
1. After logging in, use the search bar on Nemlig.com
2. Search for "mælk" (milk) or any product
3. Look for API call in Network tab

**What to document:**
```
Endpoint URL: [TO BE FILLED]
Method: [GET/POST]
Query Parameters: [e.g., ?q=mælk&limit=20]

Request Headers:
[PASTE HERE]

Response Body:
[PASTE JSON SAMPLE HERE]
```

### Implementation Status
- [ ] Endpoint discovered
- [ ] Parameters documented
- [ ] Response format documented
- [ ] Code updated
- [ ] Tested successfully

---

## Add to Cart

### Endpoint Discovery

**Steps to discover:**
1. After logging in, find a product
2. Click "Add to cart"
3. Look for API call in Network tab

**What to document:**
```
Endpoint URL: [TO BE FILLED]
Method: [POST/PUT]

Request Body:
[PASTE JSON HERE]

Response Body:
[PASTE JSON HERE]
```

### Implementation Status
- [ ] Endpoint discovered
- [ ] Request format documented
- [ ] Response format documented
- [ ] Code updated
- [ ] Tested successfully

---

## View Cart

### Endpoint Discovery

**Steps to discover:**
1. After logging in and adding items, view your cart
2. Look for API call that loads cart data

**What to document:**
```
Endpoint URL: [TO BE FILLED]
Method: [GET/POST]

Response Body:
[PASTE JSON HERE]
```

### Implementation Status
- [ ] Endpoint discovered
- [ ] Response format documented
- [ ] Code updated
- [ ] Tested successfully

---

## Order History

### Endpoint Discovery

**Steps to discover:**
1. After logging in, navigate to order history
2. Look for API call that loads orders

**What to document:**
```
Endpoint URL: [TO BE FILLED]
Method: [GET]
Query Parameters: [e.g., ?limit=10]

Response Body:
[PASTE JSON HERE]
```

### Implementation Status
- [ ] Endpoint discovered
- [ ] Response format documented
- [ ] Code updated
- [ ] Tested successfully

---

## Delivery Slots

### Endpoint Discovery

**Steps to discover:**
1. During checkout, view available delivery times
2. Look for API call that loads time slots

**What to document:**
```
Endpoint URL: [TO BE FILLED]
Method: [GET]

Response Body:
[PASTE JSON HERE]
```

### Implementation Status
- [ ] Endpoint discovered
- [ ] Response format documented
- [ ] Code updated
- [ ] Tested successfully

---

## Tips for Reverse Engineering

1. **Use Incognito Mode** - Clean slate without cached data
2. **Filter by XHR/Fetch** - Reduces noise from images, CSS, etc.
3. **Look for JSON responses** - API endpoints typically return JSON
4. **Check the Preview tab** - Easier to read than raw Response tab
5. **Note authentication headers** - Essential for all authenticated requests
6. **Copy as cURL** - Right-click request → Copy → Copy as cURL for easy testing
7. **Test with curl first** - Verify the endpoint works before coding
8. **Document everything** - You'll thank yourself later

## Testing Endpoints with curl

Once you find an endpoint, test it:

```bash
# Authentication example
curl -X POST 'https://webapi.prod.knl.nemlig.it/api/auth/login' \
  -H 'Content-Type: application/json' \
  -d '{"email":"your@email.com","password":"yourpassword"}'

# Using the token
curl 'https://webapi.prod.knl.nemlig.it/api/products/search?q=milk' \
  -H 'Authorization: Bearer YOUR_TOKEN_HERE'
```

## Common Issues

- **CORS errors in browser** - Normal, won't affect your MCP server
- **Rate limiting** - Wait between requests, respect their servers
- **Session expiry** - Note how long tokens last
- **Required headers** - Some APIs check User-Agent, Referer, etc.

---

**Next Update:** [DATE] - [What you discovered]
