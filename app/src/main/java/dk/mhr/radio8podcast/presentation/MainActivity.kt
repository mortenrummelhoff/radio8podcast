package dk.mhr.radio8podcast.presentation

//import androidx.compose.foundation.interaction.collectIsPressedAsState

import android.content.Context
import android.media.session.MediaSession
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
import dk.mhr.radio8podcast.service.PlayerWorker
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

            podcastViewModel.LIFECYCLEOWNER = this
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
            PodcastNavHostComposable().PodCastNavHost("WearApp", this, this, exoPlayer)
        }
    }



    override fun onDestroy() {
        Log.i(DEBUG_LOG, "onDestroy called. Releasing components")
        //if ()

        //exoPlayer.stop()
        //exoPlayer.release()
        //podcastViewModel.session?.release()
        super.onDestroy()
    }

}
