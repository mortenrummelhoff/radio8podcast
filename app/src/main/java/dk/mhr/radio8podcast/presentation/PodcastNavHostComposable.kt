package dk.mhr.radio8podcast.presentation

//import androidx.compose.ui.Modifier

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.offline.DownloadService
import androidx.navigation.NavHostController
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import dk.mhr.radio8podcast.presentation.navigation.Screen
import dk.mhr.radio8podcast.service.PodcastDownloadService
import dk.mhr.radio8podcast.service.PodcastUtils

@UnstableApi class PodcastNavHostComposable {

    @Composable
    fun PodCastNavHost(
        context: Context,
        navController: NavHostController,
        startDestination: String = Screen.Landing.route,
    ) {

        Log.i(DEBUG_LOG, "Starting Navigation host")
        SwipeDismissableNavHost(
            navController = navController,
            startDestination = startDestination
        ) {
            composable(Screen.Landing.route) {
                PodcastLandingComposable(context).landingScreen(onNavigateToFetchPodcast = {
                    Log.i(DEBUG_LOG, "Calling navigate to ShowPodcasts")
                    navController.navigate(Screen.ShowPodcast.route) { popUpTo(Screen.Landing.route) }
                },
                    onNavigateToSeeDownloadList = {
                        //TODO: instead of calling fetchDownloadList from here, it should be put into the SeeDownloads composable.
                        podcastViewModel.fetchDownloadList(PodcastUtils.getDownloadManager(context).downloadIndex)
                        navController.navigate(Screen.SeeDownloads.route)
                    },
                    onPodCastListen = {
                        navController.navigate(route = Screen.PodcastPlayer.route) { popUpTo(Screen.SeeDownloads.route) }
                    }
                )

            }
            composable(Screen.ShowPodcast.route) {
                PodcastListComposable(context).ShowPodcastList(onPodCastDownload = { title, link, audio ->
                    Log.i(DEBUG_LOG, "Download Podcast clicked!: $title, Link: $link, audio: $audio")
                    //TODO: This should be moved to service layer as it is not part of any navigation changes!!
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
                    "I'm Done here"
                })
            }
            composable(Screen.SeeDownloads.route) {
                SeeDownloadListComposable().SeeDownloadList(onPodCastListen = {
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