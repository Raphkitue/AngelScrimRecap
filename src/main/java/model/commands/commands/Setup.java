package model.commands.commands;

import java.util.Arrays;
import java.util.List;
import model.commands.Argument;
import model.commands.Commands;

public enum Setup implements Commands
{
    SETUP_LANGUAGE("setup lang",
        Argument.mandatory("lang", "", Argument.ArgumentType.SPECIFIC)),
    SETUP_DELAY("setup vote",
        Argument.mandatory("delay", "", Argument.ArgumentType.STRING)),
    SETUP("setup bot",
        Argument.optional("channel", "", Argument.ArgumentType.CHANNEL)),
    HELP("help",
        Argument.optional("category", "", Argument.ArgumentType.SPECIFIC));

    private final String command;
    private final List<Argument> arguments;

    Setup(String command, Argument... arguments)
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
