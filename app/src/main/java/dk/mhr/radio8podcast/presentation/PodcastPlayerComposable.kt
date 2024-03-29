package dk.mhr.radio8podcast.presentation


import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioManager
//import android.media.session.MediaSession
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import kotlinx.coroutines.delay
import org.json.JSONObject

class PodcastPlayerComposable(val context: Context) {

    @SuppressLint("UnsafeOptInUsageError")
    @Composable
    fun showPlayer() {

        var contentPositionString by remember { mutableStateOf("") }
        var durationString by remember { mutableStateOf("") }
        val padding = 6.dp

        val (contentString) = when {
            podcastViewModel.playerIsPlaying.value -> stringResource(R.string.increateVolume) to stringResource(R.string.increateVolume)
            else -> stringResource(R.string.increateVolume) to stringResource(R.string.increateVolume)
        }

        val (playIcon) = when {
            podcastViewModel.playerIsPlaying.value -> painterResource(R.drawable.icons_pause) to painterResource(R.drawable.icons_pause)
            else -> painterResource(R.drawable.icons_play) to painterResource(R.drawable.icons_play)
        }

        LaunchedEffect(Unit) {
            //TODO: this cannot be good :)
            while (true) {
                contentPositionString =
                    podcastViewModel.formatLength(totalSecs = podcastViewModel.controller?.contentPosition!!)
                //save duration to state
                delay(1000)

                if (!podcastViewModel.controller?.isLoading!!) {
                    durationString =
                        podcastViewModel.formatLength(podcastViewModel.controller?.duration!!)
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
                        //checked = !checked
                        Log.i(
                            DEBUG_LOG,
                            "Checked: ${podcastViewModel.playerIsPlaying?.value!!}" + ", hasNextMediaItem: " + podcastViewModel.controller?.hasNextMediaItem()
                        )
                        if (!podcastViewModel.playerIsPlaying?.value!!) {

                            val focusLock = Any()

                            var playbackDelayed = false
                            var playbackNowAuthorized = false
                            //TODO: refactor this. Should not be handled inside composable ui
                            val res =
                                podcastViewModel.audioManager.requestAudioFocus(podcastViewModel.focusRequest!!)
                            synchronized(focusLock) {
                                playbackNowAuthorized = when (res) {
                                    AudioManager.AUDIOFOCUS_REQUEST_FAILED -> {
                                        Log.i(
                                            DEBUG_LOG,
                                            "AudioFocus request AUDIOFOCUS_REQUEST_FAILED"
                                        )
                                        false
                                    }
                                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                                        Log.i(
                                            DEBUG_LOG,
                                            "AudioFocus granted. AUDIOFOCUS_REQUEST_GRANTED"
                                        )
                                        true
                                    }
                                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                                        Log.i(
                                            DEBUG_LOG,
                                            "AudioFocus request delayed granted. AUDIOFOCUS_REQUEST_DELAYED"
                                        )
                                        playbackDelayed = true
                                        true
                                    }
                                    else -> false
                                }
                            }
                            if (playbackNowAuthorized) {
                                Log.i(DEBUG_LOG, "playbackNowAuthorized. Start player")
                                Toast.makeText(context, "Headset connected: " + podcastViewModel.bluetoothStateMonitor.isHeadsetConnected,
                                    Toast.LENGTH_SHORT).show()
                                podcastViewModel.controller?.play()
                            }

                        } else {
                            podcastViewModel.audioManager.abandonAudioFocusRequest(podcastViewModel.focusRequest!!)
                            podcastViewModel.controller?.pause()
                        }

                    }
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
                Row(
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.padding(10.dp, 0.dp)
                ) {
                    podcastViewModel.controller?.currentMediaItem?.mediaId?.let {
                        Text(
                            text = it,
                            overflow = TextOverflow.Ellipsis,
                            softWrap = true,
                            maxLines = 3,
                            fontSize = 12.sp
                        )
                    }
                }

                if (durationString.isEmpty()) {
                    Text(contentPositionString)
                } else {
                    Text("$contentPositionString --$durationString")
                }
                Spacer(Modifier.size(2.dp))
                Row {
                    Button(onClick = {
                        Log.i(DEBUG_LOG, "-15s")
                        podcastViewModel.controller?.seekTo(podcastViewModel.controller?.currentPosition!! - 15000)
                    }) {
                        Text("-15s")
                    }
                    Spacer(Modifier.padding(30.dp, 4.dp, 30.dp, 4.dp))
                    Button(onClick = {
                        Log.i(DEBUG_LOG, "+15s")
                        podcastViewModel.controller?.seekTo(podcastViewModel.controller?.currentPosition!! + 15000)
                    }) {
                        Text("+15s")
                    }
                }
                Row {
                    Button(onClick = {
                        Log.i(DEBUG_LOG, "DecreaseVol called")
                        podcastViewModel.controller?.decreaseDeviceVolume()
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
                        podcastViewModel.controller?.increaseDeviceVolume()
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
