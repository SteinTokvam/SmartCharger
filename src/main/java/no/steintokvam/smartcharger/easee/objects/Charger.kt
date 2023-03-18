package no.steintokvam.smartcharger.easee.objects

import java.time.LocalDateTime

data class Charger(
        val id: String,
        val name: String,
        val color: Int,
        val createdOn: String,
        val updatedOn: String,
        val levelOfAccess: Int,
        val productCode: Int
)
