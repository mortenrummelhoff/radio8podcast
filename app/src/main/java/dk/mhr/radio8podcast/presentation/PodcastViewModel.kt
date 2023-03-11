package dk.mhr.radio8podcast.presentation

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadIndex
import dk.mhr.radio8podcast.service.PodcastService
import kotlinx.coroutines.launch
import java.io.IOException

class PodcastViewModel(private val podcastService: PodcastService) : ViewModel() {

    val downloadList = mutableStateListOf<DataDownload>()

    var downloadChanged = MutableLiveData<String>()
    var podcasts = MutableLiveData<String>()
    var podcastList: String = String()

    fun fetchDownloadList(downloadIndex: DownloadIndex) {
        Log.i("MHR", "fetchingDownloadList" + downloadIndex.getDownloads().count);
        viewModelScope.launch {
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
                                downloadList.add(DataDownload(cursor.download))
                                downloadList[index] = downloadList[index].copy(cursor.download)
                                index++;
                            }
                    }
            } catch (e: IOException) {
                Log.e("MHR", "Failed to load downloads.", e)
            }
        }
        Log.i("MHR", "fetchingDownloadList done");
    }

    data class DataDownload(var download: Download)

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