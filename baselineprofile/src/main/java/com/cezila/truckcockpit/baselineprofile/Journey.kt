package com.cezila.truckcockpit.baselineprofile

import androidx.benchmark.macro.MacrobenchmarkScope
import androidx.test.uiautomator.By
import androidx.test.uiautomator.Until

internal const val TARGET_PACKAGE = "com.cezila.truckcockpit"

private const val UI_TIMEOUT_MS = 10_000L

/**
 * CAR_SPEED and CAR_ENERGY are runtime permissions. Granting them up front keeps the system
 * dialog out of the journey, so startup is measured the way a returning driver sees it.
 * `--user current`: AAOS runs apps as the driver (user 10), not as the headless system user 0
 * that a plain `pm grant` targets.
 */
internal fun MacrobenchmarkScope.grantVehiclePermissions() {
    listOf("android.car.permission.CAR_SPEED", "android.car.permission.CAR_ENERGY").forEach {
        device.executeShellCommand("pm grant --user current $packageName $it")
    }
}

/** The tab bar is drawn once the permission gate has let the screens through. */
internal fun MacrobenchmarkScope.waitForDashboard() {
    check(device.wait(Until.hasObject(By.text("Dashboard")), UI_TIMEOUT_MS)) { "Dashboard tab not shown" }
}

/** Critical journey: Dashboard → Trip → Dashboard through the tab bar. */
internal fun MacrobenchmarkScope.dashboardToTripAndBack() {
    openTab("Trip")
    openTab("Dashboard")
}

private fun MacrobenchmarkScope.openTab(label: String) {
    val tab = checkNotNull(device.findObject(By.text(label))) { "Tab $label not found" }
    tab.click()
    device.wait(Until.hasObject(By.text(label).selected(true)), UI_TIMEOUT_MS)
    device.waitForIdle()
}
