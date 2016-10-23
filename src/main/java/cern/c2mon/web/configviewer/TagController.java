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
package cern.c2mon.web.configviewer;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.TagService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.List;
import java.util.Map;

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
  private HistoryService2 historyService;

  @RequestMapping(value = "/{id}", method = GET)
  public Tag getTag(@PathVariable final Long id) {
    return tagService.get(id);
  }

  @RequestMapping(value = "/{id}/history", method = GET)
  public List<Map<String, Object>> getTagHistory(@PathVariable final Long id) {
    return historyService.get(id);
  }

  @RequestMapping(value = "/search", method = GET)
  public Collection<Tag> search(@RequestParam final String query) {
    return tagService.findByName(query + "*");
  }
}
