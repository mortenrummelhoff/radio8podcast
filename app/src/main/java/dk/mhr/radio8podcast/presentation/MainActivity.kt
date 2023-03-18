/* While this template provides a good starting point for using Wear Compose, you can always
 * take a look at https://github.com/android/wear-os-samples/tree/main/ComposeStarter and
 * https://github.com/android/wear-os-samples/tree/main/ComposeAdvanced to find the most up to date
 * changes to the libraries and their usages.
 */

package dk.mhr.radio8podcast.presentation

//import androidx.compose.foundation.interaction.collectIsPressedAsState

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.room.Room
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.data.AppDatabase
import dk.mhr.radio8podcast.presentation.navigation.AUDIO_URL
import dk.mhr.radio8podcast.presentation.navigation.DOWNLOAD_ID
import dk.mhr.radio8podcast.presentation.navigation.Screen
import dk.mhr.radio8podcast.presentation.navigation.TITLE
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import dk.mhr.radio8podcast.service.PodcastDownloadService
import dk.mhr.radio8podcast.service.PodcastService
import dk.mhr.radio8podcast.service.PodcastUtils
import kotlinx.coroutines.Dispatchers
import java.net.URLDecoder
import java.net.URLEncoder

var podcastService = PodcastService(Dispatchers.IO)
var podcastViewModel = PodcastViewModel(podcastService);
val DEBUG_LOG = "MHR";

class MainActivity : ComponentActivity(), LifecycleOwner {

    private val appDatabase by lazy { AppDatabase.getDatabase(this) }
    private val exoPlayer by lazy { ExoPlayer.Builder(this).build() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            podcastViewModel.podcastDao = appDatabase.podcastDao()

            //val player = ExoPlayer.Builder(LocalContext.current).build()
            exoPlayer.experimentalSetOffloadSchedulingEnabled(true)
            val downloadIndex = PodcastUtils.getDownloadManager(LocalContext.current).downloadIndex
            //podcastViewModel.fetchDownloadList(downloadIndex)

            PodcastUtils.getDownloadTracker(LocalContext.current).addListener {
                podcastViewModel.fetchDownloadList(downloadIndex)
            }
//                if (!podcastViewModel.downloadChanged.hasObservers()) {
//                    podcastViewModel.downloadChanged.observe(this){
//                        podcastViewModel.fetchDownloadList(downloadIndex)
//                    }
//                }

                //Log.i("MHR", "onDownloadsChanged called. UI update")
                //podcastViewModel.downloadChanged.postValue("UPDATE_UI")
            //}


            Log.i(DEBUG_LOG, "Oncreate called we have player: $exoPlayer")
            PodCastNavHost("WearApp", this, this, exoPlayer)
        }
    }
}


@Composable
fun FetchPodcasts(onNavigateToShowPodcasts: () -> Unit, onNavigateToSeeDownloadList: () -> Unit) {

    // Log.i("MHR", "FetchPodcasts called. Show chip with button action")
    val interactionSource = remember { MutableInteractionSource() }
// Observe changes to the binary state for these interactions
    val isDragged by interactionSource.collectIsDraggedAsState()
    //val isPressed by interactionSource.collectIsPressedAsState()



}


@Composable
fun PodCastNavHost(
    startDestination: String = "WearApp",
    lifecycleOwner: LifecycleOwner,
    context: Context,
    player: ExoPlayer,
    modifier: Modifier = Modifier
) {

    val navController = rememberSwipeDismissableNavController()
    val API_KEY = stringResource(R.string.api_key)


    SwipeDismissableNavHost(
        //modifier = modifier,
        navController = navController,
        startDestination = startDestination
    ) {
        composable(Screen.Landing.route) {
            WearApp(onNavigateToFetchPodcast = {
                Log.i(DEBUG_LOG, "Calling navigate to ShowPodcasts")
                podcastViewModel.loadPodcast(API_KEY)

                if (!podcastViewModel.podcasts.hasObservers()) {
                    podcastViewModel.podcasts.observe(lifecycleOwner) { t ->
                        podcastViewModel.podcastList = t
                        Log.i(DEBUG_LOG, "Observe called...")
                        navController.navigate(Screen.ShowPodcast.route) { popUpTo(Screen.Landing.route) }
                    }
                }
            },
                onNavigateToSeeDownloadList = {
                    podcastViewModel.fetchDownloadList(PodcastUtils.getDownloadManager(context).downloadIndex)
                    navController.navigate(Screen.SeeDownloads.route)
                })
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
            ).SeeDownloadList(onPodCastListen = { downloadId, audio, title ->
                Log.i("MHR", "DownloadId: $downloadId")
                navController.navigate(
                    route = Screen.PodcastPlayer.route + "/" + URLEncoder.encode(downloadId, "UTF8") + "/" + URLEncoder.encode(audio, "UTF8") + "/" + URLEncoder.encode(title, "UTF8")
                ) { popUpTo(Screen.SeeDownloads.route) }
            }, onPodCastDelete = {download ->
                Log.i("MHR", "Now delete download: ${download.download.request.id}")
                PodcastUtils.getDownloadManager(context).removeDownload(download.download.request.id)
            })
        }
        composable(
            route = Screen.PodcastPlayer.route + "/{" + DOWNLOAD_ID + "}/{" + AUDIO_URL + "}/{" + TITLE + "}",
            arguments = listOf(navArgument(DOWNLOAD_ID) { NavType.StringType }, navArgument(AUDIO_URL) { NavType.StringType }, navArgument(TITLE) {NavType.StringType})

        ) {
            val download = PodcastUtils.getDownloadManager(LocalContext.current).downloadIndex.getDownload(
                URLDecoder.decode(
                    it.arguments?.getString(
                        DOWNLOAD_ID
                    ), "UTF8"
                )
            )

            PodcastPlayerComposable(player).showPlayer(it.arguments?.getString(AUDIO_URL), URLDecoder.decode(it.arguments?.getString(TITLE),"UTF8"),
                download )
        }
    }

}

@Composable
fun WearApp(
    onNavigateToFetchPodcast: () -> Unit,
    onNavigateToSeeDownloadList: () -> Unit
) {

    val padding = 6.dp
    Log.i("MHR", "WearApp called")
    Radio8podcastTheme {
        /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
         * version of LazyColumn for wear devices with some added features. For more information,
         * see d.android.com/wear/compose.
         */
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colors.background),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Chip(onClick = onNavigateToFetchPodcast,
                colors = ChipDefaults.chipColors(contentColor = MaterialTheme.colors.background),
                modifier = Modifier.clickable { Log.i("MHR", "We have click!!") },
                label = {
                    Text(text = "Undskyld vi roder")
                },
                secondaryLabel = {
                    Text("Click to fetch")
                }
            )
            Spacer(Modifier.size(padding))
            Chip(onClick = onNavigateToSeeDownloadList,
                colors = ChipDefaults.chipColors(contentColor = MaterialTheme.colors.background),
                label = {
                    Text(text = "Downloads")
                },
                secondaryLabel = {
                    //Log.i("MHR", "secondaryLabel->" + podcastViewModel.podcasts.value)
                    Text("Click to see")
                }
            )
            //Greeting(greetingName = greetingName)
            //FetchPodcasts(onNavigateToFetchPodcast, onNavigateToSeeDownloadList)
        }

    }
}
