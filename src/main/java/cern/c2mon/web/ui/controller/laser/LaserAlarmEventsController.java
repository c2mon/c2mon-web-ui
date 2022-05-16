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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

@Profile("enableLaser")
@Controller
public class LaserAlarmEventsController {

    public static final String LASER_ALARM_EVENT_URL = "/laseralarmevents/";

    public static final String LASER_ALARM_EVENT_FORM_URL = LASER_ALARM_EVENT_URL + "form";

    public static final String LASER_ALARM_EVENT_CSV_URL = LASER_ALARM_EVENT_URL + "csv";

    public static final String LASER_ALARM_EVENT_TITLE = "Alarm Events";

    public static final String START_DATE_PARAMETER = "START";

    public static final String END_DATE_PARAMETER = "END";

    public static final String TEXT_SEARCH_PARAMETER = "TEXT";

    public static final String PRIORITY_PARAMETER = "PRIORITY";

    private static final String DATE_FORMAT = "yyyy-MM-dd-HH:mm";

    @Autowired
    private LaserUserConfigService laserUserConfigService;

    @Autowired
    private LaserAlarmEventService laserAlarmEventService;

    @RequestMapping(value = LASER_ALARM_EVENT_URL + "{configName}", method = { RequestMethod.GET })
    public final String viewAlarmDefinitionsByConfigNameBetweenDate(@PathVariable(value = "configName") final String configName,
                                                                    @RequestParam(value = START_DATE_PARAMETER, required = false) final String startTime,
                                                                    @RequestParam(value = END_DATE_PARAMETER, required = false) final String endTime,
                                                                    @RequestParam(value = TEXT_SEARCH_PARAMETER, required = false) final String textSearch,
                                                                    @RequestParam(value = PRIORITY_PARAMETER, required = false) final List<Integer> priority,
                                                                    Model model) {

        Optional<LaserUserConfig> laserUserConfig = laserUserConfigService.findUserConfiguration(configName);

        if((!laserUserConfig.isPresent()) || priority == null){
            return ("redirect:" + LASER_ALARM_EVENT_FORM_URL + "?error=" + configName);
        }

        List<LaserAlarmLogUserConfig> alarmEvents = getAlarmDefinitions(laserUserConfig.get(), configName, startTime, endTime, textSearch, priority);
        String description = " (From " + startTime + " to " + endTime + ")";

        model.addAttribute("title", LASER_ALARM_EVENT_TITLE);
        model.addAttribute("configName", configName);
        model.addAttribute("csvviewer", csvViewerUrl(configName, startTime, endTime, textSearch, priority));
        model.addAttribute("description", description);
        model.addAttribute("alarmdefinitions", alarmEvents);
        return "laser/alarmevents";
    }

    private final String csvViewerUrl(String configName, String startTime, String endTime, String textSearch, List<Integer> priority){
        String csvViewer = "./csv/" + configName + "?" + START_DATE_PARAMETER + "=" + startTime + "&" + END_DATE_PARAMETER + "=" + endTime;

        if(textSearch != null && !textSearch.isEmpty()) {
            csvViewer += "&" + TEXT_SEARCH_PARAMETER + "=" + textSearch;
        }
        for (Integer p : priority) {
            csvViewer += ("&" + PRIORITY_PARAMETER + "=" + p);
        }

        return csvViewer;
    }

    private final List<LaserAlarmLogUserConfig> getAlarmDefinitions(final LaserUserConfig laserUserConfig, final String configName,
                                                                 final String startTime, final String endTime,
                                                                 final String textSearch, final List<Integer> priority){
        List<LaserAlarmLogUserConfig> alarmEvents = new ArrayList<>();
        if (configName != null && startTime != null && endTime != null) {
            if(textSearch != null) {
                alarmEvents = laserAlarmEventService.findAllAlarmsByConfigIdAndPriorityAndTextBetweenDates(
                        laserUserConfig.getConfigId(), startTime, endTime, priority, textSearch);
            }else{
                alarmEvents = laserAlarmEventService.findAllAlarmsByConfigIdAndPriorityBetweenDates(
                        laserUserConfig.getConfigId(), startTime, endTime, priority);
            }
        }
        return alarmEvents;
    }

    @RequestMapping(value = LASER_ALARM_EVENT_CSV_URL + "/{configName}", method = { RequestMethod.GET })
    public final String viewAlarmDefinitionsByConfigNameBetweenDateCsv(@PathVariable(value = "configName") final String configName,
                                                                       @RequestParam(value = START_DATE_PARAMETER, required = false) final String startTime,
                                                                       @RequestParam(value = END_DATE_PARAMETER, required = false) final String endTime,
                                                                       @RequestParam(value = TEXT_SEARCH_PARAMETER, required = false) final String textSearch,
                                                                       @RequestParam(value = PRIORITY_PARAMETER, required = false) final List<Integer> priority,
                                                                       Model model) {

        Optional<LaserUserConfig> laserUserConfig = laserUserConfigService.findUserConfiguration(configName);

        if((!laserUserConfig.isPresent()) || priority == null){
            return ("redirect:" + LASER_ALARM_EVENT_FORM_URL + "?error=" + configName);
        }

        List<LaserAlarmLogUserConfig> alarmEvents = getAlarmDefinitions(laserUserConfig.get(), configName, startTime, endTime, textSearch, priority);

        StringBuilder csv = new StringBuilder();
        String header = "Timestamp,Alarm Id,Alarm Name,Priority,Active,Oscillating,Problem Description\n";
        csv.append(header);

        for(LaserAlarmLogUserConfig alarm : alarmEvents){
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

    @RequestMapping(value = LASER_ALARM_EVENT_FORM_URL, method = { RequestMethod.GET, RequestMethod.POST })
    public String viewUserConfigWithForm(@RequestParam(value = "id", required = false) final String id,
                                         @RequestParam(value = "error", required = false) final String wrongId,
                                         @RequestParam(value = "start", required = false) final String startDate,
                                         @RequestParam(value = "end", required = false) final String endDate,
                                         @RequestParam(value = "startTime", required = false) final String startTime,
                                         @RequestParam(value = "endTime", required = false) final String endTime,
                                         @RequestParam(value = "textSearch", required = false) final String textSearch,
                                         @RequestParam(value = "priority", required = false) final List<Integer> priority,
                                         final Model model) {

        if (id == null) {
            List<LaserUserConfig> laserUserConfigs = laserUserConfigService.findAllUserConfigurations();
            model.addAttribute("laseruserconfigs", laserUserConfigs);
            model.addAttribute("formSubmitUrl", LASER_ALARM_EVENT_FORM_URL);
        }
        return handleForm(id, wrongId, startDate, endDate, startTime, endTime, textSearch, priority, model);
    }

    @RequestMapping(value = LASER_ALARM_EVENT_FORM_URL + "/{configIdOrName}", method = { RequestMethod.GET, RequestMethod.POST })
    public String viewUserConfigWithForm(@PathVariable(value = "configIdOrName") final String configIdOrName,
                                         @RequestParam(value = "id", required = false) final String id,
                                         @RequestParam(value = "error", required = false) final String wrongId,
                                         @RequestParam(value = "start", required = false) final String startDate,
                                         @RequestParam(value = "end", required = false) final String endDate,
                                         @RequestParam(value = "startTime", required = false) final String startTime,
                                         @RequestParam(value = "endTime", required = false) final String endTime,
                                         @RequestParam(value = "textSearch", required = false) final String textSearch,
                                         @RequestParam(value = "priority", required = false) final List<Integer> priority,
                                         final Model model) {

        if (id == null) {
            List<LaserUserConfig> laserUserConfigs = laserUserConfigService.findAllUserConfigurations();
            model.addAttribute("laseruserconfigs", laserUserConfigs);
            model.addAttribute("formSubmitUrl", LASER_ALARM_EVENT_FORM_URL);

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

        return handleForm(id, wrongId, startDate, endDate, startTime, endTime, textSearch, priority, model);
    }

    private String handleForm(String id, String wrongId, String startDate, String endDate, String startTime,
                              String endTime, String textSearch, List<Integer> priority, Model model) {

        StringBuilder redirect = new StringBuilder();
        redirect.append("redirect:" + LASER_ALARM_EVENT_URL);

        if (id == null) {

         if (wrongId != null) {
             model.addAttribute("error", wrongId);
         }

         // let's pre-fill the date boxes with the current date
         DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
         DateFormat timeFormat = new SimpleDateFormat("HH:mm");

         Date currentDate = new Date();
         model.addAttribute("defaultToDate", dateFormat.format(currentDate));
         model.addAttribute("defaultToTime", timeFormat.format(currentDate));

         Date oneHourBeforeDate = new Date(currentDate.getTime() - 3600 * 1000);
         model.addAttribute("defaultFromDate", dateFormat.format(oneHourBeforeDate));
         model.addAttribute("defaultFromTime", timeFormat.format(oneHourBeforeDate));

        }else {
            redirect.append(id);

            if (startDate != null && endDate != null && startTime != null && endTime != null) {
                redirect.append("?" + START_DATE_PARAMETER + "=" + startDate + "-" + startTime +
                        "&" + END_DATE_PARAMETER + "=" + endDate + "-" + endTime);
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

        return "laser/alarmeventsform";
    }
}
