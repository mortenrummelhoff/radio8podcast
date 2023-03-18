package dk.mhr.radio8podcast.presentation


import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.offline.Download
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.data.PodcastEntity
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PodcastPlayerComposable(private val player: ExoPlayer) {


    private fun startPlay(audio: String?, mediaItem: MediaItem?, events: () -> Unit) {
        Log.i(DEBUG_LOG, "Preparing player: " + audio.toString());
        Log.i(
            DEBUG_LOG,
            "hasNextMediaItem: " + player.hasNextMediaItem() + ", hasPreviousMediaItem: " + player.hasPreviousMediaItem()
        )

        var startP = 0L

        podcastViewModel.viewModelScope.launch {
            mediaItem?.let {
                withContext(Dispatchers.IO) {
                    val podcastEntity = podcastViewModel.podcastDao.findByUrl(it.mediaId)
                    Log.i(DEBUG_LOG, "Has database any data: $podcastEntity")

                    if (podcastEntity == null) {
                        Log.i(DEBUG_LOG, "No entry in db for mediaId: ${mediaItem.mediaId}")
                        val newPodcastEntity = PodcastEntity(0, mediaItem.mediaId, 0)
                        podcastViewModel.podcastDao.insertPodcast(newPodcastEntity)
                    } else {
                        startP = podcastEntity.startPosition!!
                    }
                }.let {
                    if (player.mediaItemCount == 0) {
                        mediaItem?.let { player.setMediaItem(it, startP) }
                        player.prepare()
                        Log.i(DEBUG_LOG, "Start play some podcast")
                    } else {
                        Log.i(DEBUG_LOG, "player already has media loaded: " + player.currentMediaItem?.mediaId)
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
                        "CurrentMediaItemId" + player.currentMediaItem?.mediaId + ", contentPosition: " + player.contentPosition
                    )

                    player.play()
                }
            }
        }


        //player.setPlaybackSpeed(1.0f)
    }

    private fun stopPlay() {
        player.pause()
        val currentPosition:Long = player.currentPosition
        val currentMediaItem = player.currentMediaItem ?: return
        podcastViewModel.viewModelScope.launch {
            withContext(Dispatchers.IO){
                val podcastEntity = podcastViewModel.podcastDao.findByUrl(currentMediaItem.mediaId)
                if (podcastEntity != null) {
                    val updatedPodcastEntity = podcastEntity.copy(podcastEntity.uid, podcastEntity.url, currentPosition)
                    podcastViewModel.podcastDao.updatePodcast(updatedPodcastEntity)
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
        val mediaItem = download?.request?.toMediaItem()

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
                contentPositionString = podcastViewModel.formatLength(totalSecs = player.contentPosition)
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
                            startPlay(audio, mediaItem, events = {

                            })
                        } else {
                            stopPlay()
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
                Text(title.toString(), softWrap = true, maxLines = 2)
                if (durationString.isEmpty()) {
                    Text(contentPositionString)
                } else {
                    Text("$contentPositionString --$durationString")
                }
                //Text(text = "Time: ")
                Spacer(Modifier.size(padding))
                Row {
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
                }
            }
        }
    }


    class PlayerEventLister(val eventHappened: (k: Int) -> Unit) : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            (0 until events.size()).forEach {
                Log.i("MHR", "onEvents called: $player Event: ${events.get(it)}")
                eventHappened(events.get(it))
            }
        }
    }

    data class ContentPositionClass(var con: String)


    data class PlayableDownload(val download: Download, val startPosition: Int)

}