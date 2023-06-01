package dk.mhr.radio8podcast.service

import android.content.Context
import android.os.Bundle
import android.support.v4.media.session.MediaControllerCompat
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player.Command
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import androidx.media3.session.SessionCommand
import androidx.media3.session.SessionResult
import com.google.common.util.concurrent.Futures
import com.google.common.util.concurrent.ListenableFuture
import dk.mhr.radio8podcast.presentation.DEBUG_LOG


@UnstableApi class PodcastPlayerService: MediaSessionService() {
    private var mediaSession: MediaSession? = null

    // Create your Player and MediaSession in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        val exoPlayer = ExoPlayer.Builder(this).setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setDataSourceFactory(
                    PodcastUtils.getDataSourceFactory(this))).setRenderersFactory(
            PodcastUtils.buildRenderersFactory(this, true)).build()
        exoPlayer.experimentalSetOffloadSchedulingEnabled(true)
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
        clearListener()
        super.onDestroy()

    }

    @UnstableApi private class PodcastMediaCallback(val context: Context) : MediaSession.Callback {
        override fun onPlayerCommandRequest(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            playerCommand: Int
        ): Int {

            Log.i(DEBUG_LOG, "playerCommandRequest: " + playerCommand)
            return super.onPlayerCommandRequest(session, controller, playerCommand)
        }

        override fun onCustomCommand(
            session: MediaSession,
            controller: MediaSession.ControllerInfo,
            customCommand: SessionCommand,
            args: Bundle
        ): ListenableFuture<SessionResult> {
            Log.i(DEBUG_LOG, "onCustomCommand called: $customCommand")
            val onCustomCommand = super.onCustomCommand(session, controller, customCommand, args)
            return onCustomCommand
        }

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            Log.i(DEBUG_LOG, "onConnect called")


            Log.i(DEBUG_LOG, "controller.packageName: ${controller.packageName}")

            val onConnect = super.onConnect(session, controller)
            val onConnectUpdated = onConnect.availablePlayerCommands.buildUpon().addAllCommands().build()
            Log.i(DEBUG_LOG, "Commands: " + onConnect.availableSessionCommands.commands)

            return MediaSession.ConnectionResult.accept(onConnect.availableSessionCommands, onConnectUpdated)
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

            return Futures.immediateFuture(updatedMediaItems)
        }

    }
}