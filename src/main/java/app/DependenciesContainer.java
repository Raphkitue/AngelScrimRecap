package app;

import repository.installs.IInstallsRepository;
import repository.installs.JSONInstalls;
import repository.recap.IRecapRepository;
import repository.recap.JSONRecaps;
import repository.teams.ITeamsRepository;
import repository.teams.JSONTeams;

public class DependenciesContainer
{

    private static final IInstallsRepository installsRepo = new JSONInstalls("installs.json");
    private static final ITeamsRepository teamsRepo = new JSONTeams("teams.json");

    private static final IRecapRepository recapsRepo = new JSONRecaps("recaps.json");

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

    public IRecapRepository getRecapsRepo()
    {
        return recapsRepo;
    }
}
