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
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    @Test
    fun testTrackerToggle() {
        // 1. Wait for the home screen to load. 
        composeTestRule.waitUntil(60000) {
            composeTestRule.onAllNodesWithText("TRACKER", ignoreCase = true).fetchSemanticsNodes().isNotEmpty()
        }

        // 2. Verify that the initial state is "OFF"
        composeTestRule.onNodeWithText("OFF", substring = true).assertIsDisplayed()

        // 3. Perform a click on the switch. 
        composeTestRule.onNodeWithContentDescription("Location Switch").performClick()

        // 4. After clicking, it should change to "ON"
        // We wait for "ON" and ensure we pick the one related to Location status
        composeTestRule.waitUntil(30000) {
            composeTestRule.onAllNodes(hasText("Location", substring = true) and hasText("ON", substring = true))
                .fetchSemanticsNodes().isNotEmpty()
        }
        
        composeTestRule.onAllNodes(hasText("Location", substring = true) and hasText("ON", substring = true))
            .onFirst()
            .assertIsDisplayed()
    }
}
