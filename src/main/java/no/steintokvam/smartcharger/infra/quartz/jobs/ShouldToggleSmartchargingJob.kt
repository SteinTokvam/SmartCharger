package no.steintokvam.smartcharger.infra.quartz.jobs

import no.steintokvam.smartcharger.SmartCharger
import no.steintokvam.smartcharger.infra.ValueStore
import no.steintokvam.smartcharger.objects.ChargingTimes
import org.quartz.Job
import org.quartz.JobExecutionContext
import java.time.LocalDateTime

class ShouldToggleSmartchargingJob: Job {//TODO: sett opp en trigger

    override fun execute(context: JobExecutionContext?) {
        //hvis man treffer starttidspunkt så må lading startes
        val hasStarted = SmartCharger().runSmartcharging(ValueStore.chargingTimes)
        if(hasStarted && ValueStore.chargingTimes.prices[0].time_end.isAfter(LocalDateTime.now())) {
            val tmpPrices = ValueStore.chargingTimes.prices.toMutableList()
            tmpPrices.removeAt(0)
            //TODO: må oppdatre batterinivå før kall på denne
            val kwhLeftToCharge = ValueStore.totalCapacityKwH - SmartCharger().calculateBatteryLevel(
                ValueStore.remainingPercent,
                ValueStore.totalCapacityKwH
            )
            ValueStore.chargingTimes = ChargingTimes(tmpPrices,
                kwhLeftToCharge, ValueStore.chargingTimes.estimatedChargeTime, ValueStore.finnishChargingBy)
        }
    }
}