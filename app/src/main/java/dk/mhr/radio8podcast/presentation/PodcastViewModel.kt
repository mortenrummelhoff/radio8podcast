package dk.mhr.radio8podcast.presentation

import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.AudioManager.OnAudioFocusChangeListener
import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
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


    lateinit var exoPlayer: ExoPlayer
    var player: MediaController? = null
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

    fun preparePlayer(mediaItem: MediaItem?) {
        Log.i(DEBUG_LOG, "Preparing player for : ${mediaItem?.mediaId}");
        var startP = 0L

        podcastViewModel.viewModelScope.launch {
            mediaItem?.let {
                withContext(Dispatchers.IO) {
                    val podcastEntity = podcastViewModel.podcastRepository.podcastDao.findByUrl(it.mediaId)

                    if (podcastEntity == null) {
                        Log.i( DEBUG_LOG,"Podcast does not exist in db for mediaId: ${mediaItem.mediaId} Creating new")
                        val newPodcastEntity = PodcastEntity(0, mediaItem.mediaId, 0)
                        podcastViewModel.podcastRepository.podcastDao.insertPodcast(newPodcastEntity)
                    } else {
                        Log.i( DEBUG_LOG,"Podcast exist in db for mediaId: ${mediaItem.mediaId} With startPosition: ${podcastEntity.startPosition}")
                        startP = podcastEntity.startPosition!!
                        //startP = 0;
                    }
                }.let {
                    if (player?.mediaItemCount == 0) {
                        mediaItem?.let { player?.setMediaItem(it, startP)
                            player?.prepare()
                            Log.i(DEBUG_LOG, "Start play some podcast")
                        }

                    } else {
                        if (mediaItem?.mediaId.equals(player?.currentMediaItem?.mediaId)) {
                            Log.i(DEBUG_LOG,"Player already loaded with same mediaItem: $mediaItem")
                        } else {
                            Log.i(DEBUG_LOG, "Player loaded with other mediaItem that selected. Load new into player: $mediaItem")
                            mediaItem?.let { player?.setMediaItem(it, startP)
                                player?.prepare()}

                        }
                    }

                    Log.i(
                        DEBUG_LOG,
                        "CurrentMediaItemId " + player?.currentMediaItem?.mediaId + ", contentPosition: " + player?.contentPosition +
                                ", contentLength: " + player?.contentDuration
                    )
                }
            }
        }
    }

//    val onStartAction: () -> Unit = {
//        Log.i(DEBUG_LOG, "onClick called from compose. And here we have podcastViewMode")
//    }


    private fun stopPlayEvent() {
        val currentPosition: Long? = player?.currentPosition
        val currentMediaItem = player?.currentMediaItem ?: return
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


    class PodcastMediaCallback(val context: Context) : MediaSession.Callback {
        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            Log.i(DEBUG_LOG, "playerCommandRequest: " +playerCommand)
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            Log.i(DEBUG_LOG, "onConnect called")
            return super.onConnect(session, controller)
        }

        override fun onDisconnected(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ) {
            Log.i(DEBUG_LOG, "onDisconnected called")
            super.onDisconnected(session, controller)
        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            Log.i(DEBUG_LOG, "onPostConnect called")
            super.onPostConnect(session, controller)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            Log.i(DEBUG_LOG, "onAddMediaItems called!!")
            val download = PodcastUtils.getDownloadManager(context).downloadIndex.getDownload(mediaItems[0].mediaId)
            val updatedMediaItems: MutableList<MediaItem> = mutableListOf()

            if (download != null) {
                updatedMediaItems.add(download.request.toMediaItem())
            }

            //val updatedMediaItems = mediaItems.map { it.buildUpon().setUri(it.mediaId).build() }.toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
            //return super.onAddMediaItems(mediaSession, controller, mediaItems)
        }

        override fun onSetMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>,
            startIndex: Int,
            startPositionMs: Long
        ): ListenableFuture<MediaSession.MediaItemsWithStartPosition> {
            Log.i(DEBUG_LOG, "onSetMediaItems : $mediaItems, StartIndex: $startIndex, size: ${mediaItems.size}" )

            val download = PodcastUtils.getDownloadManager(context).downloadIndex.getDownload(mediaItems[startIndex].mediaId)
            val updatedMediaItems: MutableList<MediaItem> = mutableListOf()

            if (download != null) {
                updatedMediaItems.add(startIndex, download.request.toMediaItem())
            }

            //return Futures.immediateFuture()

            return super.onSetMediaItems(
                mediaSession,
                controller,
                updatedMediaItems,
                startIndex,
                startPositionMs
            )
        }



//        override fun onMediaButtonEvent(mediaButtonIntent: Intent): Boolean {
//            Log.i(DEBUG_LOG, "Somebody hit the mediaButton: $mediaButtonIntent")
//
//            mediaButtonIntent.action
//
//            return true;
//            //return super.onMediaButtonEvent(mediaButtonIntent)
//        }


    }

    class PodcastAudioFocusChange: OnAudioFocusChangeListener {
        override fun onAudioFocusChange(p0: Int) {
            Log.i(DEBUG_LOG, "onAudioFocusChange called: $p0")
        }
    }

}