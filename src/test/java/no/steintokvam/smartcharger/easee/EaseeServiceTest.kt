package no.steintokvam.smartcharger.easee

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class EaseeServiceTest {
    private val easeeService = EaseeService()

    @Test
    fun testGetChargers() {

        val chargers = easeeService.getChargerId()

        assertTrue(chargers[0].id == "EHE6ZQU7")
    }

    @Test
    fun testGetChargerState(){
        val chargerID = "EHE6ZQU7"
        val chargerState = easeeService.getChargerState(chargerID)
        assertTrue(chargerState.isOnline)
    }
}