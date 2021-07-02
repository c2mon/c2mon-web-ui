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
package cern.c2mon.web.ui.controller;

import cern.c2mon.client.ext.history.command.CommandRecord;
import cern.c2mon.web.ui.service.HistoryCommandService;
import cern.c2mon.web.ui.service.HistoryService;
import cern.c2mon.web.ui.util.FormUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * A controller for the command history viewer.
 */
@Controller
@Slf4j
public class CommandHistoryController {

    /**
     * Base URL for the history viewer
     */
    public static final String HISTORY_URL = "/commandhistory/";

    /** Parameter: MAX RECORDS */
    public static final String MAX_RECORDS_PARAMETER = "RECORDS";

    /** Parameter: LAST DAYS */
    public static final String LAST_DAYS_PARAMETER = "DAYS";

    /** Start Date */
    public static final String START_DATE_PARAMETER = "START";

    /** End Date */
    public static final String END_DATE_PARAMETER = "END";

    /**
     * A URL to the history viewer with input form
     */
    public static final String HISTORY_FORM_URL = "/commandhistory/form";

    /**
     * Title for the history form page
     */
    public static final String HISTORY_FORM_TITLE = "Command History (table)";

    /**
     * Instruction for the history form page
     */
    public static final String HISTORY_FORM_INSTR = "Enter a Command Id to create a Table View.";

    /** How many records in history to ask for. 100 looks ok! */
    private static final int HISTORY_RECORDS_TO_ASK_FOR = 100;

    /**
     * A history service
     */
    @Autowired
    private HistoryCommandService historyService;

    /**
     * Method responsible of redirecting to the form
     * @param model supplies attributes used for rendering views
     * @return Redirects to the form
     */
    @RequestMapping(value = HISTORY_URL, method = {RequestMethod.GET})
    public final String viewHistory(final Model model) {
        log.info(HISTORY_URL);
        return ("redirect:" + HISTORY_FORM_URL);
    }

    /**
     * Display Command history
     * @param id       the last 100 records of the given command id are being shown
     * @param maxRecords number of records requested
     * @param lastDays last days to which records must belong to
     * @param startTime initial date to which requested records must belong to
     * @param endTime final date to which request records mustbelong to
     * @param response the html result is written to that HttpServletResponse response
     * @param model  supplies attributes used for rendering views
     * @return Displays the history of a given command id.
     */
    @RequestMapping(value = HISTORY_URL + "{id}", method = {RequestMethod.GET})
    public final String viewHistory(@PathVariable(value = "id") final String id,
                                    @RequestParam(value = MAX_RECORDS_PARAMETER, required = false) final String
                                            maxRecords,
                                    @RequestParam(value = LAST_DAYS_PARAMETER, required = false) final String lastDays,
                                    @RequestParam(value = START_DATE_PARAMETER, required = false) final String startTime,
                                    @RequestParam(value = END_DATE_PARAMETER, required = false) final String endTime,
                                    final HttpServletResponse response, final Model model) {
        log.info("/commandhistory/{id} " + id);

        List<CommandRecord> history = new ArrayList<>();
        String description = null;

        try {
            if (startTime != null && endTime != null) {
                history = historyService.requestCommandHistory(Long.parseLong(id), HistoryService.stringToLocalDateTime(startTime),
                        HistoryService.stringToLocalDateTime(endTime));
                description = " (From " + startTime + " to " + endTime + ")";
            } else if (lastDays != null) {
                history = historyService.requestCommandHistoryForLastDays(Long.parseLong(id), Integer.parseInt(lastDays));
                description = "(Last " + lastDays + " days)";
            } else if (id != null) {
                int numRecords = maxRecords != null ? Integer.parseInt(maxRecords) : HISTORY_RECORDS_TO_ASK_FOR;
                history = historyService.requestCommandHistory(Long.parseLong(id), numRecords);
                description = "(Last " + numRecords + " records)";
            }
        } catch (Exception e) {
            log.error("Exception while trying to view the Command history", e);
            return ("redirect:" + HISTORY_FORM_URL + "?error=" + id);
        }
        List<CommandRecord> historyReverse = new ArrayList<>(history);
        Collections.reverse(historyReverse);

        model.addAttribute("commandId", id);
        model.addAttribute("description", description);
        model.addAttribute("history", history);
        model.addAttribute("title", HISTORY_FORM_TITLE);
        return "commandhistory";
    }

    /**
     * View Input form to display Command history
     * @param id    command id
     * @param wrongId returns error if wrongId not null
     * @param records specifies number of records to request
     * @param days specifies the number of days that records must belong to
     * @param startDate initial date that records must belong to
     * @param endDate final date that records must belong to
     * @param startTime initial time that records must belong to
     * @param endTime end time that records must belong to
     * @param model Spring MVC Model instance to be filled in before
     *              jsp processes it
     * @return Displays an input form for a command id, and if a POST was made with
     * a command id, it redirects to HISTORY_URL + id + LAST_RECORDS_URL + records
     */
    @RequestMapping(value = HISTORY_URL + "form", method = {RequestMethod.GET, RequestMethod.POST})
    public final String viewHistoryFormPost(@RequestParam(value = "id", required = false) final String id,
                                            @RequestParam(value = "error", required = false) final String wrongId,
                                            @RequestParam(value = "records", required = false) final String records,
                                            @RequestParam(value = "days", required = false) final String days,
                                            @RequestParam(value = "start", required = false) final String startDate,
                                            @RequestParam(value = "end", required = false) final String endDate,
                                            @RequestParam(value = "startTime", required = false) final String startTime,
                                            @RequestParam(value = "endTime", required = false) final String endTime,
                                            final Model model) {
        log.info(HISTORY_URL + "form" + id);

        if (id == null) {
            model.addAllAttributes(FormUtility.getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL, null,
                    null));

            if (wrongId != null) {
                model.addAttribute("error", wrongId);
            }

            // let's pre-fill the date boxes with the current date
            DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
            DateFormat timeFormat = new SimpleDateFormat("HH:mm");

            Date currentDate = new Date();
            model.addAttribute("defaultToDate", dateFormat.format(currentDate));
            model.addAttribute("defaultToTime", timeFormat.format(currentDate));

            Date oneHourBeforeDate = new Date(currentDate.getTime() - 3600 * 1000);
            model.addAttribute("defaultFromDate", dateFormat.format(oneHourBeforeDate));
            model.addAttribute("defaultFromTime", timeFormat.format(oneHourBeforeDate));
        } else if (days != null) {
            return ("redirect:" + HISTORY_URL + id + "?" + LAST_DAYS_PARAMETER + "=" + days);
        } else if (startDate != null) {
            return ("redirect:" + HISTORY_URL + id + "?" + START_DATE_PARAMETER + "=" + startDate + "-" + startTime + "&" +
                    END_DATE_PARAMETER + "=" + endDate + "-" + endTime);
        } else if (records != null) {
            return ("redirect:" + HISTORY_URL + id + "?" + MAX_RECORDS_PARAMETER + "=" + records);
        }
        return "trend/commandTrendViewForm";
    }
}