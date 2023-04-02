package dk.mhr.radio8podcast.service

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dk.mhr.radio8podcast.presentation.DEBUG_LOG
import dk.mhr.radio8podcast.presentation.PodcastViewModel


class PodcastPlayerService: MediaSessionService() {
    private var mediaSession: MediaSession? = null

    // Create your Player and MediaSession in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        val exoPlayer = ExoPlayer.Builder(this).setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setDataSourceFactory(
                    PodcastUtils.getDataSourceFactory(this))).setRenderersFactory(
            PodcastUtils.buildRenderersFactory(this, true)).build()
        mediaSession = MediaSession.Builder(this, exoPlayer).setCallback(PodcastMediaCallback(this)).build()
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession

    override fun onDestroy() {
        Log.i(DEBUG_LOG, "onDestroy called. Releasing media session, player and mediaSessionService")

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()

    }

    class PodcastMediaCallback(val context: Context) : MediaSession.Callback {
        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {
            Log.i(DEBUG_LOG, "playerCommandRequest: " +playerCommand)
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {


            return super.onCustomCommand(session, controller, customCommand, args)
        }

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            Log.i(DEBUG_LOG, "onConnect called")
            return super.onConnect(session, controller)
        }

        override fun onDisconnected(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ) {
            Log.i(DEBUG_LOG, "onDisconnected called")
            super.onDisconnected(session, controller)
        }

        override fun onPostConnect(session: MediaSession, controller: MediaSession.ControllerInfo) {
            Log.i(DEBUG_LOG, "onPostConnect called")
            super.onPostConnect(session, controller)
        }

        override fun onAddMediaItems(
            mediaSession: MediaSession,
            controller: MediaSession.ControllerInfo,
            mediaItems: MutableList<MediaItem>
        ): ListenableFuture<MutableList<MediaItem>> {
            Log.i(DEBUG_LOG, "onAddMediaItems called!!")
            val download = PodcastUtils.getDownloadManager(context).downloadIndex.getDownload(mediaItems[0].mediaId)
            val updatedMediaItems: MutableList<MediaItem> = mutableListOf()

            if (download != null) {
                updatedMediaItems.add(download.request.toMediaItem())
            }

            //val updatedMediaItems = mediaItems.map { it.buildUpon().setUri(it.mediaId).build() }.toMutableList()
            return Futures.immediateFuture(updatedMediaItems)
            //return super.onAddMediaItems(mediaSession, controller, mediaItems)
        }

    }
}