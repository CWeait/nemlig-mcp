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

### Expected Directory Layout

```
nemlig-mcp/
├── src/
│   ├── index.ts           # Main entry point
│   ├── server.ts          # MCP server implementation
│   ├── tools/             # MCP tool implementations
│   ├── resources/         # MCP resource handlers
│   ├── prompts/           # MCP prompt templates
│   ├── types/             # TypeScript type definitions
│   └── utils/             # Utility functions
├── tests/
│   ├── unit/              # Unit tests
│   ├── integration/       # Integration tests
│   └── fixtures/          # Test fixtures
├── docs/
│   └── api.md             # API documentation
├── .github/
│   └── workflows/         # CI/CD workflows
├── package.json
├── tsconfig.json
├── .eslintrc.js
├── .prettierrc
├── README.md
└── CLAUDE.md              # This file
```

### Key Files

- **package.json**: Project dependencies and scripts
- **tsconfig.json**: TypeScript configuration
- **src/server.ts**: Core MCP server logic
- **src/tools/**: Each tool should be in its own file
- **README.md**: User-facing documentation

---

## Development Workflow

### Initial Setup

When setting up the project for the first time:

1. **Initialize Package Structure**
   ```bash
   npm init -y
   npm install @modelcontextprotocol/sdk
   npm install -D typescript @types/node
   npm install -D eslint prettier
   npm install -D jest @types/jest ts-jest
   ```

2. **Configure TypeScript**
   - Use `strict` mode
   - Target ES2022 or later
   - Enable `esModuleInterop`
   - Set `outDir` to `dist/`

3. **Set up Build Scripts**
   ```json
   {
     "scripts": {
       "build": "tsc",
       "dev": "tsc --watch",
       "test": "jest",
       "lint": "eslint src/**/*.ts",
       "format": "prettier --write src/**/*.ts"
     }
   }
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
   - Run tests: `npm test`
   - Check types: `npm run build`
   - Lint code: `npm run lint`
   - Format code: `npm run format`

---

## Code Conventions

### TypeScript Guidelines

1. **Type Safety**
   - Always use explicit types for function parameters and return values
   - Avoid `any` - use `unknown` if type is truly unknown
   - Use union types and type guards appropriately
   - Define interfaces for all data structures

2. **Naming Conventions**
   - **Files**: kebab-case (`order-tool.ts`)
   - **Classes**: PascalCase (`OrderManager`)
   - **Functions**: camelCase (`getOrderDetails`)
   - **Constants**: UPPER_SNAKE_CASE (`MAX_RETRIES`)
   - **Interfaces**: PascalCase, prefix with `I` if ambiguous (`IOrderData`)

3. **Function Structure**
   ```typescript
   /**
    * Brief description of what the function does
    * @param paramName - Description
    * @returns Description of return value
    * @throws Description of errors thrown
    */
   export async function functionName(
     paramName: ParamType
   ): Promise<ReturnType> {
     // Input validation
     if (!paramName) {
       throw new Error('paramName is required');
     }

     // Main logic
     const result = await someOperation(paramName);

     // Return
     return result;
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
   ```typescript
   // 1. External dependencies
   import { Server } from '@modelcontextprotocol/sdk/server/index.js';

   // 2. Internal modules
   import { OrderTool } from './tools/order-tool.js';
   import { ProductTool } from './tools/product-tool.js';

   // 3. Types
   import type { OrderData, ProductData } from './types/index.js';

   // 4. Utilities
   import { logger } from './utils/logger.js';
   ```

3. **Comments**
   - Use JSDoc for public APIs
   - Add inline comments for complex logic only
   - Keep comments up-to-date with code
   - Explain "why" not "what"

---

## MCP Server Guidelines

### Server Implementation

1. **Server Initialization**
   ```typescript
   import { Server } from '@modelcontextprotocol/sdk/server/index.js';
   import { StdioServerTransport } from '@modelcontextprotocol/sdk/server/stdio.js';

   const server = new Server(
     {
       name: 'nemlig-mcp',
       version: '1.0.0',
     },
     {
       capabilities: {
         tools: {},
         resources: {},
         prompts: {},
       },
     }
   );
   ```

2. **Tool Registration**
   - Each tool should have a clear, descriptive name
   - Provide comprehensive input schemas
   - Include detailed descriptions for AI understanding
   - Return structured, consistent results

   ```typescript
   server.setRequestHandler(ListToolsRequestSchema, async () => {
     return {
       tools: [
         {
           name: 'search_products',
           description: 'Search for products in the Nemlig catalog',
           inputSchema: {
             type: 'object',
             properties: {
               query: {
                 type: 'string',
                 description: 'Search query for products',
               },
               limit: {
                 type: 'number',
                 description: 'Maximum number of results',
                 default: 10,
               },
             },
             required: ['query'],
           },
         },
       ],
     };
   });
   ```

3. **Tool Implementation**
   ```typescript
   server.setRequestHandler(CallToolRequestSchema, async (request) => {
     const { name, arguments: args } = request.params;

     switch (name) {
       case 'search_products':
         return await handleSearchProducts(args);
       default:
         throw new Error(`Unknown tool: ${name}`);
     }
   });
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

1. **Test Structure**
   ```typescript
   describe('OrderTool', () => {
     describe('getOrderDetails', () => {
       it('should return order details for valid order ID', async () => {
         // Arrange
         const orderId = '12345';

         // Act
         const result = await getOrderDetails(orderId);

         // Assert
         expect(result).toBeDefined();
         expect(result.orderId).toBe(orderId);
       });

       it('should throw error for invalid order ID', async () => {
         // Arrange
         const invalidId = 'invalid';

         // Act & Assert
         await expect(getOrderDetails(invalidId)).rejects.toThrow();
       });
     });
   });
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
**Status:** Initial Documentation for New Project
