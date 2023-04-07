package no.steintokvam.smartcharger.infra.quartz.jobs

import no.steintokvam.smartcharger.easee.EaseeService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.slf4j.LoggerFactory

class AuthenticationJob: Job {
    private val LOGGER = LoggerFactory.getLogger(this::class.java)
    override fun execute(context: JobExecutionContext?) {
        LOGGER.info("refreshing authentication token.")
        EaseeService().refreshToken()
    }
}