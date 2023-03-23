package no.steintokvam.smartcharger

import no.steintokvam.smartcharger.easee.EaseeService
import no.steintokvam.smartcharger.electricity.ElectricityPrice
import no.steintokvam.smartcharger.electricity.PriceService
import no.steintokvam.smartcharger.objects.ChargingTimes
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.temporal.ChronoUnit
import kotlin.math.roundToInt

class SmartCharger {
    private val easeeService = EaseeService()
    private val priceService = PriceService()

    fun isCharging(): Boolean {
        return easeeService.getChargerState().totalPower > 0
    }

    fun getLowestPrices(date: LocalDateTime, zone: String, hoursToChargeIn: Int, estimatedChargingTime: Int) : List<ElectricityPrice> {
        val allPrices = priceService.getPrices(zone, date.toLocalDate())
        val cutOffTime = LocalDateTime.now().plusHours(hoursToChargeIn.toLong())
        return allPrices
            .filter { it.time_start.isAfter(LocalDateTime.now()) }
            .filter { it.time_start.isBefore(cutOffTime) }
            .sortedBy { it.NOK_per_kWh }.subList(0, estimatedChargingTime)
    }

    fun getChargingTimes(remainingPercent: Int, totalCapacityKwH: Int, finishChargingBy: LocalDateTime): ChargingTimes {
        val currentBatteryLevel = calculateBatteryLevel(remainingPercent, totalCapacityKwH)
        val kwhLeftToCharge = totalCapacityKwH - currentBatteryLevel
        //dette er et desimaltall i antall timer
        val estimatedChargeTime = (kwhLeftToCharge / 8.9f).roundToInt()//TODO: burde byttes ut med en variabel satt til hva nå enn nåværende hastighet er

        val lowestPrices = getLowestPrices(LocalDateTime.now(), "NO1", getHoursBetween(LocalDateTime.now(), finishChargingBy), estimatedChargeTime).sortedBy { it.time_start }

        return ChargingTimes(lowestPrices, kwhLeftToCharge, estimatedChargeTime, finishChargingBy)
    }

    private fun getHoursBetween(now: LocalDateTime, then: LocalDateTime): Int {
        return ChronoUnit.HOURS.between(now, then).toInt()
    }

    private fun calculateBatteryLevel(remainingPercent: Int, totalCapacityKwH: Int): Int {
        return (totalCapacityKwH * (remainingPercent / 100f)).roundToInt()
    }

    fun runSmartcharging(chargingTimes: ChargingTimes) {
        /*
        * Denne forventer at timene er sortert på den som kommer først, og at når timen er over så fjernes den fra listen så man alltid
        * får inn neste ladeperiode på første element
         */

        if(chargingTimes.prices[0].time_start.hour == LocalTime.now().hour) {
            //StartCharging
            toggleCharging()
        } else if(LocalTime.now().hour >= chargingTimes.finnishChargingBy.hour) {
            // denne skrur på lading dersom den ikke står på nå og må derfor sjekke for det
            toggleCharging()
        } else {
            //stop charging
            toggleCharging()
        }
    }

    fun toggleCharging(): Int {
        return easeeService.toggleCharging()
    }

    fun stopCharging() {

    }
}