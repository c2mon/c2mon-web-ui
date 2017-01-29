package cern.c2mon.web.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import cern.c2mon.client.common.tag.CommandTag;
import cern.c2mon.client.core.CommandService;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.web.ui.service.ProcessService;

/**
 * @author Justin Lewis Salmon
 */
@RestController
public class CommandController2 {

  @Autowired
  private CommandService commandService;

  @Autowired
  private ProcessService processService;

  @RequestMapping("/api/commands/{id}")
  public Object getCommand(@PathVariable("id") Long id) throws Exception {
    CommandTag command = commandService.getCommandTag(id);

    // Hack in the process and equipment names
    ObjectNode node = new ObjectMapper().valueToTree(command);

        // Add the process and equipment name, because it's useful
    ProcessConfiguration process = processService.getProcessConfiguration(command.getProcessId());
    node.put("processName", process.getProcessName());

    EquipmentConfiguration equipment = process.getEquipmentConfiguration(command.getEquipmentId());
    node.put("equipmentName", equipment.getName());

    return node;
  }
}
