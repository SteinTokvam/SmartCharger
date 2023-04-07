package no.steintokvam.smartcharger.infra.quartz.jobs

import no.steintokvam.smartcharger.SmartCharger
import org.quartz.Job
import org.quartz.JobExecutionContext

class StartChargingJob: Job {
    override fun execute(context: JobExecutionContext?) {
        SmartCharger().startCharging()
    }
}