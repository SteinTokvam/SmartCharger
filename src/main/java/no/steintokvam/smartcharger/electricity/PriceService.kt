package no.steintokvam.smartcharger.electricity

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import okhttp3.OkHttpClient
import okhttp3.Request
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt


class PriceService {
    private val LOGGER: Logger = LoggerFactory.getLogger(PriceService::class.java)
    private val BASE_URL = "https://www.hvakosterstrommen.no/api/v1/prices"
    private val client = OkHttpClient()
    private val formatter = DateTimeFormatter.ISO_DATE_TIME
    private val dateTimeDeserializer = LocalDateTimeDeserializer(formatter)
    private val dateTimeSerializer = LocalDateTimeSerializer(formatter)
    private val javaTimeModule = JavaTimeModule()
        .addSerializer(LocalDateTime::class.java, dateTimeSerializer)
        .addDeserializer(LocalDateTime::class.java, dateTimeDeserializer)
    private val mapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(javaTimeModule)

    private val ALL_ZONES = listOf("NO1", "NO2", "NO3", "NO4", "NO5")

    fun getPrices(zone: String, date: LocalDate): List<ElectricityPrice> {
        if (!ALL_ZONES.contains(zone)) {
            LOGGER.warn(String.format("Zone %s is not valid", zone))
            return emptyList()
        }
        val request = createRequest(zone, date)
        val response = client.newCall(request).execute()

        val collectionType = mapper.typeFactory.constructCollectionType(List::class.java, ElectricityPrice::class.java)
        val prices = mapper.readValue<List<ElectricityPrice>>(response.body?.charStream()?.readText(), collectionType)
            .onEach { it.NOK_per_kWh = it.NOK_per_kWh.format(2) }

        return prices
    }
    private fun Float.format(scale: Int) = "%.${scale}f".format(Locale.US, this).toFloat()

    private fun createRequest(zone: String, date: LocalDate): Request {
        val month = date.monthValue
        val endpoint = if (month > 10)
            "/" + date.year + "/" + month + "-" + date.dayOfMonth + "_" + zone + ".json"
        else
            "/" + date.year + "/0" + month + "-" + date.dayOfMonth + "_" + zone + ".json"

        return Request.Builder()
            .url(BASE_URL + endpoint)
            .get()
            .addHeader("accept", "application/json")
            .build()
    }
}