package cern.c2mon.web.ui.service.laser;

import cern.c2mon.client.ext.history.laser.LaserAlarmUserConfig;
import cern.c2mon.client.ext.history.laser.repo.LaserAlarmUserConfigRepoService;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("enableLaser")
@Service
public class LaserAlarmDefinitionService {

    @Autowired
    private LaserAlarmUserConfigRepoService laserAlarmUserConfigRepoService;

    public final List<LaserAlarmUserConfig> findAllAlarmDefinitionsByConfigId(Long configId) {
        return laserAlarmUserConfigRepoService.findByConfigId(configId);
    }

    public final List<LaserAlarmUserConfig> findAllAlarmDefinitionsByConfigIdAndPriorityAndTextSearchBetweenDates(Long configId,
                                                                                          LocalDateTime startDateTime,
                                                                                          LocalDateTime endDateTime,
                                                                                          List<Integer> priorities,
                                                                                          String textSearch) {
        return laserAlarmUserConfigRepoService.findAllByConfigIdAndPriorityAndTextBetweenDates(configId, startDateTime, endDateTime, textSearch, priorities);
    }

    public final List<LaserAlarmUserConfig> findAllAlarmDefinitionsByConfigIdAndPriorityBetweenDates(Long configId,
                                                                                          LocalDateTime startDateTime,
                                                                                          LocalDateTime endDateTime,
                                                                                          List<Integer> priorities) {
        return laserAlarmUserConfigRepoService.findAllByConfigIdAndPriorityBetweenDates(configId, startDateTime, endDateTime, priorities);
    }
}
