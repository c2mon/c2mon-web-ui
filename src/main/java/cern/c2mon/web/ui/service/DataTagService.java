package cern.c2mon.web.ui.service;

import cern.c2mon.client.ext.history.data.DataTagRecord;
import cern.c2mon.client.ext.history.data.repo.DataTagRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DataTagService {

    @Autowired
    private DataTagRepoService dataTagRepoService;

    public final DataTagRecord getDataTagById(final Long dataTagId) {
        Optional<DataTagRecord> dataTag = dataTagRepoService.findById(dataTagId);
        return dataTag.isPresent() ? dataTag.get() : null;
    }

    public final List<DataTagRecord> getDataTagByName(final String dataTagName) {
        return dataTagRepoService.findFirst10ByNameContainingIgnoreCase(dataTagName);
    }
}
