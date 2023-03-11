package dk.mhr.radio8podcast.presentation

import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadIndex
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import dk.mhr.radio8podcast.service.PodcastDownloadTracker
import java.io.IOException
import java.nio.charset.Charset

class SeeDownloadListComposable(
    val downloadTracker: PodcastDownloadTracker,
    val downloadIndex: DownloadIndex
) {

    fun formatState(state: Int): String {
        return when (state) {
            1 -> "Start Downloading"
            2 -> "Downloading"
            3 -> "Ready"
            else -> "Not ready"
        }
    }

    @Composable
    fun SeeDownloadList(onPodCastListen: (audio: String, title: String) -> Unit) {
        Log.i("MHR", "SeeDownloadList called!")
        Log.i("MHR", "downloadIndex:" + downloadIndex)
        //downloadTracker.
        var checked by remember { mutableStateOf(true) }
        val terminalDownloads: MutableList<Download> = ArrayList()
        try {
            downloadIndex.getDownloads()
                .use { cursor ->
                    while (cursor.moveToNext()) {
                        terminalDownloads.add(cursor.download)
                    }
                }
        } catch (e: IOException) {
            Log.e("MHR", "Failed to load downloads.", e)
        }

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

                terminalDownloads.forEach {download ->
                    Spacer(Modifier.size(padding))

                    TitleCard(
                        onClick = {
                            onPodCastListen(download.request.uri.toString(), download.request.data.toString(Charset.defaultCharset()))
                        },
                        title = {
                            Text(text = download.request.data.toString(Charset.defaultCharset()), maxLines = 2)
                        },
                        contentColor = MaterialTheme.colors.background,
                    ) {
                        Text(text = formatState(download.state))
                    }
                }

            }
        }

    }




}