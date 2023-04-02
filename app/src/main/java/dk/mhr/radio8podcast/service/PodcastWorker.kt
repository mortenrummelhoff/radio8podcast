package dk.mhr.radio8podcast.service

import android.content.Context
import android.util.Log
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.work.Worker
import androidx.work.WorkerParameters
import dk.mhr.radio8podcast.data.PodcastEntity
//import com.google.android.exoplayer2.offline.Download
import dk.mhr.radio8podcast.presentation.DEBUG_LOG
import dk.mhr.radio8podcast.presentation.podcastViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext

class PodcastWorker(appContext: Context, workerParams: WorkerParameters) :
    Worker(appContext, workerParams) {
    override fun doWork(): Result {

        Log.i(DEBUG_LOG, "Start: $tags")

        if (tags.contains("find_currently_played")) {
            val findCurrentPlaying = podcastViewModel.podcastRepository.podcastDao.findCurrentPlaying()
            Log.i(DEBUG_LOG, "Currently playing: $findCurrentPlaying")

            if (findCurrentPlaying != null) {
                val download = PodcastUtils.getDownloadManager(applicationContext).downloadIndex.getDownload(findCurrentPlaying?.url!!)
                preparePlayer(download?.request?.toMediaItem())
            }
        }

        if (tags.contains("prepare_player_selected_download")) {
            val selectedDownloadId = inputData.getString("downloadId")
            val download = PodcastUtils.getDownloadManager(applicationContext).downloadIndex.getDownload(selectedDownloadId!!)
            preparePlayer(download?.request?.toMediaItem())

        }

        // Indicate whether the work finished successfully with the Result
        return Result.success()
    }

    private fun preparePlayer(mediaItem: MediaItem?) {
        Log.i(DEBUG_LOG, "Preparing player for : ${mediaItem?.mediaId}")
        var startP = 0L
        //val player = podcastViewModel.player
        runBlocking {
            withContext(Dispatchers.IO) {
                val podcastEntity = podcastViewModel.podcastRepository.podcastDao.findByUrl(mediaItem?.mediaId!!)

                if (podcastEntity == null) {
                    Log.i( DEBUG_LOG,"Podcast does not exist in db for mediaId: ${mediaItem.mediaId} Creating new")
                    val newPodcastEntity = PodcastEntity(0, mediaItem.mediaId, 0)
                    podcastViewModel.podcastRepository.podcastDao.insertPodcast(newPodcastEntity)
                } else {
                    Log.i( DEBUG_LOG,"Podcast exist in db for mediaId: ${mediaItem.mediaId} With startPosition: ${podcastEntity.startPosition}")
                    startP = podcastEntity.startPosition!!
                    //startP = 0;
                }
            }.apply {
                Log.i(DEBUG_LOG, "It this then run!!")
                withContext(Dispatchers.Main) {
                    Log.i(DEBUG_LOG, "It this then run MAIN!!")
                    if (podcastViewModel.player?.mediaItemCount == 0) {
                        mediaItem?.let {
                            podcastViewModel.player?.setMediaItem(it, startP)
                            podcastViewModel.player?.prepare()
                            Log.i(DEBUG_LOG, "Start play some podcast")
                        }

                    } else {
                        if (mediaItem?.mediaId.equals(podcastViewModel.player?.currentMediaItem?.mediaId)) {
                            Log.i(
                                DEBUG_LOG,
                                "Player already loaded with same mediaItem: $mediaItem"
                            )
                        } else {
                            Log.i(
                                DEBUG_LOG,
                                "Player loaded with other mediaItem that selected. Load new into player: $mediaItem"
                            )
                            mediaItem?.let {
                                podcastViewModel.player?.setMediaItem(it, startP)
                                podcastViewModel.player?.prepare()
                            }

                        }
                    }

                    Log.i(
                        DEBUG_LOG,
                        "CurrentMediaItemId " + podcastViewModel.player?.currentMediaItem?.mediaId + ", contentPosition: " + podcastViewModel.player?.contentPosition +
                                ", contentLength: " + podcastViewModel.player?.contentDuration
                    )
                }
            }
        }
    }

}
