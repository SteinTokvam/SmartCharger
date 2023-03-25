package no.steintokvam.smartcharger

import no.steintokvam.smartcharger.easee.EaseeService
import no.steintokvam.smartcharger.electricity.ElectricityPrice
import no.steintokvam.smartcharger.infra.ValueStore
import no.steintokvam.smartcharger.objects.ChargingTimes
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class SmartCharger {

    private val LOGGER: Logger = LoggerFactory.getLogger(this::class.java)
    private val easeeService = EaseeService()

    fun isChargingFastEnough(): Boolean {
        return easeeService.getChargerState().totalPower > 4f
    }

    private fun isCurrentlyCharging(): Boolean {
        return easeeService.getChargerState().totalPower > 0f
    }

    private fun getLowestPrices(
        date: LocalDateTime,
        hoursToChargeIn: Int,
        estimatedChargingTime: Int
    ) : List<ElectricityPrice> {
        val allPrices = ValueStore.prices
        var cutOffTime = LocalDateTime.now().plusHours(hoursToChargeIn.toLong())

        if(cutOffTime.isBefore(LocalDateTime.now())) {
            cutOffTime = LocalDateTime.of(date.toLocalDate().plusDays(1L), LocalTime.now().plusHours(hoursToChargeIn.toLong()))
        }

        if(getHoursBetween(LocalDateTime.now(), cutOffTime) < estimatedChargingTime || allPrices.isEmpty()) {
            //tar lengre tid å lade enn man har til den er ferdig aka må kjøre på
            return emptyList()
        }

        return allPrices
            .filter { it.time_start.isAfter(LocalDateTime.now()) }
            .filter { it.time_start.isBefore(cutOffTime) }
            .sortedBy { it.NOK_per_kWh }
            .subList(0, estimatedChargingTime)
    }

    fun getChargingTimes(remainingPercent: Int, totalCapacityKwH: Int, finishChargingBy: LocalDateTime): ChargingTimes {
        val currentBatteryLevel = calculateBatteryLevel(remainingPercent, totalCapacityKwH)
        val kwhLeftToCharge = totalCapacityKwH - currentBatteryLevel
        //dette er et desimaltall i antall timer
        val estimatedChargeTime = (kwhLeftToCharge / ValueStore.currentChargingSpeed).roundToInt()

        val lowestPrices = getLowestPrices(
            LocalDateTime.now(),
            getHoursBetween(LocalDateTime.now(), finishChargingBy),
            estimatedChargeTime
        )

        if(lowestPrices.isEmpty()) {
            return ChargingTimes(emptyList(), 0, 0, ValueStore.finnishChargingBy)
        }

        return ChargingTimes(lowestPrices.sortedBy { it.time_start }, kwhLeftToCharge, estimatedChargeTime, finishChargingBy)
    }

    private fun getHoursBetween(now: LocalDateTime, then: LocalDateTime): Int {
        return ChronoUnit.HOURS.between(now, then).toInt()
    }

    fun calculateBatteryLevel(remainingPercent: Int, totalCapacityKwH: Int): Int {
        return (totalCapacityKwH * (remainingPercent / 100f)).roundToInt()
    }

    fun runSmartcharging(chargingTimes: ChargingTimes): Boolean {//Burde heller returnere status.. lader/lader_ikke
        /*
        * Denne forventer at timene er sortert på den som kommer først, og at når timen er over så fjernes den fra listen så man alltid
        * får inn neste ladeperiode på første element
         */

        val now = LocalDateTime.now()
        if(chargingTimes.prices[0].time_end.isAfter(now)){
            //stop charging
            stopCharging()
            return false
        } else if(chargingTimes.prices[0].time_start.isBefore(now) && !isCurrentlyCharging()) {
            //StartCharging
            startCharging()
            return true
        } else if(LocalTime.now().hour >= chargingTimes.finnishChargingBy.hour && !isCurrentlyCharging()) {
            startCharging()
            return true
        }
        return false
    }

    private fun startCharging(): Int {
        if(isCurrentlyCharging()) {
            LOGGER.warn("Tried to start charging while we already are charging.")
            return 400
        }
        return easeeService.startCharging()
    }

    private fun stopCharging(): Int {
        if(!isCurrentlyCharging()) {
            LOGGER.warn("Tried to stop charging when charging were stopped.")
            return 400
        }
        return easeeService.stopCharging()
    }
}