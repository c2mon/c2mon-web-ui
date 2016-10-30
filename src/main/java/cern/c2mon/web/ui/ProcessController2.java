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

package cern.c2mon.web.ui;

import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.web.ui.service.ProcessService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Justin Lewis Salmon
 */
@RestController
public class ProcessController2 {

  @Autowired
  ProcessService processService;

  @RequestMapping("/api/processes")
  public List<ProcessConfiguration> getProcesses() throws Exception {
    List<ProcessConfiguration> processes = new ArrayList<>();

    for (String processName : processService.getProcessNames()) {
      processes.add(processService.getProcessConfiguration(processName));
    }

    return processes;
  }

  @RequestMapping("/api/processes/{name}")
  public ProcessConfiguration getProcess(@PathVariable("name") String name) throws Exception {
    return processService.getProcessConfiguration(name);
  }
}
