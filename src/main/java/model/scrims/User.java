package model.scrims;

import java.util.Objects;

public class User
{
    private final String userId;
    private final String name;
    private String role;

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

    public void setRole(String role)
    {
        this.role = role;
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

    @Override
    public int hashCode()
    {
        return Objects.hash(userId);
    }
}
