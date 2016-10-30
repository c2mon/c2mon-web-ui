/*******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved.
 * <p>
 * This file is part of the CERN Control and Monitoring Platform 'C2MON'.
 * C2MON is free software: you can redistribute it and/or modify it under the
 * terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation, either version 3 of the license.
 * <p>
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for
 * more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with C2MON. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/

package cern.c2mon.web.ui;

import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.ConfigurationReportHeader;
import cern.c2mon.web.ui.service.ConfigHistoryService;
import cern.c2mon.web.ui.service.ConfigLoaderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.naming.CannotProceedException;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Justin Lewis Salmon
 */
@Controller
@ResponseBody
public class ConfigController2 {

  @Autowired
  ConfigHistoryService configHistoryService;

  @Autowired
  ConfigLoaderService configLoaderService;

  @RequestMapping(value = "/api/config/history", method = GET)
  public List<ConfigurationReportHeader> getConfigHistory(@RequestParam(required = false, defaultValue = "true") final boolean refresh) {
    return configHistoryService.getCachedReports(refresh);
  }

  @RequestMapping(value = "/api/config/{id}/run", method = POST)
  public void runConfig(@PathVariable("id") Long configId) throws CannotProceedException {
    configLoaderService.applyConfiguration(configId);
  }

  @RequestMapping(value = "/api/config/{id}/progress", method = GET)
  public ProgressUpdate getConfigProgress(@PathVariable("id") Long configId) {
    return configLoaderService.getProgressUpdate(configId);
  }

  @RequestMapping(value = "/api/config/{id}", method = GET)
  public List<ConfigurationReport> getConfigReports(@PathVariable("id") Long configId) {
    return configLoaderService.getConfigurationReports(String.valueOf(configId));
  }
}
