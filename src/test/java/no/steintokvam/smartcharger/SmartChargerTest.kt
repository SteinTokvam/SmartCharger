package no.steintokvam.smartcharger

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

class SmartChargerTest {

    private val smartCharger = SmartCharger()
    private val date = LocalDate.of(2023, 3, 21)

    @Test
    fun testIsCharging() {
        //Denne testen antar at laderen ikke lader da den faktisk sjekker statusen til laderen

        assertFalse(smartCharger.isCharging())
    }

    @Test
    fun getFiveLowestPrices() {
        val lowestPrices = smartCharger.getLowestPrices(date, "NO1", 5)
        assertThat(lowestPrices[0].NOK_per_kWh).isEqualTo(1f)
        assertThat(lowestPrices[0].time_start).isEqualTo(LocalDateTime.of(2023, 3, 21, 23, 0))
        assertThat(lowestPrices[1].NOK_per_kWh).isEqualTo(1.03f)
        assertThat(lowestPrices[2].NOK_per_kWh).isEqualTo(1.05f)
        assertThat(lowestPrices[3].NOK_per_kWh).isEqualTo(1.06f)
        assertThat(lowestPrices[4].NOK_per_kWh).isEqualTo(1.07f)
    }

    @Test
    fun setChargingPeriod() {
        val hasSetChargingPeriod = smartCharger.setChargingPeriod(date, "NO1")
        assertTrue(hasSetChargingPeriod)
    }
}