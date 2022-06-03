package cern.c2mon.web.ui.controller.laser;

import cern.c2mon.client.ext.history.laser.LaserAlarmLogUserConfig;
import cern.c2mon.client.ext.history.laser.LaserUserConfig;
import cern.c2mon.web.ui.service.laser.LaserAlarmEventService;
import cern.c2mon.web.ui.service.laser.LaserUserConfigService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Profile("enableLaser")
@Controller
public class LaserAlarmStateController {

    public static final String LASER_ALARM_STATE_URL = "/laseralarmstate/";

    public static final String LASER_ALARM_STATE_FORM_URL = LASER_ALARM_STATE_URL + "form";

    public static final String LASER_ALARM_STATE_CSV_URL = LASER_ALARM_STATE_URL + "csv";

    public static final String LASER_ALARM_STATE_TITLE = "Alarm State";

    public static final String AT_DATE_PARAMETER = "AT";

    public static final String TEXT_SEARCH_PARAMETER = "TEXT";

    public static final String PRIORITY_PARAMETER = "PRIORITY";

    private static final String DATE_FORMAT = "yyyy-MM-dd-HH:mm";

    public static final String PAGE_NUMBER_PARAMETER = "PAGENO";

    private static final Integer PAGE_SIZE = 20;

    @Autowired
    private LaserUserConfigService laserUserConfigService;

    @Autowired
    private LaserAlarmEventService laserAlarmEventService;

    @RequestMapping(value = LASER_ALARM_STATE_URL + "/{configName}", method = { RequestMethod.GET })
    public String viewActiveAlarms(@PathVariable(value = "configName") final String configName,
                                   @RequestParam(value = AT_DATE_PARAMETER, required = false) final String time,
                                   @RequestParam(value = TEXT_SEARCH_PARAMETER, required = false) final String textSearch,
                                   @RequestParam(value = PRIORITY_PARAMETER, required = false) final List<Integer> priority,
                                   @RequestParam(value = PAGE_NUMBER_PARAMETER, required = false) final Integer pageNo,
                                   Model model) {

        Optional<LaserUserConfig> laserUserConfig = laserUserConfigService.findUserConfiguration(configName);

        if((!laserUserConfig.isPresent()) || priority == null){
            return ("redirect:" + LASER_ALARM_STATE_FORM_URL + "?error=" + configName);
        }

        int pageNumber = pageNo == null ? 1 : pageNo;

        Page<LaserAlarmLogUserConfig> activeAlarms = null;
        if (configName != null && time != null) {
            if(textSearch != null) {
                activeAlarms = laserAlarmEventService.findActiveAlarmsByConfigIdAndPriorityAndTextAtGivenTime(
                        laserUserConfig.get().getConfigId(), time, priority, textSearch, PAGE_SIZE, pageNumber - 1);
            }else{
                activeAlarms = laserAlarmEventService.findActiveAlarmsByConfigIdAndPriorityAtGivenTime(
                        laserUserConfig.get().getConfigId(), time, priority, PAGE_SIZE, pageNumber - 1);
            }
        }

        String description = " (At " + time + ")";

        model.addAttribute("title", LASER_ALARM_STATE_TITLE);
        model.addAttribute("configName", configName);
        model.addAttribute("csvviewer", csvViewerUrl(configName, time, textSearch, priority));
        model.addAttribute("description", description);
        model.addAttribute("activeAlarms", activeAlarms == null ? new ArrayList<>() : activeAlarms.getContent());
        model.addAttribute("totalPages", activeAlarms.getTotalPages());
        model.addAttribute("pageNumber", pageNumber);
        return "laser/alarmstate";
    }

    private final String csvViewerUrl(String configName, String time, String textSearch, List<Integer> priority){
        String csvViewer = "./csv/" + configName + "?" + AT_DATE_PARAMETER + "=" + time;

        if(textSearch != null && !textSearch.isEmpty()) {
            csvViewer += "&" + TEXT_SEARCH_PARAMETER + "=" + textSearch;
        }
        for (Integer p : priority) {
            csvViewer += ("&" + PRIORITY_PARAMETER + "=" + p);
        }

        return csvViewer;
    }

    @RequestMapping(value = LASER_ALARM_STATE_CSV_URL + "/{configName}", method = { RequestMethod.GET })
    public String viewAlarmStateForm(@PathVariable(value = "configName") final String configName,
                                     @RequestParam(value = AT_DATE_PARAMETER, required = false) final String time,
                                     @RequestParam(value = TEXT_SEARCH_PARAMETER, required = false) final String textSearch,
                                     @RequestParam(value = PRIORITY_PARAMETER, required = false) final List<Integer> priority,
                                     Model model) {

        Optional<LaserUserConfig> laserUserConfig = laserUserConfigService.findUserConfiguration(configName);

        if((!laserUserConfig.isPresent()) || priority == null){
            return ("redirect:" + LASER_ALARM_STATE_FORM_URL + "?error=" + configName);
        }

        List<LaserAlarmLogUserConfig> activeAlarms = new ArrayList<>();
        if (configName != null && time != null) {
            if(textSearch != null) {
                activeAlarms = laserAlarmEventService.findActiveAlarmsByConfigIdAndPriorityAndTextAtGivenTime(
                        laserUserConfig.get().getConfigId(), time, priority, textSearch);
            }else{
                activeAlarms = laserAlarmEventService.findActiveAlarmsByConfigIdAndPriorityAtGivenTime(
                        laserUserConfig.get().getConfigId(), time, priority);
            }
        }

        StringBuilder csv = new StringBuilder();
        String header = "Timestamp,Alarm Id,Alarm Name,Priority,Active,Oscillating,Problem Description\n";
        csv.append(header);

        for(LaserAlarmLogUserConfig alarm : activeAlarms){
            csv.append(alarm.getServerTime() + "," +
                    alarm.getId() + "," +
                    alarm.getFaultFamily() + ":" + alarm.getFaultMember() + ":" + alarm.getFaultCode()+ "," +
                    alarm.getPriority() + "," +
                    alarm.getActive() + "," +
                    alarm.getOscillating() + "," +
                    alarm.getProblemDescription() +
                    "\n");
        }

        model.addAttribute("csv", csv);
        return "raw/csv";
    }

    @RequestMapping(value = LASER_ALARM_STATE_FORM_URL, method = { RequestMethod.GET, RequestMethod.POST })
    public String viewUserConfigWithForm(@RequestParam(value = "id", required = false) final String id,
                                         @RequestParam(value = "error", required = false) final String wrongId,
                                         @RequestParam(value = "date", required = false) final String date,
                                         @RequestParam(value = "time", required = false) final String time,
                                         @RequestParam(value = "textSearch", required = false) final String textSearch,
                                         @RequestParam(value = "priority", required = false) final List<Integer> priority,
                                         final Model model) {

        if (id == null) {
            List<LaserUserConfig> laserUserConfigs = laserUserConfigService.findAllUserConfigurations();
            model.addAttribute("laseruserconfigs", laserUserConfigs);
            model.addAttribute("formSubmitUrl", LASER_ALARM_STATE_FORM_URL);
        }

        return handleForm(id, wrongId, date, time, textSearch, priority, model);
    }

    @RequestMapping(value = LASER_ALARM_STATE_FORM_URL + "/{configIdOrName}", method = { RequestMethod.GET, RequestMethod.POST })
    public String viewUserConfigWithForm(@PathVariable(value = "configIdOrName") final String configIdOrName,
                                         @RequestParam(value = "id", required = false) final String id,
                                         @RequestParam(value = "error", required = false) final String wrongId,
                                         @RequestParam(value = "date", required = false) final String date,
                                         @RequestParam(value = "time", required = false) final String time,
                                         @RequestParam(value = "textSearch", required = false) final String textSearch,
                                         @RequestParam(value = "priority", required = false) final List<Integer> priority,
                                         final Model model) {

        if (id == null) {
            List<LaserUserConfig> laserUserConfigs = laserUserConfigService.findAllUserConfigurations();
            model.addAttribute("laseruserconfigs", laserUserConfigs);
            model.addAttribute("formSubmitUrl", LASER_ALARM_STATE_FORM_URL);

            if(LaserUtil.isNumeric(configIdOrName)){
                Optional<LaserUserConfig> laserUserConfig = LaserUtil.findConfigById(laserUserConfigs, Long.parseLong(configIdOrName));
                if(laserUserConfig.isPresent()){
                    model.addAttribute("configName", laserUserConfig.get().getConfigName());
                }else{
                    model.addAttribute("error", configIdOrName);
                }
            }else{
                if(LaserUtil.containsConfigName(laserUserConfigs, configIdOrName)){
                    model.addAttribute("configName", configIdOrName);
                }else{
                    model.addAttribute("error", configIdOrName);
                }
            }
        }

        return handleForm(id, wrongId, date, time, textSearch, priority, model);
    }

    private String handleForm(String id, String wrongId, String date, String time, String textSearch, List<Integer> priority, Model model){
        StringBuilder redirect = new StringBuilder();
        redirect.append("redirect:" + LASER_ALARM_STATE_URL);

        if (id == null) {

            if (wrongId != null) {
                model.addAttribute("error", wrongId);
            }

            // let's pre-fill the date boxes with the current date
            DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
            DateFormat timeFormat = new SimpleDateFormat("HH:mm");

            Date currentDate = new Date();
            model.addAttribute("defaultAtDate", dateFormat.format(currentDate));
            model.addAttribute("defaultAtTime", timeFormat.format(currentDate));

        }else {

            redirect.append(id);

            if (date != null) {
                redirect.append("?" + AT_DATE_PARAMETER + "=" + date + "-" + time);
            }

            if (textSearch != null && !textSearch.isEmpty()) {
                redirect.append("&" + TEXT_SEARCH_PARAMETER + "=" + textSearch);
            }

            if (priority != null) {
                for (Integer p : priority) {
                    redirect.append("&" + PRIORITY_PARAMETER + "=" + p);
                }
            }

            return redirect.toString();
        }

        return "laser/alarmstateform";
    }

}
