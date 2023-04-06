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
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.*
import kotlin.math.roundToInt


class PriceService {
    private val LOGGER: Logger = LoggerFactory.getLogger(this::class.java)
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
        val prices: MutableList<ElectricityPrice> = getPriceForDay(zone, date)

        if(LocalTime.now().isAfter(LocalTime.of(15,0))) {
            prices.addAll(getPriceForDay(zone, date.plusDays(1)))
        }

        return prices
    }

    private fun getPriceForDay(zone: String, date: LocalDate): MutableList<ElectricityPrice> {
        if (!ALL_ZONES.contains(zone)) {
            LOGGER.warn(String.format("Zone %s is not valid", zone))
            return mutableListOf()
        }
        val request = createRequest(zone, date)
        val response = client.newCall(request).execute()

        val collectionType = mapper.typeFactory.constructCollectionType(List::class.java, ElectricityPrice::class.java)
        return mapper.readValue<List<ElectricityPrice>>(response.body?.charStream()?.readText(), collectionType)
            .onEach { it.NOK_per_kWh = it.NOK_per_kWh.format(2) }.toMutableList()
    }

    private fun Float.format(scale: Int) = "%.${scale}f".format(Locale.US, this).toFloat()

    private fun createRequest(zone: String, date: LocalDate): Request {
        val day = date.dayOfMonth
        val month = date.monthValue
        val monthEndpoint = if (month > 10)
            "/$month"
        else
            "/0$month"

        val dayEndpoint = if (day > 10)
            "-$day"
        else
            "-0$day"

        val endpoint =
            "/" + date.year + monthEndpoint + dayEndpoint + "_" + zone + ".json"


        return Request.Builder()
            .url(BASE_URL + endpoint)
            .get()
            .addHeader("accept", "application/json")
            .build()
    }
}