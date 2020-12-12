package app;

import repository.installs.IInstallsRepository;
import repository.installs.JSONInstalls;
import repository.teams.ITeamsRepository;
import repository.teams.InMemoryTeams;
import repository.teams.JSONTeams;
import repository.users.IUsersRepository;
import repository.users.InMemoryUsers;

public class DependenciesContainer
{

    private static final IInstallsRepository installsRepo = new JSONInstalls("installs.json");
    private static final ITeamsRepository teamsRepo = new JSONTeams("teams.json");
    private static final IUsersRepository usersRepo = new InMemoryUsers();

    private static final DependenciesContainer instance = new DependenciesContainer();

    public static DependenciesContainer getInstance()
    {
        return instance;
    }

    private DependenciesContainer()
    {
    }

    public IInstallsRepository getInstallsRepo()
    {
        return installsRepo;
    }

    public ITeamsRepository getTeamsRepo()
    {
        return teamsRepo;
    }

    public static IUsersRepository getUsersRepo()
    {
        return usersRepo;
    }
}
