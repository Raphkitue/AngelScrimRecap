package model.commands;

import discord4j.common.util.Snowflake;
import discord4j.core.object.entity.Message;
import exceptions.MissingArgumentException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import model.commands.Argument.Necessity;
import reactor.util.Logger;
import reactor.util.Loggers;

public class Commander
{

    public static final List<String> PREFIXES = Arrays.asList("Angel,", "ag", "under");

    private static final Logger log = Loggers.getLogger(Commander.class);

    private Commander() { }

    public static boolean validate(Commands command, String message)
    {
        return PREFIXES.stream().map(s -> s + " " + command.getCommand()).anyMatch(message::startsWith);
    }


    public static String removeCommand(Commands command, String message)
    {
        return PREFIXES.stream().map(s -> s + " " + command.getCommand()).filter(message::startsWith).findFirst().map(s -> message.replace(s, "")).orElse(message);
    }

    public static String parseMention(String mention)
    {
        return mention.contains("<@!") ? mention.replace("<@!", "").replace(">", "") : parseRoleMention(mention);
    }

    public static String parseChannel(String mention)
    {
        return mention.contains("<#") ? mention.replace("<#", "").replace(">", "") : null;
    }

    public static String parseRoleMention(String mention)
    {
        return mention.contains("<@&") ? mention.replace("<@&", "").replace(">", "") : null;
    }

    public static String parseArgument(Commands command, String message, Argument arg)
    {
        List<String> args = Arrays.stream(removeCommand(command, message).split("\\s+"))
            .filter(s -> !s.isEmpty())
            .map(String::trim)
            .collect(Collectors.toList());

        for (int i = 0; i < command.getArguments().size(); i++)
        {
            if (command.getArguments().get(i).getName().equals(arg.getName()))
            {
                if (args.size() > i)
                {
                    if (arg.getName().contains("@"))
                    { return parseMention(args.get(i)); }
                    if (arg.getName().contains("#"))
                    { return parseChannel(args.get(i)); }
                    if (arg.getName().equals("mapname")){
                        return args.stream().skip(i).collect(Collectors.joining(" "));
                    }

                    return args.get(i);
                }
                return null;
            }
        }
        return null;
    }

    public static Optional<String> getArgument(Commands command, String message, String argname)
    {
        return Optional.ofNullable(parseArgument(command, message, Argument.any(argname, "", Argument.ArgumentType.STRING)));
    }

    public static String getText(Commands command, String message)
    {
        String args = removeCommand(command, message);

        Iterator<Argument> iterator = command.getArguments().iterator();

        return Arrays.stream(args.split("\\s+"))
            .filter(s -> !s.isEmpty())
            .dropWhile(s -> {
                Argument arg;
                if (iterator.hasNext())
                { arg = iterator.next(); }
                else
                { return false; }
                return arg.getNecessity().equals(Necessity.MANDATORY)
                    || parseArgument(command, message, arg) != null;
            })
            .collect(Collectors.joining(" "));
    }

    public static String getMandatoryArgument(Commands command, Message message, String argname) throws MissingArgumentException
    {
        String s = parseArgument(command, message.getContent(), Argument.any(argname, "", Argument.ArgumentType.STRING));
        if (s == null || s.isEmpty()){
            throw new MissingArgumentException("missing arg" + argname, argname, message.getChannelId().asString(), message.getGuildId().orElse(Snowflake.of(0)).asString());
        }
        return s;
    }
}
