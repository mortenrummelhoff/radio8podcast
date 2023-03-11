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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.navigation.*
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.navigation.AUDIO_URL
import dk.mhr.radio8podcast.presentation.navigation.Screen
import dk.mhr.radio8podcast.presentation.navigation.TITLE
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import dk.mhr.radio8podcast.service.PodcastDownloadService
import dk.mhr.radio8podcast.service.PodcastUtils
import dk.mhr.radio8podcast.service.PodcastService
import kotlinx.coroutines.Dispatchers
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset
import java.util.Base64.Encoder


var podcastService = PodcastService(Dispatchers.IO)
var podcastViewModel = PodcastViewModel(podcastService);
val DEBUG_LOG = "MHR";



class MainActivity : ComponentActivity(), LifecycleOwner {

    //var listenEvent: () -> Unit
    //var player:ExoPlayer

//    init{
//        val player = ExoPlayer.Builder(this).build()
//        val downloadIndex = PodcastUtils.getDownloadManager(this).downloadIndex
//        podcastViewModel.fetchDownloadList(downloadIndex)
//
//        PodcastUtils.getDownloadTracker(this).addListener {
//            podcastViewModel.fetchDownloadList(downloadIndex)
//        }
//    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {

            val player = ExoPlayer.Builder(this).build()
            val downloadIndex = PodcastUtils.getDownloadManager(this).downloadIndex
            //podcastViewModel.fetchDownloadList(downloadIndex)

            PodcastUtils.getDownloadTracker(this).addListener {
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


            Log.i(DEBUG_LOG, "Oncreate called we have player: $player")
            PodCastNavHost("WearApp", this, this, player)
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


    val asState: State<Boolean> = interactionSource.collectIsPressedAsState()


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
                Log.i("MHR", "Calling navigate to ShowPodcasts")
                podcastViewModel.loadPodcast(API_KEY)

                if (!podcastViewModel.podcasts.hasObservers()) {
                    podcastViewModel.podcasts.observe(lifecycleOwner) { t ->
                        podcastViewModel.podcastList = t
                        Log.i("MHR", "Observe called...")
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
                Log.i("MHR", "Download Podcast clicked!: $title, Link: $link, audio: $audio")

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
                PodcastUtils.getDownloadTracker(context),
                PodcastUtils.getDownloadManager(context).downloadIndex,
                lifecycleOwner
            ).SeeDownloadList(onPodCastListen = { audio, title ->
                navController.navigate(
                    route = Screen.PodcastPlayer.route + "/" + URLEncoder.encode(audio, "UTF8") + "/" + URLEncoder.encode(title, "UTF8")
                ) { popUpTo(Screen.Landing.route) }
            })
        }
        composable(
            route = Screen.PodcastPlayer.route + "/{" + AUDIO_URL + "}/{" + TITLE + "}",
            arguments = listOf(navArgument(AUDIO_URL) { NavType.StringType }, navArgument(TITLE) {NavType.StringType})

        ) {
            PodcastPlayerComposable(player).showPlayer(it.arguments?.getString(AUDIO_URL), URLDecoder.decode(it.arguments?.getString(TITLE),"UTF8") )
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
