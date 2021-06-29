/******************************************************************************
 * Copyright (C) 2010-2021 CERN. All rights not expressly granted are reserved.
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

import cern.c2mon.client.ext.history.command.CommandRecord;
import cern.c2mon.client.ext.history.command.CommandRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * HistoryService providing the XML representation for the history of a given command tag.
 */
@Service
public class HistoryCommandService {

    @Autowired
    private CommandRecordService commandRecordService;

    /**
     * Used to make a request for HistoryData.
     *
     * @param commandId The command tag id whose history we are looking for
     * @param localStartTime The start time expressed in the local time zone
     * @param localEndTime The end time expressed in the local time zone
     * @return history as a List of Command Tag
     */
    public final List<CommandRecord> requestCommandHistory(final Long commandId, final LocalDateTime localStartTime, final LocalDateTime localEndTime) {
        return commandRecordService.findAllByIdAndTimeBetweenOrderByTimeDesc(commandId, localStartTime, localEndTime);
    }

    /**
     * Used to make a request for HistoryData of an command tag.
     *
     * @param commandId      The command id whose history we are looking for
     * @param numberOfDays number of days to go back in History
     * @return history as a List of HistoryTagValueUpdates
     */
    public final List<CommandRecord> requestCommandHistoryForLastDays(final Long commandId, final int numberOfDays) {
        LocalDateTime now = LocalDateTime.now();
        return commandRecordService.findAllByIdAndTimeBetweenOrderByTimeDesc(commandId, now.minusDays(numberOfDays), now);
    }

    /**
     * Used to make a request for HistoryData of the last x command tags
     *
     * @param commandId The command id
     * @param numRecords number of records to be retrieved
     * @return The last N command records for the given command id
     */
    public final List<CommandRecord> requestCommandHistory(final Long commandId, final int numRecords) {
        return commandRecordService.findAllByIdOrderByTimeDesc(commandId, PageRequest.of(0, numRecords)).getContent();
    }

}
