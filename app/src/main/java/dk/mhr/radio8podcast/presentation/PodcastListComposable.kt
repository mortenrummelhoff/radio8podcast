package dk.mhr.radio8podcast.presentation

import android.media.Image
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material.*
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import dk.mhr.radio8podcast.service.PodcastUtils
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.timerTask

class PodcastListComposable {

    var jsonArray: JSONArray = JSONArray()

    init {
        var lis = podcastViewModel.podcastList
        var jsonObject = JSONObject(lis)
        jsonArray = jsonObject.getJSONArray("results")
    }

    @Composable
    fun ShowPodcastList(
        onPodCastDownload: (title: String, link: String, audio: String) -> Unit,
        lifecycleOwner: LifecycleOwner
    ) {

        val padding = 6.dp
        Radio8podcastTheme {

            Column(
                modifier = Modifier
                    .captionBarPadding().fillMaxWidth().verticalScroll(ScrollState(0))
                    .padding(20.dp, 30.dp, 20.dp, 30.dp)
                    .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,


            ) {
                (0 until jsonArray.length()).forEach {
                    val record = jsonArray.getJSONObject(it)
                    Spacer(Modifier.size(padding))

                    TitleCard(
                        onClick = {
                            onPodCastDownload(
                                "${record.get("title_original")}",
                                "${record.get("link")}",
                                "${record.get("audio")}"
                            )
                        },
                        title = {
                            Text(text = "${record.get("title_original")}", maxLines = 2)
                        },
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(text = podcastViewModel.formatLength(totalSecs = "${record.get("audio_length_sec")}".toLong()*1000))
                            Spacer(modifier = Modifier.size(6.dp))

                            Button(
                                onClick = {},
                                modifier = Modifier.size(24.dp).wrapContentSize(Alignment.Center)
                            ) {
                                Icon(
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp),
                                    //.wrapContentSize(align = Alignment.Center, ),
                                    painter = painterResource(R.drawable.ic_download)
                                )
                            }
                        }
                    }

                }

            }
        }
    }

}