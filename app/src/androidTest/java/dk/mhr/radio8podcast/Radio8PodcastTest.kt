package dk.mhr.radio8podcast

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.printToLog
import androidx.navigation.testing.TestNavHostController
import dk.mhr.radio8podcast.presentation.MainActivity
import dk.mhr.radio8podcast.presentation.PodcastNavHostComposable
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class Radio8PodcastTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    private lateinit var navController: TestNavHostController

    @Before
    fun setupAppNavHost() {
        composeTestRule.setContent {
            navController = TestNavHostController(LocalContext.current)
            navController.navigatorProvider.addNavigator(ComposeNavigator())
            PodcastNavHostComposable().PodCastNavHost(LocalContext.current, navController)
        }
    }

    @Test
    fun testFetchPodcastsClickedTest() {
        // Start the app
        composeTestRule.setContent {
            Radio8podcastTheme {
                MainActivity()

            }
        }
        composeTestRule.waitForIdle() // Advances the clock until Compose is idle
        composeTestRule.onRoot().printToLog("TAG")
    }
}