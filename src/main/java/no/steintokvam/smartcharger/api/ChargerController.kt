package no.steintokvam.smartcharger.api

import jakarta.websocket.server.PathParam
import no.steintokvam.smartcharger.SmartCharger
import no.steintokvam.smartcharger.api.objects.ToggleSmartCharging
import no.steintokvam.smartcharger.infra.ValueStore
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import kotlin.reflect.jvm.internal.ReflectProperties.Val

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

    @PostMapping("/smartcharging/chargingthreshold")
    fun setChargingThreshold(@RequestParam speed: Float) {
        ValueStore.chargingThreshold = speed
    }

    @PostMapping("smartscharging/totalCapacity")
    fun setTotalCapacity(@RequestParam totalCapacity: Int) {
        ValueStore.totalCapacityKwH = totalCapacity
    }
}