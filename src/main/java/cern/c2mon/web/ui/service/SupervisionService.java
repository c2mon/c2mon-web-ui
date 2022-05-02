package cern.c2mon.web.ui.service;


import cern.c2mon.client.ext.history.supervision.ServerSupervisionEvent;
import cern.c2mon.client.ext.history.supervision.SupervisionEventRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class SupervisionService {

  @Autowired
  private SupervisionEventRepository supervisionEventRepository;

  public final List<ServerSupervisionEvent> requestControlHistoryData(final Long alarmId, final LocalDateTime localStartTime, final LocalDateTime localEndTime) {
    return supervisionEventRepository.findAllDistinctByIdAndEventTimeBetween(alarmId, localStartTime, localEndTime);
  }

  public final List<ServerSupervisionEvent> requestControlHistoryDataForLastDays(final Long dataTagId, final int numberOfDays){
    LocalDateTime now = LocalDateTime.now();
    return supervisionEventRepository.findAllDistinctByIdAndEventTimeBetweenOrderByEventTimeDesc(dataTagId, now.minusDays(numberOfDays), now);
  }

  public final List<ServerSupervisionEvent> requestControlHistoryData(final Long dataTagId,
                                                                      final int numberOfRecords) {
    return supervisionEventRepository.findAllDistinctByIdOrderByEventTimeDesc(dataTagId, PageRequest.of(0, numberOfRecords)).getContent();
  }

}
