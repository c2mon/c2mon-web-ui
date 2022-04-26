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

import java.sql.Date;
import java.sql.Timestamp;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import cern.c2mon.client.ext.history.HistoryManager;
import cern.c2mon.client.ext.history.common.HistoryLoadingConfiguration;
import cern.c2mon.client.ext.history.common.HistoryLoadingManager;
import cern.c2mon.client.ext.history.common.HistoryProvider;
import cern.c2mon.client.ext.history.common.HistoryTagValueUpdate;
import cern.c2mon.client.ext.history.common.exception.HistoryProviderException;
import cern.c2mon.client.ext.history.common.exception.LoadingParameterException;
import cern.c2mon.client.ext.history.updates.HistoryTagValueUpdateImpl;
import cern.c2mon.web.ui.controller.TrendViewController;
import cern.c2mon.web.ui.util.InvalidPoint;


/**
 * HistoryService providing the XML representation for the history of a given tag.
 */
@Service
public class HistoryService {

  /**
   * HistoryService logger
   */
  private static Logger logger = LoggerFactory.getLogger(HistoryService.class);

  /** Date format used in our trend views */
  private static final String CHART_DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

  /** App base url */
  private static final String BASE_URL = "../";

  /** Date format used in History Queries */
  private static final String DATE_FORMAT = "dd/MM/yyyy-HH:mm";

  @Autowired
  private HistoryManager historyManager;

  /**
   * @return XML representation of Tag's History
   *
   * @param dataTagId id of the datatag
   * @param numberOfRecords number of records to look back in the history
   *
   * @throws HistoryProviderException in case a HistoryProvider cannot be created
   * @throws LoadingParameterException in case of an invalid configurations
   *
   * @throws Exception if tag was not found or a non-numeric id was requested
   *  ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   */
  public final String getHistoryXml(final String dataTagId, final int numberOfRecords)
          throws HistoryProviderException, LoadingParameterException  {

    final List<HistoryTagValueUpdate> historyValues = requestHistoryData(dataTagId, numberOfRecords);
    final String description = "(Last " + numberOfRecords + " records)";
    final String trendURL = "?" + TrendViewController.MAX_RECORDS_PARAMETER + "=" + numberOfRecords;
    return toXml(historyValues, dataTagId, description, trendURL);
  }


  /**
   * @return XML representation of Tag's History
   *
   * @param dataTagId id of the datatag
   * @param numberOfDays number of days to go back in History
   *
   * @throws HistoryProviderException in case a HistoryProvider cannot be created
   * @throws LoadingParameterException in case of an invalid configurations
   * @throws Exception if tag was not found or a non-numeric id was requested
   *  ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   */
  public final String getHistoryXmlForLastDays(final String dataTagId, final int numberOfDays)
          throws HistoryProviderException, LoadingParameterException  {

    final List<HistoryTagValueUpdate> historyValues = requestHistoryDataForLastDays(dataTagId,
            numberOfDays);
    final String description = "(Last " + numberOfDays + " days)";
    final String trendURL = "?" + TrendViewController.LAST_DAYS_PARAMETER + "=" + numberOfDays;
    return toXml(historyValues, dataTagId, description, trendURL);
  }


  /**
   * @return XML representation of Tag's History
   *
   * @param dataTagId id of the datatag
   *
   * @param startTime
   * @param endTime StartTime -> EndTime = Period of History to return
   *
   * @throws HistoryProviderException in case a HistoryProvider cannot be created
   * @throws LoadingParameterException in case of an invalid configurations
   * @throws Exception if tag was not found or a non-numeric id was requested
   *  ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   */
  public final String getHistoryXml(final String dataTagId,
                                    final Timestamp startTime,
                                    final Timestamp endTime)
          throws HistoryProviderException, LoadingParameterException  {

    final List<HistoryTagValueUpdate> historyValues = requestHistoryData(dataTagId, startTime, endTime);

    final String description = " (From " + startTime + " to " + endTime + ")";
    final String trendURL = "?" + TrendViewController.START_DATE_PARAMETER + "=" + startTime
            + "&amp;" + TrendViewController.END_DATE_PARAMETER + "=" + endTime;
    return toXml(historyValues, dataTagId, description, trendURL);
  }


  /**
   * @return XML representation of Tag's History
   *
   * Same as {@link #getHistoryXml(String, Timestamp, Timestamp)}
   * but startTime and endTime are given as strings (should follow {@link #DATE_FORMAT})
   *
   */
  public final String getHistoryXml(final String dataTagId,
                                    final String startTime,
                                    final String endTime)
          throws HistoryProviderException, LoadingParameterException, ParseException  {

    final List<HistoryTagValueUpdate> historyValues =
            requestHistoryData(dataTagId,
                    stringToTimestamp(startTime),
                    stringToTimestamp(endTime));

    final String description = " (From " + startTime + " to " + endTime + ")";
    final String trendURL = "?" + TrendViewController.START_DATE_PARAMETER + "=" + startTime
            + "&amp;" + TrendViewController.END_DATE_PARAMETER + "=" + endTime;
    return toXml(historyValues, dataTagId, description, trendURL);
  }


  /**
   * @return CSV representation of Tag's History
   *
   *  ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   */
  public final String getHistoryCSV(final List<HistoryTagValueUpdate> historyValues,
                                    final boolean isBooleanData) {

    return toCSV(historyValues, isBooleanData);
  }

  /**
   * @return CSV representation of Tag's History
   *
   * @param dataTagId id of the datatag
   * @param numberOfRecords number of records to look back in the history
   *
   * @throws HistoryProviderException in case a HistoryProvider cannot be created
   * @throws LoadingParameterException in case of an invalid configurations
   * @throws Exception if tag was not found or a non-numeric id was requested
   *  ({@link TagIdException}), or any other exception
   * thrown by the underlying service gateway.
   */
  public final String getHistoryCSV(final String dataTagId, final int numberOfRecords)
          throws HistoryProviderException, LoadingParameterException  {

    final List<HistoryTagValueUpdate> historyValues = requestHistoryData(dataTagId, numberOfRecords);
    final boolean isBooleanData = isBooleanData(historyValues);
    return toCSV(historyValues, isBooleanData);
  }

  public final String getHistoryCSVText(final String dataTagId, final int numberOfRecords)
          throws HistoryProviderException, LoadingParameterException  {

    final List<HistoryTagValueUpdate> historyValues = requestHistoryData(dataTagId, numberOfRecords);
    final boolean isBooleanData = isBooleanData(historyValues);
    return toCSVText(historyValues, isBooleanData);
  }

  public final String getHistoryCSVTextForLastDays(final String dataTagId, final int numberOfDays)
          throws HistoryProviderException, LoadingParameterException  {
    final List<HistoryTagValueUpdate> historyValues = requestHistoryDataForLastDays(dataTagId,
            numberOfDays);
    final boolean isBooleanData = isBooleanData(historyValues);
    return toCSVText(historyValues, isBooleanData);
  }

  public final String getHistoryCSVText(final String dataTagId,
                                        final String startTime,
                                        final String endTime)
          throws HistoryProviderException, LoadingParameterException, ParseException  {

    final List<HistoryTagValueUpdate> historyValues =
            requestHistoryData(dataTagId,
                    stringToTimestamp(startTime),
                    stringToTimestamp(endTime));
    final boolean isBooleanData = isBooleanData(historyValues);
    return toCSVText(historyValues, isBooleanData);
  }

  /**
   * Used to make a request for HistoryData.
   * @param dataTagId The tag id whose history we are looking for
   * @param numberOfRecords number of records to retrieve from history
   *
   * @throws HistoryProviderException in case a HistoryProvider cannot be created
   * @throws LoadingParameterException in case of an invalid configurations
   * @return history as a List of HistoryTagValueUpdates
   */
  public final List<HistoryTagValueUpdate> requestHistoryData(final String dataTagId,
                                                              final int numberOfRecords)
          throws HistoryProviderException, LoadingParameterException {

    // other values that can be used //
    Integer numberOfDays;
    Date startDate;
    Date endDate;
    Timestamp startTime = null;
    Timestamp endTime = null;
    // other values that can be used //


    final long id = Long.parseLong(dataTagId);
    Collection<Long> dataTagIds = new ArrayList<>();
    dataTagIds.add(id);
    final HistoryLoadingManager loadingManager = historyManager
            .createHistoryLoadingManager(getHistoryProvider(), dataTagIds);

    final HistoryLoadingConfiguration configuration = new HistoryLoadingConfiguration();
    configuration.setLoadInitialValues(true);
    configuration.setMaximumRecords(numberOfRecords);

    loadingManager.setConfiguration(configuration);
    try {
      loadingManager.beginLoading(false);
    }
    catch (LoadingParameterException e) {
      logger.error("The configurations is invalid.", e);
      throw new LoadingParameterException("The configuration is invalid", e);
    }

    final List<HistoryTagValueUpdate> historyValues = new ArrayList<>();
    for (final Long tagId : dataTagIds) {
      historyValues.addAll(loadingManager.getAllHistoryConverted(tagId));
    }
    return historyValues;
  }

  /**
   * Used to make a request for HistoryData.
   * @param dataTagId The tag id whose history we are looking for
   *
   * @throws HistoryProviderException in case a HistoryProvider cannot be created
   * @throws LoadingParameterException in case of an invalid configurations
   * @return history as a List of HistoryTagValueUpdates
   */
  public final List<HistoryTagValueUpdate> requestHistoryData(final String dataTagId
          , final Timestamp startTime
          , final Timestamp endTime)
          throws HistoryProviderException, LoadingParameterException {

    final long id = Long.parseLong(dataTagId);
    Collection<Long> dataTagIds = new ArrayList<>();
    dataTagIds.add(id);
    final HistoryLoadingManager loadingManager = historyManager
            .createHistoryLoadingManager(getHistoryProvider(), dataTagIds);

    final HistoryLoadingConfiguration configuration = new HistoryLoadingConfiguration();
    configuration.setLoadInitialValues(true);
    configuration.setStartTime(startTime);
    configuration.setEndTime(endTime);

    loadingManager.setConfiguration(configuration);
    try {
      loadingManager.beginLoading(false);
    }
    catch (LoadingParameterException e) {
      logger.error("The configurations is invalid.", e);
      throw new LoadingParameterException("The configuration is invalid", e);
    }

    final List<HistoryTagValueUpdate> historyValues = new ArrayList<>();
    for (final Long tagId : dataTagIds) {
      historyValues.addAll(loadingManager.getAllHistoryConverted(tagId));
    }
    return historyValues;
  }

  /**
   * Used to make a request for HistoryData.
   *
   * @param dataTagId The tag id whose history we are looking for
   * @param numberOfDays number of days to go back in History
   *
   * @throws HistoryProviderException in case a HistoryProvider cannot be created
   * @throws LoadingParameterException in case of an invalid configurations
   * @return history as a List of HistoryTagValueUpdates
   */
  public final List<HistoryTagValueUpdate> requestHistoryDataForLastDays(final String dataTagId,
                                                                         final int numberOfDays)
          throws HistoryProviderException, LoadingParameterException {

    final long id = Long.parseLong(dataTagId);
    Collection<Long> dataTagIds = new ArrayList<>();
    dataTagIds.add(id);
    final HistoryLoadingManager loadingManager = historyManager
            .createHistoryLoadingManager(getHistoryProvider(), dataTagIds);

    final HistoryLoadingConfiguration configuration = new HistoryLoadingConfiguration();
    configuration.setLoadInitialValues(true);
    configuration.setNumberOfDays(numberOfDays);

    loadingManager.setConfiguration(configuration);
    try {
      loadingManager.beginLoading(false);
    }
    catch (LoadingParameterException e) {
      logger.error("The configurations is invalid.", e);
      throw new LoadingParameterException("The configuration is invalid", e);
    }

    final List<HistoryTagValueUpdate> historyValues = new ArrayList<>();
    for (final Long tagId : dataTagIds) {
      historyValues.addAll(loadingManager.getAllHistoryConverted(tagId));
    }
    return historyValues;
  }

  /**
   * @return History values of the specified TagId in XML format.
   *
   * @param id The TagId whose history we are XML-ing
   *
   * @param historyDescription Description for the history values
   * (for example: Last 100 records)
   *
   * @param trendURL a trend view showing the same historyValues
   */
  public final String toXml(final List<HistoryTagValueUpdate> historyValues, final String id
          , final String historyDescription, final String trendURL) {

    // example: <history id="15685" historyDescription="Last 100 records" >
    StringBuffer historyXml = new StringBuffer();
    historyXml.append("<history");
    historyXml.append(" id=\"" + id + "\"");
    historyXml.append(" historyDescription=\"" + historyDescription + "\"");
    historyXml.append(" trendURL=\"" + trendURL + "\"");
    historyXml.append(" >");
    for (HistoryTagValueUpdate h : historyValues) {
      HistoryTagValueUpdateImpl q = (HistoryTagValueUpdateImpl) h;
      historyXml.append(q.getXml());
    }
    historyXml.append("</history>");
    return historyXml.toString();
  }

  /**
   * @return True if the list of history values represent Boolean Data, false otherwise.
   * This is useful when creating a Trend View, so that the data is represented in the best way.
   */
  public final boolean isBooleanData(final List<HistoryTagValueUpdate> historyValues) {

    final int size = historyValues.size();
    if (size > 0) {
      final HistoryTagValueUpdate h = historyValues.iterator().next();
      final String type = h.getValueClassName();
      return type.contains("Boolean");
    }
    return false;
  }

  /**
   * @return History values of the specified TagId in CSV format.
   *
   * @see http://dygraphs.com/data.html#csv
   */
  public final String toCSV(final List<HistoryTagValueUpdate> historyValues, final boolean isBooleanData) {

    StringBuffer historyCSV = new StringBuffer();
    final int size = historyValues.size();
    for (int i = 0; i < size; i++) {

      final HistoryTagValueUpdate h = historyValues.get(i);
      HistoryTagValueUpdateImpl q = (HistoryTagValueUpdateImpl) h;
      double value;

      historyCSV.append("\"");

      try {
        if (isBooleanData) {
          value = getChartCompatibleBooleanValue(q.getValue());
        } else {
          value = (new Double(q.getValue().toString())).doubleValue();
        }
      } catch (final Exception e) {
        value = 0;
      }

      historyCSV.append(
              formatToDygraphCompatibleDate(q.getServerTimestamp())
                      + "," + value + "," + q.getValueDescription());

      historyCSV.append("," + q.getDataTagQuality().getDescription().replace('\n', ' '));

      historyCSV.append("\\n\"");

      if (i != size - 1) {
        historyCSV.append(" + ");
      }
      historyCSV.append("\n");
    }
    return historyCSV.toString();
  }

  public final String toCSVText(final List<HistoryTagValueUpdate> historyValues, final boolean isBooleanData) {

    StringBuffer historyCSV = new StringBuffer();

    String header = "Server Timestamp,Tag Value,Value Description,Data Tag Quality Description\n";
    historyCSV.append(header);

    final int size = historyValues.size();
    for (int i = 0; i < size; i++) {

      final HistoryTagValueUpdate h = historyValues.get(i);
      HistoryTagValueUpdateImpl q = (HistoryTagValueUpdateImpl) h;
      double value;

      try {
        if (isBooleanData) {
          value = getChartCompatibleBooleanValue(q.getValue());
        } else {
          value = (new Double(q.getValue().toString())).doubleValue();
        }
      } catch (final Exception e) {
        value = 0;
      }

      historyCSV.append(
              formatToDygraphCompatibleDate(q.getServerTimestamp())
                      + "," + value + "," + q.getValueDescription());

      historyCSV.append("," + q.getDataTagQuality().getDescription().replace('\n', ' '));

      historyCSV.append("\n");
    }
    return historyCSV.toString();
  }


  /**
   * @return A Date formated to be compatible with the Dygraph chart library.
   * @see http://dygraphs.com/data.html#csv
   *
   * For example: 2009/07/12 12:34:56
   *
   */
  private static String formatToDygraphCompatibleDate(final Timestamp timestamp) {

    // 2009/07/12 12:34:56
    SimpleDateFormat dateFormat = new SimpleDateFormat(CHART_DATE_FORMAT);
    dateFormat.setLenient(false);
    String outputDate = null;
    outputDate = dateFormat.format(timestamp);
    return outputDate;
  }

  /**
   * @return Scans the specified list of history points and returns a list
   * of the invalid ones.
   *
   * @param historyValues A list of history points.
   */
  public Collection<InvalidPoint> getInvalidPoints(final List<HistoryTagValueUpdate> historyValues) {

    final Collection<InvalidPoint> invalidPoints = new ArrayList<>();

    final Iterator<HistoryTagValueUpdate> i = historyValues.iterator();

    while (i.hasNext()) {
      final HistoryTagValueUpdate h = i.next();
      if (!h.getDataTagQuality().isValid()) {

        final String time = formatToDygraphCompatibleDate(h.getServerTimestamp());
        final String invalidationReason = h.getDataTagQuality().getDescription();

        invalidPoints.add(new InvalidPoint(time, invalidationReason));
      }
    }
    return invalidPoints;
  }


  /**
   * @return Boolean values should be replaced with 0 or 1
   * to be represented in the chart.
   *
   * @param booleanValue The boolean value (as String).
   */
  private double getChartCompatibleBooleanValue(final Object booleanValue) {

    if (booleanValue.toString().equals("true")) {
      return 1;
    }
    else {
      return 0;
    }
  }

  /**
   * @return Converts a string to Timestamp
   *
   * @param dateString
   * should represent a Date in the following format: {@link TrendViewController#DATE_FORMAT}
   *
   * @throws ParseException in case of wrong Date Format
   */
  public static Timestamp stringToTimestamp(final String dateString) throws ParseException {

    DateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    dateFormat.setLenient(false);
    java.util.Date date = dateFormat.parse(dateString);
    final long time = date.getTime();
    return new Timestamp(time);
  }

  /**
   * @return Converts a string to Timestamp
   *
   * @param dateString
   * should represent a Date in the following format: {@link TrendViewController#DATE_FORMAT}
   *
   * @throws ParseException in case of wrong Date Format
   */
  public static LocalDateTime stringToLocalDateTime(final String dateString) throws ParseException {
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATE_FORMAT);
    return LocalDateTime.parse(dateString, formatter);
  }

  private HistoryProvider getHistoryProvider() throws HistoryProviderException {
    HistoryProvider provider = null;

    try {
      provider = historyManager.getHistoryProviderFactory().createHistoryProvider();
    }
    catch (HistoryProviderException e) {
      logger.error("Can't load any history because a HistoryProvider cannot be created.", e);
      throw new HistoryProviderException("Cannot retrieve the data from the Short term log " +
              "because no history provider is accessible.");
    }

    return provider;
  }
}
