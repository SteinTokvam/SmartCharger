package no.steintokvam.smartcharger

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class SmartChargerTest {

    @Test
    fun testIsCharging() {
        //Denne testen antar at laderen ikke lader da den faktisk sjekker statusen til laderen
        val smartCharger = SmartCharger()
        assertFalse(smartCharger.isCharging())
    }
}