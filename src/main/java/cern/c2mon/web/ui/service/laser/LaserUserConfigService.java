package cern.c2mon.web.ui.service.laser;

import cern.c2mon.client.ext.history.laser.LaserUserConfig;
import cern.c2mon.client.ext.history.laser.repo.LaserUserConfigRepoService;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("enableLaser")
@Service
public class LaserUserConfigService {

    @Autowired
    private LaserUserConfigRepoService laserUserConfigRepoService;

    public final List<LaserUserConfig> findAllUserConfigurationByName(String configName) {
        return laserUserConfigRepoService.findAllByConfigName(configName);
    }

    public final List<LaserUserConfig> findAllUserConfigurations() {
        return laserUserConfigRepoService.findAll();
    }

    public final Optional<LaserUserConfig> findUserConfiguration(String configName) {
        return laserUserConfigRepoService.findByConfigName(configName);
    }
}
