package dk.mhr.radio8podcast.presentation


import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.FilterQuality
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.media3.common.util.UnstableApi
import androidx.wear.compose.material.*
import coil.compose.rememberAsyncImagePainter
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import java.text.SimpleDateFormat
import java.util.*

@UnstableApi class PodcastListComposable(val context: Context) {


    private fun getDateTime(s: String): String? {
        try {
            val sdf = SimpleDateFormat("dd/MM/yyyy")
            val netDate = Date(s.toLong())
            return sdf.format(netDate)
        } catch (e: Exception) {
            return e.toString()
        }
    }

    fun showMessage(context: Context, message:String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    @OptIn(ExperimentalFoundationApi::class)
    @Composable
    fun ShowPodcastList(
        onPodCastDownload: (title: String, link: String, audio: String) -> Unit,
        lifecycleOwner: LifecycleOwner
    ) {
        val API_KEY = stringResource(R.string.api_key)


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

                val scalingListState = rememberScalingLazyListState()
                val loadNextPage by remember {
                    derivedStateOf {
                        Log.i(DEBUG_LOG, "ShowButton: " + scalingListState.centerItemIndex)
                        scalingListState.centerItemIndex >= podcastViewModel.podcastByIdList.size-1
                    }
                }

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

                        val alreadyDownloaded = (podcastViewModel.downloadList.filter {
                                dataDownload ->
                            Log.i(DEBUG_LOG, "DataDownload id: " + dataDownload.download.value.request.id + ", Title: " + podcast.get("title"))
                                 dataDownload.download.value.request.id.equals(podcast.get("title"))

                        }.isNotEmpty())
                        Log.i(DEBUG_LOG, "Already downloaded: $alreadyDownloaded")

                        Spacer(Modifier.size(padding))
                        Row(
                        ) {

                            TitleCard(
                                backgroundPainter =

                                    CardDefaults.imageWithScrimBackgroundPainter(
                                        backgroundImagePainter = rememberAsyncImagePainter(
                                            podcast.get("thumbnail"),
                                            filterQuality = FilterQuality.None,
                                            contentScale = ContentScale.Inside
                                        ), backgroundImageScrimBrush =
                                        if (alreadyDownloaded) {
                                            Brush.sweepGradient(colors = listOf(
                                                MaterialTheme.colors.background.copy(alpha = 0.9f),
                                                MaterialTheme.colors.background.copy(alpha = 0.8f),
                                                MaterialTheme.colors.background.copy(alpha = 0.9f),
                                            ))
                                        } else {Brush.linearGradient(colors = listOf(
                                                MaterialTheme.colors.background.copy(alpha = 0.6f),
                                                MaterialTheme.colors.background.copy(alpha = 0.5f),
                                            ))
                                        }
                                    )
                                ,
                                enabled = !alreadyDownloaded,
                                onClick = {
                                    if (alreadyDownloaded) {
                                        showMessage(context, "Already downloaded")
                                    } else {
                                        onPodCastDownload(
                                            "${podcast.get("title")}",
                                            "${podcast.get("link")}",
                                            "${podcast.get("audio")}"
                                        )
                                    }
                                },
                                title = {

                                    Text(
                                        text = getDateTime(
                                            podcast.get("pub_date_ms").toString()
                                        ).toString(),
                                        fontSize = 14.sp, fontWeight = FontWeight.Bold
                                    )

                                },
                                time = {
                                    Text(
                                        text =
                                        if (!alreadyDownloaded) {
                                            podcastViewModel.formatLength(
                                                totalSecs = "${
                                                    podcast.get(
                                                        "audio_length_sec"
                                                    )
                                                }".toLong() * 1000
                                            )
                                        } else {
                                                "Downloaded"
                                               },
                                        fontSize = 12.sp, fontWeight = FontWeight.Bold
                                    )
                                }
                            ) {

                                Row(
                                    modifier = Modifier.fillMaxSize(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        fontSize = 16.sp,
                                        text = "${podcast.get("title")}",fontWeight = FontWeight.ExtraBold,
                                        maxLines = 4
                                    )
                                    Spacer(modifier = Modifier.size(6.dp))
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
                }
            }
        }
    }

}