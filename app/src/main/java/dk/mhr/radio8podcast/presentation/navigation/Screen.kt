package dk.mhr.radio8podcast.presentation.navigation

const val AUDIO_URL = "AUDIO_URL"
const val TITLE = "TITLE"
const val DOWNLOAD_ID = "DOWNLOAD_ID"

sealed class Screen(
    val route: String
) {
    object Landing:  Screen("WearApp")
    object ShowPodcast: Screen("ShowPodcasts")
    object SeeDownloads: Screen("SeeDownloads")
    object PodcastPlayer: Screen("PodcastPlayer")

}