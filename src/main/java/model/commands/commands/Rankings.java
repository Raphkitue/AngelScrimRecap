package model.commands.commands;

import java.util.Arrays;
import java.util.List;

import model.commands.Argument;
import model.commands.Commands;

public enum Rankings implements Commands {
    DEBUG("debug", "marc"),
    RANKINGS_START("rankings create", "newlead",
        Argument.mandatory("channel", "Placeholder", Argument.ArgumentType.CHANNEL),
        Argument.optional("name", "Placeholder", Argument.ArgumentType.STRING)),
    RANKINGS_ENROLL("rankings enroll", "enroll",
        Argument.mandatory("channel", "Placeholder", Argument.ArgumentType.EXISTING_RANKING),
        Argument.mandatory("battletag", "Placeholder", Argument.ArgumentType.STRING),
        Argument.mandatory("mainrole", "Placeholder", Argument.ArgumentType.SPECIFIC)),
    RANKINGS_DELETE("rankings delete", "delete",
        Argument.mandatory("channel", "Placeholder", Argument.ArgumentType.EXISTING_RANKING),
        Argument.mandatory("battletag", "Placeholder", Argument.ArgumentType.STRING)),
    RANKINGS_REMOVE("rankings remove", "removelead",
        Argument.mandatory("channel", "Placeholder", Argument.ArgumentType.EXISTING_RANKING)),
    RANKINGS_CONF("rankings conf", "configure",
        Argument.mandatory("channel", "Placeholder", Argument.ArgumentType.EXISTING_RANKING),
        Argument.mandatory("confmode", "Placeholder", Argument.ArgumentType.SPECIFIC)),
    RANKINGS_UPDATE("rankings update", "updatelead",
        Argument.mandatory("channel", "Placeholder", Argument.ArgumentType.EXISTING_RANKING)),
    RANKINGS_RENAME("rankings rename", "renamelead",
        Argument.mandatory("channel", "Placeholder", Argument.ArgumentType.EXISTING_RANKING),
        Argument.mandatory("name", "Placeholder", Argument.ArgumentType.STRING));


    private final String command;
    private final String slashCommand;
    private final List<Argument> arguments;

    Rankings(String command, String slashCommand, Argument... arguments) {
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
