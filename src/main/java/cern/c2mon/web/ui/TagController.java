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

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.ConfigurationService;
import cern.c2mon.client.core.TagService;
import cern.c2mon.shared.client.tag.TagConfig;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Justin Lewis Salmon
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {

  @Autowired
  private TagService tagService;

  @Autowired
  private ConfigurationService configurationService;

  @Autowired
  private ElasticsearchService elasticsearchService;

  @Autowired
  private ObjectMapper mapper;

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
    return merge(mapper.convertValue(tag, JsonNode.class), mapper.convertValue(config, JsonNode.class));
  }

  @RequestMapping(value = "/{id}/history", method = GET)
  public List<Object[]> getTagHistory(@PathVariable final Long id,
                                      @RequestParam("min") Long min,
                                      @RequestParam("max") Long max) {
    return elasticsearchService.getHistory(id, min, max);
  }

  @RequestMapping(value = "/search", method = GET)
  public Collection<Tag> search(@RequestParam final String query) {
    return tagService.findByName(query);
  }

  @RequestMapping(value = "/top", method = GET)
  public Collection<Tag> getTopTags(@RequestParam final Integer size) {
    return elasticsearchService.getTopTags(size);
  }

  private JsonNode merge(JsonNode a, JsonNode b) {
    Iterator<String> fieldNames = b.fieldNames();

    while (fieldNames.hasNext()) {
      String fieldName = fieldNames.next();
      JsonNode jsonNode = a.get(fieldName);

      if (jsonNode != null && jsonNode.isObject()) {
        merge(jsonNode, b.get(fieldName));
      } else {
        if (a instanceof ObjectNode) {
          JsonNode value = b.get(fieldName);
          ((ObjectNode) a).put(fieldName, value);
        }
      }
    }

    return a;
  }
}
