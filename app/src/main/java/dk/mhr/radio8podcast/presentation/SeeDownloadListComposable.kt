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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment


import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.LifecycleOwner
import androidx.wear.compose.material.*
import androidx.wear.compose.material.dialog.Alert
import androidx.wear.compose.material.dialog.Dialog
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.Download.*
import com.google.android.exoplayer2.offline.DownloadIndex
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.theme.Radio8podcastTheme
import dk.mhr.radio8podcast.service.PodcastDownloadTracker
import dk.mhr.radio8podcast.service.PodcastDownloadTracker.Listener
import dk.mhr.radio8podcast.service.PodcastUtils
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import java.nio.charset.Charset
import kotlin.math.roundToInt

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
        onPodCastDelete: (download: PodcastViewModel.DataDownload) -> Unit,
        context: Context
    ) {
        Log.i("MHR", "SeeDownloadList called!")



        LaunchedEffect(Unit) {

            while (true) {
                val ongoingDownloads = PodcastUtils.getDownloadManager(context).currentDownloads
                if (ongoingDownloads.isEmpty()) break

                Log.i(DEBUG_LOG, "We have ongoing downloads. UI must be updated!!")
                for (item in ongoingDownloads) {
                    Log.i(DEBUG_LOG, "Iterate through ongoingDownloads: " + item + " -> download pct: " + item.percentDownloaded)
                    podcastViewModel.downloadList.find {
                        Log.i(DEBUG_LOG, "find in downloadList: " + it)
                        it.download.value.request.id == item.request.id
                    }.let {it2 ->
                        Log.i(DEBUG_LOG, "Download found in list: " + it2)

                        if (it2 != null) {
                            val dIndex = podcastViewModel.downloadList.indexOf(it2)
                            if (dIndex > -1) {
                                Log.i(DEBUG_LOG, "Found index in list: " + dIndex)
                                //podcastViewModel.downloadList[dIndex] = PodcastViewModel.DataDownload(item, it2.startPosition)
                                podcastViewModel.downloadList[dIndex] =
                                    podcastViewModel.downloadList[dIndex].copy(
                                        download = mutableStateOf(item),
                                        startPosition = it2.startPosition
                                    )
                                //podcastViewModel.downloadList[dIndex].download = item
                                //podcastViewModel.downloadList
                                Log.i(
                                    DEBUG_LOG,
                                    "download in downloadList: " + dIndex + " updated. New pct: " +
                                            podcastViewModel.downloadList[dIndex].download.value.percentDownloaded
                                )
                            }
                        }
                    }
                }
                delay(2000)
            }
            Log.i(DEBUG_LOG, "Ongoing downloads finished.")
        }

        val showDialog = remember { mutableStateOf(false) }
        lateinit var selectedDownload: PodcastViewModel.DataDownload
        val padding = 6.dp
        Radio8podcastTheme {
            if (podcastViewModel.downloadList.isEmpty()) {
                Column(
                    Modifier.fillMaxSize().padding(20.dp, 30.dp, 20.dp, 30.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("No downloads available")
                }
            }
            if (showDialog.value) {
                showConfirmDialog(
                    selectedDownload, onPodCastDelete, showDialog.value
                ) {
                    Log.i(DEBUG_LOG, "onDismiss called!!!")
                    showDialog.value = false
                }
            }

            LazyColumn(modifier = Modifier
            .captionBarPadding().fillMaxWidth()
//                .padding(10.dp, 30.dp, 10.dp, 30.dp)
                .background(MaterialTheme.colors.background),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            ) {
                itemsIndexed(podcastViewModel.downloadList) { index, download ->

                    Spacer(Modifier.size(padding))

                    TitleCard(
                        onClick = {
                            onPodCastListen(
                                download.download.value.request.id,
                                download.download.value.request.uri.toString(),
                                download.download.value.request.data.toString(Charset.defaultCharset())
                            )
                        },
                        title = {
                            Text(
                                text = download.download.value.request.data.toString(Charset.defaultCharset()),
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
                                Text(fontSize = 12.sp, text = podcastViewModel.formatLength(download.startPosition))
                            } else {
                                Text(fontSize = 12.sp, text = formatState(download.download.value.state))
                            }
                            //Spacer(modifier = Modifier.size(6.dp))
                            Text(fontSize = 12.sp,
                                text = " " + (download.download.value.bytesDownloaded / 1024 / 1024).toString() + "mb (" +
                                        download.download.value.percentDownloaded.roundToInt() + "%)"
                            )
                            //Spacer(modifier = Modifier.size(6.dp))
                            Button(
                                onClick = {
                                    selectedDownload = download
                                    showDialog.value = true
                                },
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


    //@Composable@Composable
    @Composable
    fun showConfirmDialog(
        selectedDownload: PodcastViewModel.DataDownload,
        onPodCastDelete: (download: PodcastViewModel.DataDownload) -> Unit,
        showDialog: Boolean,
        onDismiss: () -> Unit
    ) {
        Box {
//            //var showDialog by remember { mutableStateOf(false) }
//            Column(
//                modifier = Modifier.fillMaxSize().padding(horizontal = 20.dp),
//                verticalArrangement = Arrangement.Center,
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Chip(
//                    onClick = {onPodCastDelete(selectedDownload)},
//                    label = { Text("Show dialog") },
//                    colors = ChipDefaults.secondaryChipColors(),
//                )
//            }
            val scrollState = rememberScalingLazyListState()
            Dialog(
                showDialog = showDialog,
                onDismissRequest = onDismiss,
                scrollState = scrollState,
            ) {
                Alert(
                    scrollState = scrollState,
                    verticalArrangement = Arrangement.spacedBy(0.dp, Alignment.Top),
                    contentPadding =
                    PaddingValues(start = 10.dp, end = 10.dp, top = 24.dp, bottom = 52.dp),
                    icon = {
                        Icon(
                            painter = painterResource(id = R.drawable.garbage),
                            contentDescription = "trashcan",
                            modifier = Modifier.size(24.dp)
                                .wrapContentSize(align = Alignment.Center),
                        )
                    },
                    title = {
                        Text(
                            text = selectedDownload.download.value.request.id,
                            textAlign = TextAlign.Center
                        )
                    },
                    message = {
                        Text(
                            text = "Confirm delete",
                            textAlign = TextAlign.Center,
                            style = MaterialTheme.typography.body2
                        )
                    },
                ) {
                    item {
                        Chip(
                            label = { Text("Ok") },
                            onClick = {
                                onPodCastDelete(selectedDownload)
                                onDismiss()
                            },
                            colors = ChipDefaults.primaryChipColors(),
                        )
                    }
                    item {
                        Chip(
                            label = { Text("Cancel") },
                            onClick = { onDismiss() },
                            colors = ChipDefaults.secondaryChipColors(),
                        )
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