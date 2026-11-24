package com.cezila.truckcockpit.baselineprofile

import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Generates the Baseline Profile (and startup profile) for `:app`.
 * Run with `scripts/baseline-profile.sh generate` on a rooted AAOS emulator: the Gradle task
 * can't pull the output on AAOS multi-user (see the script header).
 */
@RunWith(AndroidJUnit4::class)
class BaselineProfileGenerator {

    @get:Rule
    val rule = BaselineProfileRule()

    @Test
    fun generate() = rule.collect(packageName = TARGET_PACKAGE, includeInStartupProfile = true) {
        grantVehiclePermissions()
        pressHome()
        startActivityAndWait()
        waitForDashboard()
        dashboardToTripAndBack()
    }
}
