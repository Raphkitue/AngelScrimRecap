package repository.users;

import java.util.List;
import model.scrims.Team;
import model.scrims.User;

public interface IUsersRepository
{
    List<User> getUsers();
    List<User> getUsersByRole(String role);

    User getUserById(String userId);

    void updateUser(User user);

    void deleteUser(String userId);
}
