package io.jadu.crisisprotect.feature.common

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class EventPresentationTest {
    @Test
    fun `accepts HTTP and HTTPS source links`() {
        assertTrue(isSafeWebUrl("https://earthquake.usgs.gov/event"))
        assertTrue(isSafeWebUrl("http://example.com"))
    }

    @Test
    fun `rejects unsafe and absent schemes`() {
        assertFalse(isSafeWebUrl("javascript:alert(1)"))
        assertFalse(isSafeWebUrl("file:///private/data"))
        assertFalse(isSafeWebUrl("earthquake.usgs.gov/event"))
    }
}
