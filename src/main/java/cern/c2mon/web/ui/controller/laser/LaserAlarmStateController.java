package cern.c2mon.web.ui.controller.laser;

import cern.c2mon.client.ext.history.laser.LaserAlarmLogUserConfig;
import cern.c2mon.client.ext.history.laser.LaserUserConfig;
import cern.c2mon.web.ui.service.laser.LaserAlarmStateService;
import cern.c2mon.web.ui.service.laser.LaserUserConfigService;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
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

    @Autowired
    private LaserUserConfigService laserUserConfigService;

    @Autowired
    private LaserAlarmStateService laserAlarmStateService;

    @RequestMapping(value = LASER_ALARM_STATE_URL + "/{configName}", method = { RequestMethod.GET })
    public String viewActiveAlarms(@PathVariable(value = "configName") final String configName,
                                   @RequestParam(value = AT_DATE_PARAMETER, required = false) final String time,
                                   @RequestParam(value = TEXT_SEARCH_PARAMETER, required = false) final String textSearch,
                                   @RequestParam(value = PRIORITY_PARAMETER, required = false) final List<Integer> priority,
                                   Model model) {

        Optional<LaserUserConfig> laserUserConfig = laserUserConfigService.findAlUserConfigurationByName(configName);
        if(!laserUserConfig.isPresent() || priority == null){
            return ("redirect:" + LASER_ALARM_STATE_FORM_URL + "?error=" + configName);
        }

        List<LaserAlarmLogUserConfig> activeAlarms = getActiveAlarms(laserUserConfig.get(), configName, time, textSearch, priority);
        String description = " (At " + time + ")";

        model.addAttribute("title", LASER_ALARM_STATE_TITLE);
        model.addAttribute("configName", configName);
        model.addAttribute("csvviewer", csvViewerUrl(configName, time, textSearch, priority));
        model.addAttribute("description", description);
        model.addAttribute("activeAlarms", activeAlarms);
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

    private final List<LaserAlarmLogUserConfig> getActiveAlarms(final LaserUserConfig laserUserConfig, final String configName,
                                                                 final String time, final String textSearch, final List<Integer> priority){
        List<LaserAlarmLogUserConfig> activeAlarms = new ArrayList<>();
        if (configName != null && time != null) {
            if(textSearch != null) {
                activeAlarms = laserAlarmStateService.findActiveAlarmsByConfigIdAndPriorityAndTextAtGivenTime(
                        laserUserConfig.getConfigId(), time, priority, textSearch);
            }else{
                activeAlarms = laserAlarmStateService.findActiveAlarmsByConfigIdAndPriorityAtGivenTime(
                        laserUserConfig.getConfigId(), time, priority);
            }
        }
        return activeAlarms;
    }

    @RequestMapping(value = LASER_ALARM_STATE_CSV_URL + "/{configName}", method = { RequestMethod.GET })
    public String viewAlarmStateForm(@PathVariable(value = "configName") final String configName,
                                     @RequestParam(value = AT_DATE_PARAMETER, required = false) final String time,
                                     @RequestParam(value = TEXT_SEARCH_PARAMETER, required = false) final String textSearch,
                                     @RequestParam(value = PRIORITY_PARAMETER, required = false) final List<Integer> priority,
                                     Model model) {

        Optional<LaserUserConfig> laserUserConfig = laserUserConfigService.findAlUserConfigurationByName(configName);
        if(!laserUserConfig.isPresent() || priority == null){
            return ("redirect:" + LASER_ALARM_STATE_FORM_URL + "?error=" + configName);
        }

        List<LaserAlarmLogUserConfig> activeAlarms = getActiveAlarms(laserUserConfig.get(), configName, time, textSearch, priority);

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

        StringBuilder redirect = new StringBuilder();
        redirect.append("redirect:" + LASER_ALARM_STATE_URL);

        if (id == null) {
            List<LaserUserConfig> laserUserConfigs = laserUserConfigService.findAllUserConfigurations();
            model.addAttribute("laseruserconfigs", laserUserConfigs);
            model.addAttribute("formSubmitUrl", LASER_ALARM_STATE_FORM_URL);

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
