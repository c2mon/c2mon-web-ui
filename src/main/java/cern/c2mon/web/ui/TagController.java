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
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.client.core.service.TagService;
import cern.c2mon.shared.client.configuration.ConfigConstants;
import cern.c2mon.shared.client.configuration.ConfigurationReport;
import cern.c2mon.shared.client.configuration.api.tag.DataTag;
//import cern.c2mon.shared.client.expression.Expression;
import cern.c2mon.shared.client.tag.TagConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static java.lang.String.format;
import static org.springframework.web.bind.annotation.RequestMethod.GET;
import static org.springframework.web.bind.annotation.RequestMethod.PATCH;
import static org.springframework.web.bind.annotation.RequestMethod.POST;

/**
 * @author Justin Lewis Salmon
 */
@RestController
@RequestMapping("/api/tags")
public class TagController implements TagListener {

  @Autowired
  private TagService tagService;

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

    TagConfig config = ((List<TagConfig>) configurationService.getTagConfigurations(
        Collections.singletonList(tag.getId()))).get(0);

    // Merge the tag and its config to avoid the extra call
    return JsonUtils.merge(mapper.convertValue(tag, JsonNode.class), mapper.convertValue(config, JsonNode.class));
  }

  @RequestMapping(value = "/{id}/history", method = GET)
  public List<Object[]> getTagHistory(@PathVariable final Long id,
                                      @RequestParam("min") Long min,
                                      @RequestParam("max") Long max) {
    return elasticsearchService.getHistory(id, min, max);
  }

  @RequestMapping(value = "/{id}/expressions/{name}", method = POST)
  public ResponseEntity<?> addExpression(@PathVariable final Long id, @PathVariable final String name, @RequestBody String expressionText) {
    DataTag tag = DataTag.update(id)
        .expression(name, expressionText)
        .build();

    ConfigurationReport report = configurationService.updateDataTag(tag);

    if (report.getStatus().equals(ConfigConstants.Status.OK)) {
      URI location = ServletUriComponentsBuilder
          .fromCurrentRequest().path("/{name}")
          .buildAndExpand(name).toUri();

      return ResponseEntity.created(location).build();
    } else {
      return ResponseEntity.badRequest().build();
    }
  }

  @RequestMapping(value = "/{id}/expressions/{name}", method = PATCH)
  public ResponseEntity<?> updateExpression(@PathVariable final Long id, @PathVariable final String name, @RequestBody String expressionText) {
    DataTag tag = DataTag.update(id)
        .expression(name, expressionText)
        .build();

    ConfigurationReport report = configurationService.updateDataTag(tag);

    if (report.getStatus().equals(ConfigConstants.Status.OK)) {
      URI location = ServletUriComponentsBuilder
          .fromCurrentRequest().path("/{name}")
          .buildAndExpand(name).toUri();

      return ResponseEntity.created(location).build();
    } else {
      return ResponseEntity.badRequest().build();
    }
  }

  @MessageMapping("/tags/{id}")
  public void subscribe(@DestinationVariable Long id) throws Exception {
    tagService.subscribe(id, this);
    this.brokerMessagingTemplate.convertAndSend("/topic/tags/" + id, tagService.get(id));
  }

  @Override
  public void onInitialUpdate(Collection<Tag> tags) {
    for (Tag tag : tags){
      this.brokerMessagingTemplate.convertAndSend("/topic/tags/" + tag.getId(), tag);
    }
  }

  @Override
  public void onUpdate(Tag tag) {
    System.out.println(format("Got a tag update: %s = %s", tag.getName(), tag.getValue()));
    this.brokerMessagingTemplate.convertAndSend("/topic/tags/" + tag.getId(), tag);
  }

  @RequestMapping(value = "/search", method = GET)
  public Collection<Tag> search(@RequestParam final String query) {
    return elasticsearchService.findByName(query);
  }

  @RequestMapping(value = "/top", method = GET)
  public Collection<Tag> getTopTags(@RequestParam final Integer size) {
    return elasticsearchService.getTopTags(size);
  }
}
