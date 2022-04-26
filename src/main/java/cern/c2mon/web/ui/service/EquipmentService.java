package cern.c2mon.web.ui.service;

import cern.c2mon.client.ext.history.equipment.EquipmentRecord;
import cern.c2mon.client.ext.history.equipment.repo.EquipmentRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepoService equipmentRepoService;

    public final EquipmentRecord getEquipmentById(final Long equipmentId) {
        Optional<EquipmentRecord> equipment = equipmentRepoService.findById(equipmentId);
        return equipment.isPresent() ? equipment.get() : null;
    }

    public final List<EquipmentRecord> getEquipmentByName(final String equipmentName) {
        return equipmentRepoService.findFirst10ByNameContainingIgnoreCase(equipmentName);
    }
}
