package no.steintokvam.smartcharger

import no.steintokvam.smartcharger.infra.ValueStore
import org.junit.jupiter.api.Test
import java.time.LocalDateTime

class SmartChargerTest {

    private val smartCharger = SmartCharger()
    private val date = LocalDateTime.of(2023, 3, 21, 0, 0)

    @Test
    fun testIsCharging() {
        //Denne testen antar at laderen ikke lader da den faktisk sjekker statusen til laderen

        //assertFalse(smartCharger.isCharging())
    }

    @Test
    fun getFiveLowestPrices() {//TODO: Må mocke prisene og datoene
        /*
        val lowestPrices = smartCharger.getLowestPrices(date, "NO1", 7, 5)
        assertThat(lowestPrices[0].NOK_per_kWh).isEqualTo(0.79f)
        assertThat(lowestPrices[0].time_start).isEqualTo(LocalDateTime.of(2023, 3, 22, 4, 0))
        assertThat(lowestPrices[1].NOK_per_kWh).isEqualTo(0.81f)
        assertThat(lowestPrices[2].NOK_per_kWh).isEqualTo(0.83f)
        assertThat(lowestPrices[3].NOK_per_kWh).isEqualTo(0.84f)
        assertThat(lowestPrices[4].NOK_per_kWh).isEqualTo(0.86f)
        */
    }

    @Test
    fun testToggleCharging() {
        //val hasToggledCharging = smartCharger.toggleCharging()
        //assertTrue(hasToggledCharging == 400)//Får 400 fordi ingen bil er plugget i og man kan dermed ikke toggle
    }

    @Test
    fun testCalculateGetChargingTimes() {
//        val chargingTimes = smartCharger.getChargingTimes(20, 77, LocalDateTime.now().plusHours(24L))
/*
        val lowestPrices = smartCharger.getLowestPrices(LocalDateTime.now(), 10, 7).sortedBy { it.time_start }

        assertThat(chargingTimes.prices[0].NOK_per_kWh == lowestPrices[0].NOK_per_kWh)
        assertThat(chargingTimes.prices[6].NOK_per_kWh == lowestPrices[6].NOK_per_kWh)
        assertThat(chargingTimes.kwhLeftToCharge == 62)
        */
    }

    @Test
    fun testGetPricesIfCanBeChargedInTime() {//TODO: må reimplementeres
        //ValueStore.prices = PriceService().getPrices("NO1", LocalDate.now())
        ValueStore.finnishChargingBy = LocalDateTime.now().plusHours(20)
        ValueStore.currentChargingSpeed = 20f
//        val tmpChargingTimes = smartCharger.getChargingTimes(
//            ValueStore.remainingPercent,
//            ValueStore.totalCapacityKwH,
//            ValueStore.finnishChargingBy
//        )
        //assertTrue(tmpChargingTimes.prices.isNotEmpty())
    }
}