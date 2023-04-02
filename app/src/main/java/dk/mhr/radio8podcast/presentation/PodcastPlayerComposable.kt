package dk.mhr.radio8podcast.presentation


import android.content.Context
import android.media.AudioManager
//import android.media.session.MediaSession
import androidx.media3.session.MediaSession
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
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

class PodcastPlayerComposable(val context:Context) {

    @Composable
    fun showPlayer() {

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
                    podcastViewModel.formatLength(totalSecs = podcastViewModel.player?.contentPosition!!)
                //save duration to state
                delay(1000)

                if (!podcastViewModel.player?.isLoading!!) {
                    durationString = podcastViewModel.formatLength(podcastViewModel.player?.duration!!)
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
                            "Checked: $checked" + ", hasNextMediaItem: " + podcastViewModel.player?.hasNextMediaItem()
                        )
                        if (checked) {

                            val focusLock = Any()

                            var playbackDelayed = false
                            var playbackNowAuthorized = false

                            val res = podcastViewModel.audioManager?.requestAudioFocus(
                                podcastViewModel.focusRequest!!)
                            synchronized(focusLock) {
                                playbackNowAuthorized = when (res) {
                                    AudioManager.AUDIOFOCUS_REQUEST_FAILED -> false
                                    AudioManager.AUDIOFOCUS_REQUEST_GRANTED -> {
                                        Log.i(DEBUG_LOG, "AudioFocus granted. Now I start player")

//                                        podcastViewModel.session = MediaSession(context, "PodcastService").apply {
//                                            setCallback(PodcastViewModel.PodcastMediaCallback())
//                                            setMetadata(MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE, "Her kommer pipi")
//                                                .putString(MediaMetadata.METADATA_KEY_ALBUM_ART_URI, "https://production.listennotes.com/podcasts/star-wars-7x7-the-daily-star-wars-podcast-HN08OoDE7pc-AIg3cZVKCsL.1400x1400.jpg")
//                                                .putString(MediaMetadata.METADATA_KEY_ARTIST, "Artist pipi").build())
//                                            setSessionActivity(PendingIntent.getActivity(context, 0,
//                                                Intent.makeMainActivity(ComponentName(context, MainActivity::class.java)),
//                                                PendingIntent.FLAG_IMMUTABLE))
//                                            //controller.
//
//
//                                            setPlaybackState(PlaybackState.Builder().setState(PlaybackState.STATE_PLAYING, 0, 1f)
//                                            .setActions(
//                                                PlaybackState.ACTION_PLAY
//                                                    or PlaybackState.ACTION_PLAY_PAUSE
//                                                    or PlaybackState.ACTION_SKIP_TO_NEXT
//                                                    or PlaybackState.ACTION_SKIP_TO_PREVIOUS).build())
//                                            isActive = true
//
//                                        }


                                        podcastViewModel.player?.play()

                                        true
                                    }
                                    AudioManager.AUDIOFOCUS_REQUEST_DELAYED -> {
                                        playbackDelayed = true
                                        false
                                    }
                                    else -> false
                                }
                            }



                            //podcastViewModel.session?.controller?.
//                            podcastViewModel.session?.isActive = true
//                            val mediaMetadata = MediaMetadata.Builder().putString(MediaMetadata.METADATA_KEY_TITLE, "Her kommer pipi").build()
//                            podcastViewModel.session?.setMetadata(mediaMetadata)

                            //podcastViewModel.player?.play()

                        } else {
                            podcastViewModel.player?.pause()
                            //podcastViewModel.session?.isActive = false
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
                    podcastViewModel.player?.currentMediaItem?.mediaId?.let { Text(text = it, overflow = TextOverflow.Ellipsis, softWrap = true, maxLines = 3, fontSize = 12.sp) }
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
                        podcastViewModel.player?.seekTo(podcastViewModel.player?.currentPosition!! - 15000)
                    }) {
                        Text("-15s")
                    }
                    Spacer(Modifier.padding(30.dp, 4.dp, 30.dp, 4.dp))
                    Button(onClick = {
                        Log.i(DEBUG_LOG, "+15s")
                        podcastViewModel.player?.seekTo(podcastViewModel.player?.currentPosition!! + 15000)
                    }) {
                        Text("+15s")
                    }
                }
                //Spacer(Modifier.size(padding))
                Row {
                    Button(onClick = {
                        Log.i(DEBUG_LOG, "DecreaseVol called")
                        podcastViewModel.player?.decreaseDeviceVolume()
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
                        podcastViewModel.player?.increaseDeviceVolume()
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