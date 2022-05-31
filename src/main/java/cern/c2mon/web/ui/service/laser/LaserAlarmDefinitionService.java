package cern.c2mon.web.ui.service.laser;

import cern.c2mon.client.ext.history.laser.LaserAlarmDefinition;
import cern.c2mon.client.ext.history.laser.repo.LaserAlarmDefinitionRepoService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Profile("enableLaser")
@Service
public class LaserAlarmDefinitionService {

    @Autowired
    private LaserAlarmDefinitionRepoService laserAlarmDefinitionService;

    public final List<LaserAlarmDefinition> findAllAlarmDefinitionsByConfigIdAndPriorityAndTextSearch(
            Long configId, List<Integer> priorities, String textSearch) {
        return laserAlarmDefinitionService.findAllByConfigIdAndPriorityAndText(
                configId, textSearch, priorities);
    }

    public final List<LaserAlarmDefinition> findAllAlarmDefinitionsByConfigIdAndPriority(
            Long configId, List<Integer> priorities) {
        return laserAlarmDefinitionService.findAllByConfigIdAndPriority(configId, priorities);
    }

    public final Page<LaserAlarmDefinition> findAllAlarmDefinitionsByConfigIdAndPriorityAndTextSearch(
            Long configId, List<Integer> priorities, String textSearch, Integer pageSize, Integer pageNumber) {
        return laserAlarmDefinitionService.findAllByConfigIdAndPriorityAndText(
                configId, textSearch, priorities, PageRequest.of(pageNumber, pageSize));
    }

    public final Page<LaserAlarmDefinition> findAllAlarmDefinitionsByConfigIdAndPriority(
            Long configId, List<Integer> priorities, Integer pageSize, Integer pageNumber) {
        return laserAlarmDefinitionService.findAllByConfigIdAndPriority(configId, priorities, PageRequest.of(pageNumber, pageSize));
    }
}
