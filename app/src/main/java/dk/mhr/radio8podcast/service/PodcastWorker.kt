package dk.mhr.radio8podcast.service

import android.content.Context
import android.util.Log
import androidx.work.Worker
import androidx.work.WorkerParameters
//import com.google.android.exoplayer2.offline.Download
import dk.mhr.radio8podcast.presentation.DEBUG_LOG
import dk.mhr.radio8podcast.presentation.podcastViewModel

class PodcastWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        Log.i(DEBUG_LOG, "Start: $tags")

        if (tags.contains("find_currently_played")) {
            val findCurrentPlaying = podcastViewModel.podcastRepository.podcastDao.findCurrentPlaying()
            Log.i(DEBUG_LOG, "Currently playing: $findCurrentPlaying")

            if (findCurrentPlaying != null) {
                val download = PodcastUtils.getDownloadManager(applicationContext).downloadIndex.getDownload(findCurrentPlaying?.url!!)
                podcastViewModel.preparePlayer(download?.request?.toMediaItem())
            }
        }

        if (tags.contains("prepare_player_selected_download")) {
            val selectedDownloadId = inputData.getString("downloadId")
            val download = PodcastUtils.getDownloadManager(applicationContext).downloadIndex.getDownload(selectedDownloadId!!)
            podcastViewModel.preparePlayer(download?.request?.toMediaItem())

        }

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }
}
