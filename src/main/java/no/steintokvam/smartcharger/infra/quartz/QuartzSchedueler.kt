package no.steintokvam.smartcharger.infra.quartz

import no.steintokvam.smartcharger.infra.quartz.jobs.AuthenticationJob
import no.steintokvam.smartcharger.infra.quartz.jobs.GetChargingTimesJob
import no.steintokvam.smartcharger.infra.quartz.jobs.GetPricesJob
import org.quartz.*
import org.quartz.SimpleScheduleBuilder.simpleSchedule
import org.quartz.TriggerBuilder.newTrigger
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.*


class QuartzSchedueler {

    private val LOGGER: Logger = LoggerFactory.getLogger(this::class.java)

    fun schedueleJobs(){
        LOGGER.info("Scheduling jobs.")
        val scheduelerFactory: SchedulerFactory = StdSchedulerFactory()
        val schedueler: Scheduler = scheduelerFactory.scheduler
        schedueler.start()

        scheduleJob(createPriceJobTrigger(), createPriceJobDetail(), schedueler)
        scheduleJob(createGetChargingTimesJobTrigger(), createGetChargingTimesJobDetail(schedueler), schedueler)
        scheduleJob(createAuthenticationJobTrigger(), createAuthenticationJobDetail(), schedueler)
    }

    private fun scheduleJob(trigger: Trigger, jobDetail: JobDetail, schedueler: Scheduler) {
        schedueler.scheduleJob(jobDetail, trigger)
        LOGGER.info("Job scheduled for " + trigger.nextFireTime.toString())
    }

    private fun createAuthenticationJobTrigger(): Trigger {
        return newTrigger()
            .withIdentity("startCharging", "chargingGroup")
            .startAt(Date.from(LocalDateTime.of(LocalDate.now(), LocalTime.of(23,0)).atZone(ZoneId.systemDefault()).toInstant()))
            .withSchedule(simpleSchedule()
                .withIntervalInHours(23)
                .repeatForever())
            .build()
    }

    private fun createAuthenticationJobDetail(): JobDetail {
        return JobBuilder.newJob(AuthenticationJob::class.java).withIdentity("authenticationJob", "authenticationGroup").build()
    }

    private fun createGetChargingTimesJobTrigger(): Trigger {//denne vil da reestimere ladetiden hvert 5 minutt sett at det er en bil som lader fort nok for øyeblikket
        return newTrigger()
            .withIdentity("getGetChargingTimesTrigger", "getGetChargingTimesTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * ? * *"))//At second :00, every 5 minutes starting at minute :00, of every hour
            .build()
    }

    private fun createGetChargingTimesJobDetail(schedueler: Scheduler): JobDetail {
        return JobBuilder.newJob(GetChargingTimesJob::class.java)
            .withIdentity("createGetChargingTimesJob", "createGetChargingTimesGroup")
            .setJobData(JobDataMap(mutableMapOf(Pair("schedueler", schedueler))))
            .build()
    }

    private fun createPriceJobTrigger(): Trigger {
        return newTrigger()
            .withIdentity("getPriceTrigger", "getPriceTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 15 14 ? * * *"))//At 14:15:00pm every day
            .build()
    }

    private fun createPriceJobDetail(): JobDetail {
        return JobBuilder.newJob(GetPricesJob::class.java).withIdentity("createPriceJob", "createPriceGroup").build()
    }
}