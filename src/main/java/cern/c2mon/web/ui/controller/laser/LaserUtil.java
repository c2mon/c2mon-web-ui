package cern.c2mon.web.ui.controller.laser;

import cern.c2mon.client.ext.history.laser.LaserUserConfig;

import java.util.List;
import java.util.Optional;


public class LaserUtil {

    public static boolean isNumeric(String str) {
        return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
    }

    public static boolean containsConfigName(final List<LaserUserConfig> laserUserConfigs, final String configName){
        return laserUserConfigs.stream().filter(o -> o.getConfigName().equals(configName)).findFirst().isPresent();
    }

    public static Optional<LaserUserConfig> findConfigById(final List<LaserUserConfig> laserUserConfigs, final Long configId){
        return laserUserConfigs.stream().filter(o -> o.getConfigId().equals(configId)).findFirst();
    }
}
