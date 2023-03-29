package dk.mhr.radio8podcast.service

//import androidx.media.app.NotificationCompat
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioManager
import android.media.session.MediaSession
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import com.google.android.exoplayer2.MediaItem
import dk.mhr.radio8podcast.R
import dk.mhr.radio8podcast.presentation.DEBUG_LOG
import dk.mhr.radio8podcast.presentation.MainActivity
import dk.mhr.radio8podcast.presentation.podcastViewModel
import kotlinx.coroutines.*

class PlayerWorker(context: Context, workerParameters: WorkerParameters):
    CoroutineWorker(context, workerParameters) {

    private val audioManagerManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    private var nextNotificationId: Int = 999

    init{
        Log.i(DEBUG_LOG, "Create notificationChannel")
        val importance = NotificationManager.IMPORTANCE_DEFAULT
        val channel = NotificationChannel("Channel1", "Podcast", importance)
        channel.description = "PodcastPlayerChannel"
        notificationManager.createNotificationChannel(channel)
    }

    override suspend fun getForegroundInfo(): ForegroundInfo {
        Log.i(DEBUG_LOG, "getForegroundInfo called!!!" + notificationManager.notificationChannels)
        return createForegroundInfo(0, "Loading")
    }

    private fun createNotification(currentPosition: Long, mediaId: String?): Notification {
        Log.i(DEBUG_LOG, "createNotification")

        val notification = NotificationCompat.Builder(applicationContext, "Channel1")
            .setContentTitle(mediaId)
            .setContentText(mediaId)
            .setSilent(true)
            .setContentText(podcastViewModel.formatLength(currentPosition))
            .setSmallIcon(R.drawable.music_note)
            .setOngoing(true)
            .setContentIntent(PendingIntent.getActivity(podcastViewModel.CONTEXT, 0,
                Intent.makeMainActivity(ComponentName(podcastViewModel.CONTEXT!!, MainActivity::class.java)),
                PendingIntent.FLAG_IMMUTABLE))
            // Add the cancel action to the notification which can
            // be used to cancel the worker
        //    .addAction(android.R.drawable.ic_delete, cancel, intent)
            .build()
        return notification

    }


    override suspend fun doWork(): Result {
        Log.i(DEBUG_LOG, "Start work for active player keeping it from sleep")

        podcastViewModel.session?.isActive = true

        val isPlaying = MutableLiveData(true)
        val mediaItem = MutableLiveData<MediaItem>()
        val currentPosition = MutableLiveData<Long>(0)

        while (isPlaying.value!!) {

            runBlocking {
                withContext(Dispatchers.Main) {
                    isPlaying.value = podcastViewModel.player!!.isPlaying
                    mediaItem.value = podcastViewModel.player!!.currentMediaItem
                    currentPosition.value = podcastViewModel.player!!.currentPosition
                }
            }
            notificationManager.notify(nextNotificationId, createNotification(currentPosition.value!!, mediaItem.value?.mediaId))
            delay(5000)
        }
        Log.i(DEBUG_LOG, "Finish work for active player allowing to go to sleep")
        podcastViewModel.session?.isActive = false
        return Result.success()
    }

    private fun createForegroundInfo(currentPosition: Long?, mediaId: String?): ForegroundInfo {
        Log.i(DEBUG_LOG, "createForegroundInfo called: $currentPosition, mediaId: $mediaId")
        return ForegroundInfo(nextNotificationId, createNotification(currentPosition!!, mediaId))
    }



}