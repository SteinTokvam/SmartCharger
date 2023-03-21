package no.steintokvam.smartcharger.objects

import java.time.LocalTime

data class WeeklyScheduele(//dayOfWeek: 0-6
    val days: List<Days>,
    val isEnabled: Boolean
)

data class Days(
    val ranges: List<Range>,
    val dayOfWeek: Int
)

data class Range(
    val chargingCurrentLimit: Int,
    val startTime: LocalTime,
    val stopTime: LocalTime
)
