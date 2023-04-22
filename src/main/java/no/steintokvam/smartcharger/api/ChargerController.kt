package no.steintokvam.smartcharger.api

import no.steintokvam.smartcharger.SmartCharger
import no.steintokvam.smartcharger.api.objects.ToggleSmartCharging
import no.steintokvam.smartcharger.easee.EaseeService
import no.steintokvam.smartcharger.easee.objects.AccessToken
import no.steintokvam.smartcharger.infra.ValueStore
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import java.time.LocalDateTime

@RestController
class ChargerController {

    private val smartCharger = SmartCharger()

    @PostMapping("/smartcharging/on")
    fun turnOnSmartcharging(): ToggleSmartCharging {
        if(ValueStore.smartChargingEnabled) {
            return ToggleSmartCharging(gotToggled = false, alreadyWasToggled = true)
        }
        ValueStore.smartChargingEnabled = true
        smartCharger.resetSmartcharging(LocalDateTime.now())
        return ToggleSmartCharging(gotToggled = true, alreadyWasToggled = false)
    }
    @PostMapping("/smartcharging/off")
    fun turnOffSmartcharging(): ToggleSmartCharging {
        if(!ValueStore.smartChargingEnabled) {
            return ToggleSmartCharging(gotToggled = false, alreadyWasToggled = true)
        }
        ValueStore.smartChargingEnabled = false
        smartCharger.resetSmartcharging(LocalDateTime.now())
        return ToggleSmartCharging(gotToggled = true, alreadyWasToggled = false)
    }

    @PostMapping("/smartcharging/chargingthreshold")
    fun setChargingThreshold(@RequestParam speed: Float) {
        ValueStore.chargingThreshold = speed
    }

    @PostMapping("/smartcharging/totalCapacity")
    fun setTotalCapacity(@RequestParam totalCapacity: Int) {
        ValueStore.totalCapacityKwH = totalCapacity
    }

    @PostMapping("/smartcharging/remainingPercent")
    fun setRemainingPercent(@RequestParam remainingPercent: Int) {
        ValueStore.initialBatteryPercent = remainingPercent
        ValueStore.remainingPercent = remainingPercent
    }

    @GetMapping("/test/refresh")
    fun getNewAccessToken(): AccessToken {//TODO: fjern denne om refresh token nå funker
        EaseeService().refreshToken()
        return ValueStore.accessToken
    }
}