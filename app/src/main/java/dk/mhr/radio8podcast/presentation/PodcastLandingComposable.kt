package dk.mhr.radio8podcast.presentation

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewModelScope
import androidx.wear.compose.material.*
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.data.PodcastEntity
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class PodcastLandingComposable {

    @Composable
    fun landingScreen (
        onNavigateToFetchPodcast: () -> Unit,
        onNavigateToSeeDownloadList: () -> Unit,
        onPodCastListen: (downloadId: String, title: String) -> Unit
    ) {

        val padding = 6.dp
        Log.i("MHR", "WearApp called")
        Radio8podcastTheme {
            /* If you have enough items in your list, use [ScalingLazyColumn] which is an optimized
             * version of LazyColumn for wear devices with some added features. For more information,
             * see d.android.com/wear/compose.
             */
            Column(
                modifier = Modifier
                    .captionBarPadding().fillMaxSize()
                    .background(MaterialTheme.colors.background)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Chip(onClick = onNavigateToFetchPodcast,
                    colors = ChipDefaults.chipColors(contentColor = MaterialTheme.colors.background),
                    modifier = Modifier.clickable { Log.i("MHR", "We have click!!") },
                    label = {
                        Text(text = "Undskyld vi roder")
                    },
                    secondaryLabel = {
                        Text("Click to fetch")
                    }
                )
                Spacer(Modifier.size(padding))
                Chip(onClick = onNavigateToSeeDownloadList,
                    colors = ChipDefaults.chipColors(contentColor = MaterialTheme.colors.background),
                    label = {
                        Text(text = "Downloads")
                    },
                    secondaryLabel = {
                        //Log.i("MHR", "secondaryLabel->" + podcastViewModel.podcasts.value)
                        Text("Click to see")
                    }
                )

                val currentlyPlaying = remember { mutableStateOf<PodcastEntity?>(null) }

                LaunchedEffect(Unit) {
                    podcastViewModel.viewModelScope.launch {
                        withContext(Dispatchers.IO) {
                            podcastViewModel.podcastRepository.podcastDao.findCurrentPlaying().let {
                                it.let {
                                    currentlyPlaying.value = it
                                }
                            }
                        }
                    }
                }

                if (currentlyPlaying.value != null) {
                    Spacer(Modifier.size(padding))

                    Chip(
                        modifier = Modifier.padding(start = 0.dp),
                        colors = ChipDefaults.chipColors(
                            contentColor = MaterialTheme.colors.onSurface,
                            backgroundColor = MaterialTheme.colors.background
                        ),
                        onClick = {
                            onPodCastListen(
                                currentlyPlaying.value!!.url!!,
                                currentlyPlaying.value!!.url!!
                            )
                        },
                        label = {
                            Text(
                                text = currentlyPlaying.value!!.url!!,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                textAlign = TextAlign.Center
                            )
                        },
                        icon = {
                            Icon(
                                painter = painterResource(id = R.drawable.music_note),
                                contentDescription = "PlayButton",
                                modifier = Modifier.size(ChipDefaults.SmallIconSize)
                                    .padding(start = 0.dp)
                                    .wrapContentSize(align = Alignment.Center)
                            )
                        }
                    )


                }

                //Greeting(greetingName = greetingName)
                //FetchPodcasts(onNavigateToFetchPodcast, onNavigateToSeeDownloadList)
            }

        }
    }
}