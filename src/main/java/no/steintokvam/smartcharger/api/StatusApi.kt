package no.steintokvam.smartcharger.api

import no.steintokvam.smartcharger.api.objects.Status
import no.steintokvam.smartcharger.infra.ValueStore
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class StatusApi {

    @GetMapping("/status")
    fun status(): Status {
        return Status(ValueStore.isSmartCharging, ValueStore.currentChargingSpeed, ValueStore.smartChargingEnabled, ValueStore.chargingThreshold, ValueStore.totalCapacityKwH, ValueStore.remainingPercent, ValueStore.chargingTimes)
    }
}