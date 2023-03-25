package no.steintokvam.smartcharger.easee

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.steintokvam.smartcharger.easee.objects.AccessToken
import no.steintokvam.smartcharger.easee.objects.Authentication
import no.steintokvam.smartcharger.easee.objects.Charger
import no.steintokvam.smartcharger.easee.objects.ChargerState
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EaseeService {

    private val BASE_URL = "https://api.easee.cloud/api"
    private val client = OkHttpClient()

    private val formatter = DateTimeFormatter.ISO_DATE_TIME//TODO: mapperen burde være en sentral pålass og hentes derifra
    private val dateTimeDeserializer = LocalDateTimeDeserializer(formatter)
    private val dateTimeSerializer = LocalDateTimeSerializer(formatter)
    private val javaTimeModule = JavaTimeModule()
        .addSerializer(LocalDateTime::class.java, dateTimeSerializer)
        .addDeserializer(LocalDateTime::class.java, dateTimeDeserializer)
    private val mapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(javaTimeModule)

    fun authenticate(user: String, password: String): AccessToken {
        val auth = mapper.writeValueAsString(Authentication(user, password))
        val body: RequestBody = auth.toRequestBody("Application/json".toMediaType())
        val request = createPostRequest("/accounts/login", body)
        val response = client.newCall(request).execute()
        return mapper.readValue(response.body?.charStream()?.readText(), AccessToken::class.java)
    }

    fun getChargerId(): List<Charger> {
        val request = createGetRequest("/chargers")
        val response = client.newCall(request).execute()

        val collectionType = mapper.typeFactory.constructCollectionType(List::class.java, Charger::class.java)
        return mapper.readValue(response.body?.charStream()?.readText(), collectionType)
    }

    fun getChargerState(): ChargerState {
        val chargerID = getChargerId()[0].id
        val request = createGetRequest("/chargers/$chargerID/state")
        val response = client.newCall(request).execute()
        return mapper.readValue(response.body?.charStream()?.readText(), ChargerState::class.java)
    }

    fun resumeCharging(): Int {
        return toggleCharging("resume_charging")
    }

    fun pauseCharging(): Int {
        return toggleCharging("pause_charging")
    }

    private fun toggleCharging(command: String): Int {
        val chargerId = getChargerId()[0].id
        val body: RequestBody = "".toRequestBody("Application/json".toMediaType())
        val request = createPostRequest("/chargers/$chargerId/commands/$command", body)
        val response = client.newCall(request).execute()
        if(response.isSuccessful) {
            return response.code
        }
        return response.code
    }

    private fun createGetRequest(endpoint: String): Request {
        return createRequestBuilder(endpoint)
            .get()
            .build()
    }

    private fun createPostRequest(endpoint: String, body: RequestBody): Request {
        return createRequestBuilder(endpoint)
            .post(body)
            .build()
    }

    private fun createRequestBuilder(endpoint: String): Request.Builder {
        return Request.Builder()
            .url(BASE_URL+endpoint)
            .addHeader("accept", "application/json")
            .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImN0eSI6IkpXVCJ9.eyJBY2NvdW50SWQiOjU1MzE4LCJVc2VySWQiOjQyMDU4LCJ1bmlxdWVfbmFtZSI6IlN0ZWluIFBldHRlciBUb2t2YW0iLCJyb2xlIjoiVXNlciIsIm5iZiI6MTY3OTc1NTgzMCwiZXhwIjoxNjc5ODQyMjMwLCJpYXQiOjE2Nzk3NTU4MzB9.B1v2s6KjQsQhHL4WV8Q0hZmUm-YNRkWADwoSfEsnrOA")
    }
}