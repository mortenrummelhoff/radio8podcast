package dk.mhr.radio8podcast.presentation

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.content.ComponentName
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import androidx.work.*
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import dk.mhr.radio8podcast.data.AppDatabase
import dk.mhr.radio8podcast.data.PodcastRepository
import dk.mhr.radio8podcast.service.*
import kotlinx.coroutines.Dispatchers

var podcastService = PodcastService(Dispatchers.IO)
var podcastViewModel = PodcastViewModel(podcastService);
val DEBUG_LOG = "MHR";

@UnstableApi class MainActivity : ComponentActivity(), LifecycleOwner {


    private val appDatabase by lazy { AppDatabase.getDatabase(this) }
    //private lateinit var controllerFuture: ListenableFuture<MediaController>
    val playerEventLister = PodcastViewModel.PlayerEventListerUpdated(this)
    val focusChangeListener = PodcastViewModel.PodcastAudioFocusChange()
    val bluetoothStateMonitor = BluetoothStateMonitor(this)
//    val bluetoothManager = this.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
    //val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
    override fun onCreate(savedInstanceState: Bundle?) {
        podcastViewModel.bluetoothStateMonitor = bluetoothStateMonitor
        //installSplashScreen()
        super.onCreate(savedInstanceState)
        //val bluetoothManager = this.getSystemService(BLUETOOTH_SERVICE) as BluetoothManager
        val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
        podcastViewModel.audioManager = audioManager
        setContent {
            Toast.makeText(this, "Starting", Toast.LENGTH_SHORT).show()
            podcastViewModel.focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).run {
                setAudioAttributes(AudioAttributes.Builder().run {
                    setUsage(AudioAttributes.USAGE_MEDIA)
                    setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    build()
                })
                setAcceptsDelayedFocusGain(true)
                setOnAudioFocusChangeListener(focusChangeListener)
                build()
            }

            podcastViewModel.LIFECYCLEOWNER = this
            podcastViewModel.podcastRepository = PodcastRepository(appDatabase.podcastDao())
            val downloadIndex = PodcastUtils.getDownloadManager(LocalContext.current).downloadIndex

            PodcastUtils.getDownloadTracker(LocalContext.current).addListener {
                podcastViewModel.fetchDownloadList(downloadIndex)
            }

            //setTheme(android.R.style.Theme_DeviceDefault)
            PodcastNavHostComposable().PodCastNavHost("WearApp", this, this)
        }
    }

    override fun onStart() {
        super.onStart()
        bluetoothStateMonitor.start()

        podcastViewModel.controllerFuture =
            MediaController.Builder(
                this,
                SessionToken(this, ComponentName(this, PodcastPlayerService::class.java))
            )
                .buildAsync()
        podcastViewModel.controllerFuture.addListener({ setController() }, MoreExecutors.directExecutor())


//        Log.i(DEBUG_LOG, "onStart called!!!!!!")
//        val sessionToken = SessionToken(this, ComponentName(this, PodcastPlayerService::class.java))
//        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()
//        controllerFuture.addListener(
//            {
//                val mediaController = controllerFuture.get()
//
//                    podcastViewModel.player = mediaController
//                    mediaController.removeListener(playerEventLister)
//                    mediaController.addListener(playerEventLister)
//                    //mediaController.setPlaybackSpeed(1.0f)
//
//            },
//            MoreExecutors.directExecutor()
//        )
    }

    private fun setController() {
        val controller = podcastViewModel.controller ?: return

        controller.removeListener(playerEventLister)
        controller.addListener(playerEventLister)
    }

    override fun onResume() {
        Log.i(DEBUG_LOG, "onResume called!!")

        val findCurrentlyPlayedWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<PodcastWorker>().
        setInputData(workDataOf("" to "")).addTag("find_currently_played").build()
        WorkManager.getInstance(applicationContext).enqueue(findCurrentlyPlayedWorkRequest)
        super.onResume()
    }

    override fun onPause() {
        Log.i(DEBUG_LOG, "onPause called!!")
        super.onPause()
    }

    override fun onStop() {
        super.onStop()
        MediaController.releaseFuture(podcastViewModel.controllerFuture)
        bluetoothStateMonitor.stop()
    }

    override fun onDestroy() {
        Log.i(DEBUG_LOG, "onDestroy called. Releasing components")
//        podcastViewModel.player?.release()
//        podcastViewModel.session?.release()
        super.onDestroy()
    }

}
