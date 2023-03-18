package no.steintokvam.smartcharger

import no.steintokvam.smartcharger.easee.EaseeService

class SmartCharger {
    private val easeeService = EaseeService()

    fun isCharging(): Boolean {
        return easeeService.getChargerState().totalPower > 0
    }
}