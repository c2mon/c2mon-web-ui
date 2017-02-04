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

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.web.ui.service.ProcessService;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
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

  @Autowired
  TagService tagService;

  @RequestMapping("/api/processes")
  public List<ProcessConfiguration> getProcesses() throws Exception {
    List<ProcessConfiguration> processes = new ArrayList<>();

    for (String processName : processService.getProcessNames()) {
      processes.add(processService.getProcessConfiguration(processName));
    }

    return processes;
  }

  @RequestMapping("/api/processes/{name}")
  public Object getProcess(@PathVariable("name") String name) throws Exception {
    ProcessConfiguration process = processService.getProcessConfiguration(name);
    ObjectNode node = new ObjectMapper().valueToTree(process);

    // Hack in the status tags (unfortunately there is no API method for getting
    // these, and the don't come with the process configuration
    Tag processStatusTag = null;
    List<Tag> statusTags = (List<Tag>) tagService.findByName("*" + name.substring(2, name.length()) + ":STATUS");
    if (statusTags.size() >= 1) {
      for (Tag tag : statusTags) {
        if (tag.getEquipmentIds().isEmpty() && !tag.getProcessIds().isEmpty()) {
          processStatusTag = tag;
          break;
        }
      }
    }

    if (processStatusTag != null) {
      node.put("statusTagId", processStatusTag.getId());
    }

    for (EquipmentConfiguration equipment : process.getEquipmentConfigurations().values()) {
      Tag equipmentStatusTag = null;

      statusTags = (List<Tag>) tagService.findByName("*" + equipment.getName().substring(6, equipment.getName().length()) + ":STATUS");

      if (statusTags.size() == 0) {
        statusTags = (List<Tag>) tagService.findByName("*" + equipment.getName() + ":STATUS");
      }

      if (statusTags.size() >= 1) {
        for (Tag tag : statusTags) {
          if (!tag.getEquipmentIds().isEmpty() && !tag.getProcessIds().isEmpty()) {
            equipmentStatusTag = tag;
            break;
          }
        }
      }

      if (equipmentStatusTag != null) {
        ObjectNode equipmentNode = (ObjectNode) node.get("equipmentConfigurations").get(String.valueOf(equipment.getId()));
        equipmentNode.put("statusTagId", equipmentStatusTag.getId());
      }
    }

    return node;
  }
}
