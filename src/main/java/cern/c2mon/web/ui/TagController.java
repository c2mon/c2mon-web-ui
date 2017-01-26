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
package cern.c2mon.web.ui;

import cern.c2mon.client.common.listener.TagListener;
import cern.c2mon.client.common.tag.Tag;
//import cern.c2mon.client.core.elasticsearch.ElasticsearchService;
import cern.c2mon.client.core.ConfigurationService;
import cern.c2mon.client.core.TagService;
import cern.c2mon.client.core.jms.SupervisionListener;
import cern.c2mon.client.core.manager.SupervisionManager;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
//import cern.c2mon.shared.client.expression.Expression;
import cern.c2mon.shared.client.supervision.SupervisionEvent;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.shared.common.supervision.SupervisionConstants;
import cern.c2mon.web.ui.service.ProcessService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import static cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity.EQUIPMENT;
import static cern.c2mon.shared.common.supervision.SupervisionConstants.SupervisionEntity.PROCESS;
import static java.lang.String.format;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Justin Lewis Salmon
 */
@RestController
@RequestMapping("/api/tags")
public class TagController implements TagListener/*, SupervisionListener*/ {

  @Autowired
  private TagService tagService;

  @Autowired
  private ProcessService processService;

  @Autowired
  private ConfigurationService configurationService;

  @Autowired
  private ElasticsearchService elasticsearchService;

  @Autowired
  SupervisionManager supervisionManager;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private SimpMessagingTemplate brokerMessagingTemplate;

  @PostConstruct
  public void init() {
//    List<Long> processIds = new ArrayList<>();
//    List<Long> equipmentIds = new ArrayList<>();
//
//    for (String processName : processService.getProcessNames()) {
//      ProcessConfiguration process = processService.getProcessConfiguration(processName);
//
//      processIds.add(process.getProcessID());
//      equipmentIds.addAll(process.getEquipmentConfigurations().keySet());
//    }
//
//    // Subscribe to all supervision events
//    supervisionManager.addSupervisionListener(this, processIds, equipmentIds, Collections.emptyList());
  }

  @RequestMapping(value = "/{id}", method = GET)
  public Object getTag(@PathVariable final String id) throws IOException {
    Tag tag;

    if (StringUtils.isNumeric(id)) {
      tag = tagService.get(Long.valueOf(id));
    } else {
      tag = ((List<Tag>) tagService.findByName(id)).get(0);
    }

    return mergeTagConfig(tag);
  }

  @RequestMapping(value = "/{id}/history", method = GET)
  public List<Object[]> getTagHistory(@PathVariable final Long id,
                                      @RequestParam("min") Long min,
                                      @RequestParam("max") Long max) {
    return elasticsearchService.getHistory(id, min, max);
  }

//  @RequestMapping(value = "/{id}/expressions/{name}", method = POST)
//  public ResponseEntity<?> addExpression(@PathVariable final Long id, @PathVariable final String name, @RequestBody String expressionText) {
//    DataTag tag = DataTag.update(id)
//        .expression(name, expressionText)
//        .build();
//
//    ConfigurationReport report = configurationService.updateDataTag(tag);
//
//    if (report.getStatus().equals(ConfigConstants.Status.OK)) {
//      URI location = ServletUriComponentsBuilder
//          .fromCurrentRequest().path("/{name}")
//          .buildAndExpand(name).toUri();
//
//      return ResponseEntity.created(location).build();
//    } else {
//      return ResponseEntity.badRequest().build();
//    }
//  }
//
//  @RequestMapping(value = "/{id}/expressions/{name}", method = PATCH)
//  public ResponseEntity<?> updateExpression(@PathVariable final Long id, @PathVariable final String name, @RequestBody String expressionText) {
//    DataTag tag = DataTag.update(id)
//        .expression(name, expressionText)
//        .build();
//
//    ConfigurationReport report = configurationService.updateDataTag(tag);
//
//    if (report.getStatus().equals(ConfigConstants.Status.OK)) {
//      URI location = ServletUriComponentsBuilder
//          .fromCurrentRequest().path("/{name}")
//          .buildAndExpand(name).toUri();
//
//      return ResponseEntity.created(location).build();
//    } else {
//      return ResponseEntity.badRequest().build();
//    }
//  }

  @MessageMapping("/tags/{id}")
  public void subscribe(@DestinationVariable Long id) throws Exception {
    tagService.subscribe(id, this);
    this.brokerMessagingTemplate.convertAndSend("/topic/tags/" + id, mergeTagConfig(tagService.get(id)));
  }

  @Override
  public void onInitialUpdate(Collection<Tag> tags) {
    for (Tag tag : tags) {
      this.brokerMessagingTemplate.convertAndSend("/topic/tags/" + tag.getId(), mergeTagConfig(tag));
    }
  }

  @Override
  public void onUpdate(Tag tag) {
    this.brokerMessagingTemplate.convertAndSend("/topic/tags/" + tag.getId(), mergeTagConfig(tag));
  }

//  @Override
//  public void onSupervisionUpdate(SupervisionEvent event) {
//    System.out.println(format("Got a supervision update: %s %s %s %s %s",
//        event.getName(), event.getEntity(), event.getEntityId(), event.getMessage(), event.getStatus()));
//
//    if (event.getEntity().equals(PROCESS)) {
//      this.brokerMessagingTemplate.convertAndSend("/topic/supervision/process/" + event.getEntityId(), event);
//    } else if (event.getEntity().equals(EQUIPMENT)) {
//      this.brokerMessagingTemplate.convertAndSend("/topic/supervision/equipment/" + event.getEntityId(), event);
//    }
//  }

  @RequestMapping(value = "/search", method = GET)
  public Collection<Object> search(@RequestParam final String query) {
    return mergeTagConfigs((List<Tag>) elasticsearchService.findByName(query));
  }

  @RequestMapping(value = "/top", method = GET)
  public Collection<Object> getTopTags(@RequestParam final Integer size) {
    List<Tag> tags = filterUnknownTags(elasticsearchService.getTopTags(size));
    Collections.reverse(tags);
    return mergeTagConfigs(tags);
  }

  private List<Tag> filterUnknownTags(List<Tag> tags) {
    return tags.stream().filter(tag -> tag.getDataTagQuality().isExistingTag()).collect(Collectors.toList());
  }

  private Collection<Object> mergeTagConfigs(List<Tag> tags) {
    return tags.stream().map(this::mergeTagConfig).collect(Collectors.toList());
  }

  private Object mergeTagConfig(Tag tag) {
    TagConfig config = ((List<TagConfig>) configurationService.getTagConfigurations(
        Collections.singletonList(tag.getId()))).get(0);

    // Merge the tag and its config to avoid the extra call
    ObjectNode merged = (ObjectNode) JsonUtils.merge(mapper.convertValue(tag, JsonNode.class), mapper.convertValue(config, JsonNode.class));

    // Add the process and equipment name, because it's useful
    ProcessConfiguration process = processService.getProcessConfiguration(tag.getProcessIds().iterator().next());
    merged.put("processName", process.getProcessName());

    if (!tag.getEquipmentIds().isEmpty()) {
      EquipmentConfiguration equipment = process.getEquipmentConfiguration(tag.getEquipmentIds().iterator().next());
      merged.put("equipmentName", equipment.getName());
    }

    return merged;
  }
}
