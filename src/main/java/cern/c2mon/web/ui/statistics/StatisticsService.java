/*******************************************************************************
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
 ******************************************************************************/
package cern.c2mon.web.ui.statistics;

import cern.c2mon.client.ext.history.lifecycle.ServerLifecycleEvent;
import cern.c2mon.client.ext.history.lifecycle.ServerLifecycleEventRepository;
import cern.c2mon.client.ext.history.supervision.ServerSupervisionEvent;
import cern.c2mon.client.ext.history.supervision.SupervisionEventRepository;
import cern.c2mon.shared.client.statistics.TagStatisticsResponse;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.web.ui.service.ProcessService;
import cern.c2mon.web.ui.statistics.charts.WebChart;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * This class acts as a service to provide statistics about the C2MON server and
 * running DAQ processes from various sources.
 *
 * @author Justin Lewis Salmon
 */
@Service
public class StatisticsService {

  @Autowired
  private ServerLifecycleEventRepository serverLifecycleEventRepository;

  @Autowired
  private SupervisionEventRepository supervisionEventRepository;

  /**
   * Reference to the {@link ProcessService} bean.
   */
  @Autowired
  private ProcessService processService;

  @Autowired
  private cern.c2mon.client.core.service.StatisticsService statisticsService;

  /**
   * A list of pre-configured chart objects generated by the
   * c2mon-statistics-generator module.
   */
  private List<WebChart> charts = new ArrayList<>();

  /**
   * Retrieve a pre-configured chart object.
   *
   * @param chartId the id of the chart
   *
   * @return the {@link WebChart} instance corresponding to the given id, or
   *         null if the chart was not found.
   */
  public WebChart getChart(String chartId) {
    for (WebChart chart : charts) {
      if (chart.getChartId().equals(chartId)) {
        return chart;
      }
    }

    return null;
  }

  /**
   * Set the list of pre-configured chart objects generated by the
   * c2mon-statistics-generator module.
   *
   * @param charts the charts to set
   */
  public void setCharts(List<WebChart> charts) {
    this.charts = charts;
  }

  /**
   * Retrieve a list of {@link SupervisionEvent} objects from the STL for a
   * given process for a given year.
   *
   * @param name the process name
   * @param year the year of events to retrieve
   *
   * @return the list of {@link SupervisionEvent} objects
   *
   * @throws Exception if an invalid year was given, or if an error occurs
   *           getting the process id
   */
  public List<ServerSupervisionEvent> getSupervisionEventsForYear(String name, Integer year) throws Exception {
    Long id = processService.getProcessConfiguration(name).getProcessID();

    // Generate dates for the first and last days of the given year.
    LocalDateTime from = LocalDateTime.of(year, Month.JANUARY, 1, 00, 00);
    LocalDateTime to = LocalDateTime.of(year, Month.DECEMBER, 31, 23, 59);
    // Retrieve a list (ServerSupervisionEvent), find by id and date, order by date.
    List<ServerSupervisionEvent> serverSupervisionEvents = supervisionEventRepository.findAllDistinctByIdAndEventTimeBetween(id,from,to);
    Collections.sort(serverSupervisionEvents, (o1,o2) -> o1.getEventTime().compareTo(o2.getEventTime()));
    return serverSupervisionEvents;
  }

  /**
   * Retrieve a list of {@link ServerLifecycleEvent} objects from the STL for a
   * given year.
   *
   * @param year the year of events to retrieve
   *
   * @return the list of {@link ServerLifecycleEvent} objects
   *
   * @throws ParseException if an invalid year was given
   */
  public List<ServerLifecycleEvent> getServerLifecycleEventsForYear(Integer year) throws ParseException {
    // Generate dates for the first and last days of the given year.
    DateFormat format = new SimpleDateFormat("yyyy-MM-dd");
    Date from = format.parse(String.valueOf(year) + "-01-01");
    Date to = format.parse(String.valueOf(year) + "-12-31");
    // Retrieve a list (ServerLifecycleEvent), order by date.
    List<ServerLifecycleEvent> serverLifecycleEvents = serverLifecycleEventRepository.findByEventTimeBetween(from, to);
    Collections.sort(serverLifecycleEvents, (o1,o2) -> o1.getEventTime().compareTo(o2.getEventTime()));

    return serverLifecycleEvents;
//    return mapper.getServerLifecycleEvents(new Timestamp(from.getTime()), new Timestamp(to.getTime()));
  }

//  /**
//   * Retrieve a list of names of C2MON servers in the cluster. Could be one or
//   * more.
//   *
//   * @return a list of server names
//   */
//  public List<String> getServerNames() {
//    return serverLifecycleEventRepository.findServerNames();
//    //return mapper.getServerNames();
//  }

  /**
   * Retrieve the list of names of all currently configured processes.
   *
   * @return the list of process names
   */
  public Collection<String> getProcessNames() {
    return processService.getProcessNames();
  }

  /**
   * Since the server names in the STL are not completely consistent (the names
   * were changed a couple of times), this method normalises them so that they
   * are all the same.
   *
   * @param events the list of events to normalise
   *
   * @return the normalised list
   */
  public List<ServerLifecycleEvent> normaliseServerNames(List<ServerLifecycleEvent> events) {
    for (ServerLifecycleEvent event : events) {
      if (event.getServerName().equals("C2MON-primary") || event.getServerName().equals("C2MON-PRO1") || event.getServerName().equals("C2MON-TIM-PRO1")) {
        event.setServerName("C2MON-TIM-PRO1");
      }
      if (event.getServerName().equals("C2MON-second") || event.getServerName().equals("C2MON-PRO2") || event.getServerName().equals("C2MON-TIM-PRO2")) {
        event.setServerName("C2MON-TIM-PRO2");
      }
    }

    return events;
  }

  /**
   * Retrieve the current tag statistics from the server.
   *
   * @return a {@link TagStatisticsResponse} object
   */
  public TagStatisticsResponse getTagStatistics() {
    return statisticsService.getTagStatistics();
  }
}
