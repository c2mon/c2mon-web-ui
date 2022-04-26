package cern.c2mon.web.ui.service;

import cern.c2mon.client.ext.history.alarm.AlarmRecord;
import cern.c2mon.client.ext.history.alarm.repo.AlarmRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class AlarmSearchService {

    @Autowired
    private AlarmRepoService alarmRepoService;

    public final List<AlarmRecord> findAlarmByFaultFamily(final String faultFamily) {
        return alarmRepoService.findFirst10ByFaultFamilyContainingIgnoreCase(faultFamily);
    }

    public final List<AlarmRecord> findAlarmByFaultMember(final String faultMember) {
        return alarmRepoService.findFirst10ByFaultMemberContainingIgnoreCase(faultMember);
    }

    public final List<AlarmRecord> findAlarmByFaultCode(final Integer faultCode) {
        return alarmRepoService.findFirst10ByFaultCode(faultCode);
    }

    public final List<AlarmRecord> findAlarmByFaultFamilyAndFaultMember(final String faultFamily, final String faultMember) {
        return alarmRepoService.findFirst10ByFaultFamilyContainingIgnoreCaseAndFaultMemberContainingIgnoreCase(faultFamily, faultMember);
    }

    public final List<AlarmRecord> findAlarmByFaultFamilyAndFaultCode(final String faultFamily, final Integer faultCode) {
        return alarmRepoService.findFirst10ByFaultFamilyContainingIgnoreCaseAndFaultCode(faultFamily, faultCode);
    }

    public final List<AlarmRecord> findAlarmByFaultMemberAndFaultCode(final String faultMember, final Integer faultCode) {
        return alarmRepoService.findFirst10ByFaultMemberContainingIgnoreCaseAndFaultCode(faultMember, faultCode);
    }

    public final List<AlarmRecord> findAlarmByFaultFamilyAndFaultMemberAndFaultCode(final String faultFamily, final String faultMember, Integer faultCode) {
        return alarmRepoService.findFirst10ByFaultFamilyContainingIgnoreCaseAndFaultMemberContainingIgnoreCaseAndFaultCode(faultFamily, faultMember, faultCode);
    }

    public final List<AlarmRecord> findAlarmByTagId(final Long tagId) {
        return alarmRepoService.findByTagId(tagId);
    }

    public final Optional<AlarmRecord> findAlarmById(final Long alarmId) {
        return alarmRepoService.findById(alarmId);
    }
}
