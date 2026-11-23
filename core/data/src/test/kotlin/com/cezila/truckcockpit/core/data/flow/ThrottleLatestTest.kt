package com.cezila.truckcockpit.core.data.flow

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class ThrottleLatestTest {

    @Test
    fun `a burst collapses to the first and the latest value`() = runTest {
        // 20 values in 200 ms (100 updates/s), throttled to one per 250 ms.
        val source = flow {
            repeat(20) { i ->
                emit(i)
                delay(10)
            }
        }

        val values = mutableListOf<Int>()
        source.throttleLatest(250).collect { values += it }

        assertEquals(listOf(0, 19), values)
    }

    @Test
    fun `values are spaced at least one period apart`() = runTest {
        // 100 updates/s for 1 s.
        val source = flow {
            repeat(100) { i ->
                emit(i)
                delay(10)
            }
        }

        val times = mutableListOf<Long>()
        source.throttleLatest(250).collect { times += testScheduler.currentTime }

        times.zipWithNext { a, b -> assert(b - a >= 250) { "Emissions $a and $b are closer than 250 ms" } }
        assertEquals(5, times.size) // t = 0, 250, 500, 750, 1000
    }

    @Test
    fun `slow sources pass through unchanged`() = runTest {
        val source = flow {
            emit(1)
            delay(1_000)
            emit(2)
        }

        val values = mutableListOf<Int>()
        source.throttleLatest(250).collect { values += it }

        assertEquals(listOf(1, 2), values)
    }
}
