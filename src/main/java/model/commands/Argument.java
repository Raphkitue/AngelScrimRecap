package model.commands;

import java.util.Objects;

public class Argument
{
    public enum Necessity
    {
        OPTIONAL,
        MANDATORY;
    }

    private String name;
    private Necessity necessity;

    private Argument(String name, Necessity necessity)
    {
        this.name = name;
        this.necessity = necessity;
    }

    private Argument(String name)
    {
        this.name = name;
    }

    public static Argument optional(String name)
    {
        return new Argument(name, Necessity.OPTIONAL);
    }

    public static Argument mandatory(String name)
    {
        return new Argument(name, Necessity.MANDATORY);
    }

    public static Argument any(String name)
    {
        return new Argument(name);
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
