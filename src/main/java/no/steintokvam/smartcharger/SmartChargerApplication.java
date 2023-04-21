package no.steintokvam.smartcharger;

import no.steintokvam.smartcharger.easee.EaseeService;
import no.steintokvam.smartcharger.electricity.PriceService;
import no.steintokvam.smartcharger.infra.ValueStore;
import no.steintokvam.smartcharger.infra.quartz.QuartzSchedueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.time.LocalDate;

@SpringBootApplication
public class SmartChargerApplication {

	public static void main(String[] args) {
		Logger LOGGER = LoggerFactory.getLogger(SmartChargerApplication.class);
		SpringApplication.run(SmartChargerApplication.class, args);
		ValueStore.chargerID = System.getenv("chargerID");
		if(ValueStore.chargerID == null || ValueStore.chargerID.isEmpty()) {
			LOGGER.error("Charger ID not set. exiting.");
			return;
		}
		LOGGER.info("Got chargerID: " + ValueStore.chargerID);
		String user = System.getenv("user");
		String passwd = System.getenv("password");
		if(user.isEmpty() || passwd.isEmpty()) {
			LOGGER.error("User or password is empty. exiting.");
			return;
		}
		ValueStore.accessToken = new EaseeService().authenticate(user, passwd);
		LOGGER.info("Authenticated against Easee servers.");
		ValueStore.prices = new PriceService().getPrices(ValueStore.zone, LocalDate.now());
		new QuartzSchedueler().schedueleJobs();
	}
}
