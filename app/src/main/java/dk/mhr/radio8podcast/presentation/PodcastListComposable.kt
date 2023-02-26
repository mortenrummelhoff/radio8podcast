package dk.mhr.radio8podcast.presentation

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material.Chip
import androidx.wear.compose.material.ChipDefaults
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import org.json.JSONArray
import org.json.JSONObject

class PodcastListComposable(podcastViewModel: PodcastViewModel) : ViewModel() {



    @Composable
    fun ShowPodcastList(lifecycleOwner: LifecycleOwner) {


        var lis = podcastViewModel.podcastList
        var jsonObject = JSONObject(lis)
        var jsonArray: JSONArray = jsonObject.getJSONArray("results")

        Log.i("MHR", "JsonArray:" + jsonArray.length().toString())
        val padding = 4.dp
        Radio8podcastTheme {
            Column(
                modifier = Modifier
                    .captionBarPadding().fillMaxWidth().verticalScroll(ScrollState(0)).padding(padding)
                    .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally

            ) {
                Log.i("MHR", "WearApp called")
                (0 until jsonArray.length()).forEach {
                    val record = jsonArray.getJSONObject(it)

                    Chip(
                        onClick = {},
                        colors = ChipDefaults.chipColors(contentColor = MaterialTheme.colors.background),
                        label = {
                            Text(text = "${record.get("title_original")}")
                        },
                    )
                    Spacer(Modifier.size(padding))
                }

            }
        }
    }

}