# CLAUDE.md - AI Assistant Development Guide

## Project Overview

**Project Name:** nemlig-mcp
**Project Type:** Model Context Protocol (MCP) Server
**Purpose:** MCP server implementation for Nemlig (Danish online grocery service)

This document provides comprehensive guidance for AI assistants working on this codebase.

---

## Table of Contents

1. [Project Structure](#project-structure)
2. [Development Workflow](#development-workflow)
3. [Code Conventions](#code-conventions)
4. [MCP Server Guidelines](#mcp-server-guidelines)
5. [Testing Strategy](#testing-strategy)
6. [Git Workflow](#git-workflow)
7. [Common Tasks](#common-tasks)
8. [Security Considerations](#security-considerations)

---

## Project Structure

### Actual Directory Layout (Kotlin)

```
nemlig-mcp/
├── src/
│   ├── main/
│   │   ├── kotlin/com/nemlig/mcp/
│   │   │   ├── server/          # MCP server implementation
│   │   │   │   ├── Main.kt      # Entry point
│   │   │   │   └── NemligMcpServer.kt
│   │   │   ├── client/          # Nemlig API client
│   │   │   │   └── NemligClient.kt
│   │   │   ├── tools/           # MCP tool implementations
│   │   │   │   └── NemligTools.kt
│   │   │   ├── models/          # Data models
│   │   │   │   └── Models.kt
│   │   │   └── config/          # Configuration
│   │   │       └── Config.kt
│   │   └── resources/
│   │       └── logback.xml      # Logging configuration
│   └── test/
│       └── kotlin/com/nemlig/mcp/
│           └── ConfigTest.kt    # Tests
├── build.gradle.kts             # Gradle build configuration
├── settings.gradle.kts          # Gradle settings
├── gradle.properties            # Gradle properties
├── .env.example                 # Environment template
├── .gitignore                   # Git ignore rules
├── README.md                    # User documentation
└── CLAUDE.md                    # This file
```

### Key Files

- **build.gradle.kts**: Project dependencies and Gradle configuration
- **src/main/kotlin/.../server/Main.kt**: Entry point for the server
- **src/main/kotlin/.../server/NemligMcpServer.kt**: Core MCP server logic
- **src/main/kotlin/.../client/NemligClient.kt**: HTTP client for Nemlig API
- **src/main/kotlin/.../tools/NemligTools.kt**: MCP tool implementations
- **src/main/kotlin/.../models/Models.kt**: Data models for products, cart, orders
- **.env.example**: Environment variable template
- **README.md**: User-facing documentation

---

## Development Workflow

### Initial Setup

The project is already configured! But if setting up from scratch:

1. **Prerequisites**
   ```bash
   # Verify Java 17+
   java -version

   # Gradle wrapper is included
   ./gradlew --version
   ```

2. **Dependencies** (already in build.gradle.kts)
   - MCP Kotlin SDK: `io.modelcontextprotocol:kotlin-sdk`
   - OkHttp for HTTP client
   - Kotlinx Serialization for JSON
   - Kotlinx Coroutines for async operations
   - Logback for logging

3. **Build Commands**
   ```bash
   ./gradlew build          # Build the project
   ./gradlew run            # Run the server
   ./gradlew test           # Run tests
   ./gradlew clean          # Clean build artifacts
   ```

### Development Cycle

1. **Before Making Changes**
   - Read existing code thoroughly
   - Understand the MCP protocol requirements
   - Check for related tests

2. **During Development**
   - Write code incrementally
   - Test changes locally
   - Follow TypeScript best practices
   - Add appropriate error handling

3. **After Changes**
   - Run tests: `./gradlew test`
   - Check compilation: `./gradlew build`
   - Run the server: `./gradlew run`
   - Clean build: `./gradlew clean build`

---

## Code Conventions

### Kotlin Guidelines

1. **Type Safety**
   - Use explicit types for public function parameters and return values
   - Avoid `Any` - use specific types or sealed classes
   - Use nullable types (`T?`) appropriately
   - Define data classes for all data structures
   - Use sealed classes for restricted hierarchies

2. **Naming Conventions**
   - **Files**: PascalCase matching the main class (`NemligClient.kt`)
   - **Classes**: PascalCase (`NemligClient`)
   - **Functions**: camelCase (`getOrderDetails`)
   - **Constants**: UPPER_SNAKE_CASE (`MAX_RETRIES`)
   - **Data Classes**: PascalCase (`Product`, `Order`)
   - **Properties**: camelCase (`productId`, `userName`)

3. **Function Structure**
   ```kotlin
   /**
    * Brief description of what the function does
    *
    * @param paramName Description of parameter
    * @return Description of return value
    * @throws Exception Description of errors thrown
    */
   suspend fun functionName(
       paramName: ParamType
   ): Result<ReturnType> {
       // Input validation
       require(paramName.isNotBlank()) { "paramName is required" }

       // Main logic
       return try {
           val result = someOperation(paramName)
           Result.success(result)
       } catch (e: Exception) {
           Result.failure(e)
       }
   }
   ```

4. **Error Handling**
   - Use custom error classes for specific error types
   - Always include error context
   - Log errors appropriately
   - Return meaningful error messages to users

5. **Async/Await**
   - Prefer async/await over raw promises
   - Always handle promise rejections
   - Use try/catch blocks appropriately
   - Consider timeout mechanisms for external calls

### Code Organization

1. **One Concept Per File**
   - Each MCP tool should be in its own file
   - Group related utilities together
   - Keep files under 300 lines when possible

2. **Import Organization**
   ```kotlin
   package com.nemlig.mcp.server

   // 1. External dependencies (sorted alphabetically)
   import io.modelcontextprotocol.kotlin.sdk.*
   import kotlinx.coroutines.runBlocking
   import kotlinx.serialization.json.*
   import mu.KotlinLogging
   import okhttp3.*

   // 2. Internal modules (sorted alphabetically)
   import com.nemlig.mcp.client.NemligClient
   import com.nemlig.mcp.config.Config
   import com.nemlig.mcp.tools.NemligTools

   // 3. Models
   import com.nemlig.mcp.models.*
   ```

3. **Comments**
   - Use JSDoc for public APIs
   - Add inline comments for complex logic only
   - Keep comments up-to-date with code
   - Explain "why" not "what"

---

## MCP Server Guidelines

### Server Implementation

1. **Server Initialization** (Kotlin)
   ```kotlin
   // See src/main/kotlin/com/nemlig/mcp/server/NemligMcpServer.kt
   class NemligMcpServer(private val config: Config) {
       private val client = NemligClient(config.nemlig)
       private val tools = NemligTools(client)

       fun getServerInfo(): ServerInfo {
           return ServerInfo(
               name = config.server.name,
               version = config.server.version
           )
       }
   }
   ```

2. **Tool Registration** (Kotlin)
   - Each tool should have a clear, descriptive name
   - Provide comprehensive input schemas
   - Include detailed descriptions for AI understanding
   - Return structured, consistent results

   ```kotlin
   fun listTools(): List<Tool> = listOf(
       Tool(
           name = "search_products",
           description = "Search for products in the Nemlig catalog",
           inputSchema = buildJsonObject {
               put("type", "object")
               putJsonObject("properties") {
                   putJsonObject("query") {
                       put("type", "string")
                       put("description", "Search query for products")
                   }
                   putJsonObject("limit") {
                       put("type", "number")
                       put("description", "Maximum number of results")
                       put("default", 20)
                   }
               }
               putJsonArray("required") { add("query") }
           }
       )
   )
   ```

3. **Tool Implementation** (Kotlin)
   ```kotlin
   suspend fun callTool(name: String, arguments: JsonObject): JsonElement {
       return when (name) {
           "search_products" -> tools.searchProducts(arguments)
           "get_product_details" -> tools.getProductDetails(arguments)
           "view_cart" -> tools.viewCart(arguments)
           "add_to_cart" -> tools.addToCart(arguments)
           else -> buildJsonObject {
               put("success", false)
               put("error", "Unknown tool: $name")
           }
       }
   }
   ```

### Resource Handling

1. **Resource URIs**
   - Use consistent URI schemes
   - Make URIs human-readable
   - Document URI patterns

2. **Resource Implementation**
   ```typescript
   server.setRequestHandler(ListResourcesRequestSchema, async () => {
     return {
       resources: [
         {
           uri: 'nemlig://orders',
           name: 'Orders',
           description: 'User order history',
           mimeType: 'application/json',
         },
       ],
     };
   });
   ```

### Prompt Templates

1. **Create Reusable Prompts**
   - Define prompts for common workflows
   - Include clear descriptions
   - Use dynamic arguments appropriately

---

## Testing Strategy

### Unit Tests

1. **Test Structure** (Kotlin)
   ```kotlin
   class NemligClientTest {
       @Test
       fun `should load default configuration`() {
           // Arrange & Act
           val config = ConfigLoader.load()

           // Assert
           assertNotNull(config)
           assertEquals("nemlig-mcp", config.server.name)
       }

       @Test
       fun `should handle failed authentication`() = runTest {
           // Arrange
           val client = NemligClient(mockConfig)

           // Act
           val result = client.authenticate()

           // Assert
           assertTrue(result.isFailure)
       }
   }
   ```

2. **Test Coverage**
   - Aim for 80%+ coverage
   - Test happy paths and error cases
   - Test edge cases and boundary conditions
   - Mock external dependencies

### Integration Tests

1. **Test MCP Protocol**
   - Test tool registration
   - Test tool execution
   - Test resource access
   - Test prompt templates

2. **End-to-End Scenarios**
   - Test complete user workflows
   - Test error handling across components
   - Test performance under load

---

## Git Workflow

### Branch Strategy

1. **Branch Naming**
   - Feature branches: `claude/feature-name-{sessionId}`
   - Bug fixes: `claude/fix-description-{sessionId}`
   - Documentation: `claude/docs-description-{sessionId}`

2. **Commit Messages**
   ```
   type(scope): brief description

   Longer description if needed

   https://claude.ai/code/session_XXX
   ```

   Types: `feat`, `fix`, `docs`, `refactor`, `test`, `chore`

3. **Before Committing**
   - Run all tests
   - Run linter
   - Format code
   - Review changes

### Pull Requests

1. **PR Description Template**
   ```markdown
   ## Summary
   - What changes were made
   - Why these changes were needed

   ## Test Plan
   - [ ] Unit tests pass
   - [ ] Integration tests pass
   - [ ] Manual testing completed

   ## Related Issues
   Closes #XX

   https://claude.ai/code/session_XXX
   ```

2. **PR Checklist**
   - Code follows style guidelines
   - Tests added for new functionality
   - Documentation updated
   - No console.log or debug code
   - No commented-out code

---

## Common Tasks

### Adding a New Tool

1. Create tool file in `src/tools/`
2. Define TypeScript interfaces for input/output
3. Implement tool logic with error handling
4. Register tool in server
5. Add unit tests
6. Update documentation

### Adding API Integration

1. Create API client in `src/utils/`
2. Define TypeScript types for API responses
3. Implement error handling and retries
4. Add rate limiting if needed
5. Mock API in tests
6. Document API requirements

### Debugging

1. **Enable Debug Logging**
   - Add logging statements
   - Use environment variables for log levels
   - Never log sensitive information

2. **Common Issues**
   - Check MCP protocol version compatibility
   - Verify tool schema format
   - Check async/await usage
   - Review error messages

---

## Security Considerations

### API Keys and Secrets

1. **Never Commit Secrets**
   - Use environment variables
   - Add `.env` to `.gitignore`
   - Document required environment variables

2. **Environment Configuration**
   ```typescript
   const config = {
     apiKey: process.env.NEMLIG_API_KEY,
     apiUrl: process.env.NEMLIG_API_URL || 'https://api.nemlig.com',
   };

   if (!config.apiKey) {
     throw new Error('NEMLIG_API_KEY environment variable is required');
   }
   ```

### Input Validation

1. **Always Validate User Input**
   - Check required parameters
   - Validate data types
   - Sanitize strings
   - Validate ranges and limits

2. **Prevent Injection Attacks**
   - Use parameterized queries
   - Escape special characters
   - Validate URLs and paths

### Error Messages

1. **Don't Leak Sensitive Information**
   - Don't expose API keys in errors
   - Don't expose internal paths
   - Provide helpful but safe messages

---

## Additional Resources

### MCP Documentation
- [MCP Specification](https://modelcontextprotocol.io/)
- [MCP SDK Documentation](https://github.com/modelcontextprotocol/sdk)

### TypeScript Resources
- [TypeScript Handbook](https://www.typescriptlang.org/docs/)
- [TypeScript Best Practices](https://www.typescriptlang.org/docs/handbook/declaration-files/do-s-and-don-ts.html)

### Nemlig API
- Document API endpoints as they are discovered
- Keep API documentation up-to-date

---

## AI Assistant Notes

### When Working on This Project

1. **Always Read First**
   - Read relevant files before making changes
   - Understand the full context
   - Check for existing patterns

2. **Follow Established Patterns**
   - Match existing code style
   - Use existing utilities
   - Follow naming conventions

3. **Be Thorough**
   - Test changes completely
   - Update documentation
   - Consider edge cases

4. **Communicate Clearly**
   - Explain what you're doing
   - Ask clarifying questions
   - Document decisions

5. **Security First**
   - Validate all inputs
   - Handle errors properly
   - Never expose secrets

### Common Pitfalls to Avoid

1. Don't create files unnecessarily - prefer editing existing files
2. Don't skip tests - they catch bugs early
3. Don't ignore TypeScript errors - fix them properly
4. Don't commit commented-out code - delete it
5. Don't use `any` type - use proper types
6. Don't forget error handling - it's critical for MCP servers
7. Don't skip documentation - future you (and others) will thank you

---

**Last Updated:** 2026-01-23
**Version:** 1.0.0
**Status:** Kotlin Implementation Complete - API Integration Pending

## Current Implementation Status

✅ **Completed:**
- Kotlin project structure with Gradle
- MCP server framework
- Tool definitions (7 tools)
- Nemlig API client structure
- Data models
- Configuration system
- Logging setup
- Basic tests
- Comprehensive documentation

🚧 **Pending:**
- Reverse engineer Nemlig.com API endpoints
- Implement actual API calls in NemligClient.kt
- Integrate MCP Kotlin SDK stdio transport
- Add comprehensive test coverage
- Implement authentication flow
- Add more error handling

## Quick Start for AI Assistants

When working on this codebase:

1. **Before API Implementation:**
   - Review network traffic from nemlig.com
   - Document API endpoints in comments
   - Update NemligClient.kt with actual endpoints

2. **Key Files to Modify:**
   - `NemligClient.kt` - Add actual API implementations
   - `Models.kt` - Adjust to match real API responses
   - `Main.kt` - Integrate MCP SDK stdio transport

3. **Testing:**
   - Run `./gradlew test` after changes
   - Add tests in `src/test/kotlin/`
   - Mock HTTP responses for testing
