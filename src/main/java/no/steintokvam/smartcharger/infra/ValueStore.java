package no.steintokvam.smartcharger.infra;

import com.fasterxml.jackson.annotation.JsonProperty;
import no.steintokvam.smartcharger.easee.objects.AccessToken;
import no.steintokvam.smartcharger.electricity.ElectricityPrice;
import no.steintokvam.smartcharger.objects.ChargingTimes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.Collections.emptyList;

public class ValueStore {

    public static AccessToken accessToken = new AccessToken("", 0, emptyList(), "", "");
    public static boolean isCurrentlyCharging = false;
    public static boolean isSmartCharging = false;
    public static boolean smartChargingEnabled = true;
    public static int remainingPercent = 20;
    public static int totalCapacityKwH = 10;//76;
    public static LocalDateTime finnishChargingBy = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
    public static float currentChargingSpeed = 3f;

    public static float chargingThreshold = 1f;

    public static boolean smartChargingSchedueled = false;

    public static LocalDateTime lastReestimate = LocalDateTime.now().minusDays(1L);
    public static ChargingTimes chargingTimes = new ChargingTimes(emptyList(), 0, 0, finnishChargingBy);

    public static List<ElectricityPrice> prices = emptyList();
    public static String zone = "NO1";
}
