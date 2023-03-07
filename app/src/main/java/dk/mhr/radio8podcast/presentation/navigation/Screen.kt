package dk.mhr.radio8podcast.presentation.navigation

const val AUDIO_URL = "AUDIO_URL"

sealed class Screen(
    val route: String
) {
    object Landing:  Screen("WearApp")
    object ShowPodcast: Screen("ShowPodcasts")
    object SeeDownloads: Screen("SeeDownloads")
    object PodcastPlayer: Screen("PodcastPlayer")

}