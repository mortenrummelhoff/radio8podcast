package dk.mhr.radio8podcast.presentation

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.offline.Download
import androidx.media3.exoplayer.offline.DownloadIndex
import androidx.media3.session.MediaController
import androidx.media3.session.MediaSession
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dk.mhr.radio8podcast.data.PodcastEntity
import dk.mhr.radio8podcast.data.PodcastRepository
import dk.mhr.radio8podcast.service.BluetoothStateMonitor
import dk.mhr.radio8podcast.service.PlayerForegroundWorker
import dk.mhr.radio8podcast.service.PodcastService
import dk.mhr.radio8podcast.service.PodcastUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject

class PodcastViewModel(private val podcastService: PodcastService) : ViewModel() {

    init {
        Log.i("MHR", "PodcastViewModel initialized")

    }

    lateinit var bluetoothStateMonitor: BluetoothStateMonitor
    lateinit var controllerFuture: ListenableFuture<MediaController>
    val controller: MediaController? get() = if (controllerFuture.isDone) controllerFuture.get() else null


//    lateinit var exoPlayer: ExoPlayer
    //var player: MediaController? = null
    lateinit var audioManager: AudioManager
    var focusRequest: AudioFocusRequest? = null
    var LIFECYCLEOWNER: LifecycleOwner? = null
    //var session: MediaSession? = null

    //var player: Player? = null

    //lateinit var podcastDao:PodcastDao
    lateinit var podcastRepository: PodcastRepository

    val downloadList = mutableStateListOf<DataDownload>()

    var next_episode_pub_date = ""
    val podcastByIdList = mutableStateListOf<JSONObject>()

    var loadingState = false

    val playerIsPlaying = mutableStateOf(false)

    fun formatLength(totalSecs: Long): String {
        val hours = totalSecs / 1000 / 3600;
        val minutes = (totalSecs / 1000 % 3600) / 60;
        val seconds = totalSecs / 1000 % 60;

        return if (hours == 0L && minutes == 0L)
            String.format("%02d", seconds) else if (hours == 0L)
            String.format("%02d:%02d", minutes, seconds) else
            String.format("%2d:%02d:%02d", hours, minutes, seconds)

    }

    fun fetchDownloadList(downloadIndex: DownloadIndex) {
        Log.i(DEBUG_LOG, "fetchingDownloadList" + downloadIndex.getDownloads().count);
        viewModelScope.launch {
            downloadList.clear()
            downloadList.addAll(
                podcastService.fetchDownloadPodcastList(
                    podcastRepository.podcastDao,
                    downloadIndex
                )
            )
            Log.i(DEBUG_LOG, "fetchingDownloadList done");
        }
    }

    fun fetchPodcastById(API_KEY: String) {
        Log.i(
            DEBUG_LOG,
            "fetchPodcastById called!!!, next_episode_pub_date: $next_episode_pub_date"
        )

        viewModelScope.launch {

            val podcastJSONObject =
                podcastService.fetchPodcastById(API_KEY, "", next_episode_pub_date)
            //podcastsById.value = podcastJSONObject


            //val podcastByIdList: MutableList<JSONObject> = ArrayList()
            val jsonObject = JSONObject(podcastJSONObject)
            next_episode_pub_date = jsonObject.get("next_episode_pub_date").toString()

            val jsonArray = jsonObject.getJSONArray("episodes")

            (0 until jsonArray.length()).forEach {
                podcastByIdList.add(jsonArray.getJSONObject(it))
            }
            loadingState = false
        }
    }

    class PlayerEventListerUpdated(val context: Context) : Player.Listener {

        override fun onIsPlayingChanged(isPlaying: Boolean) {
            Log.i(DEBUG_LOG, "onIsPlayingChanged: $isPlaying")
            podcastViewModel.playerIsPlaying?.value = isPlaying
            super.onIsPlayingChanged(isPlaying)
        }

        override fun onPlaybackStateChanged(playbackState: Int) {
            Log.i(DEBUG_LOG, "onPlaybackStateChanged: $playbackState")
            super.onPlaybackStateChanged(playbackState)
        }

        override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
            Log.i(DEBUG_LOG, "onPlaybackParametersChanged: $playbackParameters")
            super.onPlaybackParametersChanged(playbackParameters)
        }

        override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
            Log.i(DEBUG_LOG, "onAudioAttributesChanged: $audioAttributes")
            super.onAudioAttributesChanged(audioAttributes)
        }

        override fun onEvents(player: Player, events: Player.Events) {
            (0 until events.size()).forEach {
                Log.i(DEBUG_LOG, "onEvents called: $player Event: ${events.get(it)}")

                if (Player.EVENT_IS_PLAYING_CHANGED == events.get(it)) {
                    if (!player.isPlaying) {
                        Log.i(DEBUG_LOG, "Stop player event. Stopping")
                        podcastViewModel.stopPlayEvent()
                    } else {
                        val cMediaItem = player.currentMediaItem
                        podcastViewModel.viewModelScope.launch {
                            withContext(Dispatchers.IO) {
                                podcastViewModel.podcastRepository.podcastDao.findCurrentPlaying()
                                    .let { podcastEntity ->
                                        Log.i(
                                            DEBUG_LOG,
                                            "Found CurrentlyPlaying: $podcastEntity"
                                        )
                                        if (podcastEntity != null) {
                                            podcastViewModel.podcastRepository.podcastDao.updatePodcast(
                                                podcastEntity.copy(
                                                    currentPlaying = false
                                                )
                                            )
                                        }
                                    }

                                Log.i(
                                    DEBUG_LOG, "Trying setting podcastEntry to currentPlaying: " +
                                            cMediaItem?.mediaId
                                )
                                val podcastEntity =
                                    podcastViewModel.podcastRepository.podcastDao.findByUrl(cMediaItem?.mediaId!!)
                                if (podcastEntity != null) {
                                    Log.i(DEBUG_LOG, "Found podcastEntry: $podcastEntity")
                                    val updatedPodcastEntity =
                                        podcastEntity.copy(currentPlaying = true)
                                    podcastViewModel.podcastRepository.podcastDao.updatePodcast(
                                        updatedPodcastEntity
                                    )
                                }
                            }
                        }
                        Log.i(DEBUG_LOG, "Player started. Create and start PlayerWorkRequest!")

                        val playerWorkRequest: WorkRequest =
                            OneTimeWorkRequestBuilder<PlayerForegroundWorker>().setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                                .build()
                        WorkManager.getInstance(context).enqueue(playerWorkRequest)

                    }
                }
            }
        }
    }

//    val onStartAction: () -> Unit = {
//        Log.i(DEBUG_LOG, "onClick called from compose. And here we have podcastViewMode")
//    }


    private fun stopPlayEvent() {
        val currentPosition: Long? = controller?.currentPosition
        val currentMediaItem = controller?.currentMediaItem ?: return
        podcastViewModel.viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val podcastEntity =
                    podcastViewModel.podcastRepository.podcastDao.findByUrl(currentMediaItem.mediaId)
                if (podcastEntity != null) {
                    val updatedPodcastEntity = podcastEntity.copy(startPosition = currentPosition)
                    Log.i(DEBUG_LOG, "Updating currentPosition into DAO: $updatedPodcastEntity")
                    podcastViewModel.podcastRepository.podcastDao.updatePodcast(updatedPodcastEntity)
                }
            }
        }
    }

    data class DataDownload(val download: MutableState<Download>, val startPosition: Long)


    class PodcastAudioFocusChange: OnAudioFocusChangeListener {
        override fun onAudioFocusChange(p0: Int) {
            Log.i(DEBUG_LOG, "onAudioFocusChange called: $p0")
        }
    }

}