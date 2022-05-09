package cern.c2mon.web.ui.controller.laser;

import cern.c2mon.client.ext.history.laser.LaserAlarmUserConfig;
import cern.c2mon.client.ext.history.laser.LaserUserConfig;
import cern.c2mon.web.ui.service.laser.LaserAlarmDefinitionService;
import cern.c2mon.web.ui.service.laser.LaserUserConfigService;

import javax.servlet.http.HttpServletResponse;
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

    @Autowired
    private LaserUserConfigService laserUserConfigService;

    @Autowired
    private LaserAlarmDefinitionService laserAlarmDefinitionService;

    @RequestMapping(value = LASER_ALARM_DEFINITION_URL + "/{configName}", method = { RequestMethod.GET })
    public String viewAlarmDefinition(@PathVariable(value = "configName") final String configName,
                                      final HttpServletResponse response,
                                      final Model model){

        Optional<LaserUserConfig> laserUserConfig = laserUserConfigService.findAlUserConfigurationByName(configName);
        if(!laserUserConfig.isPresent()){
            return ("redirect:" + LASER_ALARM_DEFINITION_FORM_URL + "?error=" + configName);
        }

        List<LaserAlarmUserConfig> alarmDefinitions = laserAlarmDefinitionService.findAllAlarmDefinitionsByConfigId(laserUserConfig.get().getConfigId());

        model.addAttribute("title", LASER_ALARM_DEFINITION_TITLE);
        model.addAttribute("csvviewer", "./csv/" + configName);
        model.addAttribute("configName", configName);
        model.addAttribute("alarmdefinitions", alarmDefinitions);
        return "laser/alarmdefinition";
    }

    @RequestMapping(value = LASER_ALARM_DEFINITION_CSV_URL + "/{id}", method = { RequestMethod.GET })
    public String viewCsv(@PathVariable final String id, final HttpServletResponse response, Model model) {
        Optional<LaserUserConfig> laserUserConfig = laserUserConfigService.findAlUserConfigurationByName(id);
        if(!laserUserConfig.isPresent()){
            return ("redirect:" + LASER_ALARM_DEFINITION_FORM_URL + "?error=" + id);
        }

        List<LaserAlarmUserConfig> alarmDefinitions = laserAlarmDefinitionService.findAllAlarmDefinitionsByConfigId(laserUserConfig.get().getConfigId());

        StringBuilder csv = new StringBuilder();
        String header = "Last Updated,Alarm Id,Alarm Name,System Name,Priority,Enabled,Problem Description\n";
        csv.append(header);

        for(LaserAlarmUserConfig alarmUserConfig : alarmDefinitions){
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

    @RequestMapping(value = LASER_ALARM_DEFINITION_FORM_URL + "/{id}", method = { RequestMethod.GET })
    public String viewUserConfigWithForm(@PathVariable final String id, final Model model) {
        List<LaserUserConfig> laserUserConfigs = laserUserConfigService.findAllUserConfigurations();
        model.addAttribute("laseruserconfigs", laserUserConfigs);
        model.addAttribute("formSubmitUrl", LASER_ALARM_DEFINITION_FORM_URL);
        return "laser/alarmdefinitionsform";
    }

    @RequestMapping(value = LASER_ALARM_DEFINITION_FORM_URL, method = { RequestMethod.GET, RequestMethod.POST })
    public String viewUserConfigFormPost(@RequestParam(value = "id", required = false) final String id,
                                      @RequestParam(value = "error", required = false) final String wrongId,
                                      final Model model) {

        if (id == null) {
            List<LaserUserConfig> laserUserConfigs = laserUserConfigService.findAllUserConfigurations();
            model.addAttribute("laseruserconfigs", laserUserConfigs);
            model.addAttribute("formSubmitUrl", LASER_ALARM_DEFINITION_FORM_URL);

            if (wrongId != null) {
                model.addAttribute("error", wrongId);
            }

        } else {
            return ("redirect:" + LASER_ALARM_DEFINITION_URL + id);
        }

        return "laser/alarmdefinitionsform";
    }
}
