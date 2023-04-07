package no.steintokvam.smartcharger.easee

import no.steintokvam.smartcharger.infra.ValueStore
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

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