package no.steintokvam.smartcharger;

import no.steintokvam.smartcharger.easee.EaseeService;
import no.steintokvam.smartcharger.easee.objects.AccessToken;
import no.steintokvam.smartcharger.infra.ValueStore;
import no.steintokvam.smartcharger.infra.quartz.QuartzSchedueler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartChargerApplication {

	
	public static void main(String[] args) {
		Logger LOGGER = LoggerFactory.getLogger(SmartChargerApplication.class);
		String user = System.getenv("user");
		String passwd = System.getenv("passord");
		if(user.isEmpty() || passwd.isEmpty()) {
			LOGGER.info("User or password is empty. exiting.");
			return;
		}
		ValueStore.accessToken = new EaseeService().authenticate(user, passwd);
		SpringApplication.run(SmartChargerApplication.class, args);
		String chargingTimes = String.format(
       """
    {
    	prices: [],
    	kwhLeftToCharge: 0,
    	estimatedChargeTime: 0,
    	finnishChargingBy: %s
    }
""", ValueStore.finnishChargingBy.toString());
		LOGGER.info(String.format("""
				ValueStore
				  isCurrentlyCharging = %s
				  isSmartCharging = %s
				  smartChargingEnabled = %s
				  remainingPercent = %d
				  totalCapacityKwH = %d
				  finnishChargingBy = %s
				  currentChargingSpeed = %f
				  lastReestimate = %s
				  chargingTimes = %s
				  prices = %s
				  zone = %s""",
				ValueStore.isCurrentlyCharging,
				ValueStore.isSmartCharging,
				ValueStore.smartChargingEnabled,
				ValueStore.remainingPercent,
				ValueStore.totalCapacityKwH,
				ValueStore.finnishChargingBy,
				ValueStore.currentChargingSpeed,
				ValueStore.lastReestimate,
				chargingTimes,
				"[]",
				ValueStore.zone));
		new QuartzSchedueler().schedueleJobs();
	}
}
