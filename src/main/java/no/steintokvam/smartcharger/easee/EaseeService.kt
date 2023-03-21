package no.steintokvam.smartcharger.easee

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.steintokvam.smartcharger.easee.objects.Charger
import no.steintokvam.smartcharger.easee.objects.ChargerState
import no.steintokvam.smartcharger.electricity.ElectricityPrice
import no.steintokvam.smartcharger.objects.Days
import no.steintokvam.smartcharger.objects.Range
import no.steintokvam.smartcharger.objects.WeeklyScheduele
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate
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

    fun setChargingPeriod(prices: List<ElectricityPrice>): Boolean {
        val todayDate = LocalDate.now()
        val rangesToday = prices
            .filter { it.time_start.dayOfMonth == todayDate.dayOfMonth }
            .map {
                val chargingCurrentLimit = 25
                Range(chargingCurrentLimit, it.time_start.toLocalTime(), it.time_end.toLocalTime())
            }
        val rangesTomorrow = prices
            .filter { it.time_start.dayOfMonth == todayDate.dayOfMonth.plus(1) }
            .map {
                val chargingCurrentLimit = 25
                Range(chargingCurrentLimit, it.time_start.toLocalTime(), it.time_end.toLocalTime())
            }

        val days = listOf(
            Days(rangesToday, todayDate.dayOfWeek.minus(1).value),
            Days(rangesTomorrow, todayDate.dayOfWeek.value)
        )

        val scheduele = WeeklyScheduele(days, true)
        val chargerId = "EHE6ZQU7"//getChargerId()[0].id
        val bodyString = mapper.writeValueAsString(scheduele)
        val body: RequestBody = bodyString.toRequestBody("application/json".toMediaType())
        val request = createPostRequest("/$chargerId/weekly_charge_plan", body)
        val response = client.newCall(request).execute()
        if(response.isSuccessful) {
            return true
        }
        return false
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
            .addHeader("Authorization", "Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCIsImN0eSI6IkpXVCJ9.eyJBY2NvdW50SWQiOjU1MzE4LCJVc2VySWQiOjQyMDU4LCJ1bmlxdWVfbmFtZSI6IlN0ZWluIFBldHRlciBUb2t2YW0iLCJyb2xlIjoiVXNlciIsIm5iZiI6MTY3OTQyMTc4OCwiZXhwIjoxNjc5NTA4MTg4LCJpYXQiOjE2Nzk0MjE3ODh9.isuay318SqqM2cyJ1S34YG-x7dFx-HzP3b_7SGoW-jw")
    }
}