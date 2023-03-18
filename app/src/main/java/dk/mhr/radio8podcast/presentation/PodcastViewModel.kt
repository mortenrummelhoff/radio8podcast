package dk.mhr.radio8podcast.presentation

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadIndex
import dk.mhr.radio8podcast.data.PodcastDao
import dk.mhr.radio8podcast.service.PodcastService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

class PodcastViewModel(private val podcastService: PodcastService) : ViewModel() {

    init {
        Log.i("MHR", "PodcastViewModel initialized")
    }

    lateinit var podcastDao:PodcastDao

    val downloadList = mutableStateListOf<DataDownload>()

    var downloadChanged = MutableLiveData<String>()
    var podcasts = MutableLiveData<String>()
    var podcastList: String = String()

    fun formatLength(totalSecs: Long): String {
        val hours = totalSecs / 1000 / 3600;
        val minutes = (totalSecs / 1000 % 3600) / 60;
        val seconds = totalSecs / 1000 % 60;

        return if (hours == 0L)
            String.format("%02d:%02d", minutes, seconds) else
            String.format("%2d:%02d:%02d", hours, minutes, seconds)

    }

    fun fetchDownloadList(downloadIndex: DownloadIndex) {
        Log.i("MHR", "fetchingDownloadList" + downloadIndex.getDownloads().count);
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

            downloadList.clear()
            //val terminalDownloads: MutableList<Download> = ArrayList()
            try {
                downloadIndex.getDownloads()
                    .use { cursor ->
                            Log.i("MHR", "using cursor");
                            var index = 0;
                            while (cursor.moveToNext()) {
                                Log.i("MHR", "index" + index + ", Download: " + cursor.download.bytesDownloaded);
                                //terminalDownloads.add(cursor.download)
                                //val dataDownload = DataDownload(cursor.download)
                                var startP: Long = 0
                                podcastDao.findByUrl(cursor.download.request.id).let {
                                    Log.i(DEBUG_LOG, "Found entry in database. StartPosition: " + it.startPosition)
                                    startP = it.startPosition!!
                                }

                                downloadList.add(DataDownload(cursor.download, startP))
                                downloadList[index] = downloadList[index].copy(cursor.download, startP)

                                index++;
                            }
                    }
            } catch (e: IOException) {
                Log.e("MHR", "Failed to load downloads.", e)
            }
        }
        Log.i("MHR", "fetchingDownloadList done");
    }}

    data class DataDownload(var download: Download, val startPosition: Long)

    fun loadPodcast(API_KEY: String): String {

        var pods = "initial value"
        viewModelScope.launch {

            pods = podcastService.fetchPodcasts(API_KEY)
            //podcastService.fetchDetails(API_KEY, "1")
            //Log.i("MHR", pods)
            podcasts.postValue(pods);
        }
        return pods
    }
//        viewModelScope.launch {
//            val result = withContext(Dispatchers.IO) {
//                var listenNotesService = ListenNotesService()
//                listenNotesService.main()
//            }
//        }
//        Log.i("MHR", result.toString())

//        val result = viewModelScope.async {
//            // Coroutine that will be canceled when the ViewModel is cleared.
//            println("Fetching from service")
//
//            var listenNotesService = ListenNotesService()
//            val main = listenNotesService.main()
//            Log.i("MHR", main.toString())
//            main
//        }
//        result.invokeOnCompletion {
//            if (it == null) {
//                Log.i("MHR", "here are result:${result.getCompleted()}")
//            } else {
//                it.printStackTrace()
//            }
//        }


//    }
}