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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
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

    @Composable
    fun showPlayer(title: String?) {

        var checked by remember { mutableStateOf(true) }
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

        LaunchedEffect(Unit) {
            while (true) {
                contentPositionString =
                    podcastViewModel.formatLength(totalSecs = player.contentPosition)
                //save duration to state
                delay(1000)

                if (!player.isLoading) {
                    durationString = podcastViewModel.formatLength(player.duration)
                }
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
                Spacer(Modifier.size(2.dp))
                Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.padding(10.dp, 0.dp)) {
                    Text(text = title!!, overflow = TextOverflow.Ellipsis, softWrap = true, maxLines = 3, fontSize = 12.sp)
                }

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