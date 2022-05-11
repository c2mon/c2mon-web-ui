package cern.c2mon.web.ui.service.laser;

import cern.c2mon.client.ext.history.laser.LaserAlarmLogUserConfig;
import cern.c2mon.client.ext.history.laser.repo.LaserAlarmEventRepoService;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

@Profile("enableLaser")
@Service
public class LaserAlarmEventService {

    @Autowired
    private LaserAlarmEventRepoService laserAlarmEventRepoService;


    public final List<LaserAlarmLogUserConfig> findAllAlarmsByConfigIdAndPriorityAndTextBetweenDates(Long configId,
                                                                                                       String startTime,
                                                                                                       String endTime,
                                                                                                       List<Integer> priorities,
                                                                                                       String textSearch) {
        return laserAlarmEventRepoService.findAllAlarmsByConfigIdAndPriorityAndTextBetweenDates(configId, startTime, endTime, textSearch, priorities);
    }

    public final List<LaserAlarmLogUserConfig> findAllAlarmsByConfigIdAndPriorityBetweenDates(Long configId,
                                                                                                 String startTime,
                                                                                                 String endTime,
                                                                                                 List<Integer> priorities) {
        return laserAlarmEventRepoService.findAllAlarmsByConfigIdAndPriorityBetweenDates(configId, startTime, endTime, priorities);
    }

    public final List<LaserAlarmLogUserConfig> findActiveAlarmsByConfigIdAndPriorityAndTextAtGivenTime(Long configId,
                                                                                                       String time,
                                                                                                       List<Integer> priorities,
                                                                                                       String textSearch) {
        return laserAlarmEventRepoService.findAllActiveAlarmsByConfigIdAndPriorityAndTextAtGivenTime(configId, time, textSearch, priorities);
    }

    public final List<LaserAlarmLogUserConfig> findActiveAlarmsByConfigIdAndPriorityAtGivenTime(Long configId,
                                                                                                String time,
                                                                                                List<Integer> priorities) {
        return laserAlarmEventRepoService.findAllActiveAlarmsByConfigIdAndPriorityAtGivenTime(configId, time, priorities);
    }
}
