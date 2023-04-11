package no.steintokvam.smartcharger

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
        return easeeService.getChargerState().totalPower > ValueStore.chargingThreshold
    }

    private fun isCurrentlyCharging(): Boolean {
        val chargerState = easeeService.getChargerState()
        return chargerState.totalPower > 0f
    }

    fun updateCurrentChargingSpeed() {
        val chargerState = easeeService.getChargerState()
        ValueStore.currentChargingSpeed = chargerState.totalPower
    }

    private fun getLowestPrices(
        date: LocalDateTime,
        estimatedChargingTime: Int
    ) : List<ElectricityPrice> {
        val allPrices = ValueStore.prices

        if(getHoursBetween(date, ValueStore.finnishChargingBy) < estimatedChargingTime || allPrices.isEmpty()) {
            //tar lengre tid å lade enn man har til den er ferdig aka må kjøre på eller man har ingen priser
            return emptyList()
        }

        val sortedPrices = allPrices
            .filter { it.time_start.isAfter(date) || it.time_start.hour == date.hour }
            .filter { it.time_start.isBefore(ValueStore.finnishChargingBy) }
            .sortedBy { it.NOK_per_kWh }

        if(sortedPrices.size < estimatedChargingTime) {
            LOGGER.error("Has ${sortedPrices.size} prices, but expected to have at least $estimatedChargingTime prices.")
            LOGGER.info("Has ${allPrices.size} unfiltered prices. Finnish charging by is: ${ValueStore.finnishChargingBy}")
            LOGGER.info("All prices:")
            allPrices.forEach { LOGGER.info("${it.time_start}") }
            return sortedPrices
        }
        return sortedPrices
            .subList(0, estimatedChargingTime)
    }

    fun getChargingTimes(remainingPercent: Int, totalCapacityKwH: Int, finishChargingBy: LocalDateTime): ChargingTimes {
        val currentBatteryLevel = calculateBatteryLevel(remainingPercent, totalCapacityKwH)
        val kwhLeftToCharge = totalCapacityKwH - currentBatteryLevel
        //dette er et desimaltall i antall timer
        val estimatedChargeTime = (kwhLeftToCharge / ValueStore.currentChargingSpeed).roundToInt()

        val lowestPrices = getLowestPrices(
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

    fun calculateRemainingBatteryPercent(totalCapacityKwH: Int): Int {
        return ((easeeService.getChargerState().sessionEnergy / ValueStore.totalCapacityKwH) * 100).toInt()
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