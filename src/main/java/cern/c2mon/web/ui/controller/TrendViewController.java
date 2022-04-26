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

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.exception.HistoryProviderException;
import cern.c2mon.client.ext.history.common.exception.LoadingParameterException;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.web.ui.service.HistoryService;
import cern.c2mon.web.ui.service.TagIdException;
import cern.c2mon.web.ui.service.TagService;
import cern.c2mon.web.ui.util.FormUtility;
import cern.c2mon.web.ui.util.InvalidPoint;
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

import java.io.IOException;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * A controller for the Online Trend Viewer
 *
 */
@Controller
public class TrendViewController {

  /** Base URL for the viewer */
  public static final String TREND_VIEW_URL = "/trendviewer/";

  /** Date format used to pass a Date as a parameter */
  private static final String DATE_FORMAT = "dd/MM/yyyy-HH:mm";

  /** URL to define Last Records */
  public static final String LAST_RECORDS_URL = "/records/";

  /** Last Days */
  public static final String LAST_DAYS_PARAMETER = "DAYS";

  /** RECORDS */
  public static final String MAX_RECORDS_PARAMETER = "RECORDS";

  /** Start Date */
  public static final String START_DATE_PARAMETER = "START";

  /** End Date */
  public static final String END_DATE_PARAMETER = "END";

  /** A URL to the viewer with input form */
  public static final String TREND_VIEW_FORM_URL = "/trendviewer/form";

  /** Title for the form page */
  public static final String TREND_FORM_TITLE = "Trend Viewer";

  /** Instruction for the form page */
  public static final String INSTRUCTION = "Enter a Tag Id to create a Trend View.";

  /** How many records in history to ask for (default): 100 looks ok! */
  private static final int RECORDS_TO_ASK_FOR = 100;

  /**
   * Link to a custom help page. If the URL contains the placeholder "{id}" then
   * it will be replaced with the tag id.
   */
  @Value("${c2mon.web.help.url:}")
  public String helpUrl;

  /** HistoryService */
  @Autowired
  private HistoryService historyService;

  /** TagService */
  @Autowired
  private TagService tagService;

  /** TrendViewController logger */
  private static Logger logger = LoggerFactory.getLogger(TrendViewController.class);

  /**
   * @return Model with the default values added (values that are the same for
   *         all chart types).
   *
   * @param tag Used to retrieve the values
   * @param model Values are added to this model
   */
  public final Model getDefaultModel(final Model model, final Tag tag) {

    model.addAttribute("ylabel", tag.getUnit());
    model.addAttribute("tagName", tag.getName());
    model.addAttribute("legend", tag.getName());
    model.addAttribute("unit", tag.getUnit());
    model.addAttribute("id", tag.getId());
    model.addAttribute("fill_graph", true);
    List<AlarmValue> alarmValueList = (List<AlarmValue>) tag.getAlarms();
    if (alarmValueList != null && !alarmValueList.isEmpty()) {
      model.addAttribute("help_url", helpUrl.replaceAll("\\{id\\}", alarmValueList.get(0).getId().toString()));
    }
    model.addAttribute("labels", new String[] { "Server Timestamp", "[" + tag.getId() + "] " });
    model.addAttribute("title", TREND_FORM_TITLE);

    return model;
  }

  /**
   * @return Displays a Trend View for a given id.
   *
   * @param id the last records of the given tag id are being shown
   * @param lastRecords number of records to be shown
   */
  @RequestMapping(value = TREND_VIEW_URL + "{id}" + LAST_RECORDS_URL + "{lastRecords}", method = { RequestMethod.GET })
  public final String viewTrendLastRecords(@PathVariable(value = "id") final String id, @PathVariable(value = "lastRecords") final int lastRecords, Model model)
          throws IOException {

    logger.info(TREND_VIEW_URL + "{id} " + id + LAST_RECORDS_URL + "{lastRecords} ");

    try {
      final List<HistoryTagValueUpdate> historyValues = historyService.requestHistoryData(id, lastRecords);

      final boolean isBooleanData = historyService.isBooleanData(historyValues);
      final Collection<InvalidPoint> invalidPoints = historyService.getInvalidPoints(historyValues);
      final Tag tagValue = tagService.getTag(Long.parseLong(id));

      model = getDefaultModel(model, tagValue);

      model.addAttribute("CSV", historyService.getHistoryCSV(historyValues, isBooleanData));
      model.addAttribute("invalidPoints", invalidPoints);
      model.addAttribute("is_boolean", ((Boolean) (isBooleanData)));
      model.addAttribute("records", lastRecords);
      model.addAttribute("view_title", tagValue.getName());
      model.addAttribute("view_description", "(Last " + lastRecords + " records)");

      return "trend/trendView";

    } catch (Exception e) {
      logger.error(e.getMessage());
    }
    return null;
  }

  /**
   *
   * @return Model containing values needed to create a chart of type: Last days
   *
   * @param id The chart is created for that id.
   * @param days How many days to go back in history.
   * @throws TagIdException In case the specified TagId does not exist.
   */
  public final Model getLastDaysModel(Model model, final String id, final int days) throws HistoryProviderException, LoadingParameterException, TagIdException {

    final List<HistoryTagValueUpdate> historyValues = historyService.requestHistoryDataForLastDays(id, days);

    final boolean isBooleanData = historyService.isBooleanData(historyValues);
    final Collection<InvalidPoint> invalidPoints = historyService.getInvalidPoints(historyValues);
    final Tag tagValue = tagService.getTag(Long.parseLong(id));
    final String historyCSV = historyService.getHistoryCSV(historyValues, isBooleanData);

    if (tagValue == null) {
      logger.debug("TagId: " + id + " not found!");
      throw new TagIdException("TagId: " + id + " not found!");
    }

    model = getDefaultModel(model, tagValue);

    model.addAttribute("CSV", historyCSV);
    model.addAttribute("invalidPoints", invalidPoints);
    model.addAttribute("is_boolean", ((Boolean) (isBooleanData)));
    model.addAttribute("view_title", tagValue.getName());
    model.addAttribute("view_description", "(Last " + days + " days)");
    model.addAttribute("queryParameters", LAST_DAYS_PARAMETER + "=" + days);

    return model;
  }

  /**
   *
   * @return Model containing values needed to create a chart of type: Last
   *         Records
   *
   * @param id The chart is created for that id.
   * @param records How many records to go back in history.
   * @throws TagIdException In case the specified TagId does not exist.
   */
  public final Model getLastRecordsModel(Model model, final String id, final int records) throws HistoryProviderException, LoadingParameterException,
          TagIdException {

    final List<HistoryTagValueUpdate> historyValues = historyService.requestHistoryData(id, records);

    final boolean isBooleanData = historyService.isBooleanData(historyValues);
    final Collection<InvalidPoint> invalidPoints = historyService.getInvalidPoints(historyValues);
    final Tag tagValue = tagService.getTag(Long.parseLong(id));
    final String historyCSV = historyService.getHistoryCSV(historyValues, isBooleanData);

    if (tagValue == null) {
      logger.debug("TagId: " + id + " not found!");
      throw new TagIdException("TagId: " + id + " not found!");
    }

    model = getDefaultModel(model, tagValue);

    model.addAttribute("CSV", historyCSV);
    model.addAttribute("invalidPoints", invalidPoints);
    model.addAttribute("is_boolean", ((Boolean) (isBooleanData)));
    model.addAttribute("records", records);
    model.addAttribute("view_title", tagValue.getName());
    model.addAttribute("view_description", "(Last " + records + " records)");
    model.addAttribute("queryParameters", MAX_RECORDS_PARAMETER + "=" + records);

    return model;
  }

  /**
   * @return Model containing values needed to create a chart of type: StartDate
   *         -> EndDate
   *
   * @param id The chart is created for that id.
   *
   * @param start (Optional parameter) If given, this will be the Start Date of
   *          the history query. {@link TrendViewController#DATE_FORMAT}
   *
   * @param end (Optional parameter) If given, this will be the End Date of the
   *          history query. {@link TrendViewController#DATE_FORMAT}
   *
   * @throws ParseException In case StartDate or EndDate do not follow the
   *           expected format
   */
  public final Model getStartEndDateModel(Model model, final String id, final String start, final String end) throws HistoryProviderException,
          LoadingParameterException, ParseException {

    final List<HistoryTagValueUpdate> historyValues = historyService.requestHistoryData(id, stringToTimestamp(start), stringToTimestamp(end));

    final boolean isBooleanData = historyService.isBooleanData(historyValues);
    final Collection<InvalidPoint> invalidPoints = historyService.getInvalidPoints(historyValues);
    final Tag tagValue = tagService.getTag(Long.parseLong(id));
    final String historyCSV = historyService.getHistoryCSV(historyValues, isBooleanData);

    model = getDefaultModel(model, tagValue);

    model.addAttribute("CSV", historyCSV);
    model.addAttribute("invalidPoints", invalidPoints);
    model.addAttribute("is_boolean", ((Boolean) (isBooleanData)));
    model.addAttribute("view_title", tagValue.getName());
    model.addAttribute("view_description", " (From " + start + " to " + end + ")");
    model.addAttribute("queryParameters", START_DATE_PARAMETER + "=" + start + "&" + END_DATE_PARAMETER + "=" + end);

    return model;
  }

  /**
   * @return a Trend View for a given id.
   *
   *         Handles 2 cases:
   *
   *         1) Same case as viewTrendLastRecords, except RECORDS is fixed to
   *         100. 2) StartDate -> EndDate
   *
   * @param id The tag whose chart is shown
   *
   * @param start (Optional parameter) If given, this will be the Start Date of
   *          the history query. {@link TrendViewController#DATE_FORMAT}
   *
   * @param end (Optional parameter) If given, this will be the End Date of the
   *          history query. {@link TrendViewController#DATE_FORMAT}
   *
   */
  @RequestMapping(value = TREND_VIEW_URL + "{id}", method = { RequestMethod.GET })
  public final String viewTrend(@PathVariable(value = "id") final String id,
                                @RequestParam(value = MAX_RECORDS_PARAMETER, required = false) final String maxRecords,
                                @RequestParam(value = LAST_DAYS_PARAMETER, required = false) final String lastDays,
                                @RequestParam(value = START_DATE_PARAMETER, required = false) final String start,
                                @RequestParam(value = END_DATE_PARAMETER, required = false) final String end, Model model) {

    try {
      if (start != null && end != null) {
        logger.info(TREND_VIEW_URL + "{id} " + id + "start:" + start + "end:" + end);
        model = getStartEndDateModel(model, id, start, end);
      } else if (maxRecords != null) {
        logger.info(TREND_VIEW_URL + "{id} " + id + "maxRecords:" + maxRecords);
        model = getLastRecordsModel(model, id, Integer.parseInt(maxRecords));
      } else if (lastDays != null) {
        logger.info(TREND_VIEW_URL + "{id} " + id + "lastDays:" + lastDays);
        model = getLastDaysModel(model, id, Integer.parseInt(lastDays));
      } else {
        logger.info(TREND_VIEW_URL + "{id} " + id);
        model = getLastRecordsModel(model, id, RECORDS_TO_ASK_FOR);
      }
      return "trend/trendView";
    } catch (final TagIdException e) {
      return ("redirect:" + TREND_VIEW_FORM_URL + "?error=" + id);
    } catch (Exception e) {
      logger.error("Error: ", e);
    }
    return ("redirect:" + TREND_VIEW_FORM_URL + "?error=" + id);
  }

  /**
   * @return Displays an input form for a tag id. After the submit button is
   *         pressed, it redirects to the Trend.
   *
   * @param id tag id, If null then we just display an empty form. Otherwise, we
   *          redirect to corresponding controller based on which parameters
   *          were given (records, days, or Start-End date).
   *
   * @param records (Optional parameter) how many records to retrieve
   *
   * @param days (Optional parameter) how many days to go back in history
   *
   * @param startDate (Optional parameter) If given, this will be the Start Date
   *          of the history query. {@link TrendViewController#DATE_FORMAT}
   *
   * @param endDate (Optional parameter) If given, this will be the End Date of
   *          the history query. {@link TrendViewController#DATE_FORMAT}
   *
   * @param wrongId In case an error occurred while submitting the form. (TagId
   *          that does not exist is the most common case).
   *
   * @param model Spring MVC Model instance to be filled in before jsp processes
   *          it
   */
  @RequestMapping(value = TREND_VIEW_FORM_URL, method = { RequestMethod.GET, RequestMethod.POST })
  public final String viewTrendFormPost(@RequestParam(value = "id", required = false) final String id,
                                        @RequestParam(value = "error", required = false) final String wrongId, @RequestParam(value = "records", required = false) final String records,
                                        @RequestParam(value = "days", required = false) final String days, @RequestParam(value = "start", required = false) final String startDate,
                                        @RequestParam(value = "end", required = false) final String endDate, @RequestParam(value = "startTime", required = false) final String startTime,
                                        @RequestParam(value = "endTime", required = false) final String endTime, final Model model) {

    logger.info(TREND_VIEW_FORM_URL + id);
    if (id == null) {
      model.addAllAttributes(FormUtility.getFormModel(TREND_FORM_TITLE, INSTRUCTION, TREND_VIEW_FORM_URL, null, null, null));

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

      // Displays an empty form ->
      return "trend/trendViewForm";
    }

    else if (days != null) {
      return ("redirect:" + TREND_VIEW_URL + id + "?" + LAST_DAYS_PARAMETER + "=" + days);
    } else if (startDate != null) {
      return ("redirect:" + TREND_VIEW_URL + id + "?" + START_DATE_PARAMETER + "=" + startDate + "-" + startTime + "&" + END_DATE_PARAMETER + "=" + endDate
              + "-" + endTime);
    } else if (records != null) {
      return ("redirect:" + TREND_VIEW_URL + id + "?" + MAX_RECORDS_PARAMETER + "=" + records);
    }

    // Displays an empty form ->
    return "trend/trendViewForm";
  }

  /**
   * @return Converts a string to Timestamp
   *
   * @param dateString should represent a Date in the following format:
   *          {@link TrendViewController#DATE_FORMAT}
   *
   * @throws ParseException in case of wrong Date Format
   */
  private static Timestamp stringToTimestamp(final String dateString) throws ParseException {

    DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    dateFormat.setLenient(false);
    java.util.Date date = dateFormat.parse(dateString);
    final long time = date.getTime();
    return new Timestamp(time);
  }

  @RequestMapping(value = TREND_VIEW_URL, method = { RequestMethod.GET })
  public final String viewTrend(final Model model) {
    logger.info(TREND_VIEW_URL);
    return ("redirect:" + TREND_VIEW_FORM_URL);
  }
}
