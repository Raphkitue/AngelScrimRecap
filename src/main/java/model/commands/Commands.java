package model.commands;

import java.util.List;
import java.util.Optional;

public interface Commands
{
    String getName();
    String getCommand();
    String getSlashCommand();
    List<Argument> getArguments();
    Argument getArgument(String name);
}
