package model.scrims;

import Util.Jsonable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.json.simple.JSONObject;

public class Team implements Jsonable
{

    private String id;
    private String serverId;
    private String channelId;
    private String vodId;
    private String name;

    private Set<User> members;

    public Team(String serverId, String name, Set<User> members, String channelId, String vodId)
    {
        this.serverId = serverId;
        this.name = name;
        this.members = members;
        this.channelId = channelId;
        this.vodId = vodId;

        this.id = getTeamId(name, serverId);
    }

    public Team(String serverId, String name)
    {
        this.serverId = serverId;
        this.name = name;
        this.members = new HashSet<>();
        this.channelId = "";
        this.vodId = "";

        this.id = getTeamId(name, serverId);
    }

    public Team()
    {
    }

    public String getChannelId()
    {
        return channelId;
    }

    public void setRecapChannelId(String channelId)
    {
        this.channelId = channelId;
    }

    public String getVodId()
    {
        return vodId;
    }

    public void setVodId(String vodId)
    {
        this.vodId = vodId;
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

    public void setMembers(Set<User> members)
    {
        this.members = members;
    }

    public static String getTeamId(String name, String serverId)
    {return name + "-" + serverId;}


    @Override
    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", getId());
        jsonObject.put("serverId", getServerId());
        jsonObject.put("channelId", channelId);
        jsonObject.put("vodId", vodId);
        jsonObject.put("name", getName());
        jsonObject.put("members", getMembers().stream().map(User::toJson).collect(Collectors.toList()));
        return jsonObject;

    }

    @Override
    public Jsonable fromJson(JSONObject jsonObject)
    {
        id = (String) jsonObject.get("id");
        serverId = (String) jsonObject.get("serverId");
        channelId = (String) jsonObject.getOrDefault("channelId", "");
        vodId = (String) jsonObject.getOrDefault("vodId", "");
        name = (String) jsonObject.get("name");
        members = ((List<JSONObject>) jsonObject.get("members")).stream().map(j -> (User) new User().fromJson(j)).collect(Collectors.toSet());
        return this;
    }
}
