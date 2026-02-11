package com.nemlig.mcp

import com.nemlig.mcp.config.ConfigLoader
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

/**
 * Tests for configuration loading
 */
class ConfigTest {

    @Test
    fun `should load default configuration`() {
        val config = ConfigLoader.load()

        assertNotNull(config)
        assertNotNull(config.nemlig)
        assertNotNull(config.server)
    }

    @Test
    fun `should use default values when environment variables not set`() {
        val config = ConfigLoader.load()

        assertEquals("nemlig-mcp", config.server.name)
        assertEquals("1.0.0", config.server.version)
        assertEquals("https://www.nemlig.com/webapi", config.nemlig.apiUrl)
        assertEquals(30000L, config.nemlig.timeout)
    }

    @Test
    fun `should have rate limit configuration`() {
        val config = ConfigLoader.load()

        assertEquals(1, config.nemlig.rateLimit.requestsPerSecond)
        assertEquals(2, config.nemlig.rateLimit.burstSize)
    }
}
