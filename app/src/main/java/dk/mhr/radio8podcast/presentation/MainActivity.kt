package dk.mhr.radio8podcast.presentation

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.work.*
import dk.mhr.radio8podcast.data.AppDatabase
import dk.mhr.radio8podcast.data.PodcastRepository
import dk.mhr.radio8podcast.service.PodcastService
import dk.mhr.radio8podcast.service.PodcastUtils
import dk.mhr.radio8podcast.service.PodcastWorker
import kotlinx.coroutines.Dispatchers

var podcastService = PodcastService(Dispatchers.IO)
var podcastViewModel = PodcastViewModel(podcastService);
val DEBUG_LOG = "MHR";

class MainActivity : ComponentActivity(), LifecycleOwner {

    private val appDatabase by lazy { AppDatabase.getDatabase(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {

            podcastViewModel.audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
            podcastViewModel.focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_MEDIA)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)

                    build()
                })
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(PodcastViewModel.PodcastAudioFocusChange())
                build()
            }

            podcastViewModel.LIFECYCLEOWNER = this
            podcastViewModel.initializePlayer(this)

            podcastViewModel.podcastRepository = PodcastRepository(appDatabase.podcastDao())
            val downloadIndex = PodcastUtils.getDownloadManager(LocalContext.current).downloadIndex

            PodcastUtils.getDownloadTracker(LocalContext.current).addListener {
                podcastViewModel.fetchDownloadList(downloadIndex)
            }

            Log.i(DEBUG_LOG, "Oncreate called we have player: ${podcastViewModel.player}")
            PodcastNavHostComposable().PodCastNavHost("WearApp", this, this)
        }
    }

    override fun onResume() {
        Log.i(DEBUG_LOG, "onResume called!!")
        if (podcastViewModel.player?.isPlaying == false) {
            Log.i(DEBUG_LOG, "Player is not playing and properly released. initialize new player")
            podcastViewModel.initializePlayer(this)
        }

        val findCurrentlyPlayedWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<PodcastWorker>().
        setInputData(workDataOf("" to "")).addTag("find_currently_played").build()
        WorkManager.getInstance(applicationContext).enqueue(findCurrentlyPlayedWorkRequest)


        super.onResume()
    }

    override fun onPause() {
        Log.i(DEBUG_LOG, "onPause called!!")

        if (podcastViewModel.player?.isPlaying == false) {
            Log.i(DEBUG_LOG, "Player is not playing. Release it")
            podcastViewModel.player?.release()
            podcastViewModel.session?.release()
        }
        super.onPause()
    }

    override fun onDestroy() {
        Log.i(DEBUG_LOG, "onDestroy called. Releasing components")
        podcastViewModel.player?.release()
        podcastViewModel.session?.release()
        super.onDestroy()
    }

}
