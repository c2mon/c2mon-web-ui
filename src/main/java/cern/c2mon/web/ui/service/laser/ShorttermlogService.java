/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
 *
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 *
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.web.ui.service.laser;

import cern.c2mon.client.ext.history.laser.Shorttermlog;
import cern.c2mon.client.ext.history.laser.repo.ShorttermlogHistoryService;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Profile("enableLaser")
@Service
public class ShorttermlogService {

  @Autowired
  private ShorttermlogHistoryService alarmHistoryService;

  public final Page<Shorttermlog> requestAlarmHistory(final Long alarmId, final LocalDateTime localStartTime, final LocalDateTime localEndTime, Integer pageSize, Integer pageNumber) {
    return alarmHistoryService.findAllDistinctByIdAndTagServerTimeBetweenOrderByTagServerTimeDesc(alarmId, localStartTime, localEndTime, PageRequest.of(pageNumber, pageSize));
  }

  public final Page<Shorttermlog> requestAlarmHistoryForLastDays(final Long alarmId, final int numberOfDays, Integer pageSize, Integer pageNumber) {
    LocalDateTime now = LocalDateTime.now();
    return alarmHistoryService.findAllDistinctByIdAndTagServerTimeBetweenOrderByTagServerTimeDesc(alarmId, now.minusDays(numberOfDays), now, PageRequest.of(pageNumber, pageSize));
  }

  public final Page<Shorttermlog> requestAlarmHistory(final Long alarmId, final int numRecords, Integer pageSize, Integer pageNumber) {
    return alarmHistoryService.findAllDistinctByIdOrderByTagServerTimeDesc(alarmId, PageRequest.of(pageNumber, pageSize));
  }

  public final List<Shorttermlog> requestAlarmHistory(final Long alarmId, final LocalDateTime localStartTime, final LocalDateTime localEndTime) {
    return alarmHistoryService.findAllDistinctByIdAndTagServerTimeBetweenOrderByTagServerTimeDesc(alarmId, localStartTime, localEndTime);
  }
    public final List<Shorttermlog> requestAlarmHistoryForLastDays(final Long alarmId, final int numberOfDays) {
      LocalDateTime now = LocalDateTime.now();
      return alarmHistoryService.findAllDistinctByIdAndTagServerTimeBetweenOrderByTagServerTimeDesc(alarmId, now.minusDays(numberOfDays), now);
    }

  public final List<Shorttermlog> requestAlarmHistory(final Long alarmId, final int numRecords) {
    return alarmHistoryService.findAllDistinctByIdOrderByTagServerTimeDesc(alarmId, PageRequest.of(0, numRecords)).getContent();
  }
}
