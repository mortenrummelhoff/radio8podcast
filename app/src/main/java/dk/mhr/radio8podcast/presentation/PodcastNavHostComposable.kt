package dk.mhr.radio8podcast.presentation

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.navigation.DOWNLOAD_ID
import dk.mhr.radio8podcast.presentation.navigation.Screen
import dk.mhr.radio8podcast.presentation.navigation.TITLE
import dk.mhr.radio8podcast.service.PodcastDownloadService
import dk.mhr.radio8podcast.service.PodcastUtils
import java.net.URLDecoder
import java.net.URLEncoder

class PodcastNavHostComposable {

    @Composable
    fun PodCastNavHost(
        startDestination: String = "WearApp",
        lifecycleOwner: LifecycleOwner,
        context: Context
    ) {

        val navController = rememberSwipeDismissableNavController()
        val API_KEY = stringResource(R.string.api_key)


        SwipeDismissableNavHost(
            //modifier = modifier,
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.Landing.route) {
                PodcastLandingComposable(context).landingScreen(onNavigateToFetchPodcast = {
                    Log.i(DEBUG_LOG, "Calling navigate to ShowPodcasts")
                    navController.navigate(Screen.ShowPodcast.route) { popUpTo(Screen.Landing.route) }
                },
                    onNavigateToSeeDownloadList = {
                        podcastViewModel.fetchDownloadList(PodcastUtils.getDownloadManager(context).downloadIndex)
                        navController.navigate(Screen.SeeDownloads.route)
                    },
                    onPodCastListen = {
                        navController.navigate(route = Screen.PodcastPlayer.route) { popUpTo(Screen.SeeDownloads.route) }
                    }
                )

            }
            composable(Screen.ShowPodcast.route) {
                //Log.i(DEBUG_LOG, "backStackEntry: ${it.destination}")
                PodcastListComposable().ShowPodcastList(onPodCastDownload = { title, link, audio ->
                    Log.i(DEBUG_LOG, "Download Podcast clicked!: $title, Link: $link, audio: $audio")

                    val downloadRequest: DownloadRequest =
                        DownloadRequest.Builder(title, Uri.parse(audio)).setData(
                            title.encodeToByteArray()
                        ).build()

                    DownloadService.sendAddDownload(
                        context,
                        PodcastDownloadService::class.java,
                        downloadRequest,
                        false
                    )

                }, lifecycleOwner)
            }
            composable(Screen.SeeDownloads.route) {
                SeeDownloadListComposable(
                    PodcastUtils.getDownloadTracker(LocalContext.current),
                    PodcastUtils.getDownloadManager(LocalContext.current).downloadIndex,
                    lifecycleOwner
                ).SeeDownloadList(onPodCastListen = {
                    navController.navigate(route = Screen.PodcastPlayer.route)
                    { popUpTo(Screen.SeeDownloads.route) }
                }, onPodCastDelete = { download ->
                    Log.i("MHR", "Now delete download: ${download.download.value.request.id}")
                    PodcastUtils.getDownloadManager(context)
                        .removeDownload(download.download.value.request.id)
                }, context)
            }
            composable(
                route = Screen.PodcastPlayer.route
            ) {
                PodcastPlayerComposable(context).showPlayer()
            }
        }

    }
}