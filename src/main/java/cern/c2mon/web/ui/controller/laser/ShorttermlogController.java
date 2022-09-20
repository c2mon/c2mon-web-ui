/******************************************************************************
 * Copyright (C) 2010-2016 CERN. All rights not expressly granted are reserved. This file is part of the CERN Control
 * and Monitoring Platform 'C2MON'. C2MON is free software: you can redistribute it and/or modify it under the terms of
 * the GNU Lesser General Public License as published by the Free Software Foundation, either version 3 of the license.
 * C2MON is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty
 * of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with C2MON. If not, see
 * <http://www.gnu.org/licenses/>.
 *****************************************************************************/
package cern.c2mon.web.ui.controller.laser;

import cern.c2mon.client.ext.history.alarm.AlarmRecord;
import cern.c2mon.client.ext.history.laser.AlarmShorttermlog;
import cern.c2mon.web.ui.model.AlarmLogParsed;
import cern.c2mon.web.ui.service.AlarmSearchService;
import cern.c2mon.web.ui.service.HistoryService;
import cern.c2mon.web.ui.service.laser.ShorttermlogService;
import cern.c2mon.web.ui.util.FormUtility;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * A controller for the alarm history viewer.
 */
@Profile("enableLaser")
@Controller
@Slf4j
public class ShorttermlogController {

    /**
     * Base URL for the history viewer
     */
    public static final String HISTORY_URL = "/alarmlog/";

    public static final String HISTORY_CSV_URL = HISTORY_URL + "/csv/";

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
    public static final String HISTORY_FORM_URL = HISTORY_URL + "form";

    /**
     * Title for the history form page
     */
    public static final String HISTORY_FORM_TITLE = "Alarm History Viewer (table)";

    /**
     * Instruction for the history form page
     */
    public static final String HISTORY_FORM_INSTR = "Enter a Alarm Id to create a Table View.";

    /** How many records in history to ask for. 100 looks ok! */
    private static final int HISTORY_RECORDS_TO_ASK_FOR = 100;

    public static final String PAGE_NUMBER_PARAMETER = "PAGENO";

    private static final Integer PAGE_SIZE = 20;

    /**
     * A history service
     */
    @Autowired
    private ShorttermlogService historyService;

    @Autowired
    private AlarmSearchService alarmService;

    /**
     * @return Redirects to the form
     */
    @RequestMapping(value = HISTORY_URL, method = { RequestMethod.GET })
    public final String viewHistory(final Model model) {
        log.info(HISTORY_URL);
        return ("redirect:" + HISTORY_FORM_URL);
    }

    /**
     * @param id the last 100 records of the given alarm id are being shown
     * @param response the html result is written to that HttpServletResponse response
     * @return Displays the history of a given alarm id.
     */
    @RequestMapping(value = HISTORY_URL + "{id}", method = { RequestMethod.GET })
    public final String viewHistory(@PathVariable(value = "id") final String id,
            @RequestParam(value = MAX_RECORDS_PARAMETER, required = false) final String maxRecords,
            @RequestParam(value = LAST_DAYS_PARAMETER, required = false) final String lastDays,
            @RequestParam(value = START_DATE_PARAMETER, required = false) final String startTime,
            @RequestParam(value = END_DATE_PARAMETER, required = false) final String endTime,
            @RequestParam(value = PAGE_NUMBER_PARAMETER, required = false) final Integer pageNo,
            final HttpServletResponse response, final Model model) throws IOException {
        log.info("/alarmhistoryviewer/{id} " + id);

        Optional<AlarmRecord> alarm = alarmService.findAlarmById(Long.parseLong(id));

        if (!alarm.isPresent()) {
            return ("redirect:" + HISTORY_FORM_URL + "?error=" + id);
        }

        long effectiveTagId = alarm.get().getTagId();

        int pageNumber = pageNo == null ? 1 : pageNo;

        Page<AlarmShorttermlog> history = null;
        String description = null;

        try {
            if (startTime != null && endTime != null) {
                history = historyService.requestAlarmHistory(effectiveTagId,
                        HistoryService.stringToLocalDateTime(startTime), HistoryService.stringToLocalDateTime(endTime), PAGE_SIZE, pageNumber - 1);
                description = " (From " + startTime + " to " + endTime + ")";
            } else if (lastDays != null) {
                history = historyService.requestAlarmHistoryForLastDays(effectiveTagId, Integer.parseInt(lastDays), PAGE_SIZE, pageNumber - 1);
                description = "(Last " + lastDays + " days)";
            } else if (id != null) {
                int numRecords = maxRecords != null ? Integer.parseInt(maxRecords) : HISTORY_RECORDS_TO_ASK_FOR;
                history = historyService.requestAlarmHistory(effectiveTagId, numRecords, PAGE_SIZE, pageNumber - 1);
                description = "(Last " + numRecords + " records)";
            }
        } catch (Exception e) {
            e.printStackTrace();
            return ("redirect:" + HISTORY_FORM_URL + "?error=" + id);
        }

        List<AlarmLogParsed> historyReverse = history.stream().map(a -> new AlarmLogParsed(a))
                .collect(Collectors.toList());

        model.addAttribute("alarm", alarm.get());
        model.addAttribute("description", description);
        model.addAttribute("history", historyReverse);
        model.addAttribute("totalPages", history.getTotalPages());
        model.addAttribute("pageNumber", pageNumber);
        model.addAttribute("title", HISTORY_FORM_TITLE);
        return "laser/alarmlog";
    }

    @RequestMapping(value = HISTORY_CSV_URL + "{id}", method = { RequestMethod.GET })
    public final String viewHistoryCsv(@PathVariable(value = "id") final String id,
            @RequestParam(value = MAX_RECORDS_PARAMETER, required = false) final String maxRecords,
            @RequestParam(value = LAST_DAYS_PARAMETER, required = false) final String lastDays,
            @RequestParam(value = START_DATE_PARAMETER, required = false) final String startTime,
            @RequestParam(value = END_DATE_PARAMETER, required = false) final String endTime,
            final HttpServletResponse response, final Model model) throws IOException {
        log.info(HISTORY_CSV_URL + id);

        List<AlarmShorttermlog> history = new ArrayList<>();

        try {
            if (startTime != null && endTime != null) {
                history = historyService.requestAlarmHistory(Long.parseLong(id),
                        HistoryService.stringToLocalDateTime(startTime), HistoryService.stringToLocalDateTime(endTime));
            } else if (lastDays != null) {
                history = historyService.requestAlarmHistoryForLastDays(Long.parseLong(id), Integer.parseInt(lastDays));
            } else if (id != null) {
                int numRecords = maxRecords != null ? Integer.parseInt(maxRecords) : HISTORY_RECORDS_TO_ASK_FOR;
                history = historyService.requestAlarmHistory(Long.parseLong(id), numRecords);
            }
        } catch (Exception e) {
            return ("redirect:" + HISTORY_FORM_URL + "?error=" + id);
        }
        List<AlarmLogParsed> historyReverse = history.stream().map(a -> new AlarmLogParsed(a))
                .collect(Collectors.toList());
        Collections.reverse(historyReverse);

        StringBuilder csv = new StringBuilder();
        String header = "Timestamp,Active,Alarm Prefix,Alarm Suffix,Alarm Timestamp,Alarm User\n";
        csv.append(header);

        for (AlarmLogParsed alarmLog : historyReverse) {
            csv.append(alarmLog.getTagservertime() + "," + alarmLog.getTagValue() + "," + alarmLog.getAlarmPrefix()
                    + "," + alarmLog.getAlarmSuffix() + "," + alarmLog.getAlarmTimestamp() + ","
                    + alarmLog.getAlarmUser() + "\n");
        }

        model.addAttribute("csv", csv);
        return "raw/csv";
    }

    /**
     * @param id alarm id
     * @param model Spring MVC Model instance to be filled in before jsp processes it
     * @return Displays an input form for a alarm id, and if a POST was made with a tag id, it redirects to HISTORY_URL
     *         + id + LAST_RECORDS_URL + records
     */
    @RequestMapping(value = HISTORY_URL + "form", method = { RequestMethod.GET, RequestMethod.POST })
    public final String viewHistoryFormPost(@RequestParam(value = "id", required = false) final String id,
            @RequestParam(value = "error", required = false) final String wrongId,
            @RequestParam(value = "records", required = false) final String records,
            @RequestParam(value = "days", required = false) final String days,
            @RequestParam(value = "start", required = false) final String startDate,
            @RequestParam(value = "end", required = false) final String endDate,
            @RequestParam(value = "startTime", required = false) final String startTime,
            @RequestParam(value = "endTime", required = false) final String endTime, final Model model) {
        log.info(HISTORY_URL + "form" + id);

        if (id == null) {
            model.addAllAttributes(FormUtility.getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL,
                    null, null, null));

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
            return ("redirect:" + HISTORY_URL + id + "?" + START_DATE_PARAMETER + "=" + startDate + "-" + startTime
                    + "&" + END_DATE_PARAMETER + "=" + endDate + "-" + endTime);
        } else if (records != null) {
            return ("redirect:" + HISTORY_URL + id + "?" + MAX_RECORDS_PARAMETER + "=" + records);
        }
        return "trend/alarmTrendViewForm";
    }
}
