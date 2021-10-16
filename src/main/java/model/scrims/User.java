package model.scrims;

import util.Jsonable;
import java.util.Objects;
import org.json.simple.JSONObject;

public class User implements Jsonable
{

    private String userId;
    private String name;
    private String role;
    private String battletag;

    public User(String userId, String name, String role)
    {
        this.userId = userId;
        this.name = name;
        this.role = role;
    }

    public String getBattletag()
    {
        return battletag;
    }

    public void setBattletag(String battletag)
    {
        this.battletag = battletag;
    }

    public String getUserId()
    {
        return userId;
    }

    public String getName()
    {
        return name;
    }

    public String getRole()
    {
        return role;
    }

    public void setRole(String role)
    {
        this.role = role;
    }


    public void setName(String name)
    {
        this.name = name;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        { return true; }
        if (o == null || getClass() != o.getClass())
        { return false; }
        User user = (User) o;
        return Objects.equals(userId, user.userId);
    }

    public User()
    {
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(userId);
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", getUserId());
        jsonObject.put("name", getName());
        jsonObject.put("role", getRole());
        jsonObject.put("battletag", battletag);
        return jsonObject;
    }

    @Override
    public Jsonable fromJson(JSONObject j)
    {
        userId = (String) j.get("id");
        name = (String) j.get("name");
        role = (String) j.get("role");
        battletag = (String) j.getOrDefault("battletag", "");
        return this;
    }
}
