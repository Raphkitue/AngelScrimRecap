package model.scrims;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class Team
{
    private final String id;
    private final String serverId;
    private final String name;

    private final Set<User> members;

    public Team(String serverId, String name, Set<User> members)
    {
        this.serverId = serverId;
        this.name = name;
        this.members = members;

        this.id = getTeamId(name, serverId);
    }
    public Team(String serverId, String name)
    {
        this.serverId = serverId;
        this.name = name;
        this.members = new HashSet<>();

        this.id = getTeamId(name, serverId);
    }

    public String getId()
    {
        return id;
    }

    public String getServerId()
    {
        return serverId;
    }

    public String getName()
    {
        return name;
    }

    public Set<User> getMembers()
    {
        return members;
    }

    public static String getTeamId(String name, String serverId)
    {return name + "-" + serverId;}

}
