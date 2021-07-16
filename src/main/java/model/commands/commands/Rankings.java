package model.commands.commands;

import java.util.Arrays;
import java.util.List;
import model.commands.Argument;
import model.commands.Commands;

public enum Rankings implements Commands
{
    DEBUG("debug", "marc"),
    RANKINGS_START("rankings create", "newlead", Argument.mandatory("channel")),
    RANKINGS_ENROLL("rankings enroll", "enroll", Argument.mandatory("#channel"), Argument.mandatory("battletag"), Argument.mandatory("mainrole")),
    RANKINGS_DELETE("rankings delete", "delete", Argument.mandatory("#channel"), Argument.mandatory("battletag")),
    RANKINGS_REMOVE("rankings remove", "removelead", Argument.mandatory("channel")),
    RANKINGS_CONF("rankings conf", "configure", Argument.mandatory("#channel"), Argument.mandatory("confmode")),
    RANKINGS_UPDATE("rankings update", "updateLeaderboard", Argument.mandatory("#channel"));


    private final String command;
    private final String slashCommand;
    private final List<Argument> arguments;

    Rankings(String command, String slashCommand, Argument... arguments)
    {
        this.command = command;
        this.slashCommand = slashCommand;
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

    public String getSlashCommand() {
        return slashCommand;
    }

    public List<Argument> getArguments()
    {
        return arguments;
    }
}
