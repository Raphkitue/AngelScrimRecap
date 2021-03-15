package model.commands.commands;

import java.util.Arrays;
import java.util.List;
import model.commands.Argument;
import model.commands.Commands;

public enum Setup implements Commands
{
    SETUP_VOD("setup vod", Argument.mandatory("#channel")),
    SETUP_LANGUAGE("setup lang", Argument.mandatory("lang")),
    SETUP_DELAY("setup vote", Argument.mandatory("delay")),
    SETUP_RECAP("setup recap", Argument.mandatory("#channel")),
    SETUP_RANKINGS("setup rankings", Argument.mandatory("#channel")),
    SETUP("setup bot", Argument.optional("#channel")),
    HELP("help", Argument.optional("category"));

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
