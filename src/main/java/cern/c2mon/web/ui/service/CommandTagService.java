package cern.c2mon.web.ui.service;

import cern.c2mon.client.ext.history.command.CommandTagRecord;
import cern.c2mon.client.ext.history.command.repo.CommandTagRepoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CommandTagService {

    @Autowired
    private CommandTagRepoService commandTagRepoService;

    public final CommandTagRecord getCommandTagById(final Long commandTagId) {
        Optional<CommandTagRecord> commandTag = commandTagRepoService.findById(commandTagId);
        return commandTag.isPresent() ? commandTag.get() : null;
    }

    public final List<CommandTagRecord> getCommandTagByName(final String commandTagName) {
        return commandTagRepoService.findFirst10ByNameContainingIgnoreCase(commandTagName);
    }
}
