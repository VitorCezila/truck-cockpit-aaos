package com.cezila.truckcockpit.feature.trip

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import com.cezila.truckcockpit.core.ui.theme.TruckCockpitTheme
import com.github.takahirom.roborazzi.captureRoboImage
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

/** Trip at the head unit size (1920x1080, landscape), parked and driving. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w1920dp-h1080dp-land-mdpi")
class TripScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun parked() = capture(TripSamples.parked)

    @Test
    fun parked_unreachable() = capture(TripSamples.unreachableParked)

    @Test
    fun parked_estimating() = capture(TripSamples.estimatingParked)

    @Test
    fun parked_picking_destination() = capture(TripSamples.pickingDestination)

    @Test
    fun parked_no_destination() = capture(TripSamples.noDestinationParked)

    @Test
    fun parked_not_available() = capture(TripSamples.notAvailableParked)

    @Test
    fun driving() = capture(TripSamples.driving)

    @Test
    fun driving_unreachable() = capture(TripSamples.unreachableDriving)

    @Test
    fun driving_no_destination() = capture(TripSamples.noDestinationDriving)

    @Test
    fun driving_not_available() = capture(TripSamples.notAvailableDriving)

    private fun capture(state: TripState) {
        composeRule.setContent {
            TruckCockpitTheme { TripScreen(state = state, onIntent = {}) }
        }
        composeRule.onRoot().captureRoboImage()
    }
}

/**
 * The AAOS emulator's display (1408x792 at 160 dpi), minus the 89dp tab bar (88dp + divider):
 * the space the screen really gets on the smallest display it must fit.
 */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w1408dp-h703dp-land-mdpi")
class TripSmallScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun parked() = capture(TripSamples.parked)

    @Test
    fun parked_picking_destination() = capture(TripSamples.pickingDestination)

    @Test
    fun driving() = capture(TripSamples.driving)

    @Test
    fun driving_unreachable() = capture(TripSamples.unreachableDriving)

    private fun capture(state: TripState) {
        composeRule.setContent {
            TruckCockpitTheme { TripScreen(state = state, onIntent = {}) }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
