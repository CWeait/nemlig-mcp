# Nemlig MCP Server

A Model Context Protocol (MCP) server implementation for Nemlig.com (Danish online grocery service) built with Kotlin.

## Overview

This MCP server allows AI assistants like Claude to interact with Nemlig.com to help with grocery shopping tasks such as:

- 🔍 Searching for products
- 📦 Viewing product details and nutritional information
- 🛒 Managing shopping cart
- 📜 Viewing order history
- 🚚 Checking delivery time slots

## Architecture

```
┌─────────────────┐
│  Claude Desktop │
│   (MCP Client)  │
└────────┬────────┘
         │ stdio
         │
┌────────▼────────┐
│  Nemlig MCP     │
│     Server      │
│   (Kotlin)      │
└────────┬────────┘
         │ HTTP
         │
┌────────▼────────┐
│  Nemlig.com     │
│   Internal API  │
└─────────────────┘
```

## Features

### Current Tools

1. **search_products** - Search the Nemlig product catalog
2. **get_product_details** - Get detailed product information
3. **view_cart** - View shopping cart contents
4. **add_to_cart** - Add items to cart
5. **remove_from_cart** - Remove items from cart
6. **get_order_history** - View past orders
7. **get_delivery_slots** - Check available delivery times

### Planned Features

- [ ] Actual Nemlig API integration (currently using placeholders)
- [ ] Complete MCP stdio transport implementation
- [ ] Shopping list management
- [ ] Recipe-based shopping
- [ ] Price comparison and budgeting
- [ ] Automated reordering

## Prerequisites

- Java 17 or higher
- Gradle 8.x (included via wrapper)
- Nemlig.com account (for personal use)
- Claude Desktop or another MCP-compatible client

## Installation

### 1. Clone the Repository

```bash
git clone https://github.com/CWeait/nemlig-mcp.git
cd nemlig-mcp
```

### 2. Configure Environment

Copy the example environment file and add your credentials:

```bash
cp .env.example .env
```

Edit `.env` with your Nemlig.com credentials:

```bash
NEMLIG_USERNAME=your.email@example.com
NEMLIG_PASSWORD=your_password_here
```

### 3. Build the Project

```bash
./gradlew build
```

### 4. Run the Server

```bash
./gradlew run
```

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `NEMLIG_USERNAME` | Your Nemlig.com email | - |
| `NEMLIG_PASSWORD` | Your Nemlig.com password | - |
| `NEMLIG_API_URL` | Nemlig API base URL | `https://webapi.prod.knl.nemlig.it` |
| `NEMLIG_TIMEOUT` | Request timeout (ms) | `30000` |
| `SERVER_NAME` | MCP server name | `nemlig-mcp` |
| `SERVER_VERSION` | MCP server version | `1.0.0` |
| `LOG_LEVEL` | Logging level | `INFO` |

### Claude Desktop Configuration

To use this server with Claude Desktop, add to your `claude_desktop_config.json`:

```json
{
  "mcpServers": {
    "nemlig": {
      "command": "/path/to/nemlig-mcp/gradlew",
      "args": ["run"],
      "cwd": "/path/to/nemlig-mcp",
      "env": {
        "NEMLIG_USERNAME": "your.email@example.com",
        "NEMLIG_PASSWORD": "your_password_here"
      }
    }
  }
}
```

**Note:** For security, consider using a more secure method for storing credentials.

## Usage Examples

Once configured with Claude Desktop, you can ask Claude to:

### Search for Products

```
"Can you search for organic milk on Nemlig?"
```

### Add Items to Cart

```
"Add 2 liters of organic milk to my cart"
```

### View Cart

```
"What's in my Nemlig shopping cart?"
```

### Check Past Orders

```
"Show me my recent Nemlig orders"
```

### Plan Shopping

```
"Help me plan this week's groceries on Nemlig. I need ingredients for pasta, salad, and breakfast items."
```

## Development

### Project Structure

```
nemlig-mcp/
├── src/
│   ├── main/
│   │   ├── kotlin/com/nemlig/mcp/
│   │   │   ├── server/          # MCP server implementation
│   │   │   ├── client/          # Nemlig API client
│   │   │   ├── tools/           # MCP tool implementations
│   │   │   ├── models/          # Data models
│   │   │   └── config/          # Configuration
│   │   └── resources/
│   │       └── logback.xml      # Logging configuration
│   └── test/                    # Tests
├── build.gradle.kts             # Gradle build configuration
├── CLAUDE.md                    # AI assistant development guide
└── README.md                    # This file
```

### Building

```bash
# Build the project
./gradlew build

# Run tests
./gradlew test

# Run the server
./gradlew run

# Create distribution
./gradlew installDist
```

### Testing

```bash
# Run all tests
./gradlew test

# Run with coverage
./gradlew test jacocoTestReport
```

### Code Style

This project follows Kotlin coding conventions:

- Use 4 spaces for indentation
- Maximum line length: 120 characters
- Use meaningful variable and function names
- Document public APIs with KDoc

Format code:

```bash
./gradlew ktlintFormat
```

## Reverse Engineering the API

Since Nemlig.com doesn't provide a public API, you'll need to reverse-engineer their internal API:

### Steps

1. **Open Browser DevTools**
   - Navigate to nemlig.com
   - Open DevTools (F12) → Network tab

2. **Perform Actions**
   - Search for products
   - Add items to cart
   - View orders
   - Note the API endpoints, headers, and request/response formats

3. **Update the Client**
   - Modify `src/main/kotlin/com/nemlig/mcp/client/NemligClient.kt`
   - Replace placeholder implementations with actual API calls
   - Update data models in `models/Models.kt` to match API responses

4. **Test Thoroughly**
   - Add rate limiting to respect their servers
   - Handle errors gracefully
   - Cache responses when appropriate

### Example Network Analysis

Look for XHR requests like:
```
POST https://webapi.prod.knl.nemlig.it/api/v1/auth/login
GET https://webapi.prod.knl.nemlig.it/api/v1/products/search?q=milk
POST https://webapi.prod.knl.nemlig.it/api/v1/cart/add
```

## Security Considerations

⚠️ **Important Security Notes:**

1. **Credentials Storage**
   - Never commit `.env` file to version control
   - Consider using system keychain for credential storage
   - Use environment variables or secure secret management

2. **Rate Limiting**
   - Respect Nemlig's infrastructure
   - Built-in rate limiting (5 req/sec default)
   - Add delays between requests

3. **Personal Use Only**
   - This is intended for personal use only
   - Do not distribute or commercialize
   - Respect Nemlig's terms of service

4. **Data Privacy**
   - Don't log sensitive information
   - Handle personal data responsibly
   - Follow GDPR guidelines

## Troubleshooting

### Server Won't Start

```bash
# Check Java version
java -version  # Should be 17+

# Check Gradle
./gradlew --version

# Clean and rebuild
./gradlew clean build
```

### Authentication Fails

- Verify credentials in `.env`
- Check if Nemlig.com is accessible
- Review logs in `logs/nemlig-mcp.log`
- May need to reverse-engineer auth flow

### Tools Not Working

- Check MCP server logs
- Verify Claude Desktop configuration
- Test API endpoints manually
- Update API client implementation

## Contributing

This is a personal project, but suggestions are welcome:

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Submit a pull request

## License

MIT License - See LICENSE file for details

## Disclaimer

This project is not affiliated with, endorsed by, or associated with Nemlig.com or their parent company. It is an independent implementation for personal use only.

The reverse engineering of APIs may violate terms of service. Use at your own risk and ensure compliance with applicable laws and terms of service.

## Resources

- [Model Context Protocol](https://modelcontextprotocol.io/)
- [MCP Kotlin SDK](https://github.com/modelcontextprotocol/kotlin-sdk)
- [Claude Desktop](https://claude.ai/desktop)
- [Nemlig.com](https://www.nemlig.com/)

## Support

For issues, questions, or suggestions:
- Open an issue on GitHub
- Review the CLAUDE.md file for development guidance
- Check the MCP documentation

---

**Status:** 🚧 Work in Progress

Current focus:
- [ ] Complete Nemlig API reverse engineering
- [ ] Implement actual API client
- [ ] Integrate MCP Kotlin SDK stdio transport
- [ ] Add comprehensive tests
- [ ] Improve error handling
