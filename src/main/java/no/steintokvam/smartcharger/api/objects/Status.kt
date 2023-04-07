package no.steintokvam.smartcharger.api.objects

import no.steintokvam.smartcharger.objects.ChargingTimes

data class Status(
    val smartCharging: Boolean,
    val currentChargingSpeed: Float,
    val chargingTimes: ChargingTimes,
    val smartchargingEnabled: Boolean
)
