/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
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
package cern.c2mon.web.ui.service;

import cern.c2mon.client.ext.history.alarm.Alarm;
import cern.c2mon.client.ext.history.alarm.AlarmHistoryService;
import cern.c2mon.client.ext.history.alarm.HistoricAlarmQuery;
import cern.c2mon.client.ext.history.common.exception.HistoryProviderException;
import cern.c2mon.client.ext.history.common.exception.LoadingParameterException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.List;

/**
 * HistoryService providing the XML representation for the history of a given
 * alarm.
 */
@Service
public class HistoryAlarmService {

  @Autowired
  private AlarmHistoryService alarmService;

  /**
   * Used to make a request for HistoryData.
   *
   * @param alarmId         The alarm id whose history we are looking for
   * @param numberOfRecords number of records to retrieve from history
   * @return history as a List of HistoryTagValueUpdates
   */
  public final List<Alarm> requestHistoryData(final String alarmId, final int numberOfRecords) {

    final long id = Long.parseLong(alarmId);
    Page<Alarm> alarmHistory = alarmService.findBy(new HistoricAlarmQuery().id(id), new PageRequest(0,
        numberOfRecords));

    return alarmHistory.getContent();
  }

  /**
   * Used to make a request for HistoryData.
   *
   * @param dataTagId The alarm id whose history we are looking for
   * @return history as a List of Alarm
   */
  public final List<Alarm> requestHistoryData(final String dataTagId, final Timestamp startTime,
                                              final Timestamp endTime) {
    final long id = Long.parseLong(dataTagId);
    return alarmService.findBy(new HistoricAlarmQuery().id(id).between(startTime, endTime));
  }

  /**
   * Used to make a request for HistoryData of an alarm.
   *
   * @param alarmId      The alarm id whose history we are looking for
   * @param numberOfDays number of days to go back in History
   * @return history as a List of HistoryTagValueUpdates
   * @throws HistoryProviderException  in case a HistoryProvider cannot be created
   * @throws LoadingParameterException in case of an invalid configurations
   */
  public final List<Alarm> requestAlarmHistoryDataForLastDays(final String alarmId, final int numberOfDays) {
    // calculate the number of days in milliseconds
    Long daysInMillis = new Long(numberOfDays) * 24 * 60 * 60 * 1000;

    Timestamp currentTime = new Timestamp(System.currentTimeMillis());
    Timestamp startTime = new Timestamp(System.currentTimeMillis() - daysInMillis);
    return requestHistoryData(alarmId, startTime, currentTime);
  }
}
