package dk.mhr.radio8podcast.presentation

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import com.google.android.exoplayer2.Player
import com.google.android.exoplayer2.Player.Events
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import kotlinx.coroutines.delay
import java.lang.StringBuilder

class PodcastPlayerComposable(private val player: ExoPlayer) {

    fun formatLength(totalSecs: Long): String {
        val hours = totalSecs/1000 / 3600;
        val minutes = (totalSecs/1000 % 3600) / 60;
        val seconds = totalSecs/1000 % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

    private fun startPlay(audio: String?, mediaItem: MediaItem, events: () -> Unit) {
        Log.i("MHR", "Preparing player: " + audio.toString());
        Log.i(
            "MHR",
            "hasNextMediaItem: " + player.hasNextMediaItem() + ", hasPreviousMediaItem: " + player.hasPreviousMediaItem()
        )
        Log.i("MHR", "MediaItemCount: " + player.mediaItemCount)

        if (player.mediaItemCount == 0) {
            player.setMediaItem(mediaItem)
            player.prepare()
            Log.i("MHR", "Start play some podcast")
            player.addListener(PlayerEventLister {
                events
            })

        }

        Log.i("MHR", "CurrentMediaItemId" + player.currentMediaItem?.mediaId + ", contentPosition: " + player.contentPosition)

        player.play()
        player.setPlaybackSpeed(1.0f)
    }

    private fun stopPlay() {
        player.pause()
    }

    @Composable
    fun showPlayer(audio: String?, title: String?) {

        var checked by remember { mutableStateOf(true) }
        Log.i("MHR", "What state remembered: $checked")

        val mediaItem: MediaItem = MediaItem.fromUri(audio.toString())

        var contentPositionString by remember { mutableStateOf( "")}
        var duration by remember { mutableStateOf("0") }
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
            while(true) {
                //contentPositionc.con.clear()
                //contentPositionc.con.append(player.contentPosition.toString())
                //contentPositionc = contentPositionc.copy(con = StringBuilder(player.contentPosition.toString()))
                contentPositionString = formatLength(totalSecs = player.contentPosition)

                if (player.isPlaying) {
                    duration = formatLength(player.duration)
                }
                Log.i("MHR", "Am I called here??$contentPositionString")
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
                            "MHR",
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
                Text(contentPositionString + " : " + duration)
                //Text(text = "Time: ")
                Spacer(Modifier.size(padding))
                Row {
                Button(onClick = {
                    Log.i("MHR", "IncreaseVol called")
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
                    Log.i("MHR", "DecreaseVol called")
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

//    fun onPlayButtonClick() {
//        Log.i("MHR", "onPlayButtonClick called!!!")
    //return onPlayButtonClick()
//        checked = !checked
//        Log.i(
//            "MHR",
//            "Checked: $checked" + ", hasNextMediaItem: " + player.hasNextMediaItem()
//        )
//        if (checked) {
//            Log.i("MHR", "Preparing player: " + audio.toString());
//            player.setMediaItem(mediaItem)
//            player.prepare()
//            Log.i("MHR", "Start play some podcast")
//            player.play()
//            player.addListener(PlayerEventLister())
//        } else {
//            player.pause()
//        }
    //}

    class PlayerEventLister(val eventHappened:(k: Int) -> Unit ) : Player.Listener {
        override fun onEvents(player: Player, events: Player.Events) {
            (0 until events.size()).forEach {
                Log.i("MHR", "onEvents called: $player Event: ${events.get(it)}")
                eventHappened(events.get(it))
            }
        }
    }

    data class ContentPositionClass(var con: String)

}