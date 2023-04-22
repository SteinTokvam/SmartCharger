package no.steintokvam.smartcharger

import no.steintokvam.smartcharger.api.power.PowerApiService
import no.steintokvam.smartcharger.easee.EaseeService
import no.steintokvam.smartcharger.electricity.ElectricityPrice
import no.steintokvam.smartcharger.infra.ValueStore
import no.steintokvam.smartcharger.objects.ChargingTimes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class SmartCharger {

    private val LOGGER: Logger = LoggerFactory.getLogger(this::class.java)
    private val easeeService = EaseeService()

    fun isChargingFastEnough(): Boolean {
        val chargerState = easeeService.getChargerState()
        if(chargerState != null) {
            return chargerState.totalPower > ValueStore.chargingThreshold
        }
        return false
    }

    private fun isCurrentlyCharging(): Boolean {
        val chargerState = easeeService.getChargerState()
        if(chargerState != null) {
            return chargerState.totalPower > 0f
        }
        return false
    }

    fun updateCurrentChargingSpeed() {
        val chargerState = easeeService.getChargerState()
        if(chargerState != null) {
            ValueStore.currentChargingSpeed = chargerState.totalPower
        } else {
            LOGGER.warn("Couldn't update current charging speed since charger state couldn't be retrieved.")
        }
    }


    fun getChargingTimes(remainingPercent: Int, totalCapacityKwH: Int, finishChargingBy: LocalDateTime): ChargingTimes {
        val currentBatteryLevel = calculateBatteryLevel(remainingPercent, totalCapacityKwH)
        val kwhLeftToCharge = totalCapacityKwH - currentBatteryLevel
        //dette er et desimaltall i antall timer
        val estimatedChargeTime = (kwhLeftToCharge / ValueStore.currentChargingSpeed).roundToInt()

        val lowestPrices = PowerApiService().getPricesFor(
            LocalDateTime.now(),
            estimatedChargeTime
        )

        if(lowestPrices.isEmpty()) {
            return ChargingTimes(emptyList(), 0, 0, ValueStore.finnishChargingBy)
        }

        return ChargingTimes(lowestPrices.sortedBy { it.time_start }, kwhLeftToCharge, estimatedChargeTime, finishChargingBy)
    }

    fun getHoursBetween(first: LocalDateTime, second: LocalDateTime): Int {//TODO: burde være i en util klasse
        return ChronoUnit.HOURS.between(first, second).toInt()
    }

    fun calculateBatteryLevel(remainingPercent: Int, totalCapacityKwH: Int): Int {
        return (totalCapacityKwH * (remainingPercent / 100f)).roundToInt()
    }

    fun calculateRemainingBatteryPercent() {//må ha startsbatteriprosent. regne om til kwt og legge på sessionEnergy og finne prosenten av det
        val initialKwt = calculateBatteryLevel(ValueStore.initialBatteryPercent, ValueStore.totalCapacityKwH)

        val chargerState = easeeService.getChargerState()
        if(chargerState != null) {
            ValueStore.remainingPercent = (((initialKwt + chargerState.sessionEnergy) / ValueStore.totalCapacityKwH) * 100).toInt()
        }
        LOGGER.warn("Couldn't calculate remaining batteryPercent since we couldn't retrieve this charge session energy usage from Easee.")
    }

    fun startCharging(): Int {
        if(isCurrentlyCharging()) {
            LOGGER.warn("Tried to start charging while we already are charging.")
            return 400
        }
        return easeeService.resumeCharging()
    }

    fun stopCharging(): Int {
        if(!isCurrentlyCharging()) {
            LOGGER.warn("Tried to stop charging when charging were stopped.")
            return 400
        }
        return easeeService.pauseCharging()
    }
}