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
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.shared.client.alarm.AlarmValue;
import cern.c2mon.web.ui.service.TagService;
import cern.c2mon.web.ui.util.FormUtility;

/**
 * A controller for the datatag viewer
 */
@Controller
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

  /**
   * DataTagController logger
   */
  private static Logger logger = LoggerFactory.getLogger(DataTagController.class);


  /**
   * @return Redirects to the form
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/", method = { RequestMethod.GET })
  public String viewTag(final Model model) {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/");
    return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/form");
  }

  /**
   * @return
   * Displays (TagConfig + TagValue) information for a tag with the specified id.
   *
   * @param id tag id
   * @param response we write the html result to that HttpServletResponse response
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/{id}", method = { RequestMethod.GET })
  public String viewTag(@PathVariable(value = "id") final String id, final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException  {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/{id} " + id);

    Tag tag = service.getTag(new Long(id));
    if (tag == null) {
      return ("redirect:" + "/" + TAGVIEWER_PAGE_NAME + "/errorform/" + id);
    }

    model.addAttribute("title", TAG_FORM_TITLE);
    model.addAttribute("tag", tag);
    model.addAttribute("tagConfig", service.getTagConfig(new Long(id)));
    List<AlarmValue> alarmValueList = (List<AlarmValue>) tag.getAlarms();
    model.addAttribute("help_url", helpUrl.replaceAll("\\{id\\}", alarmValueList.get(0).getId().toString()));
    return "datatag";
  }

  /**
   * @return
   * In case of an error this form is shown.
   * It displays the error and you can also make a new query.
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/errorform/{id}", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewTagErrorForm(@PathVariable(value = "id") final String errorId,
      @RequestParam(value = "id", required = false) final String id, final Model model) {

    logger.info("/" + TAGVIEWER_PAGE_NAME + "/errorform " + id);

    if (id == null) {
      model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE,
          TAG_FORM_INSTR, TAG_FORM_URL, null, null));
    }
    else {
      return ("redirect:" + TAG_URL + id);
    }

    model.addAttribute("err", errorId);
    return "genericErrorForm";
  }

  /**
   * Displays a form where a datatag id can be entered.
   * @param id datatag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/form/{id}", method = { RequestMethod.GET })
  public String viewTagWithForm(@PathVariable final String id, final Model model) {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/form/{id} " + id);
    model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, id, TAG_URL + id));
    return "genericForm";
  }

  /**
   * Displays an input form for a datatag id, and if a POST was made with a datatag id, also the datatag information.
   * @param id datatag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   * @return name of a jsp page which will be displayed
   */
  @RequestMapping(value = "/" + TAGVIEWER_PAGE_NAME + "/form", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewTagFormPost(@RequestParam(value = "id", required = false) final String id, final Model model) {
    logger.info("/" + TAGVIEWER_PAGE_NAME + "/form " + id);
    if (id == null) {
      model.addAllAttributes(FormUtility.getFormModel(TAG_FORM_TITLE, TAG_FORM_INSTR, TAG_FORM_URL, null, null));
    }
     else {
       return ("redirect:" + TAG_URL + TAGVIEWER_PAGE_NAME + "/" + id);
     }

    return "genericForm";
  }
}
