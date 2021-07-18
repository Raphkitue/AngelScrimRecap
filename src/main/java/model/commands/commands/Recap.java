package model.commands.commands;

import java.util.Arrays;
import java.util.List;

import model.commands.Argument;
import model.commands.Commands;

public enum Recap implements Commands {
    RECAP_START("recap start", "addteamrole",
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    RECAP_ADD_LINE("recap add", "addteamrole",
        Argument.mandatory("username", "", Argument.ArgumentType.USERNAME),
        Argument.mandatory("teamname", "", Argument.ArgumentType.STRING)),
    RECAP_ADD_REPLAY("recap replay", "addteamrole",
        Argument.mandatory("replaycode", "", Argument.ArgumentType.STRING),
        Argument.mandatory("mapname", "", Argument.ArgumentType.SPECIFIC),
        Argument.optional("teamname", "", Argument.ArgumentType.STRING)),
    RECAP_FINISH("recap finish", "addteamrole",
        Argument.optional("teamname", "", Argument.ArgumentType.STRING));


    private final String command;
    private final String slashCommand;
    private final List<Argument> arguments;

    Recap(String command, String slashCommand, Argument... arguments) {
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
