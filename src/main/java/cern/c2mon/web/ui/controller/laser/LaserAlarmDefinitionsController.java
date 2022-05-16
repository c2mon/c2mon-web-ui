package cern.c2mon.web.ui.controller.laser;

import cern.c2mon.client.ext.history.laser.LaserAlarmDefinition;
import cern.c2mon.client.ext.history.laser.LaserUserConfig;
import cern.c2mon.web.ui.service.laser.LaserAlarmDefinitionService;
import cern.c2mon.web.ui.service.laser.LaserUserConfigService;

import java.util.ArrayList;
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
public class LaserAlarmDefinitionsController {

    public static final String LASER_ALARM_DEFINITION_URL = "/laseralarmdefinitions/";

    public static final String LASER_ALARM_DEFINITION_FORM_URL = LASER_ALARM_DEFINITION_URL + "form";

    public static final String LASER_ALARM_DEFINITION_CSV_URL = LASER_ALARM_DEFINITION_URL + "csv";

    public static final String LASER_ALARM_DEFINITION_TITLE = "Alarm Definitions";

    public static final String TEXT_SEARCH_PARAMETER = "TEXT";

    public static final String PRIORITY_PARAMETER = "PRIORITY";

    @Autowired
    private LaserUserConfigService laserUserConfigService;

    @Autowired
    private LaserAlarmDefinitionService laserAlarmDefinitionService;

    @RequestMapping(value = LASER_ALARM_DEFINITION_URL + "/{configName}", method = { RequestMethod.GET })
    public String viewAlarmDefinition(@PathVariable(value = "configName") final String configName,
                                      @RequestParam(value = TEXT_SEARCH_PARAMETER, required = false) final String textSearch,
                                      @RequestParam(value = PRIORITY_PARAMETER, required = false) final List<Integer> priority,
                                      Model model) {

        Optional<LaserUserConfig> laserUserConfig = laserUserConfigService.findUserConfiguration(configName);

        if((!laserUserConfig.isPresent()) || priority == null){
            return ("redirect:" + LASER_ALARM_DEFINITION_FORM_URL + "?error=" + configName);
        }

        List<LaserAlarmDefinition> alarmDefinitions = getAlarmDefinitions(laserUserConfig.get(), configName, textSearch, priority);

        model.addAttribute("title", LASER_ALARM_DEFINITION_TITLE);
        model.addAttribute("csvviewer", csvViewerUrl(configName, textSearch, priority));
        model.addAttribute("configName", configName);
        model.addAttribute("alarmdefinitions", alarmDefinitions);
        return "laser/alarmdefinition";
    }

    private final String csvViewerUrl(String configName, String textSearch, List<Integer> priority){
        String csvViewer = "./csv/" + configName;

        if (priority != null) {
            csvViewer += "?";
            for (Integer p : priority) {
                csvViewer += PRIORITY_PARAMETER + "=" + p + "&";
            }
        }

        if (textSearch != null && !textSearch.isEmpty()) {
            if(priority == null){
                csvViewer += "?";
            }
            csvViewer += TEXT_SEARCH_PARAMETER + "=" + textSearch;
        }

        return csvViewer;
    }

    private final List<LaserAlarmDefinition> getAlarmDefinitions(final LaserUserConfig laserUserConfig, final String configName,
                                                                 final String textSearch, final List<Integer> priority){
        List<LaserAlarmDefinition> alarmDefinitions = new ArrayList<>();
        if (configName != null) {
            if(textSearch != null) {
                alarmDefinitions = laserAlarmDefinitionService.findAllAlarmDefinitionsByConfigIdAndPriorityAndTextSearch(
                        laserUserConfig.getConfigId(), priority, textSearch);
            }else{
                alarmDefinitions = laserAlarmDefinitionService.findAllAlarmDefinitionsByConfigIdAndPriority(
                        laserUserConfig.getConfigId(), priority);
            }
        }
        return alarmDefinitions;
    }

    @RequestMapping(value = LASER_ALARM_DEFINITION_CSV_URL + "/{configName}", method = { RequestMethod.GET })
    public String viewCsv(@PathVariable(value = "configName") final String configName,
                          @RequestParam(value = TEXT_SEARCH_PARAMETER, required = false) final String textSearch,
                          @RequestParam(value = PRIORITY_PARAMETER, required = false) final List<Integer> priority,
                          Model model) {

        Optional<LaserUserConfig> laserUserConfig = laserUserConfigService.findUserConfiguration(configName);

        if((!laserUserConfig.isPresent()) || priority == null){
            return ("redirect:" + LASER_ALARM_DEFINITION_FORM_URL + "?error=" + configName);
        }

        List<LaserAlarmDefinition> alarmDefinitions = getAlarmDefinitions(laserUserConfig.get(), configName, textSearch, priority);

        StringBuilder csv = new StringBuilder();
        String header = "Last Updated,Alarm Id,Alarm Name,System Name,Priority,Enabled,Problem Description\n";
        csv.append(header);

        for(LaserAlarmDefinition alarmUserConfig : alarmDefinitions){
            csv.append(alarmUserConfig.getLastUpdated() + "," +
                    alarmUserConfig.getAlarmId() + "," +
                    alarmUserConfig.getFaultFamily() + ":" + alarmUserConfig.getFaultMember() + ":" + alarmUserConfig.getFaultCode()+ "," +
                    alarmUserConfig.getSystemName() + "," +
                    alarmUserConfig.getPriority() + "," +
                    alarmUserConfig.getEnabled() + "," +
                    alarmUserConfig.getProblemDescription() +
                    "\n");
        }

        model.addAttribute("csv", csv);
        return "raw/csv";
    }


    @RequestMapping(value = LASER_ALARM_DEFINITION_FORM_URL, method = { RequestMethod.GET, RequestMethod.POST })
    public String viewUserConfigFormPost(@RequestParam(value = "id", required = false) final String id,
                                         @RequestParam(value = "error", required = false) final String wrongId,
                                         @RequestParam(value = "textSearch", required = false) final String textSearch,
                                         @RequestParam(value = "priority", required = false) final List<Integer> priority,
                                         final Model model) {

        if (id == null) {
            List<LaserUserConfig> laserUserConfigs = laserUserConfigService.findAllUserConfigurations();
            model.addAttribute("laseruserconfigs", laserUserConfigs);
            model.addAttribute("formSubmitUrl", LASER_ALARM_DEFINITION_FORM_URL);
        }

        return handleForm(id, wrongId, textSearch,priority, model);
    }

    @RequestMapping(value = LASER_ALARM_DEFINITION_FORM_URL + "/{configIdOrName}", method = { RequestMethod.GET, RequestMethod.POST })
    public String viewUserConfigFormPost(@PathVariable(value = "configIdOrName") final String configIdOrName,
                                         @RequestParam(value = "id", required = false) final String id,
                                         @RequestParam(value = "error", required = false) final String wrongId,
                                         @RequestParam(value = "textSearch", required = false) final String textSearch,
                                         @RequestParam(value = "priority", required = false) final List<Integer> priority,
                                         final Model model) {

        if (id == null) {
            List<LaserUserConfig> laserUserConfigs = laserUserConfigService.findAllUserConfigurations();
            model.addAttribute("laseruserconfigs", laserUserConfigs);
            model.addAttribute("formSubmitUrl", LASER_ALARM_DEFINITION_FORM_URL);

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

        return handleForm(id, wrongId, textSearch,priority, model);
    }

    private String handleForm(String id, String wrongId, String textSearch, List<Integer> priority, Model model){
        StringBuilder redirect = new StringBuilder();
        redirect.append("redirect:" + LASER_ALARM_DEFINITION_URL);

        if (id == null) {

            if (wrongId != null) {
                model.addAttribute("error", wrongId);
            }

        }else {
            redirect.append(id);

            if (priority != null) {
                redirect.append("?");
                for (Integer p : priority) {
                    redirect.append(PRIORITY_PARAMETER + "=" + p + "&");
                }
            }

            if (textSearch != null && !textSearch.isEmpty()) {
                if(priority == null){
                    redirect.append("?");
                }
                redirect.append(TEXT_SEARCH_PARAMETER + "=" + textSearch);
            }

            return redirect.toString();
        }

        return "laser/alarmdefinitionsform";
    }
}
