package cern.c2mon.web.ui.model;

import cern.c2mon.client.ext.history.laser.AlarmShorttermlog;
import lombok.Data;

import java.time.LocalDateTime;

import org.springframework.boot.configurationprocessor.json.JSONException;
import org.springframework.boot.configurationprocessor.json.JSONObject;
import org.springframework.context.annotation.Profile;

@Profile("enableLaser")
@Data
public class AlarmLogParsed {

  private LocalDateTime tagservertime;
  private Long id;
  private String tagName;
  private String tagValue;
  private String tagValueDesc;
  private String tagDatatype;
  private LocalDateTime tagTime;
  private LocalDateTime tagDaqTime;

  private String alarmPrefix;
  private String alarmSuffix;
  private String alarmTimestamp;
  private String alarmUser;
  
  private String alarmMetaData;
  private String problemDescription;

  public AlarmLogParsed(AlarmShorttermlog shorttermlog) {
    this.tagservertime = shorttermlog.getTagServerTime();
    this.id = shorttermlog.getId();
    this.tagName = shorttermlog.getTagName();
    this.tagValue = shorttermlog.getTagValue();
    this.tagValueDesc = shorttermlog.getTagValueDesc();
    this.tagDatatype = shorttermlog.getTagDatatype();
    this.tagTime = shorttermlog.getTagTime();
    this.tagDaqTime = shorttermlog.getTagDaqTime();
    this.alarmMetaData = shorttermlog.getAlarmMetaData();
    
    if(this.tagValueDesc != null) {
      extractValuesFromValueDescription();
    }
    if(this.alarmMetaData != null) {
        extractValuesFromAlarmMetaData();
      }

  }

  private void extractValuesFromAlarmMetaData(){
      try {
        JSONObject obj = new JSONObject(this.alarmMetaData);
        this.problemDescription = obj.has("problemDescription") ? obj.getString("problemDescription") : null;

      } catch (JSONException e) {
        //ignore
      }
    }

  private void extractValuesFromValueDescription(){
    try {
      JSONObject obj = new JSONObject(this.tagValueDesc);
      this.alarmPrefix = obj.has("alarm_prefix") ? obj.getString("alarm_prefix") : null;
      this.alarmSuffix = obj.has("alarm_suffix") ? obj.getString("alarm_suffix") : null;
      this.alarmTimestamp = obj.has("alarm_timestamp") ? obj.getString("alarm_timestamp") : null;
      this.alarmUser = obj.has("alarm_user") ? obj.getString("alarm_user") : null;

    } catch (JSONException e) {
      //ignore
    }
  }
}