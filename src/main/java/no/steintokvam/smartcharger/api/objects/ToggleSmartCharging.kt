package no.steintokvam.smartcharger.api.objects

data class ToggleSmartCharging(
    val gotToggled: Boolean,
    val alreadyWasToggled: Boolean
)
