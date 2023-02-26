package dk.mhr.radio8podcast.service

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext

class PodcastService(private val ioDispatcher: CoroutineDispatcher) {

    suspend fun fetchPodcasts(API_KEY: String): String {
        return withContext(ioDispatcher) {
            var listenNotesService = ListenNotesApi(API_KEY)
            //listenNotesService.main()
            listenNotesService.search()?.toString(2) ?: ""
        }
    }
}