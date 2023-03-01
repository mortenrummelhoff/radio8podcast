package dk.mhr.radio8podcast.presentation

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dk.mhr.radio8podcast.service.PodcastService
import kotlinx.coroutines.launch

class PodcastViewModel(private val podcastService : PodcastService)
    : ViewModel() {



    var podcasts = MutableLiveData<String>()
    var podcastList: String = String()


    //fun callListenPodcast() {

        //val uiState: StateFlow<String> = StateFlow


        fun loadPodcast(API_KEY: String): String {


            var pods = "initial value"
            viewModelScope.launch {

                pods = podcastService.fetchPodcasts(API_KEY)
                //podcastService.fetchDetails(API_KEY, "1")
                //Log.i("MHR", pods)
                podcasts.postValue(pods);
            }
            return pods
        }
//        viewModelScope.launch {
//            val result = withContext(Dispatchers.IO) {
//                var listenNotesService = ListenNotesService()
//                listenNotesService.main()
//            }
//        }
//        Log.i("MHR", result.toString())

//        val result = viewModelScope.async {
//            // Coroutine that will be canceled when the ViewModel is cleared.
//            println("Fetching from service")
//
//            var listenNotesService = ListenNotesService()
//            val main = listenNotesService.main()
//            Log.i("MHR", main.toString())
//            main
//        }
//        result.invokeOnCompletion {
//            if (it == null) {
//                Log.i("MHR", "here are result:${result.getCompleted()}")
//            } else {
//                it.printStackTrace()
//            }
//        }


//    }
}