package no.steintokvam.smartcharger.electricity

import java.time.LocalDateTime

data class ElectricityPrice(
        var NOK_per_kWh: Float,
        val eur_per_kWh: Float,
        val exr: Float,
        val time_start: LocalDateTime,
        val time_end: LocalDateTime
)