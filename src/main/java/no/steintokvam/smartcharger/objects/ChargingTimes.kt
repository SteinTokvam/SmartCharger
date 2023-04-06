package no.steintokvam.smartcharger.objects

import no.steintokvam.smartcharger.electricity.ElectricityPrice
import java.time.LocalDateTime

data class ChargingTimes(
    val prices: List<ElectricityPrice>,
    val kwhLeftToCharge: Int,
    val estimatedChargeTime: Int,
    val finnishChargingBy: LocalDateTime
)
