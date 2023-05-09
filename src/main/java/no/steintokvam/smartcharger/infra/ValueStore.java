package no.steintokvam.smartcharger.infra;

import no.steintokvam.smartcharger.easee.objects.AccessToken;
import no.steintokvam.smartcharger.objects.ChargingTimes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import static java.util.Collections.emptyList;

public class ValueStore {

    public static AccessToken accessToken = new AccessToken("", 0, emptyList(), "", "");
    public static boolean isSmartCharging = false;
    public static boolean smartChargingEnabled = true;

    public static int initialBatteryPercent = 20;
    public static int remainingPercent = 20;
    public static int totalCapacityKwH = 76;
    public static LocalDateTime finnishChargingBy = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 0));
    public static float currentChargingSpeed = 0f;

    public static float chargingThreshold = 4f;

    public static boolean smartChargingSchedueled = false;

    public static LocalDateTime lastReestimate = LocalDateTime.now().minusDays(1L);
    public static ChargingTimes chargingTimes = new ChargingTimes(emptyList(), 0, 0, finnishChargingBy);

    public static String zone = "NO1";
    public static String chargerID = "EHE6ZQU7";

    public static String powerPriceURL = "";

    public static List<String> startJobNames = emptyList();
    public static List<String> pauseJobNames = emptyList();
}
