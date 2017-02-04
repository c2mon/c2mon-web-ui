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
import cern.c2mon.client.core.elasticsearch.ElasticsearchService;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.shared.client.tag.TagConfig;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.web.ui.service.ProcessService;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Justin Lewis Salmon
 */
@RestController
@RequestMapping("/api/tags")
public class TagController2 implements TagListener {

  @Autowired
  private TagService tagService;

  @Autowired
  private ProcessService processService;

  @Autowired
  private ConfigurationService configurationService;

  @Autowired
  private ElasticsearchService elasticsearchService;

  @Autowired
  private ObjectMapper mapper;

  @Autowired
  private SimpMessagingTemplate brokerMessagingTemplate;

  @RequestMapping(value = "/{id}", method = GET)
  public Object getTag(@PathVariable final String id) throws IOException {
    Tag tag;

    if (StringUtils.isNumeric(id)) {
      tag = tagService.get(Long.valueOf(id));
    } else {
      tag = ((List<Tag>) tagService.findByName(id)).get(0);
    }

    return mergeTagConfig(tag, getTagConfig(tag));
  }

  @RequestMapping(value = "/{id}/history", method = GET)
  public List<Object[]> getTagHistory(@PathVariable final Long id,
                                      @RequestParam("min") Long min,
                                      @RequestParam("max") Long max) {
    return elasticsearchService.getHistory(id, min, max);
  }

  @MessageMapping("/tags/{id}")
  public void subscribe(@DestinationVariable Long id) throws Exception {
    tagService.subscribe(id, this);
    Tag tag = tagService.get(id);
    this.brokerMessagingTemplate.convertAndSend("/topic/tags/" + id, mergeTagConfig(tag, getTagConfig(tag)));
  }

  @Override
  public void onInitialUpdate(Collection<Tag> tags) {
    for (Tag tag : tags) {
      this.brokerMessagingTemplate.convertAndSend("/topic/tags/" + tag.getId(), mergeTagConfig(tag, getTagConfig(tag)));
    }
  }

  @Override
  public void onUpdate(Tag tag) {
    this.brokerMessagingTemplate.convertAndSend("/topic/tags/" + tag.getId(), mergeTagConfig(tag, getTagConfig(tag)));
  }

  @RequestMapping(value = "/search", method = GET)
  public Collection<Object> search(@RequestParam final String query) {
    return mergeTagConfigs((List<Tag>) tagService.findByName(query));
  }

  @RequestMapping(value = "/top", method = GET)
  public Collection<Object> getTopTags(@RequestParam final Integer size) {
    List<Tag> tags = filterUnknownTags((List<Tag>) tagService.get(elasticsearchService.findByName("")));
    Collections.reverse(tags);
    return mergeTagConfigs(tags);
  }

  private TagConfig getTagConfig(Tag tag) {
    return configurationService.getTagConfigurations(Collections.singleton(tag.getId())).iterator().next();
  }

  private List<Tag> filterUnknownTags(List<Tag> tags) {
    return tags.stream().filter(tag -> tag.getDataTagQuality().isExistingTag()).collect(Collectors.toList());
  }

  private Collection<Object> mergeTagConfigs(List<Tag> tags) {
    List<Long> tagIds = tags.stream().map(Tag::getId).collect(Collectors.toList());
    List<TagConfig> tagConfigs = (List<TagConfig>) configurationService.getTagConfigurations(tagIds);
    List<Object> merged = new ArrayList<>();

    for (Tag tag : tags) {
      merged.add(mergeTagConfig(tag, tagConfigs.get(tags.indexOf(tag))));
    }

    return merged;
  }

  private Object mergeTagConfig(Tag tag, TagConfig config) {
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
