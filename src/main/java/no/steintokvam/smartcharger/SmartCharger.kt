package no.steintokvam.smartcharger

import no.steintokvam.smartcharger.easee.EaseeService

class SmartCharger {
    private val easeeService = EaseeService()
    private val chargerID = "EHE6ZQU7"

    fun isCharging(): Boolean {
        return easeeService.getChargerState(chargerID).totalPower > 0
    }
}