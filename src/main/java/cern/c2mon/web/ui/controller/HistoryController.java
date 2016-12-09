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

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.servlet.http.HttpServletResponse;

import cern.c2mon.client.common.tag.ClientDataTagValue;
import cern.c2mon.client.ext.history.alarm.Alarm;
import cern.c2mon.client.ext.history.alarm.AlarmHistoryService;
import cern.c2mon.client.ext.history.alarm.HistoricAlarmQuery;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.shared.client.alarm.AlarmValueImpl;
import cern.c2mon.web.ui.service.TagService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.exception.HistoryProviderException;
import cern.c2mon.client.ext.history.common.exception.LoadingParameterException;
import cern.c2mon.web.ui.service.HistoryService;
import cern.c2mon.web.ui.util.FormUtility;

/**
 * A controller for the history viewer.
 *
 * Creates Table views. Check {@link TrendViewController} for the trend views.
 */
@Controller
public class HistoryController {

  /**
   * Base URL for the history viewer
   */
  public static final String HISTORY_URL = "/historyviewer/";

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
  public static final String HISTORY_FORM_URL = "/historyviewer/form";

  /**
   * The URL to view the history of a tag in RAW XML format
   */
  public static final String HISTORY_XML_URL = HISTORY_URL + "xml";

  /**
   * The URL to view the history of a tag in CSV format
   */
  public static final String HISTORY_CSV_URL = HISTORY_URL + "csv";

  /**
   * Title for the history form page
   */
  public static final String HISTORY_FORM_TITLE = "History Viewer (table)";

  /**
   * Instruction for the history form page
   */
  public static final String HISTORY_FORM_INSTR = "Enter a Tag Id to create a Table View.";

  /** How many records in history to ask for. 100 looks ok! */
  private static final int HISTORY_RECORDS_TO_ASK_FOR = 100;

  /**
   * Link to a custom help page. If the URL contains the placeholder "{id}" then
   * it will be replaced with the tag id.
   */
  @Value("${c2mon.web.trend.viewer.help.url:}")
  public String helpUrl;

  /**
   * A history service
   */
  @Autowired
  private HistoryService service;

  @Autowired
  private TagService tagService;

  @Autowired
  private AlarmHistoryService alarmService;

  /**
   * HistoryController logger
   */
  private static Logger logger = LoggerFactory.getLogger(HistoryController.class);

  /**
   * @return Redirects to the form
   */
  @RequestMapping(value = HISTORY_URL, method = { RequestMethod.GET })
  public final String viewHistory(final Model model) {
    logger.info(HISTORY_URL);
    return ("redirect:" + HISTORY_FORM_URL);
  }

  /**
   * @return Displays the history of a given id.
   *
   * @param id the last 100 records of the given tag id are being shown
   * @param response the html result is written to that HttpServletResponse
   *          response
   */
  @RequestMapping(value = HISTORY_URL + "{id}", method = { RequestMethod.GET })
  public final String viewHistory(@PathVariable(value = "id") final String id,
      @RequestParam(value = MAX_RECORDS_PARAMETER, required = false) final String maxRecords,
      @RequestParam(value = LAST_DAYS_PARAMETER, required = false) final String lastDays,
      @RequestParam(value = START_DATE_PARAMETER, required = false) final String startTime,
      @RequestParam(value = END_DATE_PARAMETER, required = false) final String endTime, final HttpServletResponse response, final Model model) throws IOException {

    logger.info("/historyviewer/{id} " + id);

    List<HistoryTagValueUpdate> history = new ArrayList<>();
    String description = null;

    try {
      if (startTime != null && endTime != null) {
        history = service.requestHistoryData(id, HistoryService.stringToTimestamp(startTime), HistoryService.stringToTimestamp(endTime));
        description = " (From " + startTime + " to " + endTime + ")";
      } else if (lastDays != null) {
        history = service.requestHistoryDataForLastDays(id, Integer.parseInt(lastDays));
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

    setAlarmsForHistory(Long.parseLong(id), history);

    model.addAttribute("description", description);
    model.addAttribute("history", history);
    model.addAttribute("title", HISTORY_FORM_TITLE);
    model.addAttribute("help_url", helpUrl.replaceAll("\\{id\\}", id));
    return "history";
  }

  /**
   * @return Displays the History in RAW XML for a tag with the given id.
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   */
  @RequestMapping(value = HISTORY_XML_URL + "/{id}", method = { RequestMethod.GET })
  public final String viewXml(@PathVariable final String id, @RequestParam(value = MAX_RECORDS_PARAMETER, required = false) final String maxRecords,
      @RequestParam(value = LAST_DAYS_PARAMETER, required = false) final String lastDays,
      @RequestParam(value = START_DATE_PARAMETER, required = false) final String startTime,
      @RequestParam(value = END_DATE_PARAMETER, required = false) final String endTime, final Model model) throws ParseException {

    logger.info(HISTORY_XML_URL + id);
    try {

      if (id == null) {
        model.addAttribute("xml", service.getHistoryXml(id, HISTORY_RECORDS_TO_ASK_FOR));
      } else if (lastDays != null) {
        final String xml = service.getHistoryXmlForLastDays(id, Integer.parseInt(lastDays));
        model.addAttribute("xml", xml);
      }
      if (startTime != null && endTime != null) {
        final String xml = service.getHistoryXml(id, startTime, endTime);
        model.addAttribute("xml", xml);
      } else if (maxRecords != null) {
        model.addAttribute("xml", service.getHistoryXml(id, Integer.parseInt(maxRecords)));
      }

    } catch (HistoryProviderException e) {
      logger.error(e.getMessage());
      return ("redirect:" + "/historyviewer/errorform/" + id);
    } catch (LoadingParameterException e) {
      logger.error(e.getMessage());
      return ("redirect:" + "/historyviewer/errorform/" + id);
    }
    return "raw/xml";
  }

  /**
   * @return Displays an input form for a tag id, and if a POST was made with a
   *         tag id, it redirects to HISTORY_URL + id + LAST_RECORDS_URL +
   *         records
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   */
  @RequestMapping(value = HISTORY_URL + "form", method = { RequestMethod.GET, RequestMethod.POST })
  public final String viewHistoryFormPost(@RequestParam(value = "id", required = false) final String id,
      @RequestParam(value = "error", required = false) final String wrongId, @RequestParam(value = "records", required = false) final String records,
      @RequestParam(value = "days", required = false) final String days, @RequestParam(value = "start", required = false) final String startDate,
      @RequestParam(value = "end", required = false) final String endDate, @RequestParam(value = "startTime", required = false) final String startTime,
      @RequestParam(value = "endTime", required = false) final String endTime, final Model model) {

    logger.info(HISTORY_URL + "form" + id);

    if (id == null) {
      model.addAllAttributes(FormUtility.getFormModel(HISTORY_FORM_TITLE, HISTORY_FORM_INSTR, HISTORY_FORM_URL, null, null));

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
      return ("redirect:" + HISTORY_URL + id + "?" + START_DATE_PARAMETER + "=" + startDate + "-" + startTime + "&" + END_DATE_PARAMETER + "=" + endDate + "-" + endTime);
    } else if (records != null) {
      return ("redirect:" + HISTORY_URL + id + "?" + MAX_RECORDS_PARAMETER + "=" + records);
    }
    return "trend/trendViewForm";
  }


  /**
   * Helper method to populate the alarms to a given tag history from the {@link AlarmHistoryService}.
   *
   * @param tagId The id of the tag which might hold the history of alarms
   * @param tagValueUpdates The history of the tag
   */
  private void setAlarmsForHistory(Long tagId, List<HistoryTagValueUpdate> tagValueUpdates){
    Map<Timestamp, Alarm> timeToAlarmValue = new HashMap<>();
    ClientDataTagValue clientTag = tagService.getDataTagValue(tagId);

    if(clientTag.getAlarms() !=  null) {
      List<AlarmValue> alarms = new ArrayList<>(clientTag.getAlarms());

      if (!alarms.isEmpty() && !tagValueUpdates.isEmpty()) {
        // get the range of the history of the sorted list
        Timestamp start = tagValueUpdates.get(0).getServerTimestamp();
        Timestamp end = tagValueUpdates.get(tagValueUpdates.size() - 1).getServerTimestamp();
        for(Alarm alarm : alarmService.findBy(new HistoricAlarmQuery().id(alarms.get(0).getId()).between(start, end))){
          timeToAlarmValue.put(alarm.getTimestamp(), alarm);
        }
      }
      for(HistoryTagValueUpdate tagValueUpdate : tagValueUpdates){
        HistoryTagValueUpdateImpl tagValue = (HistoryTagValueUpdateImpl) tagValueUpdate;
        Timestamp currentTime = tagValueUpdate.getServerTimestamp();

        if(timeToAlarmValue.containsKey(currentTime)){
          Alarm alarm = timeToAlarmValue.get(currentTime);
          AlarmValueImpl alarmValue = new AlarmValueImpl(
              alarm.getId(),
              alarm.getFaultCode(),
              alarm.getFaultMember(),
              alarm.getFaultFamily(),
              alarm.getInfo(),
              alarm.getTagId(),
              alarm.getTimestamp(),
              alarm.isActive());
          tagValue.getAlarms().add(alarmValue);
        }
      }
    }
  }
}
