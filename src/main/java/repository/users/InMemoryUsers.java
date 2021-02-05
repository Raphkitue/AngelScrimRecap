package repository.users;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import model.scrims.Team;
import model.scrims.User;

public class InMemoryUsers implements IUsersRepository
{

    Map<String, User> users = new HashMap<>();


    @Override
    public List<User> getUsers()
    {
        return new ArrayList<>(users.values());
    }

    @Override
    public List<User> getUsersByRole(String role)
    {
        return users.values().stream()
            .filter(e -> e.getRole().equals(role))
            .collect(Collectors.toList());
    }

    @Override
    public User getUserById(String userId)
    {
        return users.get(userId);
    }

    @Override
    public void updateUser(User user)
    {
        users.put(user.getUserId(), user);
    }

    @Override
    public void deleteUser(String userId)
    {
        users.remove(userId);
    }
}
