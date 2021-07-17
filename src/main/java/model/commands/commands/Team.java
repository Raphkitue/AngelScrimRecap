package model.commands.commands;

import java.util.Arrays;
import java.util.List;
import model.commands.Argument;
import model.commands.Commands;

public enum Team implements Commands
{
    TEAM_ADD_ROLE("team add role",
        Argument.mandatory("rolename", "", Argument.ArgumentType.USERNAME),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_ADD_USER("team add player",
        Argument.mandatory("username", "", Argument.ArgumentType.USERNAME),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_REMOVE_USER("team remove",
        Argument.mandatory("username", "", Argument.ArgumentType.USERNAME),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_CLEAN("team clean",
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_SET_CAPTAIN("team set captain",
        Argument.mandatory("username", "", Argument.ArgumentType.USERNAME),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_SET_BTAG("team set btag",
        Argument.mandatory("username", "", Argument.ArgumentType.USERNAME),
        Argument.mandatory("battletag", "", Argument.ArgumentType.STRING),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_CREATE("team create",
        Argument.mandatory("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_DELETE("team delete",
        Argument.mandatory("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_RESET("team reset",
        Argument.mandatory("teamname", "", Argument.ArgumentType.STRING)),
    SETUP_VOD("setup vod",
        Argument.mandatory("channel", "", Argument.ArgumentType.CHANNEL),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    SETUP_RECAP("setup recap",
        Argument.mandatory("channel", "", Argument.ArgumentType.CHANNEL),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAMS_SHOW("team show",
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAMS_SHOW_NAMES("teams show");


    private final String command;
    private final List<Argument> arguments;

    Team(String command, Argument... arguments)
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
