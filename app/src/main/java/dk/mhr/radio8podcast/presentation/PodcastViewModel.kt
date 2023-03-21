package dk.mhr.radio8podcast.presentation

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.exoplayer2.offline.Download
import com.google.android.exoplayer2.offline.DownloadIndex
import dk.mhr.radio8podcast.data.PodcastDao
import dk.mhr.radio8podcast.service.PodcastService
import kotlinx.coroutines.launch

class PodcastViewModel(private val podcastService: PodcastService) : ViewModel() {

    init {
        Log.i("MHR", "PodcastViewModel initialized")
    }

    lateinit var podcastDao:PodcastDao

    val downloadList = mutableStateListOf<DataDownload>()

    val podcastsById = mutableStateOf("")

    var podcasts = MutableLiveData<String>()
    var podcastList: String = String()

    fun formatLength(totalSecs: Long): String {
        val hours = totalSecs / 1000 / 3600;
        val minutes = (totalSecs / 1000 % 3600) / 60;
        val seconds = totalSecs / 1000 % 60;

        return if (hours == 0L && minutes == 0L)
            String.format("%02d", seconds) else if (hours == 0L)
            String.format("%02d:%02d", minutes, seconds) else
            String.format("%2d:%02d:%02d", hours, minutes, seconds)

    }

    fun fetchDownloadList(downloadIndex: DownloadIndex) {
        Log.i("MHR", "fetchingDownloadList" + downloadIndex.getDownloads().count);
        viewModelScope.launch {
            downloadList.clear()
            downloadList.addAll(podcastService.fetchDownloadPodcastList(podcastDao, downloadIndex))
        Log.i(DEBUG_LOG, "fetchingDownloadList done");
    }}

    fun fetchPodcastById(API_KEY: String) {
        viewModelScope.launch {
            podcastsById.value = podcastService.fetchPodcastById(API_KEY, "");
        }
    }

    fun loadPodcast(API_KEY: String): String {

        var pods = "initial value"
        viewModelScope.launch {
            pods = podcastService.searchPodcasts(API_KEY)
            podcasts.postValue(pods);
        }
        return pods
    }

    data class DataDownload(val download: MutableState<Download>, val startPosition: Long)
}