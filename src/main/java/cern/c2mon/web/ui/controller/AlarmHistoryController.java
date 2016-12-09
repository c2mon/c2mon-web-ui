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
package cern.c2mon.web.ui.controller;

import cern.c2mon.client.ext.history.alarm.Alarm;
import cern.c2mon.web.ui.service.HistoryAlarmService;
import cern.c2mon.web.ui.service.HistoryService;
import cern.c2mon.web.ui.util.FormUtility;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * A controller for the alarm history viewer.
 */
@Controller
public class AlarmHistoryController {

  /**
   * Base URL for the history viewer
   */
  public static final String HISTORY_URL = "/alarmhistoryviewer/";

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
  public static final String HISTORY_FORM_URL = "/alarmhistoryviewer/form";

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

  /**
   * A history service
   */
  @Autowired
  private HistoryAlarmService service;

  /**
   * HistoryController logger
   */
  private static Logger logger = LoggerFactory.getLogger(AlarmHistoryController.class);

  /**
   * @return Redirects to the form
   */
  @RequestMapping(value = HISTORY_URL, method = {RequestMethod.GET})
  public final String viewHistory(final Model model) {
    logger.info(HISTORY_URL);
    return ("redirect:" + HISTORY_FORM_URL);
  }

  /**
   * @param id       the last 100 records of the given alarm id are being shown
   * @param response the html result is written to that HttpServletResponse
   *                 response
   * @return Displays the history of a given alarm id.
   */
  @RequestMapping(value = HISTORY_URL + "{id}", method = {RequestMethod.GET})
  public final String viewHistory(@PathVariable(value = "id") final String id,
                                  @RequestParam(value = MAX_RECORDS_PARAMETER, required = false) final String
                                      maxRecords,
                                  @RequestParam(value = LAST_DAYS_PARAMETER, required = false) final String lastDays,
                                  @RequestParam(value = START_DATE_PARAMETER, required = false) final String startTime,
                                  @RequestParam(value = END_DATE_PARAMETER, required = false) final String endTime,
                                  final HttpServletResponse response, final Model model) throws IOException {
    logger.info("/alarmhistoryviewer/{id} " + id);

    List<Alarm> history = new ArrayList<>();
    String description = null;

    try {
      if (startTime != null && endTime != null) {
        history = service.requestHistoryData(id, HistoryService.stringToTimestamp(startTime), HistoryService
            .stringToTimestamp(endTime));
        description = " (From " + startTime + " to " + endTime + ")";
      } else if (lastDays != null) {
        history = service.requestAlarmHistoryDataForLastDays(id, Integer.parseInt(lastDays));
        description = "(Last " + lastDays + " days)";
      } else if (maxRecords != null) {
        history = service.requestHistoryData(id, Integer.parseInt(maxRecords));
        description = "(Last " + maxRecords + " records)";
      } else if (id != null) {
        history = service.requestHistoryData(id, HISTORY_RECORDS_TO_ASK_FOR);
        description = "(Last " + HISTORY_RECORDS_TO_ASK_FOR + " records)";
      }
    } catch (Exception e) {
      return ("redirect:" + HISTORY_FORM_URL + "?error=" + id);
    }

    model.addAttribute("description", description);
    model.addAttribute("history", history);
    model.addAttribute("title", HISTORY_FORM_TITLE);
    return "alarmHistory";
  }

  /**
   * @param id    alarm id
   * @param model Spring MVC Model instance to be filled in before
   *              jsp processes it
   * @return Displays an input form for a alarm id, and if a POST was made with
   * a tag id, it redirects to HISTORY_URL + id + LAST_RECORDS_URL + records
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
    logger.info(HISTORY_URL + "form" + id);

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
    return "trend/alarmTrendViewForm";
  }
}
