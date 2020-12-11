package model.scrims.teams;

import model.scrims.users.User;
import java.util.List;

public class Team
{
    private final String id;
    private final String serverId;
    private final String name;

    private final List<User> members;

    public Team(String id, String serverId, String name, List<User> members)
    {
        this.id = id;
        this.serverId = serverId;
        this.name = name;
        this.members = members;
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

    public List<User> getMembers()
    {
        return members;
    }
}
