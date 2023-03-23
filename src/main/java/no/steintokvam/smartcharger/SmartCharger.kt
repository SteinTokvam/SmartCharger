package no.steintokvam.smartcharger

import no.steintokvam.smartcharger.easee.EaseeService
import no.steintokvam.smartcharger.electricity.ElectricityPrice
import no.steintokvam.smartcharger.electricity.PriceService
import java.time.LocalDate

class SmartCharger {
    private val easeeService = EaseeService()
    private val priceService = PriceService()

    fun isCharging(): Boolean {
        return easeeService.getChargerState().totalPower > 0
    }

    fun getLowestPrices(date: LocalDate, zone: String, hours: Int) : List<ElectricityPrice> {
        val allPrices = priceService.getPrices(zone, date)
        return allPrices.sortedBy { it.NOK_per_kWh }.subList(0, hours)
    }

    fun setChargingPeriod(date: LocalDate, zone: String): Boolean {
        val lowestPrices = getLowestPrices(date, zone, 5)
        return easeeService.toggleCharging()
    }

    fun stopCharging() {

    }
}