package no.steintokvam.smartcharger.infra;

import no.steintokvam.smartcharger.electricity.ElectricityPrice;

import java.util.Collections;
import java.util.List;

public class ValueStore {
    public static boolean isCurrentlyCharging = false;
    public static boolean isSmartCharging = false;
    public static boolean smartChargingEnabled = true;
    public static List<ElectricityPrice> prices = Collections.emptyList();
    public static String zone = "NO1";
}
