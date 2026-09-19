package io.jadu.crisisprotect.data.mapper

import io.jadu.crisisprotect.data.remote.UsgsEarthquakeFeatureDto
import io.jadu.crisisprotect.data.remote.UsgsEarthquakeGeometryDto
import io.jadu.crisisprotect.data.remote.UsgsEarthquakePropertiesDto
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DisasterEventMappersTest {
    @Test
    fun `decodes representative USGS GeoJSON fixture`() {
        val feed = Json { ignoreUnknownKeys = true }.decodeFromString<io.jadu.crisisprotect.data.remote.UsgsEarthquakeFeedDto>(
            """{"features":[{"id":"fixture","properties":{"title":"M 4.0 - Fixture","place":"Fixture","mag":4.0,"time":1700000000000,"updated":1700000010000,"url":"https://earthquake.usgs.gov/event"},"geometry":{"coordinates":[42.0,12.5,10.0]}}]}""",
        )

        assertEquals("fixture", feed.features.single().id)
    }

    @Test
    fun `maps valid USGS feature to normalized earthquake`() {
        val event = validFeature().toDisasterEventOrNull()

        requireNotNull(event)
        assertEquals("usgs:abc123", event.id)
        assertEquals(12.5, event.latitude!!, 0.0)
        assertEquals(42.0, event.longitude!!, 0.0)
        assertEquals(5.2, event.magnitude!!, 0.0)
        assertEquals("USGS", event.source)
    }

    @Test
    fun `drops feature without stable identifier`() {
        assertNull(validFeature().copy(id = null).toDisasterEventOrNull())
    }

    @Test
    fun `drops feature with malformed coordinates`() {
        val malformed = validFeature().copy(geometry = UsgsEarthquakeGeometryDto(listOf(181.0, 100.0)))
        assertNull(malformed.toDisasterEventOrNull())
    }

    private fun validFeature() = UsgsEarthquakeFeatureDto(
        id = "abc123",
        properties = UsgsEarthquakePropertiesDto(
            title = "M 5.2 - Test location",
            place = "Test location",
            mag = 5.2,
            time = 1_700_000_000_000,
            updated = 1_700_000_010_000,
            url = "https://earthquake.usgs.gov/example",
        ),
        geometry = UsgsEarthquakeGeometryDto(listOf(42.0, 12.5, 10.0)),
    )
}
