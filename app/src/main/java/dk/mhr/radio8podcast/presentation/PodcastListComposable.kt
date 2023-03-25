package dk.mhr.radio8podcast.presentation


import android.util.Log
import android.view.LayoutInflater
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.*

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.wear.compose.material.*

import androidx.wear.compose.material.ChipDefaults
import coil.compose.AsyncImagePainter
import coil.compose.AsyncImagePainter.State.Empty.painter
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
        val API_KEY = stringResource(R.string.api_key)
//        var loadingState by remember{ mutableStateOf(podcastViewModel.loadingState)}

        val padding = 6.dp
        Radio8podcastTheme {

            if (podcastViewModel.podcastByIdList.isEmpty()) {
                Log.i(DEBUG_LOG, "loading state: $podcastViewModel.loadingState")
                if (!podcastViewModel.loadingState) {
                    podcastViewModel.loadingState = true
                    podcastViewModel.fetchPodcastById(API_KEY)
                }

                Column(
                    Modifier.fillMaxSize().padding(20.dp, 30.dp, 20.dp, 30.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally

                ) {
                    Text("LOADING!!!")
                }
            } else {
//                val podcastByIdList: MutableList<JSONObject> = ArrayList()
//                val jsonObject = JSONObject(podcastViewModel.podcastsById.value)
//                jsonArray = jsonObject.getJSONArray("episodes")
//
//                (0 until jsonArray.length()).forEach {
//                    podcastByIdList.add(jsonArray.getJSONObject(it))
//                }
                val listState = rememberLazyListState()

                val scalingListState = rememberScalingLazyListState()
                val loadNextPage by remember {
                    derivedStateOf {
                        Log.i(DEBUG_LOG, "ShowButton: " + scalingListState.centerItemIndex)
                        scalingListState.centerItemIndex >= podcastViewModel.podcastByIdList.size-1
                    }
                }


//                val showButton by remember {
//                    derivedStateOf {
//                        Log.i(DEBUG_LOG, "ShowButton: " + listState.firstVisibleItemIndex)
//                        listState.firstVisibleItemIndex >= podcastViewModel.podcastByIdList.size-3
//                    }
//                }
//                Log.i(DEBUG_LOG, "ShowButton: $showButton")


                ScalingLazyColumn(state = scalingListState,
                    modifier = Modifier
                        .captionBarPadding().fillMaxWidth()
                        //.padding(10.dp, 30.dp, 10.dp, 30.dp)
                        .background( MaterialTheme.colors.background)
                        ,
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally,
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),



                    ) {
                    itemsIndexed(podcastViewModel.podcastByIdList) { index, podcast ->
//                    (0 until jsonArray.length()).forEach {
//                        val record = jsonArray.getJSONObject(it)
                        Spacer(Modifier.size(padding))
                        Row(
//                            Modifier.animateItemPlacement(
//                                tween(durationMillis = 250, delayMillis = 100)
//                            )
                        ) {

                            TitleCard(
                                //contentColor = Color.Transparent.copy(alpha = 0.5f),
                                backgroundPainter =
//                                CardDefaults.imageBackgroundPainter(
                                CardDefaults.imageWithScrimBackgroundPainter(
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
                                        fontSize = 12.sp, fontWeight = FontWeight.Bold
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
                                        fontSize = 12.sp, color = Color.Black, fontWeight = FontWeight.Bold
                                    )
                                }
                            ) {

                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        fontSize = 12.sp,
                                        text = "${podcast.get("title")}",fontWeight = FontWeight.ExtraBold,
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
                if (loadNextPage) {
                    Log.i(DEBUG_LOG, "ShowText: $loadNextPage. LoadingState: "+ podcastViewModel.loadingState)

                    if(!podcastViewModel.loadingState) {
                        podcastViewModel.loadingState = true;
                        podcastViewModel.fetchPodcastById(API_KEY)
                    }

//                    Column(modifier = Modifier
//                        .captionBarPadding().fillMaxWidth()
//                        //.padding(10.dp, 30.dp, 10.dp, 30.dp)
//                        .background( MaterialTheme.colors.background)
//                        ,
//                        verticalArrangement = Arrangement.Center,
//                        horizontalAlignment = Alignment.CenterHorizontally,) {
//                        Row() {
//                            Text(text = "Fetch more podcasts now!!", fontSize = 16.sp)
//                        }
//                    }

                }
            }
        }
    }

}