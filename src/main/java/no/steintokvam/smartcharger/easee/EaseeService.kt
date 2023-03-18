package no.steintokvam.smartcharger.easee

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.steintokvam.smartcharger.easee.objects.Charger
import no.steintokvam.smartcharger.easee.objects.ChargerState
import okhttp3.OkHttpClient
import okhttp3.Request

class EaseeService {

    private val BASE_URL = "https://api.easee.cloud/api"
    private val client = OkHttpClient()
    private val mapper = ObjectMapper().registerModule(KotlinModule.Builder().build())

    fun getChargerId(): List<Charger> {
        val request = createRequest("/chargers")

        val response = client.newCall(request).execute()

        return mapper.readValue(response.body?.charStream()?.readText(), mapper.typeFactory.constructCollectionType(List::class.java, Charger::class.java))
    }

    fun getChargerState(chargerID: String): ChargerState {
        val request = createRequest("/chargers/$chargerID/state")
        val response = client.newCall(request).execute()
        return mapper.readValue(response.body?.charStream()?.readText(), ChargerState::class.java)
    }

    private fun createRequest(endpoint: String): Request {
        return Request.Builder()
                .url(BASE_URL+endpoint)
                .get()
                .addHeader("accept", "application/json")
                .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImN0eSI6IkpXVCJ9.eyJBY2NvdW50SWQiOjU1MzE4LCJVc2VySWQiOjQyMDU4LCJ1bmlxdWVfbmFtZSI6IlN0ZWluIFBldHRlciBUb2t2YW0iLCJyb2xlIjoiVXNlciIsIm5iZiI6MTY3OTE1OTI1NCwiZXhwIjoxNjc5MjQ1NjU0LCJpYXQiOjE2NzkxNTkyNTR9.E2FUDC16THRbNSgCss-On-Frs5ZssbCrxFm275skwNA")
                .build()
    }
}