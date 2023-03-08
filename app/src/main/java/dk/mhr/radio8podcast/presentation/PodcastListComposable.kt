package dk.mhr.radio8podcast.presentation

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material.*
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import org.json.JSONArray
import org.json.JSONObject
import kotlin.concurrent.timerTask

class PodcastListComposable : ViewModel() {

    var jsonArray: JSONArray = JSONArray()
    init {
        var lis = podcastViewModel.podcastList
        var jsonObject = JSONObject(lis)
        jsonArray = jsonObject.getJSONArray("results")
        Log.i("MHR", "JsonArray:" + jsonArray.length().toString())
    }

    fun formatLength(totalSecs: Int): String {
        val hours = totalSecs / 3600;
        val minutes = (totalSecs % 3600) / 60;
        val seconds = totalSecs % 60;

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
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
                    .padding(padding)
                    .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Log.i("MHR", "WearApp called")
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
                        contentColor = MaterialTheme.colors.background,
                    ) {
                        Text(text = formatLength(totalSecs = "${record.get("audio_length_sec")}".toInt()))
                    }

                }

            }
        }
    }

}