package dk.mhr.radio8podcast.service

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.media3.exoplayer.offline.DownloadIndex
import dk.mhr.radio8podcast.data.PodcastDao
import dk.mhr.radio8podcast.presentation.DEBUG_LOG
import dk.mhr.radio8podcast.presentation.PodcastViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import java.io.IOException

class PodcastService(private val ioDispatcher: CoroutineDispatcher) {

    suspend fun searchPodcasts(API_KEY: String): String {
        return withContext(ioDispatcher) {
            var listenNotesService = ListenNotesApi(API_KEY)
            listenNotesService.search()?.toString(2) ?: ""
        }
    }

    suspend fun fetchPodcastById(API_KEY: String, podcastId: String, nextEpisodePubDate: String): String {
        return withContext(ioDispatcher) {
            val fetchPodcastById =
                ListenNotesApi(API_KEY).fetchPodcastById(nextEpisodePubDate = nextEpisodePubDate);
            fetchPodcastById?.toString(2) ?: ""

        }
    }

    suspend fun fetchDownloadPodcastList(podcastDao: PodcastDao, downloadIndex: DownloadIndex): ArrayList<PodcastViewModel.DataDownload> {
        return withContext(ioDispatcher) {
            val downloadList = ArrayList<PodcastViewModel.DataDownload>()
            try {
                downloadIndex.getDownloads()
                    .use { cursor ->
                        Log.i(DEBUG_LOG, "using cursor");
                        var index = 0;
                        while (cursor.moveToNext()) {
                            Log.i(
                                DEBUG_LOG,
                                "index" + index + ", Download: " + cursor.download.bytesDownloaded
                            );
                            var startP: Long = 0
                            podcastDao.findByUrl(cursor.download.request.id).let {
                                if (it != null) {
                                    Log.i(
                                        DEBUG_LOG,
                                        "Found entry in database: $it"
                                    )
                                    startP = it.startPosition!!
                                }
                            }

                            downloadList.add(PodcastViewModel.DataDownload(mutableStateOf( cursor.download), startP))
                            downloadList[index] = downloadList[index].copy(mutableStateOf( cursor.download), startP)

                            index++;
                        }
                    }
            } catch (e: IOException) {
                Log.e(DEBUG_LOG, "Failed to load downloads.", e)
            }
            downloadList
        }
    }

    suspend fun fetchDetails(API_KEY: String, podcastId: String): String {
        withContext(ioDispatcher) {
            var listenNotesService = ListenNotesApi(API_KEY)
            listenNotesService.getDetailedInfo()
        }
        return ""
    }
}