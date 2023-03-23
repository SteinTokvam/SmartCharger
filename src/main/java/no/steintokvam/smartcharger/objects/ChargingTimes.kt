package no.steintokvam.smartcharger.objects

import no.steintokvam.smartcharger.electricity.ElectricityPrice

data class ChargingTimes(
    val prices: List<ElectricityPrice>,
    val kwhLeftToCharge: Int,
    val estimatedChargeTime: Int,
    val finnishChargingBy: LocalDateTimes
)
