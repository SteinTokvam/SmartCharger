package no.steintokvam.smartcharger.infra.quartz.jobs

import no.steintokvam.smartcharger.SmartCharger
import no.steintokvam.smartcharger.electricity.ElectricityPrice
import no.steintokvam.smartcharger.infra.ValueStore
import no.steintokvam.smartcharger.objects.ChargingTimes
import org.quartz.Job
import org.quartz.JobBuilder
import org.quartz.JobDataMap
import org.quartz.JobExecutionContext
import org.quartz.Scheduler
import org.quartz.TriggerBuilder
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*


class GetChargingTimesJob: Job {
    private val LOGGER: Logger = LoggerFactory.getLogger(this::class.java)
    private val smartCharger = SmartCharger()

    override fun execute(context: JobExecutionContext?) {
        run(context)
    }

    fun run(context: JobExecutionContext?) {
        val now = LocalDateTime.now()
        if(!ValueStore.smartChargingEnabled) {
            updateFinnishByTime(now)
            LOGGER.info("Smartcharging is not enabled.")
            return
        }
        if(ValueStore.prices.isEmpty()) {
            //if no prices then we do not need to get the times to charge
            LOGGER.warn("Got no prices!")
            return
        }

        resetSmartcharging(now)
        smartCharger.updateCurrentChargingSpeed()
        if(smartCharger.isChargingFastEnough() && smartCharger.getHoursBetween(ValueStore.lastReestimate, now) > 1) {
            if(ValueStore.smartChargingSchedueled) {
                return
            }
            getChargingTimes()
            updateBatteryPercent()
            schedueleCharging(context)
        }
    }

    private fun updateBatteryPercent() {
        ValueStore.remainingPercent = smartCharger.calculateRemainingBatteryPercent()
    }

    private fun getChargingTimes() {
        val tmpChargingTimes = smartCharger.getChargingTimes(//TODO: denne tryner om den ikke klarer å finne noen priser
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
                val tmpPrice = ElectricityPrice(lastPrice.NOK_per_kWh, lastPrice.eur_per_kWh, lastPrice.exr, lastPrice.time_start, chargeElement.time_end)
                prices.removeAt(prices.size-1)
                prices.add(tmpPrice)
                lastStartTime = chargeElement.time_start
            }

            ValueStore.chargingTimes = ChargingTimes(prices, tmpChargingTimes.kwhLeftToCharge, tmpChargingTimes.estimatedChargeTime, tmpChargingTimes.finnishChargingBy)
            ValueStore.lastReestimate = LocalDateTime.now()
            logChargingtimes()
        }
    }

    private fun schedueleCharging(context: JobExecutionContext?) {
        val dataMap: JobDataMap = context!!.jobDetail.jobDataMap

        val schedueler = dataMap["schedueler"]
        if(schedueler is Scheduler && ValueStore.chargingTimes.prices.isNotEmpty()) {
            if(ValueStore.chargingTimes.prices[0].time_start.isAfter(LocalDateTime.now())) {
                val trigger = TriggerBuilder.newTrigger()
                    .withIdentity(UUID.randomUUID().toString(), UUID.randomUUID().toString())
                    .startAt(Date.from(ValueStore.chargingTimes.prices[0].time_start.atZone(ZoneId.systemDefault()).toInstant()))
                    .build()
                val jobDetail = JobBuilder.newJob(StartChargingJob::class.java)
                    .withIdentity("startChargingJob", "chargingGroup")
                    .build()
                schedueler.scheduleJob(jobDetail, trigger)//starter å lade ved første ladetid
                LOGGER.info("Smartcharging scheduled to start at ${trigger.nextFireTime}")
                ValueStore.smartChargingSchedueled = true
                smartCharger.stopCharging()
            } else {
                LOGGER.info("Start time has already been. Continuing charge.")
            }
        } else {
            LOGGER.error("Scheduler not sent to scheduler variable! got ${schedueler!!::class.java}. Can't start charging.")
        }
    }

    private fun logChargingtimes() {
        LOGGER.info("Reestimated prices.")
        ValueStore.chargingTimes.prices.forEach {
            LOGGER.info("Price: ${it.NOK_per_kWh} time_start: ${it.time_start} time_end: ${it.time_end}")
        }
    }

    private fun resetSmartcharging(now: LocalDateTime) {
        if(ValueStore.finnishChargingBy.dayOfMonth == now.dayOfMonth
            && ValueStore.finnishChargingBy.toLocalTime().isBefore(now.toLocalTime())) {
            ValueStore.isSmartCharging = false
            ValueStore.smartChargingSchedueled = false
            smartCharger.startCharging()
            updateFinnishByTime(now)
        }
    }

    private fun updateFinnishByTime(now: LocalDateTime) {
        if(ValueStore.finnishChargingBy.dayOfMonth == now.dayOfMonth
            && ValueStore.finnishChargingBy.toLocalTime().isBefore(now.toLocalTime())) {
            ValueStore.finnishChargingBy = LocalDateTime.of(
                now.toLocalDate().plusDays(1L),
                LocalTime.of(ValueStore.finnishChargingBy.hour, ValueStore.finnishChargingBy.minute)
            )
            LOGGER.info("Updated finnishChargingBy to ${ValueStore.finnishChargingBy}.")
        }
    }
}