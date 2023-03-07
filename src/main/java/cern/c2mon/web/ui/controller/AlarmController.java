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

import cern.c2mon.client.ext.history.alarm.AlarmRecord;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.web.ui.service.AlarmSearchService;
import cern.c2mon.web.ui.service.TagIdException;
import cern.c2mon.web.ui.service.WebAlarmService;
import cern.c2mon.web.ui.util.FormUtility;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * A controller for the alarm viewer
 **/
@Controller
public class AlarmController {

  /**
   * A REST-style URL to alarmviewer, combined with alarm id displays alarm information
   */
  public static final String ALARM_URL = "/alarmviewer/";

  /**
   * A REST-style URL to alarmviewer, combined with alarm id displays alarm information
   * in RAW XML
   */
  public static final String ALARM_XML_URL = "/alarmviewer/xml";

  /**
   * A URL to the alarmviewer with input form
   */
  public static final String ALARM_FORM_URL = "/alarmviewer/form";

  /**
   * Title for the alarm form page
   */
  public static final String ALARM_FORM_TITLE = "Alarm Viewer";

  /**
   * Placeholder for the alarm form page
   */
  public static final String ALARM_FORM_PLACEHOLDER = "Alarm";

  /**
   * Description for the alarm form page
   */
  public static final String ALARM_FORM_INSTR = "Enter an alarm id.";

  /**
   * Link to a custom help page. If the URL contains the placeholder "{id}" then
   * it will be replaced with the tag id.
   */
  @Value("${c2mon.web.help.url:}")
  public String helpUrl;

  /**
   * An alarm service
   */
  @Autowired
  private WebAlarmService service;

  @Autowired
  private AlarmSearchService alarmService;

  /**
   * AlarmController logger
   */
  private static Logger logger = LoggerFactory.getLogger(AlarmController.class);

  /**
   * @return Redirects to the form
   */
  @RequestMapping(value = ALARM_URL, method = {RequestMethod.GET})
  public String viewAlarm(final Model model) {
    logger.info("/alarmviewer/");
    return ("redirect:" + "/alarmviewer/form");
  }

  /**
   * @param id    alarm id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return Displays alarm information in RAW XML about an alarm with the given id.
   */
  @RequestMapping(value = ALARM_XML_URL + "/{id}", method = {RequestMethod.GET})
  public String viewXml(@PathVariable final String id, final Model model) {
    logger.info(ALARM_XML_URL + id);
    try {
      model.addAttribute("xml", service.getAlarmTagXml(id));
    } catch (TagIdException e) {
      return ("redirect:" + "/alarmviewer/errorform/" + id);
    }
    return "raw/xml";
  }

  /**
   * @param id       alarm id
   * @param response we write the html result to that HttpServletResponse response
   * @return Displays alarm information for a given alarm id.
   */
  @RequestMapping(value = ALARM_URL + "id/{id}", method = {RequestMethod.GET})
  public String viewAlarm(@PathVariable(value = "id") final String id, final HttpServletResponse response, final Model model, final HttpServletRequest request)
          throws IOException {

    AlarmValue alarm = service.getAlarmValue(Long.parseLong(id));
    if (alarm == null) {
      return ("redirect:" + "/alarmviewer/errorform/id/" + id);
    }

    model.addAttribute("alarm", alarm);
    model.addAttribute("title", ALARM_FORM_TITLE);
    if (alarm.getId() != null) {
      model.addAttribute("help_url", helpUrl.replaceAll("\\{id\\}", alarm.getId().toString()));
    }
    return "alarm";
  }

  @RequestMapping(value = ALARM_URL + "faultFamily/{faultFamily}", method = RequestMethod.GET)
  public String searchAlarmByFaultFamily(@PathVariable(value = "faultFamily") final String faultFamily,
                                         final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException {

    List<AlarmRecord> alarms = alarmService.findAlarmByFaultFamily(faultFamily);

    if (alarms.isEmpty()) {
      return ("redirect:" + "/alarmviewer/errorform/faultFamily/" + faultFamily);
    }

    return showAlarmSearchResult(alarms, model);
  }

  @RequestMapping(value = ALARM_URL + "faultMember/{faultMember}", method = RequestMethod.GET)
  public String searchAlarmByFaultMember(@PathVariable(value = "faultMember") final String faultMember,
                                         final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException {

    List<AlarmRecord> alarms = alarmService.findAlarmByFaultMember(faultMember);

    if (alarms.isEmpty()) {
      return ("redirect:" + "/alarmviewer/errorform/faultMember/" + faultMember);
    }

    return showAlarmSearchResult(alarms, model);
  }

  @RequestMapping(value = ALARM_URL + "faultCode/{faultCode}", method = RequestMethod.GET)
  public String searchAlarmByFaultCode(@PathVariable(value = "faultCode") final String faultCode,
                                       final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException {

    List<AlarmRecord> alarms = alarmService.findAlarmByFaultCode(Integer.parseInt(faultCode));

    if (alarms.isEmpty()) {
      return ("redirect:" + "/alarmviewer/errorform/faultCode/" + faultCode);
    }

    return showAlarmSearchResult(alarms, model);
  }

  @RequestMapping(value = ALARM_URL + "faultFamily/{faultFamily}/faultMember/{faultMember}", method = RequestMethod.GET)
  public String searchAlarmByFaultFamilyAndFaultMember(@PathVariable(value = "faultFamily") final String faultFamily,
                                                       @PathVariable(value = "faultMember") final String faultMember,
                                                       final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException {

    List<AlarmRecord> alarms = alarmService.findAlarmByFaultFamilyAndFaultMember(faultFamily, faultMember);

    if (alarms.isEmpty()) {
      return ("redirect:" + "/alarmviewer/errorform/faultFamily/" + faultFamily + "/faultMember/" + faultMember);
    }

    return showAlarmSearchResult(alarms, model);
  }

  @RequestMapping(value = ALARM_URL + "faultFamily/{faultFamily}/faultCode/{faultCode}", method = RequestMethod.GET)
  public String searchAlarmByFaultFamilyAndFaultCode(@PathVariable(value = "faultFamily") final String faultFamily,
                                                     @PathVariable(value = "faultCode") final String faultCode,
                                                     final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException {

    List<AlarmRecord> alarms = alarmService.findAlarmByFaultFamilyAndFaultCode(faultFamily, Integer.parseInt(faultCode));

    if (alarms.isEmpty()) {
      return ("redirect:" + "/alarmviewer/errorform/faultFamily/" + faultFamily + "/faultCode/" + faultCode);
    }

    return showAlarmSearchResult(alarms, model);
  }

  @RequestMapping(value = ALARM_URL + "faultMember/{faultMember}/faultCode/{faultCode}", method = RequestMethod.GET)
  public String searchAlarmByFaultMemberAndFaultCode(@PathVariable(value = "faultMember") final String faultMember,
                                                     @PathVariable(value = "faultCode") final String faultCode,
                                                     final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException {

    List<AlarmRecord> alarms = alarmService.findAlarmByFaultMemberAndFaultCode(faultMember, Integer.parseInt(faultCode));

    if (alarms.isEmpty()) {
      return ("redirect:" + "/alarmviewer/errorform/faultMember/" + faultMember + "/faultCode/" + faultCode);
    }

    return showAlarmSearchResult(alarms, model);
  }

  @RequestMapping(value = ALARM_URL + "faultFamily/{faultFamily}/faultMember/{faultMember}/faultCode/{faultCode}", method = RequestMethod.GET)
  public String searchAlarmByFaultFamilyAndFaultMemberAndFaultCode(@PathVariable(value = "faultFamily") final String faultFamily,
                                                                   @PathVariable(value = "faultMember") final String faultMember,
                                                                   @PathVariable(value = "faultCode") final String faultCode,
                                                                   final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException {

    List<AlarmRecord> alarms = alarmService.findAlarmByFaultFamilyAndFaultMemberAndFaultCode(faultFamily, faultMember, Integer.parseInt(faultCode));

    if (alarms.isEmpty()) {
      return ("redirect:" + "/alarmviewer/errorform/faultFamily/" + faultFamily + "/faultMember/" + faultMember + "/faultCode/" + faultCode);
    }

    return showAlarmSearchResult(alarms, model);
  }

  private String showAlarmSearchResult(List<AlarmRecord> alarms, Model model){

    if(alarms.size() == 1){
      AlarmRecord alarmRecord = alarms.get(0);

      AlarmValue alarm = service.getAlarmValue(alarmRecord.getId());
      if (alarm == null) {
        return ("redirect:" + "/alarmviewer/errorform/id/" + alarmRecord.getId());
      }

      model.addAttribute("alarm", alarm);
      model.addAttribute("title", ALARM_FORM_TITLE);
      if (alarm.getId() != null) {
        model.addAttribute("help_url", helpUrl.replaceAll("\\{id\\}", alarm.getId().toString()));
      }
      return "alarm";
    }

    model.addAttribute("title", ALARM_FORM_TITLE);
    model.addAttribute("alarms", alarms);

    return "searchform/alarmsearchlist";
  }

  /**
   * @param id    alarm id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return Displays a form where an alarm id can be entered.
   */
  @RequestMapping(value = "/alarmviewer/form/{id}", method = {RequestMethod.GET})
  public String viewAlarmWithForm(@PathVariable final String id, final Model model) {
    logger.info("/alarmviewer/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(ALARM_FORM_TITLE, ALARM_FORM_INSTR, ALARM_FORM_URL, id, ALARM_URL + id));
    return "form/genericForm";
  }

  /**
   * @param id    tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return In case of an error this form is shown.
   * It displays the error and you can also make a new query.
   * <p>
   * New queries are simply redirected to ALARM_URL + id
   */
  @RequestMapping(value = "/alarmviewer/errorform/id/{id}")
  public String viewAlarmIdErrorForm(@PathVariable(value = "id") final String errorInfo,
                                     @RequestParam(value = "id", required = false) final String id,
                                     @RequestParam(value = "faultFamily", required = false) final String faultFamily,
                                     @RequestParam(value = "faultMember", required = false) final String faultMember,
                                     @RequestParam(value = "faultCode", required = false) final Integer faultCode,
                                     final Model model) {

    String errorMessage = "Alarm with id " + errorInfo + " could not be found !";
    model.addAttribute("err", errorMessage);

    return handleAlarmForm(id, faultFamily, faultMember, faultCode, model, "form/alarmErrorForm");

  }

  @RequestMapping(value = "/alarmviewer/errorform/faultFamily/{faultFamily}")
  public String viewAlarmFaultFamilyErrorForm(@PathVariable(value = "faultFamily") final String errorInfo,
                                              @RequestParam(value = "id", required = false) final String id,
                                              @RequestParam(value = "faultFamily", required = false) final String faultFamily,
                                              @RequestParam(value = "faultMember", required = false) final String faultMember,
                                              @RequestParam(value = "faultCode", required = false) final Integer faultCode,
                                              final Model model) {
    String errorMessage = "Alarm with fault family " + errorInfo + " could not be found !";
    model.addAttribute("err", errorMessage);

    return handleAlarmForm(id, faultFamily, faultMember, faultCode, model, "form/alarmErrorForm");

  }

  @RequestMapping(value = "/alarmviewer/errorform/faultMember/{faultMember}")
  public String viewAlarmFaultMemberErrorForm(@PathVariable(value = "faultMember") final String errorInfo,
                                              @RequestParam(value = "id", required = false) final String id,
                                              @RequestParam(value = "faultFamily", required = false) final String faultFamily,
                                              @RequestParam(value = "faultMember", required = false) final String faultMember,
                                              @RequestParam(value = "faultCode", required = false) final Integer faultCode,
                                              final Model model) {

    String errorMessage = "Alarm with fault member " + errorInfo + " could not be found !";
    model.addAttribute("err", errorMessage);

    return handleAlarmForm(id, faultFamily, faultMember, faultCode, model, "form/alarmErrorForm");
  }

  @RequestMapping(value = "/alarmviewer/errorform/faultCode/{faultCode}")
  public String viewAlarmFaultCodeErrorForm(@PathVariable(value = "faultCode") final String errorInfo,
                                            @RequestParam(value = "id", required = false) final String id,
                                            @RequestParam(value = "faultFamily", required = false) final String faultFamily,
                                            @RequestParam(value = "faultMember", required = false) final String faultMember,
                                            @RequestParam(value = "faultCode", required = false) final Integer faultCode,
                                            final Model model) {
    String errorMessage = "Alarm with fault code " + errorInfo + " could not be found !";
    model.addAttribute("err", errorMessage);

    return handleAlarmForm(id, faultFamily, faultMember, faultCode, model, "form/alarmErrorForm");
  }

  @RequestMapping(value = "/alarmviewer/errorform/faultFamily/{faultFamily}/faultMember/{faultMember}")
  public String viewAlarmFaultFamilyAndFaultMemberErrorForm(@PathVariable(value = "faultFamily") final String errorInfoFF,
                                                            @PathVariable(value = "faultMember") final String errorInfoFM,
                                                            @RequestParam(value = "id", required = false) final String id,
                                                            @RequestParam(value = "faultFamily", required = false) final String faultFamily,
                                                            @RequestParam(value = "faultMember", required = false) final String faultMember,
                                                            @RequestParam(value = "faultCode", required = false) final Integer faultCode,
                                                            final Model model) {
    String errorMessage = "Alarm with fault family " + errorInfoFF + " and fault member " + errorInfoFM + " could not be found !";
    model.addAttribute("err", errorMessage);

    return handleAlarmForm(id, faultFamily, faultMember, faultCode, model, "form/alarmErrorForm");
  }

  @RequestMapping(value = "/alarmviewer/errorform/faultFamily/{faultFamily}/faultCode/{faultCode}")
  public String viewAlarmFaultFamilyAndFaultCodeErrorForm(@PathVariable(value = "faultFamily") final String errorInfoFF,
                                                          @PathVariable(value = "faultCode") final String errorInfoFC,
                                                          @RequestParam(value = "id", required = false) final String id,
                                                          @RequestParam(value = "faultFamily", required = false) final String faultFamily,
                                                          @RequestParam(value = "faultMember", required = false) final String faultMember,
                                                          @RequestParam(value = "faultCode", required = false) final Integer faultCode,
                                                          final Model model) {
    String errorMessage = "Alarm with fault family " + errorInfoFF + " and fault code " + errorInfoFC + " could not be found !";
    model.addAttribute("err", errorMessage);

    return handleAlarmForm(id, faultFamily, faultMember, faultCode, model, "form/alarmErrorForm");

  }

  @RequestMapping(value = "/alarmviewer/errorform/faultMember/{faultMember}/faultCode/{faultCode}")
  public String viewAlarmFaultMemberAndFaultCodeErrorForm(@PathVariable(value = "faultMember") final String errorInfoFM,
                                                          @PathVariable(value = "faultCode") final String errorInfoFC,
                                                          @RequestParam(value = "id", required = false) final String id,
                                                          @RequestParam(value = "faultFamily", required = false) final String faultFamily,
                                                          @RequestParam(value = "faultMember", required = false) final String faultMember,
                                                          @RequestParam(value = "faultCode", required = false) final Integer faultCode,
                                                          final Model model) {
    String errorMessage = "Alarm with fault member " + errorInfoFM + " and fault code " + errorInfoFC + " could not be found !";
    model.addAttribute("err", errorMessage);

    return handleAlarmForm(id, faultFamily, faultMember, faultCode, model, "form/alarmErrorForm");
  }

  @RequestMapping(value = "/alarmviewer/errorform/faultFamily/{faultFamily}/faultMember/{faultMember}/faultCode/{faultCode}")
  public String viewAlarmFaultFamilyAndFaultMemberAndFaultCodeErrorForm(@PathVariable(value = "faultFamily") final String errorInfoFF,
                                                                        @PathVariable(value = "faultMember") final String errorInfoFM,
                                                                        @PathVariable(value = "faultCode") final String errorInfoFC,
                                                                        @RequestParam(value = "id", required = false) final String id,
                                                                        @RequestParam(value = "faultFamily", required = false) final String faultFamily,
                                                                        @RequestParam(value = "faultMember", required = false) final String faultMember,
                                                                        @RequestParam(value = "faultCode", required = false) final Integer faultCode,
                                                                        final Model model) {
    String errorMessage = "Alarm with fault family " + errorInfoFF + " and fault member " + errorInfoFM + " and fault code " + errorInfoFC + " could not be found !";
    model.addAttribute("err", errorMessage);

    return handleAlarmForm(id, faultFamily, faultMember, faultCode, model, "form/alarmErrorForm");
  }

  /**
   * @param id    alarm id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return Displays an input form for an alarm id,
   * and if a POST was made with an alarm id, it is simply redirected to ALARM_URL + id
   */
  @RequestMapping(value = "/alarmviewer/form", method = {RequestMethod.GET, RequestMethod.POST})
  public String viewAlarmFormPost(@RequestParam(value = "id", required = false) final String id,
                                  @RequestParam(value = "faultFamily", required = false) final String faultFamily,
                                  @RequestParam(value = "faultMember", required = false) final String faultMember,
                                  @RequestParam(value = "faultCode", required = false) final Integer faultCode,
                                  final Model model) {
    logger.info("/alarmviewer/form " + id);

    return handleAlarmForm(id, faultFamily, faultMember, faultCode, model, "form/alarmForm");
  }

  private String handleAlarmForm(String id, String faultFamily, String faultMember, Integer faultCode, Model model, String returnPage){
    if (id != null && !id.isEmpty()) {
      return ("redirect:" + ALARM_URL + "id/" + id);
    } else {
      boolean faultFamilyPresent = false;
      boolean faultMemberPresent = false;
      boolean faultCodePresent = false;

      if (faultFamily != null && !faultFamily.isEmpty()) {
        faultFamilyPresent = true;
      }
      if (faultMember != null && !faultMember.isEmpty()) {
        faultMemberPresent = true;
      }
      if (faultCode != null) {
        faultCodePresent = true;
      }

      if (faultFamilyPresent && faultMemberPresent && faultCodePresent) {
        return ("redirect:" + ALARM_URL + "faultFamily/" + faultFamily + "/faultMember/" + faultMember + "/faultCode/" + faultCode);

      } else if (faultFamilyPresent && faultMemberPresent && (!faultCodePresent)) {
        return ("redirect:" + ALARM_URL + "faultFamily/" + faultFamily + "/faultMember/" + faultMember);

      } else if (faultFamilyPresent && (!faultMemberPresent) && faultCodePresent) {
        return ("redirect:" + ALARM_URL + "faultFamily/" + faultFamily + "/faultCode/" + faultCode);

      } else if (faultFamilyPresent && (!(faultMemberPresent && faultCodePresent))) {
        return ("redirect:" + ALARM_URL + "faultFamily/" + faultFamily);

      } else if ((!faultFamilyPresent) && faultMemberPresent && faultCodePresent) {
        return ("redirect:" + ALARM_URL + "faultMember/" + faultMember + "/faultCode/" + faultCode);

      } else if ((!faultFamilyPresent) && faultMemberPresent && (!faultCodePresent)) {
        return ("redirect:" + ALARM_URL + "faultMember/" + faultMember);

      } else if ((!(faultFamilyPresent && faultMemberPresent)) && faultCodePresent) {
        return ("redirect:" + ALARM_URL + "faultCode/" + faultCode);

      }else{
        model.addAllAttributes(FormUtility.getFormModel(ALARM_FORM_TITLE, ALARM_FORM_INSTR, ALARM_FORM_URL, null, ALARM_FORM_PLACEHOLDER, null));
      }
    }
    return returnPage;
  }
}
