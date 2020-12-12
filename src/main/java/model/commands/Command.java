package model.commands;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import reactor.util.Logger;
import reactor.util.Loggers;

public enum Command

{
    RECAP_START("Angel, recap start", "Starts a recap round for a scrim", Argument.optional("teamname")),
    RECAP_ADD_LINE("Angel, recap player", "Add recap for you (or a player if you're captain", Argument.optional("@username")),
    RECAP_ADD_REPLAY("Angel, recap scrim replay", "Add the code for a map played", Argument.mandatory("mapname"), Argument.mandatory("replaycode")),
    RECAP_FINISH("Angel, recap finish", "Ends a recap", Argument.optional("teamname")),

    SETUP("Angel, setup", "Initial bot setup with creation of a channel"),

    TEAM_ADD_ROLE("Angel, team add role", "Add all the users of a group to a team", Argument.mandatory("rolename"), Argument.optional("teamname")),
    TEAM_ADD_USER("Angel, team add player", "Add a player to a team", Argument.mandatory("@username"), Argument.optional("teamname")),
    TEAM_REMOVE_USER("Angel, team remove", "Remove a player from a team", Argument.mandatory("@username"), Argument.optional("teamname")),
    TEAM_SET_CAPTAIN("Angel, team set captain", "Sets the captain of a team", Argument.mandatory("@username"), Argument.optional("teamname")),
    TEAM_CREATE("Angel, team create", "Create a team", Argument.mandatory("teamname")),
    TEAM_DELETE("Angel, team delete", "Delete a team", Argument.mandatory("teamname")),
    TEAM_RESET("Angel, team reset", "Resets a team", Argument.mandatory("teamname")),
    TEAMS_SHOW("Angel, team show", "Shows teams compositions", Argument.optional("teamname")),

    HELP("Angel, help", "Prints this help");

    private static final Logger log = Loggers.getLogger(Command.class);

    private final String command;
    private final String description;
    private final List<Argument> arguments;

    Command(String command, String description, Argument ...arguments)
    {
        this.command = command;
        this.description = description;
        this.arguments = Arrays.asList(arguments);
    }

    public String getCommand()
    {
        return command;
    }

    public String getDescription()
    {
        return description;
    }

    public String parseMention(String mention)
    {
        return mention.contains("<@!") ? mention.replace("<@!","").replace(">", "") : parseRoleMention(mention);
    }

    public String parseChannel(String mention)
    {
        return mention.contains("<#") ? mention.replace("<#","").replace(">", "") : null;
    }

    public String parseRoleMention(String mention)
    {
        return mention.contains("<@&") ?  mention.replace("<@&","").replace(">", "") : null;
    }

    public String parseArgument(String message, Argument arg)
    {
        List<String> args = Arrays.stream(message.replace(command, "").split("\\s+"))
            .filter(s -> !s.isEmpty())
            .map(String::trim)
            .collect(Collectors.toList());


        for (int i = 0; i < arguments.size(); i++)
        {
            if (arguments.get(i).getName().equals(arg.getName())){

                if(args.size() > i)
                {
                    if (arg.getName().contains("@"))
                        return parseMention(args.get(i));
                    if (arg.getName().contains("#"))
                        return parseChannel(args.get(i));

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

    public String getMandatoryArgument(String message, String argname)
    {
       return parseArgument(message, Argument.any(argname));
    }

    public List<Argument> getArguments()
    {
        return arguments;
    }
}
