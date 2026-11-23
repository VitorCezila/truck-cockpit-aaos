package com.cezila.truckcockpit.feature.dashboard

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

/** Dashboard at the head unit size (1920x1080, landscape), parked and driving. */
@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(qualifiers = "w1920dp-h1080dp-land-mdpi")
class DashboardScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun parked() = capture(DashboardSamples.parked)

    @Test
    fun driving() = capture(DashboardSamples.driving)

    @Test
    fun parked_not_available() = capture(DashboardSamples.notAvailableParked)

    @Test
    fun driving_not_available() = capture(DashboardSamples.notAvailableDriving)

    private fun capture(state: DashboardState) {
        composeRule.setContent {
            TruckCockpitTheme { DashboardScreen(state = state, onIntent = {}) }
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
class DashboardSmallScreenshotTest {

    @get:Rule
    val composeRule = createComposeRule()

    @Test
    fun parked() = capture(DashboardSamples.parked)

    @Test
    fun driving() = capture(DashboardSamples.driving)

    private fun capture(state: DashboardState) {
        composeRule.setContent {
            TruckCockpitTheme { DashboardScreen(state = state, onIntent = {}) }
        }
        composeRule.onRoot().captureRoboImage()
    }
}
