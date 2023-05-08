package no.steintokvam.smartcharger.infra.quartz.jobs

import no.steintokvam.smartcharger.SmartCharger
import no.steintokvam.smartcharger.electricity.ElectricityPrice
import no.steintokvam.smartcharger.infra.ValueStore
import no.steintokvam.smartcharger.objects.ChargingTimes
import org.quartz.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
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
        val dataMap: JobDataMap = context!!.jobDetail.jobDataMap
        val jobName = "startChargingJob"
        val jobGroup = "chargingGroup"

        val scheduler = dataMap["scheduler"]

        if(scheduler is Scheduler) {
            if (!ValueStore.smartChargingEnabled) {
                smartCharger.updateFinnishByTime(now)
                LOGGER.info("Smartcharging is not enabled.")
                if(scheduler.jobGroupNames.contains(jobGroup)) {
                    for(s in ValueStore.startJobNames) {

                        scheduler.deleteJob(JobKey(jobName, jobGroup))
                        LOGGER.info("Deleted scheduled starting jobs.")
                    }
                    for(s in ValueStore.pauseJobNames) {
                        scheduler.deleteJob(JobKey(jobName, jobGroup))
                        LOGGER.info("Deleted scheduled pausing jobs.")
                    }
                    ValueStore.startJobNames = emptyList()
                    ValueStore.pauseJobNames = emptyList()
                    LOGGER.info("Currently scheduled jobs are: ${scheduler.jobGroupNames}")
                }
                return
            }
            smartCharger.updateFinnishByTime(now)
            smartCharger.updateCurrentChargingSpeed()
            if (smartCharger.isChargingFastEnough() && smartCharger.getHoursBetween(
                    ValueStore.lastReestimate,
                    now
                ) > 1
            ) {
                if (ValueStore.smartChargingSchedueled) {
                    return
                }
                getChargingTimes()
                smartCharger.calculateRemainingBatteryPercent()
                schedueleCharging(scheduler, jobGroup)
            }
        } else {
            LOGGER.error("Scheduler not sent to scheduler variable! got ${scheduler!!::class.java}. Can't start charging.")
        }
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

    private fun schedueleCharging(schedueler: Scheduler, jobGroup: String) {
        if (ValueStore.chargingTimes.prices.isNotEmpty()) {
            if (ValueStore.chargingTimes.prices[0].time_start.isAfter(LocalDateTime.now())) {
                createQuartzChargingJob(schedueler, UUID.randomUUID().toString(), jobGroup, 0, true)
                ValueStore.smartChargingSchedueled = true
                smartCharger.stopCharging()

                for(i in 1..ValueStore.chargingTimes.prices.size) {
                    createQuartzChargingJob(schedueler, UUID.randomUUID().toString(), jobGroup, i, true)
                    if(i != ValueStore.chargingTimes.prices.size-1) {
                        createQuartzChargingJob(schedueler, UUID.randomUUID().toString(), jobGroup, i, false)
                    }
                }
            } else {
                LOGGER.info("Start time has already been. Continuing charge.")
            }
        } else {
            LOGGER.info("Has no power prices! Will not schedule charging time.")
        }
    }

    private fun createQuartzChargingJob(schedueler: Scheduler, jobName: String, jobGroup: String, index: Int, isStart: Boolean) {
        if(isStart) {
            val trigger = createTriggerAndDetail(schedueler, jobName, jobGroup, index, StartChargingJob::class.java)
            LOGGER.info("Smartcharging scheduled to start at ${trigger.nextFireTime}")
            ValueStore.startJobNames.add(jobName)
        } else {
            val trigger = createTriggerAndDetail(schedueler, jobName, jobGroup, index, PauseChargingJob::class.java)
            LOGGER.info("Smartcharging scheduled to pause at ${trigger.nextFireTime}")
            ValueStore.pauseJobNames.add(jobName)
        }
    }

    private fun createTriggerAndDetail(schedueler: Scheduler, jobName: String, jobGroup: String, index: Int, jobClass: Class<out Job>): Trigger {
        val trigger = TriggerBuilder.newTrigger()
            .withIdentity(UUID.randomUUID().toString(), UUID.randomUUID().toString())
            .startAt(
                Date.from(
                    ValueStore.chargingTimes.prices[index].time_start.atZone(ZoneId.systemDefault()).toInstant()
                )
            )
            .build()
        val jobDetail = JobBuilder.newJob(jobClass)
            .withIdentity(jobName, jobGroup)
            .build()
        schedueler.scheduleJob(jobDetail, trigger)
        return trigger
    }

    private fun logChargingtimes() {
        LOGGER.info("Reestimated prices.")
        ValueStore.chargingTimes.prices.forEach {
            LOGGER.info("Price: ${it.NOK_per_kWh} time_start: ${it.time_start} time_end: ${it.time_end}")
        }
    }
}