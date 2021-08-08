package model.commands;

import discord4j.rest.util.ApplicationCommandOptionType;

import java.util.Objects;

public class Argument
{
    public enum Necessity
    {
        OPTIONAL(false),
        MANDATORY(true);

        private boolean required;

        Necessity(boolean required) {
            this.required = required;
        }

        public boolean isRequired() {
            return required;
        }
    }

    public enum ArgumentType
    {
        BATTLETAG,
        EXISTING_RANKING,
        CHANNEL,
        USERNAME,
        SPECIFIC,
        STRING;
    }

    private final String name;
    private final String description;
    private Necessity necessity;
    private final ArgumentType argumentType;

    private Argument(String name, String description, ArgumentType type, Necessity necessity)
    {
        this.name = name;
        this.description = description;
        this.argumentType = type;
        this.necessity = necessity;
    }

    private Argument(String name, String description, ArgumentType type)
    {
        this.name = name;
        this.description = description;
        this.argumentType = type;
    }

    public static Argument optional(String name, String description, ArgumentType type)
    {
        return new Argument(name, description, type, Necessity.OPTIONAL);
    }

    public static Argument mandatory(String name, String description, ArgumentType type)
    {
        return new Argument(name, description, type, Necessity.MANDATORY);
    }

    public static Argument any(String name, String description, ArgumentType type)
    {
        return new Argument(name, description, type);
    }

    public String getDescription() {
        return description;
    }

    public ArgumentType getArgumentType()
    {
        return argumentType;
    }

    public String getName()
    {
        return name;
    }

    public Necessity getNecessity()
    {
        return necessity;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        { return true; }
        if (o == null || getClass() != o.getClass())
        { return false; }
        Argument argument = (Argument) o;
        return Objects.equals(name, argument.name);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(name);
    }

    @Override
    public String toString()
    {
        if(necessity.equals(Necessity.OPTIONAL))
            return "[" + name + "]";
        else
            return "<" + name + ">";
    }
}
