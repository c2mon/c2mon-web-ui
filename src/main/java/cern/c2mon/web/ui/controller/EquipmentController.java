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

import cern.c2mon.client.ext.history.equipment.EquipmentRecord;
import cern.c2mon.client.ext.history.process.Process;
import cern.c2mon.shared.common.datatag.SourceDataTag;
import cern.c2mon.shared.common.process.EquipmentConfiguration;
import cern.c2mon.shared.common.process.ProcessConfiguration;
import cern.c2mon.web.ui.service.EquipmentService;
import cern.c2mon.web.ui.service.ProcessService;
import cern.c2mon.web.ui.util.FormUtility;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * This class acts as a controller to handle requests to view specific
 * equipment.
 *
 * @author Justin Lewis Salmon
 */
@Controller
public class EquipmentController {

  private static final String EQUIPMENTVIEWER_PAGE_NAME = "equipmentviewer";

  /**
   * A REST-style URL to tagviewer, combined with tag id displays datatag configuration
   */
  private static final String  EQUIPMENT_URL = "../";

  /**
   * A URL to the tagviewer with input form
   */
  public static final String EQUIPMENT_FORM_URL = "/" + EQUIPMENTVIEWER_PAGE_NAME + "/form";

  /**
   * Title for the datatag form page
   */
  public static final String EQUIPMENT_FORM_TITLE = "Equipment Viewer";

  /**
   * Placeholder for the datatag form page
   */
  public static final String EQUIPMENT_FORM_PLACEHOLDER = "Equipment";

  /**
   * Description for the datatag form page
   */
  public static final String EQUIPMENT_FORM_INSTR = "Enter a equipment id to view the tag's configuration.";

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
  private EquipmentService equipmentService;

  @Autowired
  private ProcessService processService;

  /**
   * @return Redirects to the form
   */
  @GetMapping(value = "/" + EQUIPMENTVIEWER_PAGE_NAME + "/")
  public String viewEquipment(final Model model) {
    return ("redirect:" + "/" + EQUIPMENTVIEWER_PAGE_NAME + "/form");
  }


  @RequestMapping(value = "/" + EQUIPMENTVIEWER_PAGE_NAME + "/id/{id}", method = RequestMethod.GET)
  public String viewEquipment(@PathVariable("id") final String id, final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException {

    EquipmentRecord equipment = equipmentService.getEquipmentById(Long.valueOf(id));
    if (equipment == null) {
      return ("redirect:" + "/" + EQUIPMENTVIEWER_PAGE_NAME + "/errorform/id/" + id);
    }

    Process process = processService.getProcessById(equipment.getProcessId());

    return ("redirect:/process/" + process.getName() + "/equipment/" + equipment.getId());
  }


  @RequestMapping(value = "/" + EQUIPMENTVIEWER_PAGE_NAME + "/name/{name}", method = RequestMethod.GET)
  public String viewEquipmentByName(@PathVariable(value = "name") final String name, final HttpServletResponse response, final Model model, final HttpServletRequest request) throws IOException  {

    List<EquipmentRecord> equipments = equipmentService.getEquipmentByName(name);

    if(equipments.isEmpty()){
      return ("redirect:" + "/" + EQUIPMENTVIEWER_PAGE_NAME + "/errorform/name/" + name);
    }

    if(equipments.size() == 1){
      return ("redirect:/process/" +  equipments.get(0).getProcessId() + "/equipment/" + equipments.get(0).getId());
    }

    model.addAttribute("title", EQUIPMENT_FORM_TITLE);
    model.addAttribute("formPlaceHolder", EQUIPMENT_FORM_PLACEHOLDER);
    model.addAttribute("formUrl", EQUIPMENTVIEWER_PAGE_NAME);
    model.addAttribute("entries", equipments);
    return "searchform/genericsearchlist";
  }

  @RequestMapping(value = "/" + EQUIPMENTVIEWER_PAGE_NAME + "/errorform/id/{id}", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewEquipmentErrorForm(@PathVariable(value="id") final String errorId,
                                 @RequestParam(value = "id", required = false) final String id,
                                 @RequestParam(value = "name", required = false) final String name,
                                 final Model model) {

    String errorMessage = "Equipment with id " + errorId + " could not be found !";
    model.addAttribute("err", errorMessage);

    if (id != null && !id.isEmpty()) {
      return ("redirect:/" + EQUIPMENTVIEWER_PAGE_NAME + "/id/" + id);

    }else
    if (name != null && !name.isEmpty()) {
      return ("redirect:/" + EQUIPMENTVIEWER_PAGE_NAME + "/name/" + name);

    }else {
      model.addAllAttributes(FormUtility.getFormModel(EQUIPMENT_FORM_TITLE, EQUIPMENT_FORM_INSTR, EQUIPMENT_FORM_URL, null, EQUIPMENT_FORM_PLACEHOLDER,null));

    }
    return "form/genericErrorForm";
  }

  @RequestMapping(value = "/" + EQUIPMENTVIEWER_PAGE_NAME + "/errorform/name/{name}", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewEquipmentNameErrorForm(@PathVariable(value="name") final String errorName,
                                     @RequestParam(value = "id", required = false) final String id,
                                     @RequestParam(value = "name", required = false) final String name,
                                     final Model model) {

    String errorMessage = "No Equipments with name like " + errorName + " could be found !";
    model.addAttribute("err", errorMessage);

    if (id != null && !id.isEmpty()) {
      return ("redirect:/" + EQUIPMENTVIEWER_PAGE_NAME + "/id/" + id);

    }else
    if (name != null && !name.isEmpty()) {
      return ("redirect:/" + EQUIPMENTVIEWER_PAGE_NAME + "/name/" + name);

    }else {
      model.addAllAttributes(FormUtility.getFormModel(EQUIPMENT_FORM_TITLE, EQUIPMENT_FORM_INSTR, EQUIPMENT_FORM_URL, null, EQUIPMENT_FORM_PLACEHOLDER,null));

    }
    return "form/genericErrorForm";
  }

  @GetMapping(value = "/" + EQUIPMENTVIEWER_PAGE_NAME + "/form/id/{id}")
  public String viewEquipmentWithForm(@PathVariable(value="id") final String id, final Model model) {
    model.addAllAttributes(FormUtility.getFormModel(EQUIPMENT_FORM_TITLE, EQUIPMENT_FORM_INSTR, EQUIPMENT_FORM_URL, id, EQUIPMENT_URL + id));
    return "form/genericForm";
  }

  @GetMapping(value = "/" + EQUIPMENTVIEWER_PAGE_NAME + "/form/name/{name}")
  public String viewEquipmentNameWithForm(@PathVariable(value="name") final String name, final Model model) {
    model.addAllAttributes(FormUtility.getFormModel(EQUIPMENT_FORM_TITLE, EQUIPMENT_FORM_INSTR, EQUIPMENT_FORM_URL, name, EQUIPMENT_URL + name));
    return "form/genericForm";
  }

  @RequestMapping(value = "/" + EQUIPMENTVIEWER_PAGE_NAME + "/form", method = { RequestMethod.GET, RequestMethod.POST })
  public String viewEquipmentFormPost(@RequestParam(value = "id", required = false) final String id,
                                @RequestParam(value = "name", required = false) final String name,
                                final Model model) {

    if (id != null && !id.isEmpty()) {
      return ("redirect:" + EQUIPMENT_URL + EQUIPMENTVIEWER_PAGE_NAME + "/id/" + id);

    }else
    if (name != null && !name.isEmpty()) {
      return ("redirect:" + EQUIPMENT_URL + EQUIPMENTVIEWER_PAGE_NAME + "/name/" + name);

    }else {
      model.addAllAttributes(FormUtility.getFormModel(EQUIPMENT_FORM_TITLE, EQUIPMENT_FORM_INSTR, EQUIPMENT_FORM_URL, null, EQUIPMENT_FORM_PLACEHOLDER,null));

    }
    return "form/genericForm";
  }

  /**
   * View a specific equipment of a given process.
   *
   * @param processIdName the name or id of the process
   * @param id          the id of the equipment to view
   * @param model       the model to be passed to the JSP processor
   *
   * @return the name of the JSP page to be processed
   *
   * @throws Exception if no equipment with the specified id was found
   */
  @RequestMapping(value = "/process/{processIdName}/equipment/{id}", method = {RequestMethod.GET})
  public String viewEquipment(@PathVariable("processIdName") final String processIdName, @PathVariable("id") final Long id, final Model model) throws Exception {

    ProcessConfiguration process;

    if(isNumeric(processIdName)){
      Process processObj = processService.getProcessById(Long.parseLong(processIdName));
      process = processService.getProcessConfiguration(processObj.getName());
      process.setProcessName(processObj.getName());
    }else {
      process = processService.getProcessConfiguration(processIdName);
      process.setProcessName(processIdName);
    }

    EquipmentConfiguration equipment = process.getEquipmentConfiguration(id);

    if (equipment == null) {
      throw new Exception("No equipment with id " + id + " was found.");
    }

    // Epic hack to make sure the list of tags is sorted by ID
    Map<Long, SourceDataTag> sortedTags = new TreeMap<>(equipment.getDataTags());
    Field field = EquipmentConfiguration.class.getDeclaredField("sourceDataTags");
    field.setAccessible(true);
    field.set(equipment, sortedTags);

    model.addAttribute("title", "Equipment Viewer");
    model.addAttribute("process", process);
    model.addAttribute("equipment", equipment);
    return "equipment";
  }

  public boolean isNumeric(String str) {
    return str.matches("-?\\d+(\\.\\d+)?");  //match a number with optional '-' and decimal.
  }
}
