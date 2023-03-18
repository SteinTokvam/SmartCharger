package no.steintokvam.smartcharger

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class SmartChargerApplication

fun main(args: Array<String>) {
	runApplication<SmartChargerApplication>(*args)
}
