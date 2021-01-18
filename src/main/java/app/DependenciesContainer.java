package app;

import net.owapi.IOWAPI;
import net.owapi.OwApiCom;
import repository.installs.IInstallsRepository;
import repository.installs.JSONInstalls;
import repository.rankings.recap.IRankingsRepository;
import repository.rankings.recap.JSONRankings;
import repository.recap.IRecapRepository;
import repository.recap.JSONRecaps;
import repository.teams.ITeamsRepository;
import repository.teams.JSONTeams;

public class DependenciesContainer
{

    private static final IInstallsRepository installsRepo = new JSONInstalls("installs.json");
    private static final ITeamsRepository teamsRepo = new JSONTeams("teams.json");

    private static final IRankingsRepository rankingsRepo = new JSONRankings("rankings.json");

    private static final IRecapRepository recapsRepo = new JSONRecaps("recaps.json");

    private static final IOWAPI owApi = new OwApiCom();

    private static final DependenciesContainer instance = new DependenciesContainer();

    public static DependenciesContainer getInstance()
    {
        return instance;
    }

    private DependenciesContainer()
    {
    }

    public IOWAPI getOwApi()
    {
        return owApi;
    }

    public IRankingsRepository getRankingsRepo()
    {
        return rankingsRepo;
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
