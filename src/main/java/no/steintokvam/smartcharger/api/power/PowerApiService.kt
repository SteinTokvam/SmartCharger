package no.steintokvam.smartcharger.api.power

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer
import com.fasterxml.jackson.module.kotlin.KotlinModule
import no.steintokvam.smartcharger.electricity.ElectricityPrice
import no.steintokvam.smartcharger.infra.ValueStore
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class PowerApiService {

    private val client = OkHttpClient()
    private val LOGGER = LoggerFactory.getLogger(this::class.java)

    private val formatter = DateTimeFormatter.ISO_DATE_TIME//TODO: mapperen burde være en sentral pålass og hentes derifra
    private val dateTimeDeserializer = LocalDateTimeDeserializer(formatter)
    private val dateTimeSerializer = LocalDateTimeSerializer(formatter)
    private val javaTimeModule = JavaTimeModule()
        .addSerializer(LocalDateTime::class.java, dateTimeSerializer)
        .addDeserializer(LocalDateTime::class.java, dateTimeDeserializer)
    private val mapper = ObjectMapper()
        .registerModule(KotlinModule.Builder().build())
        .registerModule(javaTimeModule)

    fun getAllPrices():List<ElectricityPrice> {
        val request = createGetRequest("/prices/all")
        return doRequest(request)
    }

    private fun doRequest(request: Request): List<ElectricityPrice> {
        var response: Response? = null

        try {
            response = client.newCall(request).execute()
            val collectionType = mapper.typeFactory.constructCollectionType(List::class.java, ElectricityPrice::class.java)
            val prices =
                mapper.readValue<List<ElectricityPrice>>(response.body?.charStream()?.readText(), collectionType)
                    .onEach { it.NOK_per_kWh = it.NOK_per_kWh.format(2) }.toMutableList()
            response.close()
            return prices
        } catch (e: Exception) {
            LOGGER.warn("Couldn't call ${request.url} for zone ${ValueStore.zone}")
            LOGGER.error(e.stackTraceToString())
        } finally {
            response?.close()
        }
        return emptyList()
    }

    fun getPricesFor(from: LocalDateTime, hours: Int, to: LocalDateTime):List<ElectricityPrice> {
        val request = createGetRequest("/prices/cheapest/$from/$hours/$to")
        return doRequest(request)
    }

    private fun Float.format(scale: Int) = "%.${scale}f".format(Locale.US, this).toFloat()
    private fun createGetRequest(endpoint: String): Request {
        return createRequestBuilder(endpoint)
            .get()
            .build()
    }

    private fun createRequestBuilder(endpoint: String): Request.Builder {
        return Request.Builder()
            .url(ValueStore.powerPriceURL+endpoint)
            .addHeader("accept", "application/json")
            }
}