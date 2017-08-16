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


import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.service.ConfigurationService;
import cern.c2mon.shared.client.tag.TagConfig;

/**
 * Datatag service providing the XML representation of a given datatag
 */
@Slf4j
@Service
public class TagService {

  /**
   * Gateway to C2monService
   */
  @Autowired
  private cern.c2mon.client.core.service.TagService tagManager;

  @Autowired
  private ConfigurationService configurationService;


  /**
   * Retrieves a tagConfig object from the service gateway tagManager
   * @param tagId id of the datatag
   * @return tag configuration
   */
  public TagConfig getTagConfig(final long tagId) {
    TagConfig tc = null;
    List<Long> tagIds = new ArrayList<Long>();
    tagIds.add(tagId);
    Collection<TagConfig> tagConfigs = configurationService.getTagConfigurations(tagIds);
    Iterator<TagConfig> it = tagConfigs.iterator();
    if (it.hasNext()) {
      tc = it.next();
    }
    log.debug("Tag config fetch for tag " + tagId + ": " + (tc == null ? "NULL" : "SUCCESS"));
    return tc;
  }

  /**
   * Retrieves a tagValue object from the service gateway tagManager
   * @param dataTagId id of the datatag
   * @return tag value
   */
  public Tag getTag(final long dataTagId) {
    Tag dataTag = tagManager.get(dataTagId);
    log.debug("Datatag value fetch for tag " + dataTagId + ": " + (dataTag == null ? "NULL" : "SUCCESS"));
    return dataTag;
  }
}
