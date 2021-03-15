package model.commands.commands;

import java.util.Arrays;
import java.util.List;
import model.commands.Argument;
import model.commands.Commands;

public enum Recap implements Commands
{
    RECAP_START("recap start", Argument.optional("teamname")),
    RECAP_ADD_LINE("recap add", Argument.optional("@username")),
    RECAP_ADD_REPLAY("recap replay", Argument.mandatory("replaycode"), Argument.mandatory("mapname")),
    RECAP_FINISH("recap finish");


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
