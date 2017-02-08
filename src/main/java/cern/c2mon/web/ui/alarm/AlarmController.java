package cern.c2mon.web.ui.alarm;

import java.io.IOException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cern.c2mon.client.core.elasticsearch.ElasticsearchService;
import cern.c2mon.client.core.service.AlarmService;
import cern.c2mon.shared.client.alarm.AlarmValue;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Justin Lewis Salmon
 */
@RestController
@RequestMapping("/api/alarms")
public class AlarmController {

  @Autowired
  private AlarmService alarmService;

  @Autowired
  private ElasticsearchService elasticsearchService;

  @RequestMapping(value = "/{id}", method = GET)
  public Object getAlarm(@PathVariable final Long id) throws IOException {
    Collection<AlarmValue> alarms = alarmService.getAlarms(Collections.singletonList(id));

    if (alarms.isEmpty() || alarms.size() > 1) {
      return null;
    }

    return enhance(alarms.iterator().next());
  }

  @RequestMapping(value = "/{id}/history", method = GET)
  public List<Object[]> getAlarmHistory(@PathVariable final Long id,
                                        @RequestParam("min") Long min,
                                        @RequestParam("max") Long max) {
    return elasticsearchService.getAlarmHistory(id, min, max);
  }

  @RequestMapping(value = "/search", method = GET)
  public Collection<Object> search(@RequestParam final String query) {
    return enhance(alarmService.getAlarms(elasticsearchService.findAlarmsByName(query)));
  }

  @RequestMapping(value = "/active", method = GET)
  public Collection<Object> getActiveAlarms() {
    return enhance(alarmService.getAlarms(elasticsearchService.getTopAlarms(50)));
  }

  private Collection<Object> enhance(Collection<AlarmValue> alarms) {
    return alarms.stream().map(this::enhance).collect(Collectors.toList());
  }

  private Object enhance(AlarmValue alarm) {
    ObjectNode node = new ObjectMapper().valueToTree(alarm);
    // Store a concatenation of faultFamily, faultMember and faultCode as the alarm "name"
    node.put("name", alarm.getFaultFamily() + ":" + alarm.getFaultMember() + ":" + alarm.getFaultCode());
    return node;
  }
}
