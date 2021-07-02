/******************************************************************************
 * Copyright (C) 2010-2019 CERN. All rights not expressly granted are reserved.
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.common.tag.CommandTag;
import cern.c2mon.client.core.tag.CommandTagImpl;

/**
 * Command service providing the XML representation of a given tag
 */
@Slf4j
@Service
public class CommandService {

  @Autowired
  private cern.c2mon.client.core.service.CommandService commandManager;

  /**
   * Gets the XML representation of the configuration of a command
   *
   * @param commandId id of the command
   * @return XML representation of command configuration
   * @throws TagIdException if command was not found or a non-numeric id was
   *           requested ({@link TagIdException}), or any other exception thrown
   *           by the underlying service gateway.
   */
  @SuppressWarnings("rawtypes")
  public String getCommandTagXml(final String commandId) throws TagIdException {
    try {
      CommandTagImpl command = (CommandTagImpl) getCommandTag(Long.parseLong(commandId));
      if (command.isExistingCommand())
        return command.getXml();
      else
        throw new TagIdException("No command found");
    } catch (NumberFormatException e) {
      throw new TagIdException("Invalid command id");
    }
  }

  /**
   * Retrieves a command tag object from the service gateway tagManager
   *
   * @param commandId id of the alarm
   * @return command tag
   */
  public CommandTag<Object> getCommandTag(final long commandId) {
    commandManager.clearCommandCache();
    CommandTag<Object> ct = commandManager.getCommandTag(commandId);
    String result = "UNKNOWN".equals(ct.getName()) ? "NULL" : "SUCCESS";
    log.debug("Command fetch for command #{}: {}", commandId, result);
    if("NULL".equals(result)){
      return null;
    }
    return ct;
  }
}
