package model.scrims.users;

public class User
{
    private final String userId;
    private final String name;
    private final String role;

    public User(String userId, String name, String role)
    {
        this.userId = userId;
        this.name = name;
        this.role = role;
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
}
