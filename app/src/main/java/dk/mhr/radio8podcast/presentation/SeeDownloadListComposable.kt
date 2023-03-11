package dk.mhr.radio8podcast.presentation

import android.content.Context
import android.util.Log
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember

import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.LifecycleOwner
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import androidx.wear.compose.material.TitleCard
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadIndex
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import dk.mhr.radio8podcast.service.PodcastDownloadTracker
import dk.mhr.radio8podcast.service.PodcastDownloadTracker.Listener
import java.io.IOException
import java.nio.charset.Charset

class SeeDownloadListComposable(
    val downloadTracker: PodcastDownloadTracker,
    val downloadIndex: DownloadIndex,
    val lifecycleOwner: LifecycleOwner
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


//        downloadTracker.removeListener {  }
//        downloadTracker.addListener (PodcastDownloadListener(onDownChanged = {Log.i("MHR", "Download changed!!")}))

//        if (!podcastViewModel.downloadChanged.hasObservers()) {
//             podcastViewModel.downloadChanged.observe(lifecycleOwner){
//                 podcastViewModel.fetchDownloadList(downloadIndex)
//             }
//        }

        val terminalDownloads: MutableList<Download> = ArrayList()


//        try {
//            downloadIndex.getDownloads()
//                .use { cursor ->
//                    while (cursor.moveToNext()) {
//                        terminalDownloads.add(cursor.download)
//                    }
//                }
//        } catch (e: IOException) {
//            Log.e("MHR", "Failed to load downloads.", e)
//        }

        val padding = 6.dp
        Radio8podcastTheme {
            LazyColumn() { itemsIndexed(podcastViewModel.downloadList) {index, download ->
//            Column(
//                modifier = Modifier
//                    .captionBarPadding().fillMaxWidth().verticalScroll(ScrollState(0))
//                    .padding(padding)
//                    .background(MaterialTheme.colors.background),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//
//            ) {



//                terminalDownloads.forEach {download ->
                    Spacer(Modifier.size(padding))

                    TitleCard(
                        onClick = {
                            onPodCastListen(download.download.request.uri.toString(), download.download.request.data.toString(Charset.defaultCharset()))
                        },
                        title = {
                            Text(text = download.download.request.data.toString(Charset.defaultCharset()), maxLines = 2)
                        },
                        //contentColor = MaterialTheme.colors.background,
                    ) {
                        Row {
                            Text(text = formatState(download.download.state))
                            Spacer(modifier = Modifier.size(6.dp))
                            Text(text = "size: " + (download.download.bytesDownloaded/1024/1024).toString() + "m")
                        }
                    }
                }

            }

        }
    }


    class PodcastDownloadListener(onDownChanged:() -> Unit): Listener {
        override fun onDownloadsChanged() {
            onDownloadsChanged()
        }
    }

}