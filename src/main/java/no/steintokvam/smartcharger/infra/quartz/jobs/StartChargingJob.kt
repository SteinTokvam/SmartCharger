package no.steintokvam.smartcharger.infra.quartz.jobs

import no.steintokvam.smartcharger.SmartCharger
import no.steintokvam.smartcharger.infra.ValueStore
import org.quartz.Job
import org.quartz.JobExecutionContext

class StartChargingJob: Job {
    override fun execute(context: JobExecutionContext?) {
        ValueStore.isSmartCharging = true
        SmartCharger().startCharging()
    }
}