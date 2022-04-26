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

import cern.c2mon.client.common.tag.CommandTag;
import cern.c2mon.client.ext.history.command.CommandTagRecord;
import cern.c2mon.web.ui.service.CommandService;
import cern.c2mon.web.ui.service.CommandTagService;
import cern.c2mon.web.ui.service.TagIdException;
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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;


/**
 * A controller for the command viewer
 */
@Controller
public class CommandController {

  private static final String COMMAND_VIEWER_PAGE_NAME = "commandviewer";

  /**
   * A REST-style URL to commandviewer, combined with command id displays command information
   */
  private static final String COMMAND_TAG_URL = "../";
  //public static final String COMMAND_URL = "/commandviewer/";

  /**
   * URL to commandviewer, that displays command information in RAW XML
   */
  public static final String COMMAND_XML_URL = "/" + COMMAND_VIEWER_PAGE_NAME + "/xml";

  /**
   * A URL to the commandviewer with input form
   */
  public static final String COMMAND_FORM_URL = "/" + COMMAND_VIEWER_PAGE_NAME + "/form";

  /**
   * Title for the command form page
   */
  public static final String COMMAND_FORM_TITLE = "Command Configuration Viewer";

  /**
   * Placeholder for the command form page
   */
  public static final String COMMAND_FORM_PLACEHOLDER = "Command Tag";

  /**
   * Description for the command form page
   */
  public static final String COMMAND_FORM_INSTR = "Enter a command id to view the command's configuration.";

  /**
   * A command service
   */
  @Autowired
  private CommandService service;

  @Autowired
  private CommandTagService commandTagService;

  /**
   * CommandController logger
   */
  private static Logger logger = LoggerFactory.getLogger(CommandController.class);

  /**
   * @return Redirects to the form
   */
  @RequestMapping(value = "/" + COMMAND_VIEWER_PAGE_NAME + "/", method = { RequestMethod.GET })
  public String viewCommand(final Model model) {
    return ("redirect:" + "/" + COMMAND_VIEWER_PAGE_NAME + "/form");
  }

  /**
   * @return
   * Displays command information in RAW XML about a tag with the given id.
   *
   * @param id command id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/" + COMMAND_VIEWER_PAGE_NAME + "/xml" + "/id/{id}", method = { RequestMethod.GET })
  public String viewXml(@PathVariable final String id,  final Model model) {
    try {
      model.addAttribute("xml", service.getCommandTagXml(id));
    } catch (TagIdException e) {
      return ("redirect:" + "/" + COMMAND_VIEWER_PAGE_NAME + "/errorform/id/" + id);
    }
    return "raw/xml";
  }

  /**
   * @return
   * Displays command information for a given command id.
   *
   * @param id command id
   * @param response we write the html result to that HttpServletResponse response
   */
  @RequestMapping(value = "/" + COMMAND_VIEWER_PAGE_NAME + "/id/{id}", method = { RequestMethod.GET })
  public String viewCommand(@PathVariable(value = "id") final String id, final HttpServletResponse response, final Model model) throws IOException  {

    CommandTag<?> tag = service.getCommandTag(Long.valueOf(id));
    if (tag == null) {
      return ("redirect:" + "/" + COMMAND_VIEWER_PAGE_NAME + "/errorform/id/" + id);
    }

    model.addAttribute("title", COMMAND_FORM_TITLE);
    model.addAttribute("tag", tag);
    return "commandtag";
  }

  @RequestMapping(value = "/" + COMMAND_VIEWER_PAGE_NAME + "/name/{name}", method = RequestMethod.GET)
  public String viewCommandByName(@PathVariable(value = "name") final String name, final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException {
    List<CommandTagRecord> tags = commandTagService.getCommandTagByName(name);
    if (tags.isEmpty()) {
      return ("redirect:" + "/" + COMMAND_VIEWER_PAGE_NAME + "/errorform/name/" + name);
    }

    if(tags.size() == 1){
      CommandTagRecord commandTag = tags.get(0);

      CommandTag<?> tag = service.getCommandTag(commandTag.getId());
      if (tag == null) {
        //shouldn't happen but it's to prevent any errors
        return ("redirect:" + "/" + COMMAND_VIEWER_PAGE_NAME + "/errorform/name/" + name);
      }

      model.addAttribute("title", COMMAND_FORM_TITLE);
      model.addAttribute("tag", tag);

      return "commandtag";
    }

    model.addAttribute("title", COMMAND_FORM_TITLE);
    model.addAttribute("formPlaceHolder", COMMAND_FORM_PLACEHOLDER);
    model.addAttribute("formUrl", COMMAND_VIEWER_PAGE_NAME);
    model.addAttribute("entries", tags);

    return "searchform/genericsearchlist";
  }

  /**
   * @return
   * In case of an error this form is shown.
   * It displays the error and you can also make a new query.
   * New queries are redirected to COMMAND_URL + id
   *
   * @param id tag id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/" + COMMAND_VIEWER_PAGE_NAME + "/errorform/id/{id}", method = {RequestMethod.GET, RequestMethod.POST})
  public String viewCommandErrorForm(@PathVariable(value = "id") final String errorId,
                                     @RequestParam(value = "id", required = false) final String id,
                                     @RequestParam(value = "name", required = false) final String name,
                                     final Model model) {

    String errorMessage = "Command Tag with id " + errorId + " could not be found !";
    model.addAttribute("err", errorMessage);

    if (id != null && !id.isEmpty()) {
      return ("redirect:/" + COMMAND_VIEWER_PAGE_NAME + "/id/" + id);

    } else if (name != null && !name.isEmpty()) {
      return ("redirect:/" + COMMAND_VIEWER_PAGE_NAME + "/name/" + name);

    } else {
      model.addAllAttributes(FormUtility.getFormModel(COMMAND_FORM_TITLE, COMMAND_FORM_URL, null, COMMAND_FORM_PLACEHOLDER, null));

    }
    return "form/genericErrorForm";
  }

  @RequestMapping(value = "/" + COMMAND_VIEWER_PAGE_NAME + "/errorform/name/{name}", method = {RequestMethod.GET, RequestMethod.POST})
  public String viewCommandNameErrorForm(@PathVariable(value = "name") final String errorName,
                                         @RequestParam(value = "id", required = false) final String id,
                                         @RequestParam(value = "name", required = false) final String name,
                                         final Model model) {

    String errorMessage = "Command Tag with name " + errorName + " could not be found !";
    model.addAttribute("err", errorMessage);

    if (id != null && !id.isEmpty()) {
      return ("redirect:/" + COMMAND_VIEWER_PAGE_NAME + "/id/" + id);

    } else if (name != null && !name.isEmpty()) {
      return ("redirect:/" + COMMAND_VIEWER_PAGE_NAME + "/name/" + name);

    } else {
      model.addAllAttributes(FormUtility.getFormModel(COMMAND_FORM_TITLE, COMMAND_FORM_URL, null, COMMAND_FORM_PLACEHOLDER, null));

    }
    return "form/genericErrorForm";
  }

  /**
   * @return
   * Displays a form where an command id can be entered.
   *
   * @param id command id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/commandviewer/form/id/{id}", method = { RequestMethod.GET })
  public String viewCommandWithForm(@PathVariable(value = "id") final String id, final Model model) {
    model.addAllAttributes(FormUtility.getFormModel(COMMAND_FORM_TITLE, COMMAND_FORM_INSTR,
            COMMAND_FORM_URL, id, COMMAND_TAG_URL + id));
    return "form/genericForm";
  }

  @RequestMapping(value = "/commandviewer/form/name/{name}", method = { RequestMethod.GET })
  public String viewCommandNameWithForm(@PathVariable(value = "name") final String name, final Model model) {
    model.addAllAttributes(FormUtility.getFormModel(COMMAND_FORM_TITLE, COMMAND_FORM_INSTR,
            COMMAND_FORM_URL, name, COMMAND_TAG_URL + name));
    return "form/genericForm";
  }

  /**
   * @return name of a jsp page which will be displayed
   * Displays an input form for a command id, and if a POST was made with a command id,
   * redirects to COMMAND_URL + id.
   *
   * @param id command id
   * @param model Spring MVC Model instance to be filled in before jsp processes it
   */
  @RequestMapping(value = "/" + COMMAND_VIEWER_PAGE_NAME + "/form", method = {RequestMethod.GET, RequestMethod.POST})
  public String viewTagFormPost(@RequestParam(value = "id", required = false) final String id,
                                @RequestParam(value = "name", required = false) final String name,
                                final Model model) {
    if (id != null && !id.isEmpty()) {
      return ("redirect:" + COMMAND_TAG_URL + COMMAND_VIEWER_PAGE_NAME + "/id/" + id);

    } else if (name != null && !name.isEmpty()) {
      return ("redirect:" + COMMAND_TAG_URL + COMMAND_VIEWER_PAGE_NAME + "/name/" + name);

    } else {
      model.addAllAttributes(FormUtility.getFormModel(COMMAND_FORM_TITLE, COMMAND_FORM_INSTR, COMMAND_FORM_URL, null, COMMAND_FORM_PLACEHOLDER, null));
    }
    return "form/genericForm";
  }
}
