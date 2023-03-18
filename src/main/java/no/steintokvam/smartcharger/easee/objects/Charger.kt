package no.steintokvam.smartcharger.easee.objects

import java.time.LocalDateTime

class Charger() {
    val id: String = ""
    val name: String = ""
    val color: Int = -1
    val createdOn: String = ""
    val updatedOn: String = ""
    val levelOfAccess: Int = -1
    val productCode: Int = -1
    
    constructor(id: String,
                 name: String,
                 color: Int,
                 createdOn: LocalDateTime,
                 updatedOn: LocalDateTime,
                 levelOfAccess: Int,
                 productCode: Int) : this() {

    }
}
