package no.steintokvam.smartcharger.api

import no.steintokvam.smartcharger.SmartCharger
import no.steintokvam.smartcharger.api.objects.ToggleSmartCharging
import no.steintokvam.smartcharger.infra.ValueStore
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class ChargerController {

    @PostMapping("smartcharging/on")
    fun turnOnSmartcharging(): ToggleSmartCharging {
        if(ValueStore.smartChargingEnabled) {
            return ToggleSmartCharging(gotToggled = false, alreadyWasToggled = true)
        }
        ValueStore.smartChargingEnabled = true
        SmartCharger().startCharging()
        return ToggleSmartCharging(gotToggled = true, alreadyWasToggled = false)
    }
    @PostMapping("smartcharging/off")
    fun turnOffSmartcharging(): ToggleSmartCharging {
        if(!ValueStore.smartChargingEnabled) {
            return ToggleSmartCharging(gotToggled = false, alreadyWasToggled = true)
        }
        ValueStore.smartChargingEnabled = false
        SmartCharger().startCharging()
        return ToggleSmartCharging(gotToggled = true, alreadyWasToggled = false)
    }
}