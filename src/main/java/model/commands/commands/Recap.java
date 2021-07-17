package model.commands.commands;

import java.util.Arrays;
import java.util.List;
import model.commands.Argument;
import model.commands.Commands;

public enum Recap implements Commands
{
    RECAP_START("recap start",
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    RECAP_ADD_LINE("recap add",
        Argument.mandatory("username", "", Argument.ArgumentType.USERNAME),
        Argument.mandatory("teamname", "", Argument.ArgumentType.STRING)),
    RECAP_ADD_REPLAY("recap replay",
        Argument.mandatory("replaycode", "", Argument.ArgumentType.STRING),
        Argument.mandatory("mapname", "", Argument.ArgumentType.SPECIFIC),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    RECAP_FINISH("recap finish",
        Argument.optional("teamname", "", Argument.ArgumentType.STRING));


    private final String command;
    private final List<Argument> arguments;

    Recap(String command, Argument... arguments)
    {
        this.command = command;
        this.arguments = Arrays.asList(arguments);
    }

    @Override
    public String getName()
    {
        return super.name();
    }

    public String getCommand()
    {
        return command;
    }

    public List<Argument> getArguments()
    {
        return arguments;
    }
}
