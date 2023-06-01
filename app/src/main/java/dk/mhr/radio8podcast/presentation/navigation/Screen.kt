package dk.mhr.radio8podcast.presentation.navigation

sealed class Screen(
    val route: String
) {
    object Landing:  Screen("WearApp")
    object ShowPodcast: Screen("ShowPodcasts")
    object SeeDownloads: Screen("SeeDownloads")
    object PodcastPlayer: Screen("PodcastPlayer")

}