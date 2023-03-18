package no.steintokvam.smartcharger.easee

import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

class EaseeServiceTest {

    @Test
    fun testGetChargers() {
        val easeeService = EaseeService()
        val chargers = easeeService.getChargerId()

        assertTrue(chargers[0].id == "EHE6ZQU7")
    }
}