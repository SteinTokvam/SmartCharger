package no.steintokvam.smartcharger

import no.steintokvam.smartcharger.api.power.PowerApiService
import no.steintokvam.smartcharger.easee.EaseeService
import no.steintokvam.smartcharger.infra.ValueStore
import no.steintokvam.smartcharger.objects.ChargingTimes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
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
            estimatedChargeTime,
            ValueStore.finnishChargingBy
        )

        if(lowestPrices.isEmpty()) {
            return ChargingTimes(emptyList(), 0, 0, ValueStore.finnishChargingBy)
        }

        return ChargingTimes(lowestPrices.sortedBy { it.time_start }, kwhLeftToCharge, estimatedChargeTime, finishChargingBy)
    }

    private fun calculateBatteryLevel(remainingPercent: Int, totalCapacityKwH: Int): Int {
        return (totalCapacityKwH * (remainingPercent / 100f)).roundToInt()
    }

    fun calculateRemainingBatteryPercent() {//må ha startsbatteriprosent. regne om til kwt og legge på sessionEnergy og finne prosenten av det
        val initialKwt = calculateBatteryLevel(ValueStore.initialBatteryPercent, ValueStore.totalCapacityKwH)

        val chargerState = easeeService.getChargerState()
        if(chargerState != null) {
            ValueStore.remainingPercent = (((initialKwt + chargerState.sessionEnergy) / ValueStore.totalCapacityKwH) * 100).toInt()
            return
        }
        LOGGER.warn("Couldn't calculate remaining batteryPercent since we couldn't retrieve this charge session energy usage from Easee.")
    }

    fun resetSmartcharging(now: LocalDateTime) {
        ValueStore.isSmartCharging = false
        ValueStore.smartChargingSchedueled = false
        ValueStore.lastReestimate = LocalDateTime.now().minusDays(1L)
        startCharging()
        if(ValueStore.finnishChargingBy.dayOfMonth == now.dayOfMonth
            && ValueStore.finnishChargingBy.toLocalTime().isBefore(now.toLocalTime())) {
            updateFinnishByTime(now)
        }
    }

    fun updateFinnishByTime(now: LocalDateTime) {
        if(ValueStore.finnishChargingBy.dayOfMonth == now.dayOfMonth
            && ValueStore.finnishChargingBy.toLocalTime().isBefore(now.toLocalTime())) {
            ValueStore.finnishChargingBy = LocalDateTime.of(
                LocalDate.now().plusDays(1L),
                LocalTime.of(ValueStore.finnishChargingBy.hour, ValueStore.finnishChargingBy.minute)
            )
            LOGGER.info("Updated finnishChargingBy to ${ValueStore.finnishChargingBy}.")
        } else if(ValueStore.finnishChargingBy.dayOfMonth != now.dayOfMonth) {
            ValueStore.finnishChargingBy= LocalDateTime.of(LocalDate.now().plusDays(1L), LocalTime.of(7, 0))
            LOGGER.info("Updated finnishCharging by to ${ValueStore.finnishChargingBy}. Finnish charging by hasn't been updated in over 24 hours.")
        }
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