package no.steintokvam.smartcharger.infra.quartz

import no.steintokvam.smartcharger.infra.quartz.jobs.GetChargingTimesJob
import no.steintokvam.smartcharger.infra.quartz.jobs.GetPricesJob
import org.quartz.*
import org.quartz.impl.StdSchedulerFactory
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class QuartzSchedueler {

    private val LOGGER: Logger = LoggerFactory.getLogger(this::class.java)

    fun schedueleJobs(){
        LOGGER.info("Scheduling jobs.")
        val scheduelerFactory: SchedulerFactory = StdSchedulerFactory()
        val schedueler: Scheduler = scheduelerFactory.scheduler
        schedueler.start()

        schedueleJob(createPriceJobTrigger(), createPriceJobDetail(), schedueler)
        schedueleJob(createGetChargingTimesJobTrigger(), createGetChargingTimesJobDetail(), schedueler)
    }

    private fun schedueleJob(trigger: Trigger, jobDetail: JobDetail, schedueler: Scheduler) {
        schedueler.scheduleJob(jobDetail, trigger)
        LOGGER.info("Job scheduled for " + trigger.nextFireTime.toString())
    }

    private fun createGetChargingTimesJobTrigger(): Trigger {
        return TriggerBuilder.newTrigger()
            .withIdentity("getGetChargingTimesTrigger", "getGetChargingTimesTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 */5 * ? * *"))//At second :00, every 5 minutes starting at minute :00, of every hour
            .build()
    }

    private fun createGetChargingTimesJobDetail(): JobDetail {
        return JobBuilder.newJob(GetChargingTimesJob::class.java).withIdentity("createGetChargingTimesJob", "createGetChargingTimesGroup").build()
    }

    private fun createPriceJobTrigger(): Trigger {
        return TriggerBuilder.newTrigger()
            .withIdentity("getPriceTrigger", "getPriceTrigger")
            .withSchedule(CronScheduleBuilder.cronSchedule("0 30 1,15 ? * * *"))//At second :00, at minute :30, at 01am and 15pm, of every day
            .build()
    }

    private fun createPriceJobDetail(): JobDetail {
        return JobBuilder.newJob(GetPricesJob::class.java).withIdentity("createPriceJob", "createPriceGroup").build()
    }

    private fun createTrigger(): Trigger {
        //initialize time interval
        val timeInterval = 60

        //create a trigger to be returned from the method

        // triggerNew to schedule it in main() method
        return TriggerBuilder.newTrigger().withIdentity("NAME_OF_TRIGGER", "NAME_OF_GROUP")
            .withSchedule(
                SimpleScheduleBuilder.simpleSchedule().withIntervalInSeconds(timeInterval).repeatForever()
            )
            .build()
    }

}