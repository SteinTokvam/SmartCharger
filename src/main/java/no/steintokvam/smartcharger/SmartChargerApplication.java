package no.steintokvam.smartcharger;

import no.steintokvam.smartcharger.infra.quartz.QuartzSchedueler;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SmartChargerApplication {

	public static void main(String[] args) {
		SpringApplication.run(SmartChargerApplication.class, args);
		new QuartzSchedueler().schedueleJobs();
	}
}
