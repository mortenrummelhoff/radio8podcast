package dk.mhr.radio8podcast.presentation

import android.media.Image
import android.util.Log
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material.*
import coil.compose.AsyncImagePainter
import coil.compose.rememberAsyncImagePainter
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import dk.mhr.radio8podcast.service.PodcastUtils
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.concurrent.timerTask

class PodcastListComposable {

    var jsonArray: JSONArray = JSONArray()

//    init {
//        var lis = podcastViewModel.podcastList
//        var jsonObject = JSONObject(lis)
//        jsonArray = jsonObject.getJSONArray("results")
//    }

    private fun getDateTime(s: String): String? {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val netDate = Date(s.toLong())
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ShowPodcastList(
        onPodCastDownload: (title: String, link: String, audio: String) -> Unit,
        lifecycleOwner: LifecycleOwner
    ) {



        val padding = 6.dp
        Radio8podcastTheme {

            if (podcastViewModel.podcastsById.value.isEmpty()) {
                Log.i(DEBUG_LOG, "podcastsById empty")
                Column(
                    Modifier.fillMaxSize().padding(20.dp, 30.dp, 20.dp, 30.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Text("No podcast by id!!")
                }
            } else {
                val podcastByIdList: MutableList<JSONObject> = ArrayList()
                val jsonObject = JSONObject(podcastViewModel.podcastsById.value)
                jsonArray = jsonObject.getJSONArray("episodes")

                (0 until jsonArray.length()).forEach {
                    podcastByIdList.add(jsonArray.getJSONObject(it))
                }
                val listState = rememberLazyListState()



                val showButton by remember {
                    derivedStateOf {
                        Log.i(DEBUG_LOG, "ShowButton: " + listState.firstVisibleItemIndex)
                        listState.firstVisibleItemIndex >= podcastByIdList.size-3
                    }
                }
                Log.i(DEBUG_LOG, "ShowButton: $showButton")


                LazyColumn(state = listState,
                    modifier = Modifier
                        .captionBarPadding().fillMaxWidth()
                        //.padding(10.dp, 30.dp, 10.dp, 30.dp)
                        .background( MaterialTheme.colors.background)
                        ,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),



                    ) {
                    itemsIndexed(podcastByIdList) {index, podcast ->
//                    (0 until jsonArray.length()).forEach {
//                        val record = jsonArray.getJSONObject(it)
                        Spacer(Modifier.size(padding))
                        Row(
//                            Modifier.animateItemPlacement(
//                                tween(durationMillis = 250, delayMillis = 100)
//                            )
                        ) {

                            TitleCard(
                                backgroundPainter = CardDefaults.imageWithScrimBackgroundPainter(
                                    backgroundImagePainter = rememberAsyncImagePainter(
                                        podcast.get("thumbnail"),
                                        filterQuality = FilterQuality.None,
                                        contentScale = ContentScale.Crop
                                    )
                                ),
                                onClick = {
                                    onPodCastDownload(
                                        "${podcast.get("title")}",
                                        "${podcast.get("link")}",
                                        "${podcast.get("audio")}"
                                    )
                                },
                                title = {

                                    Text(
                                        text = getDateTime(
                                            podcast.get("pub_date_ms").toString()
                                        ).toString(),
                                        fontSize = 12.sp
                                    )

                                },
                                time = {
                                    Text(
                                        text = podcastViewModel.formatLength(
                                            totalSecs = "${
                                                podcast.get(
                                                    "audio_length_sec"
                                                )
                                            }".toLong() * 1000
                                        ),
                                        fontSize = 12.sp, color = Color.Black
                                    )
                                }
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        fontSize = 12.sp,
                                        text = "${podcast.get("title")}",
                                        maxLines = 4
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))

//                            Button(
//                                onClick = {},
//                                modifier = Modifier.size(24.dp)
//                            ) {
//                                Icon(
//                                    contentDescription = null,
//                                    modifier = Modifier.size(20.dp),
//                                    //.wrapContentSize(align = Alignment.Center, ),
//                                    painter = painterResource(R.drawable.ic_download)
//                                )
//                            }
                                }
                            }
                        }
                    }
                }
                if (showButton) {
                    Log.i(DEBUG_LOG, "ShowText: $showButton")
                    Column(modifier = Modifier
                        .captionBarPadding().fillMaxWidth()
                        //.padding(10.dp, 30.dp, 10.dp, 30.dp)
                        .background( MaterialTheme.colors.background)
                        ,
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,) {
                        Row() {
                            Text(text = "Fetch more podcasts now!!", fontSize = 16.sp)
                        }
                    }

                }
            }
        }
    }

}