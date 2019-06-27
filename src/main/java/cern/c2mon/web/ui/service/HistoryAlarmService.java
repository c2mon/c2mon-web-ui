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

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import cern.c2mon.client.ext.history.alarm.Alarm;
import cern.c2mon.client.ext.history.alarm.AlarmHistoryService;
import cern.c2mon.client.ext.history.common.exception.HistoryProviderException;
import cern.c2mon.client.ext.history.common.exception.LoadingParameterException;

/**
 * HistoryService providing the XML representation for the history of a given
 * alarm.
 */
@Service
public class HistoryAlarmService {

  @Autowired
  private AlarmHistoryService alarmHistoryService;

  /**
   * Used to make a request for HistoryData.
   *
   * @param alarmId The alarm id whose history we are looking for
   * @param localStartTime The start time expressed in the local time zone
   * @param localEndTime The end time expressed in the local time zone
   * @return history as a List of Alarm
   */
  public final List<Alarm> requestAlarmHistory(final Long alarmId, final LocalDateTime localStartTime, final LocalDateTime localEndTime) {
    return alarmHistoryService.findAllDistinctByIdAndTimestampBetweenOrderByTimestampDesc(alarmId, localStartTime, localEndTime);
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
  public final List<Alarm> requestAlarmHistoryForLastDays(final Long alarmId, final int numberOfDays) {
    LocalDateTime now = LocalDateTime.now();
    return alarmHistoryService.findAllDistinctByIdAndTimestampBetweenOrderByTimestampDesc(alarmId, now.minusDays(numberOfDays), now);
  }

  /**
   * @param alarmId The alarm id
   * @param numRecords number of records to be retrieved
   * @return The last N alarm records for the given alarm id
   */
  public final List<Alarm> requestAlarmHistory(final Long alarmId, final int numRecords) {
    return alarmHistoryService.findAllDistinctByIdOrderByTimestampDesc(alarmId, new PageRequest(0, numRecords)).getContent();
  }

  /**
   * Used to make a request for HistoryData.
   *
   * @param alarmId The alarm id whose history we are looking for
   * @param localStartTime The start time expressed in the local time zone
   * @param localEndTime The end time expressed in the local time zone
   * @return history as a List of Alarm
   */
  public final List<Alarm> requestAlarmHistoryBySourceTimestamp(final Long alarmId, final LocalDateTime localStartTime, final LocalDateTime localEndTime) {
    return alarmHistoryService.findAllDistinctByIdAndSourceTimeBetweenOrderBySourceTimeDesc(alarmId, localStartTime, localEndTime);
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
  public final List<Alarm> requestAlarmHistoryBySourceTimestamForLastDays(final Long alarmId, final int numberOfDays) {
    LocalDateTime now = LocalDateTime.now();
    return alarmHistoryService.findAllDistinctByIdAndSourceTimeBetweenOrderBySourceTimeDesc(alarmId, now.minusDays(numberOfDays), now);
  }

  /**
   * @param alarmId The alarm id
   * @param numRecords number of records to be retrieved
   * @return The last N alarm records for the given alarm id
   */
  public final List<Alarm> requestAlarmHistoryBySourceTimestamp(final Long alarmId, final int numRecords) {
    return alarmHistoryService.findAllDistinctByIdOrderBySourceTimeDesc(alarmId, new PageRequest(0, numRecords)).getContent();
  }
}
