/******************************************************************************
 * Copyright (C) 2010-2020 CERN. All rights not expressly granted are reserved.
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
import cern.c2mon.client.ext.history.alarm.AlarmRecord;
import cern.c2mon.client.ext.history.data.DataTagRecord;
import cern.c2mon.web.ui.service.AlarmSearchService;
import cern.c2mon.web.ui.service.DataTagService;
import cern.c2mon.web.ui.service.TagService;
import cern.c2mon.web.ui.util.FormUtility;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

/**
 * A controller for the datatag viewer
 */
@Controller
@Slf4j
public class DataTagController {


  private static final String TAGVIEWER_PAGE_NAME = "tagviewer";

  /**
   * A REST-style URL to tagviewer, combined with tag id displays datatag configuration
   */
  private static final String TAG_URL = "../";

  /**
   * A URL to the tagviewer with input form
   */
  public static final String TAG_FORM_URL = "/" + TAGVIEWER_PAGE_NAME + "/form";

  /**
   * Title for the datatag form page
   */
  public static final String TAG_FORM_TITLE = "Tag Viewer";

  /**
   * Placeholder for the datatag form page
   */
  public static final String TAG_FORM_PLACEHOLDER = "Data Tag";

  /**
   * Description for the datatag form page
   */
  public static final String TAG_FORM_INSTR = "Enter a tag id to view the tag's configuration.";

  /**
   * Link to a custom help page. If the URL contains the placeholder "{id}" then
   * it will be replaced with the tag id.
   */
  @Value("${c2mon.web.help.url:}")
  public String helpUrl;

  /**
   * A datatag service
   */
  @Autowired
  private TagService service;

  @Autowired
  private DataTagService dataTagService;

  @Autowired
  private AlarmSearchService alarmService;

  /**
   * @return Redirects to the form
   */
  @GetMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/")
  public String viewTag(final Model model) {
    log.info("/" + TAGVIEWER_PAGE_NAME + "/");
    return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/form");
  }

  /**
   * @param id       tag id
   * @param response we write the html result to that HttpServletResponse response
   * @return Displays (TagConfig + TagValue) information for a tag with the specified id.
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/id/{id}", method = RequestMethod.GET)
  public String viewTag(@PathVariable("id") final String id, final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException {
    log.info("/" + TAGVIEWER_PAGE_NAME + "/{id} " + id);

    DataTagRecord tagrecord = dataTagService.getDataTagById(Long.valueOf(id));
    if (tagrecord == null) {
      return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/errorform/id/" + id);
    }

    Tag tag = service.getTag(Long.valueOf(id));

    model.addAttribute("title", TAG_FORM_TITLE);
    model.addAttribute("tag", tag);
    model.addAttribute("tagConfig", service.getTagConfig(Long.valueOf(id)));

    List<AlarmRecord> alarmValueList = alarmService.findAlarmByTagId(tag.getId());
    if (!alarmValueList.isEmpty()) {
      model.addAttribute("help_url", helpUrl.replaceAll("\\{id\\}", alarmValueList.get(0).getId().toString()));
    }
    return "datatag";
  }

  /**
   * @param name     tag name
   * @param response we write the html result to that HttpServletResponse response
   * @return Displays (TagConfig + TagValue) information for a tag with the specified name.
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/name/{name}", method = RequestMethod.GET)
  public String viewTagByName(@PathVariable(value = "name") final String name, final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException {
    log.info("/" + TAGVIEWER_PAGE_NAME + "/{name} " + name);

    List<DataTagRecord> tags = dataTagService.getDataTagByName(name);

    if (tags.isEmpty()) {
      return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/errorform/name/" + name);
    }


    if(tags.size() == 1){
      DataTagRecord dataTag = tags.get(0);

      Tag tag = service.getTag(Long.valueOf(dataTag.getId()));
      if (tag == null) {
        //shouldn't happen but it's to prevent any errors
        return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/errorform/name/" + name);
      }

      model.addAttribute("title", TAG_FORM_TITLE);
      model.addAttribute("tag", tag);
      model.addAttribute("tagConfig", service.getTagConfig(Long.valueOf(tag.getId())));

      List<AlarmRecord> alarmValueList = alarmService.findAlarmByTagId(tag.getId());
      if (!alarmValueList.isEmpty()) {
        //TODO why just one ??
        model.addAttribute("help_url", helpUrl.replaceAll("\\{id\\}", alarmValueList.get(0).getId().toString()));
      }
      return "datatag";
    }

    model.addAttribute("title", TAG_FORM_TITLE);
    model.addAttribute("formPlaceHolder", TAG_FORM_PLACEHOLDER);
    model.addAttribute("formUrl", TAGVIEWER_PAGE_NAME);
    model.addAttribute("entries", tags);

    return "searchform/genericsearchlist";
  }

  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/errorform/id/{id}", method = {RequestMethod.GET, RequestMethod.POST})
  public String viewTagErrorForm(@PathVariable(value = "id") final String errorId,
                                 @RequestParam(value = "id", required = false) final String id,
                                 @RequestParam(value = "name", required = false) final String name,
                                 final Model model) {

    log.info("/" + TAGVIEWER_PAGE_NAME + "/errorform ");

    String errorMessage = "Tag with id " + errorId + " could not be found !";
    model.addAttribute("err", errorMessage);

    if (id != null && !id.isEmpty()) {
      return ("redirect:/" + TAGVIEWER_PAGE_NAME + "/id/" + id);

    } else if (name != null && !name.isEmpty()) {
      return ("redirect:/" + TAGVIEWER_PAGE_NAME + "/name/" + name);

    } else {
      model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_URL, null, TAG_FORM_PLACEHOLDER, null));

    }
    return "form/genericErrorForm";
  }

  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/errorform/name/{name}", method = {RequestMethod.GET, RequestMethod.POST})
  public String viewTagNameErrorForm(@PathVariable(value = "name") final String errorName,
                                     @RequestParam(value = "id", required = false) final String id,
                                     @RequestParam(value = "name", required = false) final String name,
                                     final Model model) {

    log.info("/" + TAGVIEWER_PAGE_NAME + "/errorform ");

    String errorMessage = "Tag with name " + errorName + " could not be found !";
    model.addAttribute("err", errorMessage);

    if (id != null && !id.isEmpty()) {
      return ("redirect:/" + TAGVIEWER_PAGE_NAME + "/id/" + id);

    } else if (name != null && !name.isEmpty()) {
      return ("redirect:/" + TAGVIEWER_PAGE_NAME + "/name/" + name);

    } else {
      model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_URL, null, TAG_FORM_PLACEHOLDER, null));

    }
    return "form/genericErrorForm";
  }

  /**
   * Displays a form where a datatag id can be entered.
   *
   * @param id    datatag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   */
  @GetMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/form/id/{id}")
  public String viewTagWithForm(@PathVariable(value = "id") final String id, final Model model) {
    log.info("/" + TAGVIEWER_PAGE_NAME + "/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, id, TAG_URL + id));
    return "form/genericForm";
  }

  @GetMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/form/name/{name}")
  public String viewTagNameWithForm(@PathVariable(value = "name") final String name, final Model model) {
    log.info("/" + TAGVIEWER_PAGE_NAME + "/form/{id} " + name);
    model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, name, TAG_URL + name));
    return "form/genericForm";
  }

  /**
   * Displays an input form for a datatag id, and if a POST was made with a datatag id, also the datatag information.
   *
   * @param id    datatag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/form", method = {RequestMethod.GET, RequestMethod.POST})
  public String viewTagFormPost(@RequestParam(value = "id", required = false) final String id,
                                @RequestParam(value = "name", required = false) final String name,
                                final Model model) {
    log.info("/" + TAGVIEWER_PAGE_NAME + "/form " + id);

    if (id != null && !id.isEmpty()) {
      return ("redirect:" + TAG_URL + TAGVIEWER_PAGE_NAME + "/id/" + id);

    } else if (name != null && !name.isEmpty()) {
      return ("redirect:" + TAG_URL + TAGVIEWER_PAGE_NAME + "/name/" + name);

    } else {
      model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, null, TAG_FORM_PLACEHOLDER, null));
    }
    return "form/genericForm";
  }
}
