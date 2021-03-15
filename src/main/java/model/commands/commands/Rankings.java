package model.commands.commands;

import java.util.Arrays;
import java.util.List;
import model.commands.Argument;
import model.commands.Commands;

public enum Rankings implements Commands
{
    RANKINGS_START("rankings create", Argument.mandatory("#channel")),
    RANKINGS_ENROLL("rankings enroll", Argument.mandatory("#channel"), Argument.mandatory("battletag"), Argument.mandatory("mainrole")),
    RANKINGS_DELETE("rankings delete", Argument.mandatory("#channel"), Argument.mandatory("battletag")),
    RANKINGS_REMOVE("rankings remove", Argument.mandatory("#channel")),
    RANKINGS_CONF("rankings conf", Argument.mandatory("#channel"), Argument.mandatory("confmode")),
    RANKINGS_UPDATE("rankings update", Argument.mandatory("#channel"));


    private final String command;
    private final List<Argument> arguments;

    Rankings(String command, Argument... arguments)
    {
        this.command = command;
        this.arguments = Arrays.asList(arguments);
    }

    @Override
    public String getName()
    {
        return super.name();
    }

    @Override
    public String getCommand()
    {
        return command;
    }

    public List<Argument> getArguments()
    {
        return arguments;
    }
}
