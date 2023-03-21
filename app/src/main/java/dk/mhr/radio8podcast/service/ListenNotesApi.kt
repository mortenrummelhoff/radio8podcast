package dk.mhr.radio8podcast.service

import android.util.Log
import com.listennotes.podcast_api.exception.ListenApiException
import org.json.JSONObject
import kotlin.collections.HashMap

class ListenNotesApi(API_KEY: String) {

    var useMockData = true;
    var apiKey = API_KEY;

    val R8DIO_PODCAST_ID = "8ade9927c58244859e7c363d055c9584"

    fun getDetailedInfo() {
        try {
            val objClient = ModClient(apiKey)
            val parameters = HashMap<String, String>()
            parameters.put("id", "b6a00d39051a4a53a9b6cd66bb40e069")
//            parameters.put("next_episode_pub_date", "1479154463000")
//            parameters.put("sort", "recent_first")
            val response = objClient.fetchPodcastById(parameters)
            println(response.toJSON().toString(2))
        } catch (e: ListenApiException) {
            println(e)
        }
    }

    fun fetchPodcastById(podcastId: String = R8DIO_PODCAST_ID): JSONObject? {
        if (useMockData) {
            return JSONObject(MockFetchPodcastById().mockData);
        }

        try {
            val objClient = ModClient(apiKey)
            //objClient.getConnection()

            val parameters = HashMap<String, String>()

            parameters.put("id", podcastId)
            parameters.put("sort_by_date", "1")
            parameters.put("sort", "recent_first")

            val response = objClient.fetchPodcastById(parameters)
            //println("Response here below:")
            //println(response)
            println(response.toJSON().toString(2))
            return response.toJSON()
        } catch (e: ListenApiException) {
            e.printStackTrace()
        }
        return null
    }

    fun search(): JSONObject? {
        if (useMockData) {
            return JSONObject(MockSearch().mockSearch);
        }

        try {
            val objClient = ModClient(apiKey)
            //objClient.getConnection()

            val parameters = HashMap<String, String>()

            parameters.put("q", "\"Undskyld vi roder\"")
            parameters.put("sort_by_date", "1")
            parameters.put("type", "podcast")
            parameters.put("len_min", "10")

            parameters.put("only_in", "title,description")

            parameters.put("safe_mode", "0")

            val response = objClient.search(parameters)
            //println("Response here below:")
            //println(response)
            println(response.toJSON().toString(2))
            return response.toJSON()
        } catch (e: ListenApiException) {
            e.printStackTrace()
        }
        return null
    }
}