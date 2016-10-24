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

import java.util.*;

import javax.naming.CannotProceedException;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.shared.client.request.ClientRequestProgressReport;
import cern.c2mon.web.configviewer.ProgressUpdate;
import cern.c2mon.web.configviewer.ProgressUpdateListener;
import cern.c2mon.web.ui.util.ReportHandler;

@Service
@Slf4j
public class ConfigLoaderService {

  @Autowired
  private ConfigurationService configurationService;

  /**
   * Stores the ProgressReports.
   */
  private Map<String, ReportHandler> progressReports = new HashMap<>();

  /**
   * Stores the partial {@link ConfigurationReportHeader} objects. Each
   * configuration id may have multiple reports (as it may be run multiple
   * times).
   */
  private  List<ConfigurationReportHeader> configurationReportHeaders = new ArrayList<>();

  /**
   * Stores the full {@link ConfigurationReport} objects. Each configuration id
   * may have multiple reports (as it may be run multiple times).
   */
  private Map<String, List<ConfigurationReport>> configurationReports = new HashMap<>();

  /**
   * Map of request ids to their corresponding configuration listeners
   */
  private Map<Long, ProgressUpdateListener> listeners = new HashMap<>();

  /**
   * Applies the specified configuration and stores the Configuration Report for
   * later viewing.
   *
   * @param configurationId id of the configuration of the request
   * @throws CannotProceedException In case a serious error occurs (for example
   *           in case a null Configuration Report is received).
   */
  public void applyConfiguration(final long configurationId) throws CannotProceedException {

//    ReportHandler reportHandler = new ReportHandler(configurationId);
    //progressReports.put(String.valueOf(configurationId), reportHandler);

    ProgressUpdateListener listener = new ProgressUpdateListener();
    listeners.put(configurationId, listener);

    ConfigurationReport report = configurationService.applyConfiguration(configurationId, listener);

    log.debug("Received configuration report? -> " + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));

    if (report == null) {
      log.error("Received null report for configuration id: " + configurationId);
      throw new CannotProceedException("Did not receive configuration report");
    }
    log.debug("Report: " + report.toXML());

    if (report.getName().equals("UNKNOWN")) {
      if (configurationReports.containsKey(String.valueOf(configurationId))) {
        configurationReports.get(String.valueOf(configurationId)).add(report);
      } else {
        List<ConfigurationReport> reports = getConfigurationReports(String.valueOf(configurationId));
        if (reports.isEmpty()) {
          reports.add(report);
        }
        configurationReports.put(String.valueOf(configurationId), reports);
      }
    }

    // store the report for viewing later
    ConfigurationReportHeader header = new ConfigurationReportHeader(report.getId(), report.getName(), report.getUser(), report.getStatus(),
        report.getStatusDescription(), report.getTimestamp());
    configurationReportHeaders.add(header);
  }

  /**
   * Retrieves all full reports for a particular configuration.
   *
   * @param configurationId id of the configuration of the request
   * @return a map of full reports
   */
  public List<ConfigurationReport> getConfigurationReports(final String configurationId) {
    List<ConfigurationReport> reports;

    if (configurationReports.containsKey(configurationId)) {
      reports = configurationReports.get(configurationId);
    }

    else {
      reports = new ArrayList<>(configurationService.getConfigurationReports(Long.valueOf(configurationId)));
      Collections.sort(reports);
    }

    if (reports == null) {
      log.error("Could not retrieve Stored Configuration Report for configuration id:" + configurationId);
      throw new IllegalArgumentException("Cannot find Configuration Report for configuration id:" + configurationId);
    }
    log.debug("Successfully retrieved Stored Configuration Report for configuration id:" + configurationId);

    return reports;
  }

  /**
   * Retrieve partial information about all previous configurations.
   *
   * @return all the previously applied configuration reports
   */
  public List<ConfigurationReportHeader> getConfigurationReports(boolean refresh) {
    if (refresh || configurationReportHeaders.isEmpty()) {
      configurationReportHeaders = new ArrayList<>(configurationService.getConfigurationReports());
      Collections.reverse(configurationReportHeaders);
    }

    return configurationReportHeaders;
  }

  /**
   * @param configurationId id of the configuration request
   * @return a Progress Report for the specified configuration (must be
   *         currently running!)
   *
   * @deprecated
   */
  public ClientRequestProgressReport getProgressReportForConfiguration(final String configurationId) {

    ClientRequestProgressReport report = null;
    ReportHandler reportHandler = progressReports.get(configurationId);

    if (reportHandler != null)
      report = reportHandler.getProgressReport();

    log.debug("ClientRequestProgressReport: fetch for report: " + configurationId + ": " + (report == null ? "NULL" : "SUCCESS"));
    return report;
  }

  /**
   *
   * @param configId
   * @return
   */
  public ProgressUpdate getProgressUpdate(Long configId) {
    ProgressUpdateListener listener = listeners.get(configId);

    if (listener != null && listener.getProgress().getProgress() != null) {
      if (listener.getProgress().getProgress() == 100) {
        listeners.remove(configId);
        return null;
      }
      return listener.getProgress();
    }

    return null;
  }
}
