package dk.mhr.radio8podcast.service

import android.util.Log
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import dk.mhr.radio8podcast.presentation.DEBUG_LOG
import dk.mhr.radio8podcast.presentation.PodcastViewModel
import dk.mhr.radio8podcast.presentation.podcastViewModel


class PodcastPlayerService: MediaSessionService() {
    private var mediaSession: MediaSession? = null

    // Create your Player and MediaSession in the onCreate lifecycle event
    override fun onCreate() {
        super.onCreate()
        val exoPlayer = ExoPlayer.Builder(this).setMediaSourceFactory(
                DefaultMediaSourceFactory(this).setDataSourceFactory(
                    PodcastUtils.getDataSourceFactory(this))).build()

        podcastViewModel.exoPlayer = exoPlayer
        //podcastViewModel.player = exoPlayer
        mediaSession = MediaSession.Builder(this, exoPlayer).setCallback(PodcastViewModel.PodcastMediaCallback(this)).build()
        //mediaSession = MediaSession.Builder(this, exoPlayer).build()
    //podcastViewModel.session = mediaSession
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? = mediaSession


    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }

}