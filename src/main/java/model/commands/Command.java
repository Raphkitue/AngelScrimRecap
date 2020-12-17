package model.commands;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import model.commands.Argument.Necessity;
import reactor.util.Logger;
import reactor.util.Loggers;

public enum Command
{
    RECAP_START("recap start", "Starts a recap round for a scrim", Argument.optional("teamname")),
    RECAP_ADD_LINE("recap add", "Add recap for you (or a player if you're captain)", Argument.optional("@username")),
    RECAP_ADD_REPLAY("recap replay", "Add the code for a map played", Argument.mandatory("mapname"), Argument.mandatory("replaycode")),
    RECAP_FINISH("recap finish", "Ends a recap"),

    SETUP_VOD("setup vod", "Sets up a vod channel", Argument.mandatory("#channel")),
    SETUP_LANGUAGE("setup lang", "Sets up bot language", Argument.mandatory("lang")),
    SETUP_DELAY("setup vote", "Sets up vote delay in minutes", Argument.mandatory("delay")),
    SETUP_RECAP("setup recap", "Sets up a recap channel", Argument.mandatory("#channel")),
    SETUP("setup bot", "Initial bot setup with creation of a channel", Argument.optional("#channel")),

    TEAM_ADD_ROLE("team add role", "Add all the users of a group to a team", Argument.mandatory("@rolename"), Argument.optional("teamname")),
    TEAM_ADD_USER("team add player", "Add a player to a team", Argument.mandatory("@username"), Argument.optional("teamname")),
    TEAM_REMOVE_USER("team remove", "Remove a player from a team", Argument.mandatory("@username"), Argument.optional("teamname")),
    TEAM_SET_CAPTAIN("team set captain", "Sets the captain of a team", Argument.mandatory("@username"), Argument.optional("teamname")),
    TEAM_CREATE("team create", "Create a team", Argument.mandatory("teamname")),
    TEAM_DELETE("team delete", "Delete a team", Argument.mandatory("teamname")),
    TEAM_RESET("team reset", "Resets a team", Argument.mandatory("teamname")),
    TEAMS_SHOW("team show", "Shows teams compositions", Argument.optional("teamname")),

    HELP("help", "Prints this help");

    public static final List<String> PREFIXES = Arrays.asList("Angel,", "ag", "under");

    private static final Logger log = Loggers.getLogger(Command.class);

    private final String command;
    private final String description;
    private final List<Argument> arguments;

    Command(String command, String description, Argument... arguments)
    {
        this.command = command;
        this.description = description;
        this.arguments = Arrays.asList(arguments);
    }

    public boolean validate(String message)
    {
        return PREFIXES.stream().map(s -> s + " " + command).anyMatch(message::startsWith);
    }

    public String getCommand()
    {
        return command;
    }

    public String getDescription()
    {
        return description;
    }

    public String removeCommand(String message)
    {
        return PREFIXES.stream().map(s -> s + " " + command).filter(message::startsWith).findFirst().map(s -> message.replace(s, "")).orElse(message);
    }

    public String parseMention(String mention)
    {
        return mention.contains("<@!") ? mention.replace("<@!", "").replace(">", "") : parseRoleMention(mention);
    }

    public String parseChannel(String mention)
    {
        return mention.contains("<#") ? mention.replace("<#", "").replace(">", "") : null;
    }

    public String parseRoleMention(String mention)
    {
        return mention.contains("<@&") ? mention.replace("<@&", "").replace(">", "") : null;
    }

    public String parseArgument(String message, Argument arg)
    {
        List<String> args = Arrays.stream(removeCommand(message).split("\\s+"))
            .filter(s -> !s.isEmpty())
            .map(String::trim)
            .collect(Collectors.toList());

        for (int i = 0; i < arguments.size(); i++)
        {
            if (arguments.get(i).getName().equals(arg.getName()))
            {

                if (args.size() > i)
                {
                    if (arg.getName().contains("@"))
                    { return parseMention(args.get(i)); }
                    if (arg.getName().contains("#"))
                    { return parseChannel(args.get(i)); }

                    return args.get(i);
                }
                return null;
            }
        }
        return null;
    }

    public Optional<String> getArgument(String message, String argname)
    {
        return Optional.ofNullable(parseArgument(message, Argument.any(argname)));
    }

    public String getText(String message)
    {
        String args = removeCommand(message);

        Iterator<Argument> iterator = arguments.iterator();

        return Arrays.stream(args.split("\\s+"))
            .filter(s -> !s.isEmpty())
            .dropWhile(s -> {
                Argument arg;
                if (iterator.hasNext())
                { arg = iterator.next(); } else
                { return false; }
                return arg.getNecessity().equals(Necessity.MANDATORY)
                    || parseArgument(message, arg) != null;
            })
            .collect(Collectors.joining(" "));
    }

    public String getMandatoryArgument(String message, String argname)
    {
        return parseArgument(message, Argument.any(argname));
    }

    public List<Argument> getArguments()
    {
        return arguments;
    }
}
