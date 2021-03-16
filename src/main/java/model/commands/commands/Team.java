package model.commands.commands;

import java.util.Arrays;
import java.util.List;
import model.commands.Argument;
import model.commands.Commands;

public enum Team implements Commands
{
    TEAM_ADD_ROLE("team add role", Argument.mandatory("@rolename"), Argument.optional("teamname")),
    TEAM_ADD_USER("team add player", Argument.mandatory("@username"), Argument.optional("teamname")),
    TEAM_REMOVE_USER("team remove", Argument.mandatory("@username"), Argument.optional("teamname")),
    TEAM_CLEAN("team clean", Argument.optional("teamname")),
    TEAM_SET_CAPTAIN("team set captain", Argument.mandatory("@username"), Argument.optional("teamname")),
    TEAM_SET_BTAG("team set btag", Argument.mandatory("@username"), Argument.mandatory("battletag"), Argument.optional("teamname")),
    TEAM_CREATE("team create", Argument.mandatory("teamname")),
    TEAM_DELETE("team delete", Argument.mandatory("teamname")),
    TEAM_RESET("team reset", Argument.mandatory("teamname")),
    SETUP_VOD("setup vod", Argument.mandatory("#channel"), Argument.optional("teamname")),
    SETUP_RECAP("setup recap", Argument.mandatory("#channel"), Argument.optional("teamname")),
    TEAMS_SHOW("team show", Argument.optional("teamname")),
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
