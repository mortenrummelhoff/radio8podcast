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
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.RenderersFactory
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import dk.mhr.radio8podcast.service.DemoDownloadService
import dk.mhr.radio8podcast.service.PodcastService
import kotlinx.coroutines.Dispatchers


var podcastService = PodcastService(Dispatchers.IO)
var podcastViewModel = PodcastViewModel(podcastService);
val DEBUG_LOG = "MHR";

class MainActivity : ComponentActivity(), LifecycleOwner {

//    init {
    //val string = resources.getString(dk.mhr.radio8podcast.R.string.api_key);
    //    Log.i("MHR", "Key: " + string)
    //  }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            val player = ExoPlayer.Builder(this).build()

            Log.i(DEBUG_LOG, "Oncreate called we have player: $player")
            PodCastNavHost("WearApp", this, this, player)
        }

    }
}


@Composable
fun FetchPodcasts(onNavigateToShowPodcasts: () -> Unit, onNavigateToSeeDownloadList: () -> Unit) {

    Log.i("MHR", "FetchPodcasts called. Show chip with button action")
    val interactionSource = remember { MutableInteractionSource() }
// Observe changes to the binary state for these interactions
    val isDragged by interactionSource.collectIsDraggedAsState()
    //val isPressed by interactionSource.collectIsPressedAsState()

    val asState: State<Boolean> = interactionSource.collectIsPressedAsState()
    val padding = 6.dp
    // var prut =

//    podcasts.observe(this, Observer {
//
//    })
//    val (text) = when {
//        isPressed -> "fetch podcast" to "load"
//        else ->  podcastService.podcasts.value.orEmpty() to  podcastService.podcasts.value.orEmpty()
//    }

//    val ds = when {
//        interactionSource.collectIsPressedAsState().let { true } -> "sdfd"
//        else -> podcastService.podcasts.value.orEmpty()
//    }
//    val (text) = when {
//        isPressed -> {
//            val pair: Pair<String, String> = "fetch podcast" to "fetch podcast"
//            pair
//        }
//        else -> podcastService.podcasts.value.orEmpty() to podcastService.podcasts.value.orEmpty()
//    }


//    val (text) = when {
//        isPressed -> {
//            val pair: Pair<String, String> = "fetch podcast" to "fetch podcast"
//            pair
//        }
//        else -> podcastService.podcasts.value.orEmpty() to  podcastService.podcasts.value.orEmpty()
//    }

// Use the state to change our UI
//    val (text, color) = when {
//        isPressed -> prut to Color.DarkGray
//        else -> "" to Color.Black
//    }

    //val (text, tex) = ""
//    val (text) = when {
//        interactionSource.collectIsPressedAsState() -> "23"
//        else -> "sdf"
//    }

    Chip(onClick = onNavigateToShowPodcasts,
        colors = ChipDefaults.chipColors(contentColor = MaterialTheme.colors.background),
        label = {
            Text(text = "Undskyld vi roder")
        },
        secondaryLabel = {
            Log.i("MHR", "secondaryLabel->" + podcastViewModel.podcasts.value)
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
        composable("WearApp") {
            WearApp(onNavigateToFetchPodcast = {
                Log.i("MHR", "Calling navigate to ShowPodcasts")
                podcastViewModel.loadPodcast(API_KEY)
                podcastViewModel.podcasts.observe(lifecycleOwner) { t ->
                    podcastViewModel.podcastList = t
                    Log.i("MHR", "Observe called...")
                    navController.navigate("ShowPodcasts") { popUpTo("WearApp") }
                }
            },
                onNavigateToSeeDownloadList = {
                    navController.navigate("SeeDownloads")
                })
        }
        composable("ShowPodcasts") {
            //Log.i(DEBUG_LOG, "backStackEntry: ${it.destination}")
            PodcastListComposable(podcastViewModel).ShowPodcastList(onPodCastDownload = { title, link, audio ->
                Log.i("MHR", "Download Podcast clicked!: $title, Link: $link, audio: $audio")

                val downloadRequest: DownloadRequest =
                    DownloadRequest.Builder(audio, Uri.parse(audio)).build()
                DownloadService.sendAddDownload(
                    context,
                    DemoDownloadService::class.java,
                    downloadRequest,
                    false
                )

//                if (player.isPlaying) {
//                    //player.release()
//
//                }

//                val audioManager: AudioManager =
//                    context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

                // Build the media item.
                // Build the media item.
//                val mediaItem: MediaItem = MediaItem.fromUri(audio)
//                player.setMediaItem(mediaItem)
//                player.prepare()
//                player.play()

            }, lifecycleOwner)
        }
        composable("SeeDownloads") {
            SeeDownloadListComposable().SeeDownloadList()
        }
    }

}

@Composable
fun WearApp(
    onNavigateToFetchPodcast: () -> Unit,
    onNavigateToSeeDownloadList: () -> Unit
) {

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
            Log.i("MHR", "WearApp called")
            //Greeting(greetingName = greetingName)
            FetchPodcasts(onNavigateToFetchPodcast, onNavigateToSeeDownloadList)
        }

    }
}
