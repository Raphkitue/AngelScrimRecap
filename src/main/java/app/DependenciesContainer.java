package app;

import model.installs.IInstallsRepository;
import model.installs.InMemoryInstalls;
import model.scrims.teams.ITeamsRepository;
import model.scrims.teams.InMemoryTeams;

public class DependenciesContainer
{
    private static final IInstallsRepository installsRepo = new InMemoryInstalls();
    private static final ITeamsRepository teamsRepo = new InMemoryTeams();

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
}
