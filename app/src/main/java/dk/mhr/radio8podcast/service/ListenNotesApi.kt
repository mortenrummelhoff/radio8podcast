package dk.mhr.radio8podcast.service

import android.util.Log
import com.listennotes.podcast_api.exception.ListenApiException
import org.json.JSONObject
import kotlin.collections.HashMap

class ListenNotesApi(API_KEY: String) {

    var useMockData = true;
    var apiKey = API_KEY;

    fun search(): JSONObject? {
        if (useMockData) {
            return JSONObject(MockSearch().mockSearch);
        }

        try {
            val objClient = ModClient(apiKey)
            //objClient.getConnection()

            val parameters = HashMap<String, String>()

            parameters.put("q", "undskyld vi roder")
            parameters.put("sort_by_date", "0")
            parameters.put("type", "episode")
            parameters.put("len_min", "10")

            parameters.put("only_in", "title,description")

            parameters.put("safe_mode", "0")

            val response = objClient.search(parameters)
            //println("Response here below:")
            //println(response)
            //println(response.toJSON().toString(2))
            return response.toJSON()
        } catch (e: ListenApiException) {
            e.printStackTrace()
        }
        return null
    }
}