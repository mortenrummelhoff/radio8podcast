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
import androidx.wear.compose.material.*
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme

class PodcastPlayerComposable(val player: ExoPlayer) {


    @Composable
    fun showPlayer(audio: String?) {

        //Log.i("MHR", "Start play some podcast: " + audio.toString());
        var checked by remember { mutableStateOf(true) }

        //val interactionSource = remember { MutableInteractionSource() }

        val mediaItem: MediaItem = MediaItem.fromUri(audio.toString())

//        player.play()


        val padding = 6.dp

        val (contentString) = when {
            checked -> {
                stringResource(R.string.increateVolume) to stringResource(R.string.increateVolume)
            }
            else ->
                stringResource(R.string.increateVolume) to stringResource(R.string.increateVolume)
        }

        val (playIcon) = when {
            checked -> {
                painterResource(R.drawable.icons_pause) to painterResource(R.drawable.icons_pause)
            } else -> painterResource(R.drawable.icons_play) to painterResource(R.drawable.icons_play)
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

                ToggleButton(
                    checked = checked,
                    onCheckedChange = {
                        checked = it
                        Log.i("MHR", "Checked: $checked" + ", hasNextMediaItem: " + player.hasNextMediaItem())

                        if (checked) {
//                            if (!player.hasNextMediaItem()) {
                                Log.i("MHR", "Preparing player: " + audio.toString());
                                player.setMediaItem(mediaItem)
                                player.prepare()
//                            }
                            Log.i("MHR", "Start play some podcast")
                            player.play()
                        } else {
                            player.pause()
                        }
                    },
                    enabled = true
                ) {
//                    if (checked) {
                    Icon(
                        contentDescription = contentString,
                        modifier = Modifier
                            .size(24.dp)
                            .wrapContentSize(align = Alignment.Center),
                        painter = playIcon
                    )
//                    } else {
//
//                    }

                }

                Text(text = "Speed:" + player.playbackParameters.speed)
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