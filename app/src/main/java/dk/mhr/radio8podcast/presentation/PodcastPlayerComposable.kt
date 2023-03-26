package dk.mhr.radio8podcast.presentation


import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkRequest
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.EVENT_IS_PLAYING_CHANGED
import com.google.android.exoplayer2.Player.Listener
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.ui.StyledPlayerView.SHOW_BUFFERING_NEVER
import com.google.android.exoplayer2.ui.StyledPlayerView
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.data.PodcastEntity
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import dk.mhr.radio8podcast.service.PlayerWorker
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PodcastPlayerComposable(private val player: ExoPlayer) {


    private fun preparePlayer(audio: String?, mediaItem: MediaItem?, events: () -> Unit) {
        Log.i(DEBUG_LOG, "Preparing player: " + audio.toString());
        Log.i(
            DEBUG_LOG,
            "hasNextMediaItem: " + player.hasNextMediaItem() + ", hasPreviousMediaItem: " + player.hasPreviousMediaItem()
        )

        var startP = 0L

        podcastViewModel.viewModelScope.launch {
            mediaItem?.let {
                withContext(Dispatchers.IO) {
                    val podcastEntity =
                        podcastViewModel.podcastRepository.podcastDao.findByUrl(it.mediaId)
                    Log.i(DEBUG_LOG, "Has database any data: $podcastEntity")

                    if (podcastEntity == null) {
                        Log.i(
                            DEBUG_LOG,
                            "No entry in db for mediaId: ${mediaItem.mediaId} Creating new"
                        )
                        val newPodcastEntity = PodcastEntity(0, mediaItem.mediaId, 0)
                        podcastViewModel.podcastRepository.podcastDao.insertPodcast(newPodcastEntity)
                    } else {
                        startP = podcastEntity.startPosition!!
                    }
                }.let {
                    if (player.mediaItemCount == 0) {
                        mediaItem?.let { player.setMediaItem(it, startP) }
                        player.prepare()
                        Log.i(DEBUG_LOG, "Start play some podcast")
                    } else {
                        Log.i(
                            DEBUG_LOG,
                            "player already has media loaded: " + player.currentMediaItem?.mediaId
                        )
                        if (mediaItem?.mediaId.equals(player.currentMediaItem?.mediaId)) {
                            Log.i(DEBUG_LOG, "Same mediaItem. Just continue playing")
                        } else {
                            Log.i(DEBUG_LOG, "new mediaItem selected. Load new into player")
                            mediaItem?.let { player.setMediaItem(it, startP) }
                            player.prepare()
                            Log.i(DEBUG_LOG, "Start play some podcast")
                        }
                    }

                    Log.i(
                        DEBUG_LOG,
                        "CurrentMediaItemId " + player.currentMediaItem?.mediaId + ", contentPosition: " + player.contentPosition
                    )

                    //player.play()
                }
            }
        }


        //player.setPlaybackSpeed(1.0f)
    }

    private fun stopPlay() {
        //player.pause()
        val currentPosition: Long = player.currentPosition
        val currentMediaItem = player.currentMediaItem ?: return
        podcastViewModel.viewModelScope.launch {
            withContext(Dispatchers.IO) {
                val podcastEntity =
                    podcastViewModel.podcastRepository.podcastDao.findByUrl(currentMediaItem.mediaId)
                if (podcastEntity != null) {
                    //val updatedPodcastEntity = podcastEntity.copy(podcastEntity.uid, podcastEntity.url, currentPosition)
                    val updatedPodcastEntity = podcastEntity.copy(startPosition = currentPosition)
                    Log.i(DEBUG_LOG, "Updating currentPosition into DAO: $updatedPodcastEntity")
                    podcastViewModel.podcastRepository.podcastDao.updatePodcast(updatedPodcastEntity)
                }
            }
        }
    }


    @Composable
    fun showPlayer(audio: String?, title: String?, download: Download?) {

        var checked by remember { mutableStateOf(true) }
        Log.i(DEBUG_LOG, "What state remembered: $checked")

        //var playedDownloadList by rememberSaveable { mutableListOf() }
        //var playedDownloadList by rememberSaveable { mutableStateListOf()}

        //val mediaItem: MediaItem = MediaItem.fromUri(audio.toString())

        if (podcastViewModel.playerEventLister == null) {
            podcastViewModel.playerEventLister =
                PodcastViewModel.PlayerEventLister(eventHappened = { p, it ->
                    Log.i(DEBUG_LOG, "What happened: $it")
                    if (EVENT_IS_PLAYING_CHANGED == it) {
                        if (!player.isPlaying) {
                            stopPlay()
                        } else {

                            podcastViewModel.viewModelScope.launch {
                                withContext(Dispatchers.IO) {
                                    podcastViewModel.podcastRepository.podcastDao.findCurrentPlaying()
                                        .let {podcastEntity ->
                                            Log.i(DEBUG_LOG,
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

                                    Log.i(DEBUG_LOG, "Trying setting podcastEntry to currentPlaying: " +
                                            p.mediaId)
                                    val podcastEntity =
                                        podcastViewModel.podcastRepository.podcastDao.findByUrl(p.mediaId!!)
                                    if (podcastEntity != null) {
                                        Log.i(DEBUG_LOG, "Found podcastEntry: $podcastEntity")
                                        val updatedPodcastEntity = podcastEntity.copy(currentPlaying = true)
                                        podcastViewModel.podcastRepository.podcastDao.updatePodcast(
                                            updatedPodcastEntity
                                        )
                                    }
                                }
                            }.let {
                                val playerWorkRequest: WorkRequest = OneTimeWorkRequestBuilder<PlayerWorker>().build()
                                WorkManager.getInstance(podcastViewModel.CONTEXT!!).enqueue(playerWorkRequest)
                            }
                        }
                    }
                })
            player.addListener(podcastViewModel.playerEventLister!!)
        }

        val mediaItem = download?.request?.toMediaItem()
        preparePlayer(audio, mediaItem, events = {

        })
        var contentPositionString by remember { mutableStateOf("") }
        var durationString by remember { mutableStateOf("") }
        val padding = 6.dp


        val (contentString) = when {
            checked -> stringResource(R.string.increateVolume) to stringResource(R.string.increateVolume)
            else -> stringResource(R.string.increateVolume) to stringResource(R.string.increateVolume)
        }

        val (playIcon) = when {
            checked -> painterResource(R.drawable.icons_pause) to painterResource(R.drawable.icons_pause)
            else -> painterResource(R.drawable.icons_play) to painterResource(R.drawable.icons_play)
        }

        //var contentPosition = player.contentPosition

        LaunchedEffect(Unit) {
            while (true) {
                contentPositionString =
                    podcastViewModel.formatLength(totalSecs = player.contentPosition)
                if (player.isPlaying) {
                    durationString = podcastViewModel.formatLength(player.duration)
                }
                //save duration to state
                delay(1000)
            }
        }


        Radio8podcastTheme {
            Column(
                modifier = Modifier
                    .captionBarPadding().fillMaxWidth().verticalScroll(ScrollState(0))
                    .padding(padding)
                    .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {

                Button(
                    onClick =
                    {
                        checked = !checked
                        Log.i(
                            DEBUG_LOG,
                            "Checked: $checked" + ", hasNextMediaItem: " + player.hasNextMediaItem()
                        )
                        if (checked) {
                            player.play()

                        } else {
                            player.pause()
//                            stopPlay()
                        }

                    },
                    enabled = true
                ) {
                    Icon(
                        contentDescription = contentString,
                        modifier = Modifier
                            .size(24.dp)
                            .wrapContentSize(align = Alignment.Center),
                        painter = playIcon
                    )
                }
                Spacer(Modifier.size(padding))
                Text(text = title!!, softWrap = true, maxLines = 2, fontSize = 12.sp)
                if (durationString.isEmpty()) {
                    Text(contentPositionString)
                } else {
                    Text("$contentPositionString --$durationString")
                }
                //Text(text = "Time: ")

                Spacer(Modifier.size(2.dp))
                Row {
                    Button(onClick = {
                        Log.i(DEBUG_LOG, "-15s")
                        player.seekTo(player.currentPosition - 15000)
                    }) {
                        Text("-15s")
                    }
                    Spacer(Modifier.padding(30.dp, 4.dp, 30.dp, 4.dp))
                    Button(onClick = {
                        Log.i(DEBUG_LOG, "+15s")
                        player.seekTo(player.currentPosition + 15000)
                    }) {
                        Text("+15s")
                    }
                }
                //Spacer(Modifier.size(padding))
                Row {
                    Button(onClick = {
                        Log.i(DEBUG_LOG, "DecreaseVol called")
                        player.decreaseDeviceVolume()
                    }) {
                        Icon(
                            contentDescription = stringResource(R.string.decreateVolume),
                            modifier = Modifier
                                .size(24.dp)
                                .wrapContentSize(align = Alignment.Center),
                            painter = painterResource(R.drawable.audio_decrease_level_sound_volume_icon)
                        )
                    }
                    Spacer(Modifier.padding(4.dp, 0.dp, 0.dp, 4.dp))
                    Button(onClick = {
                        Log.i(DEBUG_LOG, "IncreaseVol called")
                        player.increaseDeviceVolume()
                    }) {
                        Icon(
                            contentDescription = stringResource(R.string.increateVolume),
                            modifier = Modifier
                                .size(24.dp)
                                .wrapContentSize(align = Alignment.Center),
                            painter = painterResource(R.drawable.audio_increase_level_sound_volume_icon)
                        )
                    }
                }
            }
        }
    }
}