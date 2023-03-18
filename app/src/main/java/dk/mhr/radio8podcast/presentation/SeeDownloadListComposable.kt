package dk.mhr.radio8podcast.presentation

import android.util.Log
import androidx.compose.foundation.layout.*

import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment


import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.wear.compose.material.Button
import androidx.wear.compose.material.Icon
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.Download.*
import com.google.android.exoplayer2.offline.DownloadIndex
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import dk.mhr.radio8podcast.service.PodcastDownloadTracker
import dk.mhr.radio8podcast.service.PodcastDownloadTracker.Listener
import java.nio.charset.Charset

class SeeDownloadListComposable(
    val downloadTracker: PodcastDownloadTracker,
    val downloadIndex: DownloadIndex,
    val lifecycleOwner: LifecycleOwner
) {


    fun formatState(state: Int): String {
        return when (state) {
            STATE_STOPPED -> "Start Downloading"
            STATE_DOWNLOADING -> "Downloading"
            STATE_COMPLETED -> "Ready"
            STATE_FAILED -> "Failed"
            STATE_REMOVING -> "Deleting"
            else -> "Unk->$state"
        }
    }

    @Composable
    fun SeeDownloadList(
        onPodCastListen: (downloadId: String, audio: String, title: String) -> Unit,
        onPodCastDelete: (download: PodcastViewModel.DataDownload) -> Unit
    ) {
        Log.i("MHR", "SeeDownloadList called!")

        val padding = 6.dp
        Radio8podcastTheme {
            if (podcastViewModel.downloadList.isEmpty()) {
                Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No downloads available")
                }
            }
            LazyColumn() {
                itemsIndexed(podcastViewModel.downloadList) { index, download ->

                    Spacer(Modifier.size(padding))

                    TitleCard(
                        onClick = {
                            onPodCastListen(
                                download.download.request.id,
                                download.download.request.uri.toString(),
                                download.download.request.data.toString(Charset.defaultCharset())
                            )
                        },
                        title = {
                            Text(
                                text = download.download.request.data.toString(Charset.defaultCharset()),
                                maxLines = 2
                            )
                        },
                        //contentColor = MaterialTheme.colors.background,
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {

                            if (download.startPosition != 0L) {
                                Text(text = podcastViewModel.formatLength(download.startPosition))
                            } else {
                                Text(text = formatState(download.download.state))
                            }
                            //Spacer(modifier = Modifier.size(6.dp))
                            Text(text = "s: " + (download.download.bytesDownloaded / 1024 / 1024).toString() + "mb")
                            //Spacer(modifier = Modifier.size(6.dp))
                            Button(
                                onClick = { onPodCastDelete(download) },
                                modifier = Modifier.size(26.dp).wrapContentSize(Alignment.Center)
                            ) {
                                Icon(
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    painter = painterResource(R.drawable.garbage)
                                )
                            }
                        }
                    }
                }

            }

        }
    }


    class PodcastDownloadListener(onDownChanged: () -> Unit) : Listener {
        override fun onDownloadsChanged() {
            onDownloadsChanged()
        }
    }

}