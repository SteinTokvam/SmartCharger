package no.steintokvam.smartcharger.infra;

import no.steintokvam.smartcharger.electricity.ElectricityPrice;
import no.steintokvam.smartcharger.objects.ChargingTimes;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import static java.util.Collections.emptyList;

public class ValueStore {
    public static boolean isCurrentlyCharging = false;
    public static boolean isSmartCharging = false;
    public static boolean smartChargingEnabled = true;
    public static int remainingPercent = 20;
    public static int totalCapacityKwH = 76;
    public static LocalDateTime finnishChargingBy = LocalDateTime.of(LocalDate.now(), LocalTime.of(23, 0));
    public static float currentChargingSpeed = 0f;

    public static LocalDateTime lastReestimate = LocalDateTime.now().minusDays(1L);
    public static ChargingTimes chargingTimes = new ChargingTimes(emptyList(), 0, 0, finnishChargingBy);

    public static List<ElectricityPrice> prices = emptyList();
    public static String zone = "NO1";
}