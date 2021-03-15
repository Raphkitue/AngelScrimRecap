package model.commands;

import java.util.List;

public interface Commands
{
    String getName();
    String getCommand();
    List<Argument> getArguments();
}
