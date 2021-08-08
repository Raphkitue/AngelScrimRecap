package model.commands.commands;

import java.util.Arrays;
import java.util.List;

import model.commands.Argument;
import model.commands.Commands;

public enum Team implements Commands {
    TEAM_ADD_ROLE("team add role", "addteamrole",
        Argument.mandatory("rolename", "", Argument.ArgumentType.USERNAME),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_ADD_USER("team add player", "addteamrole",
        Argument.mandatory("username", "", Argument.ArgumentType.USERNAME),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_REMOVE_USER("team remove", "addteamrole",
        Argument.mandatory("username", "", Argument.ArgumentType.USERNAME),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_CLEAN("team clean", "addteamrole",
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_SET_CAPTAIN("team set captain", "addteamrole",
        Argument.mandatory("username", "", Argument.ArgumentType.USERNAME),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_SET_BTAG("team set btag", "addteamrole",
        Argument.mandatory("username", "", Argument.ArgumentType.USERNAME),
        Argument.mandatory("battletag", "", Argument.ArgumentType.STRING),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_CREATE("team create", "addteamrole",
        Argument.mandatory("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_DELETE("team delete", "addteamrole",
        Argument.mandatory("teamname", "", Argument.ArgumentType.STRING)),
    TEAM_RESET("team reset", "addteamrole",
        Argument.mandatory("teamname", "", Argument.ArgumentType.STRING)),
    SETUP_VOD("setup vod", "addteamrole",
        Argument.mandatory("channel", "", Argument.ArgumentType.CHANNEL),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    SETUP_RECAP("setup recap", "addteamrole",
        Argument.mandatory("channel", "", Argument.ArgumentType.CHANNEL),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAMS_SHOW("team show", "addteamrole",
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    TEAMS_SHOW_NAMES("teams show", "addteamrole");


    private final String command;
    private final String slashCommand;
    private final List<Argument> arguments;

    Team(String command, String slashCommand, Argument... arguments) {
        this.command = command;
        this.slashCommand = slashCommand;
        this.arguments = Arrays.asList(arguments);
    }

    @Override
    public String getName() {
        return super.name();
    }

    @Override
    public String getCommand() {
        return command;
    }

    @Override
    public String getSlashCommand() {
        return slashCommand;
    }

    @Override
    public List<Argument> getArguments() {
        return arguments;
    }

    @Override
    public Argument getArgument(String name) {
        return arguments.stream().filter(e -> e.getName().equals(name)).findFirst().orElse(null);
    }
}
