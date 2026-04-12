package com.ramzmania.aicammvd

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import com.ramzmania.aicammvd.ui.screens.home.HomeActivity
import org.junit.Rule
import org.junit.Test

class HomeActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS
    )

    @Test
    fun testTrackerToggle() {
        // 1. Wait for the home screen to load. 
        // "TRACKER" text is in the bottom navigation bar which appears once data is loaded or location is handled.
        composeTestRule.waitUntil(60000) {
            composeTestRule.onAllNodesWithText("TRACKER", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Verify that the initial state is "Location : OFF"
        // Note: There's an extra space in the code: "Location  : OFF" or "Location : OFF"
        // Using substring = true to be safe.
        composeTestRule.onNodeWithText("Location", substring = true).assertIsDisplayed()
        composeTestRule.onNodeWithText("OFF", substring = true).assertIsDisplayed()

        // 3. Perform a click on the switch. 
        // We added contentDescription = "Location Switch" to CustomCircleSwitch
        composeTestRule.onNodeWithContentDescription("Location Switch").performClick()

        // 4. After clicking, it should change to "Location : ON"
        // Note: The code updates subtitleText to "Location : ON " (with a trailing space)
        composeTestRule.onNodeWithText("ON", substring = true).assertIsDisplayed()
    }
}
