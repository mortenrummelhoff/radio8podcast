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
import org.json.JSONObject

class PodcastViewModel(private val podcastService: PodcastService) : ViewModel() {

    init {
        Log.i("MHR", "PodcastViewModel initialized")
    }

    lateinit var podcastDao:PodcastDao

    val downloadList = mutableStateListOf<DataDownload>()

    var next_episode_pub_date = ""
    val podcastByIdList = mutableStateListOf<JSONObject>()

    var loadingState = false


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
        Log.i(DEBUG_LOG, "fetchingDownloadList" + downloadIndex.getDownloads().count);
        viewModelScope.launch {
            downloadList.clear()
            downloadList.addAll(podcastService.fetchDownloadPodcastList(podcastDao, downloadIndex))
        Log.i(DEBUG_LOG, "fetchingDownloadList done");
    }}

    fun fetchPodcastById(API_KEY: String) {
        Log.i(DEBUG_LOG,
            "fetchPodcastById called!!!, next_episode_pub_date: $next_episode_pub_date"
        )

        viewModelScope.launch {

            val podcastJSONObject = podcastService.fetchPodcastById(API_KEY, "", next_episode_pub_date)
            //podcastsById.value = podcastJSONObject


            //val podcastByIdList: MutableList<JSONObject> = ArrayList()
            val jsonObject = JSONObject(podcastJSONObject)
            next_episode_pub_date = jsonObject.get("next_episode_pub_date").toString()

            val jsonArray = jsonObject.getJSONArray("episodes")

            (0 until jsonArray.length()).forEach {
                podcastByIdList.add(jsonArray.getJSONObject(it))
            }
            loadingState = false
        }
    }

    data class DataDownload(val download: MutableState<Download>, val startPosition: Long)
}