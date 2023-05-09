package no.steintokvam.smartcharger.easee

import org.junit.jupiter.api.Test

class EaseeServiceTest {
    private val easeeService = EaseeService()
//har kommentert ut testene da jeg ikke vil legge inn brukernavn/passord her
    @Test
    fun testGetChargers() {
        //val chargers = easeeService.getChargerId()

        //assertTrue(chargers[0].id == "EHE6ZQU7")
    }

    @Test
    fun testGetChargerState(){
        //val chargerState = easeeService.getChargerState()
        //assertTrue(chargerState.isOnline)
    }
}