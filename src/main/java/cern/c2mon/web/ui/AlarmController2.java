package cern.c2mon.web.ui;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import cern.c2mon.client.common.tag.Tag;
import cern.c2mon.client.core.elasticsearch.ElasticsearchService;
import cern.c2mon.client.core.service.AlarmService;
import cern.c2mon.shared.client.alarm.AlarmValue;

import static org.springframework.web.bind.annotation.RequestMethod.GET;

/**
 * @author Justin Lewis Salmon
 */
@RestController
@RequestMapping("/api/alarms")
public class AlarmController2 {

  @Autowired
  private AlarmService alarmService;

  @Autowired
  private ElasticsearchService elasticsearchService;

  @RequestMapping(value = "/{id}", method = GET)
  public AlarmValue getAlarm(@PathVariable final Long id) throws IOException {
    return alarmService.getAlarms(Collections.singletonList(id)).iterator().next();
  }

  @RequestMapping(value = "/{id}/history", method = GET)
  public List<Object[]> getAlarmHistory(@PathVariable final Long id,
                                        @RequestParam("min") Long min,
                                        @RequestParam("max") Long max) {
    return elasticsearchService.getAlarmHistory(id, min, max);
  }

  @RequestMapping(value = "/active", method = GET)
  public Collection<AlarmValue> getActiveAlarms() {
    return alarmService.getAlarms(elasticsearchService.getTopAlarms(50));
  }
}
