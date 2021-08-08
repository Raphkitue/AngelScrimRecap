package model.commands.commands;

import java.util.Arrays;
import java.util.List;
import model.commands.Argument;
import model.commands.Commands;

public enum Setup implements Commands
{
    SETUP_LANGUAGE("setup lang","lang",
        Argument.mandatory("lang", "", Argument.ArgumentType.SPECIFIC)),
    SETUP_DELAY("setup vote","setupdelay",
        Argument.mandatory("delay", "", Argument.ArgumentType.STRING)),
    SETUP("setup bot","setupchannel",
        Argument.optional("channel", "", Argument.ArgumentType.CHANNEL)),
    HELP("help","help",
        Argument.optional("category", "", Argument.ArgumentType.SPECIFIC));


    private final String command;
    private final String slashCommand;
    private final List<Argument> arguments;

    Setup(String command, String slashCommand, Argument... arguments) {
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
