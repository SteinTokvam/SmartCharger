package no.steintokvam.smartcharger.infra.quartz.jobs


import no.steintokvam.smartcharger.electricity.PriceService
import no.steintokvam.smartcharger.infra.ValueStore
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class GetChargingTimesJobTest() {
    @Test
    fun testGetChargingTimeJob() {//TODO: må mocke at det lades
        ValueStore.prices = PriceService().getPrices("NO1", LocalDate.now())
        //GetChargingTimesJob().run(null)
/*

        assertTrue(ValueStore.chargingTimes.prices[0].time_start.hour == 11)
        assertTrue(ValueStore.chargingTimes.prices[0].time_end.hour == 19)
        assertTrue(ValueStore.chargingTimes.prices[1].time_start.hour == 21)
        assertTrue(ValueStore.chargingTimes.prices.size == 2)
*/

    }
}