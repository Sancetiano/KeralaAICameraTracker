package com.ramzmania.aicammvd

import android.Manifest
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.rule.GrantPermissionRule
import com.ramzmania.aicammvd.ui.screens.slider.SliderActivity
import org.junit.Rule
import org.junit.Test

class AppFlowTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<SliderActivity>()

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        Manifest.permission.ACCESS_FINE_LOCATION,
        Manifest.permission.ACCESS_COARSE_LOCATION,
        Manifest.permission.POST_NOTIFICATIONS,
        Manifest.permission.ACCESS_BACKGROUND_LOCATION
    )

    @Test
    fun testOnboardingAndNavigationToHome() {
        // If the slider was already passed, SliderActivity might have finished and moved to HomeActivity.
        // We try to find "AIWatch", but if it's not there, we check if we are already on Home.
        val isOnboardingVisible = try {
            composeTestRule.onNodeWithText("AIWatch", ignoreCase = true).assertIsDisplayed()
            true
        } catch (e: AssertionError) {
            false
        }

        if (isOnboardingVisible) {
            // 2. Swipe through the slides (there are 3 slides)
            // Swipe to 2nd slide
            composeTestRule.onRoot().performTouchInput { swipeLeft() }
            composeTestRule.onNodeWithText("DriveGuard", ignoreCase = true).assertIsDisplayed()

            // Swipe to 3rd slide
            composeTestRule.onRoot().performTouchInput { swipeLeft() }
            composeTestRule.onNodeWithText("SmartLens", ignoreCase = true).assertIsDisplayed()

            // 3. On the last slide, there should be a "Let's Track" button
            val letsTrackButton = composeTestRule.onNodeWithText("Let's Track", ignoreCase = true)
            letsTrackButton.assertIsDisplayed()

            // 4. Click the button to navigate to HomeActivity
            letsTrackButton.performClick()
        }

        // 5. Verify we are now on the Home screen by checking for "TRACKER" or "LOCATION"
        composeTestRule.waitUntil(60_000) {
            composeTestRule.onAllNodesWithText("TRACKER", ignoreCase = true).fetchSemanticsNodes().isNotEmpty() ||
            composeTestRule.onAllNodesWithText("Location not available", substring = true).fetchSemanticsNodes().isNotEmpty()
        }
        
        // Assert that we reached the home screen or the location error state
        val trackerNode = composeTestRule.onAllNodesWithText("TRACKER", ignoreCase = true)
        if (trackerNode.fetchSemanticsNodes().isNotEmpty()) {
            trackerNode.onFirst().assertIsDisplayed()
        } else {
            composeTestRule.onNodeWithText("Location not available", substring = true).assertIsDisplayed()
        }
    }
}
