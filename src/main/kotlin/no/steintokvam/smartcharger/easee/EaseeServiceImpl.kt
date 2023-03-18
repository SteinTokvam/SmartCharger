package no.steintokvam.smartcharger.easee

import okhttp3.OkHttpClient
import okhttp3.Request

class EaseeServiceImpl {

    private val BASE_URL = "https://api.easee.cloud/api"
    fun authenticate() {
        val client = OkHttpClient()


        val request = Request.Builder()
                .url(BASE_URL + "/charger")
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "sha512-3SLPi/02aN59HglkUaDsVoLQqQvu+kQ3OgjafUXVipd4DfgNG6rsif6xUhnvXiLooKYkTBBkotexdOV1MmdNEw==?81r0")
                .build()

        val response = client.newCall(request).execute()
        println(response.message)
    }

}