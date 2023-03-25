package no.steintokvam.smartcharger.infra.quartz.jobs

import no.steintokvam.smartcharger.SmartCharger
import no.steintokvam.smartcharger.electricity.ElectricityPrice
import no.steintokvam.smartcharger.infra.ValueStore
import no.steintokvam.smartcharger.objects.ChargingTimes
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class GetChargingTimesJob: Job {
    private val LOGGER: Logger = LoggerFactory.getLogger(this::class.java)
    private val smartCharger = SmartCharger()

    override fun execute(context: JobExecutionContext?) {
        LOGGER.info("Executing GetChargingTimesJob.")
        run()
    }

    fun run() {
        if(ValueStore.prices.isEmpty()) {
            //if no prices then we do not need to get the times to charge
            LOGGER.warn("Got no prices!")
            return
        }

        updateFinishByTime()

        val now = LocalDateTime.now()
        if(smartCharger.isChargingFastEnough() && smartCharger.getHoursBetween(ValueStore.lastReestimate, now) > 1) {
            ValueStore.lastReestimate = now
            getChargingTimes()
        }
    }

    private fun getChargingTimes() {
        val tmpChargingTimes = smartCharger.getChargingTimes(
            ValueStore.remainingPercent,
            ValueStore.totalCapacityKwH,
            ValueStore.finnishChargingBy
        )//finner/reestimerer antall timer man må lade basert på hvor langt unna fulladet vi er
        if(tmpChargingTimes.prices.isNotEmpty()) {
            val prices = mutableListOf<ElectricityPrice>()
            var lastStartTime = tmpChargingTimes.prices[0].time_start.minusHours(2L)
            for (chargeElement in tmpChargingTimes.prices) {
                if (lastStartTime.plusHours(1L).isBefore(chargeElement.time_start)) {
                    prices.add(chargeElement)
                }

                val lastPrice = prices[prices.size - 1]
                val tmpPrice = ElectricityPrice(lastPrice.NOK_per_kWh, lastPrice.EUR_per_kWh, lastPrice.EXR, lastPrice.time_start, chargeElement.time_end)
                prices.removeAt(prices.size-1)
                prices.add(tmpPrice)
                lastStartTime = chargeElement.time_start
            }

            ValueStore.chargingTimes = ChargingTimes(prices, tmpChargingTimes.kwhLeftToCharge, tmpChargingTimes.estimatedChargeTime, tmpChargingTimes.finnishChargingBy)
        }
    }

    private fun updateFinishByTime() {
        if(ValueStore.finnishChargingBy.dayOfMonth == LocalDate.now().dayOfMonth
            && ValueStore.finnishChargingBy.toLocalTime().isBefore(LocalTime.now())) {
            ValueStore.isSmartCharging = false
            //om man har passert tidspunktet til finnishedBy på den dagen man skal være ferdig, så setter man finnishedBy til samme tidspunkt neste dag
            ValueStore.finnishChargingBy = LocalDateTime.of(LocalDate.now().plusDays(1L), LocalTime.of(ValueStore.finnishChargingBy.hour, ValueStore.finnishChargingBy.minute))
        }
    }
}