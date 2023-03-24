package no.steintokvam.smartcharger.infra.quartz.jobs

import no.steintokvam.smartcharger.electricity.PriceService
import no.steintokvam.smartcharger.infra.ValueStore
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate

class GetPricesJob: Job {
    private val LOGGER: Logger = LoggerFactory.getLogger(this::class.java)
    val priceService = PriceService()
    override fun execute(context: JobExecutionContext?) {
        val today = LocalDate.now()
        LOGGER.info("Excecuting getPriceJob at $today")
        if(ValueStore.prices.isEmpty()) {
            ValueStore.prices = priceService.getPrices(ValueStore.zone, today)
        } else if(ValueStore.prices.size >= 24) {
            ValueStore.prices = emptyList()
            ValueStore.prices = priceService.getPrices(ValueStore.zone, today)
        }
        LOGGER.info("Got ${ValueStore.prices.size} prices.")
    }
}