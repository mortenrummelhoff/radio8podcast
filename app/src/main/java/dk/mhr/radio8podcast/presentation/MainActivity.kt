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
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavType
import androidx.navigation.navArgument
import androidx.wear.compose.material.*
import androidx.wear.compose.navigation.SwipeDismissableNavHost
import androidx.wear.compose.navigation.composable
import androidx.wear.compose.navigation.rememberSwipeDismissableNavController
import coil.compose.rememberAsyncImagePainter
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.offline.DownloadRequest
import com.google.android.exoplayer2.offline.DownloadService
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.data.AppDatabase
import dk.mhr.radio8podcast.data.PodcastEntity
import dk.mhr.radio8podcast.data.PodcastRepository
import dk.mhr.radio8podcast.presentation.navigation.AUDIO_URL
import dk.mhr.radio8podcast.presentation.navigation.DOWNLOAD_ID
import dk.mhr.radio8podcast.presentation.navigation.Screen
import dk.mhr.radio8podcast.presentation.navigation.TITLE
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import dk.mhr.radio8podcast.service.PodcastDownloadService
import dk.mhr.radio8podcast.service.PodcastService
import dk.mhr.radio8podcast.service.PodcastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.Charset

var podcastService = PodcastService(Dispatchers.IO)
var podcastViewModel = PodcastViewModel(podcastService);
val DEBUG_LOG = "MHR";

class MainActivity : ComponentActivity(), LifecycleOwner {

    private val appDatabase by lazy { AppDatabase.getDatabase(this) }
    private val exoPlayer by lazy {
        ExoPlayer.Builder(this).setMediaSourceFactory(
            DefaultMediaSourceFactory(this).setDataSourceFactory(
                PodcastUtils.getDataSourceFactory(
                    this
                )
            )
        ).build()

    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            podcastViewModel.CONTEXT = this
            podcastViewModel.player = exoPlayer
            exoPlayer.removeListener(podcastViewModel.playerEventLister)
            exoPlayer.addListener(podcastViewModel.playerEventLister)
            exoPlayer.experimentalSetOffloadSchedulingEnabled(true)
            exoPlayer.setPlaybackSpeed(1.0f)

            podcastViewModel.podcastRepository = PodcastRepository(appDatabase.podcastDao())
            val downloadIndex = PodcastUtils.getDownloadManager(LocalContext.current).downloadIndex

            PodcastUtils.getDownloadTracker(LocalContext.current).addListener {
                podcastViewModel.fetchDownloadList(downloadIndex)
            }
            Log.i(DEBUG_LOG, "Oncreate called we have player: $exoPlayer")
            PodCastNavHost("WearApp", this, this, exoPlayer)
        }
    }
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
                navController.navigate(Screen.ShowPodcast.route) { popUpTo(Screen.Landing.route) }
            },
                onNavigateToSeeDownloadList = {
                    podcastViewModel.fetchDownloadList(PodcastUtils.getDownloadManager(context).downloadIndex)
                    navController.navigate(Screen.SeeDownloads.route)
                },
                onPodCastListen = { downloadId, title ->
                    Log.i("MHR", "DownloadId: $downloadId")
                    navController.navigate(
                        route = Screen.PodcastPlayer.route + "/" + URLEncoder.encode(
                            downloadId,
                            "UTF8"
                        ) + "/" + URLEncoder.encode(
                            title,
                            "UTF8"
                        )
                    ) { popUpTo(Screen.SeeDownloads.route) }
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
            ).SeeDownloadList(onPodCastListen = { downloadId, title ->
                Log.i("MHR", "DownloadId: $downloadId")
                navController.navigate(
                    route = Screen.PodcastPlayer.route + "/" + URLEncoder.encode(
                        downloadId,
                        "UTF8"
                    ) + "/" + URLEncoder.encode(
                        title,
                        "UTF8"
                    )
                ) { popUpTo(Screen.SeeDownloads.route) }
            }, onPodCastDelete = { download ->
                Log.i("MHR", "Now delete download: ${download.download.value.request.id}")


                PodcastUtils.getDownloadManager(context)
                    .removeDownload(download.download.value.request.id)
            }, context)
        }
        composable(
            route = Screen.PodcastPlayer.route + "/{" + DOWNLOAD_ID + "}/{" + TITLE + "}",
            arguments = listOf(
                navArgument(DOWNLOAD_ID) { NavType.StringType },
                navArgument(TITLE) { NavType.StringType })

        ) {
            val download =
                PodcastUtils.getDownloadManager(LocalContext.current).downloadIndex.getDownload(
                    URLDecoder.decode(
                        it.arguments?.getString(
                            DOWNLOAD_ID
                        ), "UTF8"
                    )
                )
            podcastViewModel.preparePlayer(download?.request?.toMediaItem(), {})
            PodcastPlayerComposable(player).showPlayer(URLDecoder.decode(it.arguments?.getString(TITLE), "UTF8"))
        }
    }

}

@Composable
fun WearApp(
    onNavigateToFetchPodcast: () -> Unit,
    onNavigateToSeeDownloadList: () -> Unit,
    onPodCastListen: (downloadId: String, title: String) -> Unit
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
                .captionBarPadding().fillMaxSize()
                .background(MaterialTheme.colors.background)
                .verticalScroll(rememberScrollState()),
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

            val currentlyPlaying = remember { mutableStateOf<PodcastEntity?>(null) }

            LaunchedEffect(Unit) {
                podcastViewModel.viewModelScope.launch {
                    withContext(Dispatchers.IO) {
                        podcastViewModel.podcastRepository.podcastDao.findCurrentPlaying().let {
                            it.let {
                                currentlyPlaying.value = it
                            }
                        }
                    }
                }
            }

            if (currentlyPlaying.value != null) {
                Spacer(Modifier.size(padding))

                Chip(
                    modifier = Modifier.padding(start = 0.dp),
                    colors = ChipDefaults.chipColors(
                        contentColor = MaterialTheme.colors.onSurface,
                        backgroundColor = MaterialTheme.colors.background
                    ),
                    onClick = {
                        onPodCastListen(
                            currentlyPlaying.value!!.url!!,
                            currentlyPlaying.value!!.url!!
                        )
                    },
                    label = {
                        Text(
                            text = currentlyPlaying.value!!.url!!,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            textAlign = TextAlign.Center
                        )
                    },
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.music_note),
                            contentDescription = "PlayButton",
                            modifier = Modifier.size(ChipDefaults.SmallIconSize)
                                .padding(start = 0.dp)
                                .wrapContentSize(align = Alignment.Center)
                        )
                    }
                )


            }

            //Greeting(greetingName = greetingName)
            //FetchPodcasts(onNavigateToFetchPodcast, onNavigateToSeeDownloadList)
        }

    }
}
