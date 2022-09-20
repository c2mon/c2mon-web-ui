package cern.c2mon.web.ui.service.laser;

import cern.c2mon.client.ext.history.laser.LaserAlarmLogUserConfig;
import cern.c2mon.client.ext.history.laser.repo.LaserAlarmEventRepoService;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Profile("enableLaser")
@Service
public class LaserAlarmEventService {

    @Autowired
    private LaserAlarmEventRepoService laserAlarmEventRepoService;

    public final List<LaserAlarmLogUserConfig> findAllAlarmsByConfigIdAndPriorityAndTextBetweenDates(
            Long configId, String startTime, String endTime, List<Integer> priorities, String textSearch) {
        return laserAlarmEventRepoService.findAllAlarmsByConfigIdAndPriorityAndTextBetweenDates(
                configId, startTime, endTime, textSearch, priorities);
    }

    public final List<LaserAlarmLogUserConfig> findAllAlarmsByConfigIdAndPriorityBetweenDates(
            Long configId, String startTime, String endTime, List<Integer> priorities) {
        return laserAlarmEventRepoService.findAllAlarmsByConfigIdAndPriorityBetweenDates(
                configId, startTime, endTime, priorities);
    }

    public final Set<LaserAlarmLogUserConfig> findActiveAlarmsByConfigIdAndPriorityAndTextAtGivenTime(
            Long configId, String time, List<Integer> priorities, String textSearch) {
        return laserAlarmEventRepoService.findAllActiveAlarmsByConfigIdAndPriorityAndTextAtGivenTime(
                configId, time, textSearch, priorities);
    }

    public final Set<LaserAlarmLogUserConfig> findActiveAlarmsByConfigIdAndPriorityAtGivenTime(
            Long configId, String time, List<Integer> priorities) {
        return laserAlarmEventRepoService.findAllActiveAlarmsByConfigIdAndPriorityAtGivenTime(
                configId, time, priorities);
    }

    public final Page<LaserAlarmLogUserConfig> findAllAlarmsByConfigIdAndPriorityAndTextBetweenDates(
            Long configId, String startTime, String endTime, List<Integer> priorities, String textSearch, Integer pageSize, Integer pageNumber) {
        return laserAlarmEventRepoService.findAllAlarmsByConfigIdAndPriorityAndTextBetweenDates(
                configId, startTime, endTime, textSearch, priorities, PageRequest.of(pageNumber, pageSize));
    }

    public final Page<LaserAlarmLogUserConfig> findAllAlarmsByConfigIdAndPriorityBetweenDates(
            Long configId, String startTime, String endTime, List<Integer> priorities, Integer pageSize, Integer pageNumber) {
        return laserAlarmEventRepoService.findAllAlarmsByConfigIdAndPriorityBetweenDates(
                configId, startTime, endTime, priorities, PageRequest.of(pageNumber, pageSize));
    }
}
