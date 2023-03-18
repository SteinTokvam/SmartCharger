package no.steintokvam.smartcharger.easee

import org.junit.jupiter.api.Test


class EaseeServiceImplTest {

    @Test
    fun testConnectivity() {
        val easeeService = EaseeServiceImpl();
        easeeService.authenticate();
    }
}
