package com.cezila.truckcockpit.core.data.flow

import app.cash.turbine.test
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ShareLatestTest {

    /** A source that emits only when told to and counts how many times it was collected. */
    private class Upstream {
        val values = MutableSharedFlow<String>()
        var collections = 0
        val flow = values.onStart { collections++ }
    }

    @Test
    fun `subscribers share one collection and a late one gets the latest value`() = runTest {
        val upstream = Upstream()
        val shared = upstream.flow.shareLatest(backgroundScope)

        shared.test {
            runCurrent()
            upstream.values.emit("Parked")
            assertEquals("Parked", awaitItem())

            shared.test {
                assertEquals("Parked", awaitItem())
            }
            cancelAndIgnoreRemainingEvents()
        }

        assertEquals(1, upstream.collections)
    }

    @Test
    fun `after the last subscriber leaves, the old value is not replayed`() = runTest {
        val upstream = Upstream()
        val shared = upstream.flow.shareLatest(backgroundScope)

        shared.test {
            runCurrent()
            upstream.values.emit("Parked")
            assertEquals("Parked", awaitItem())
        }
        runCurrent()

        shared.test {
            runCurrent()
            expectNoEvents()

            upstream.values.emit("Driving")
            assertEquals("Driving", awaitItem())
        }
        assertEquals(2, upstream.collections)
    }
}
