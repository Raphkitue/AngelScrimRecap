package app;

import net.owapi.IOWAPI;
import net.owapi.OwApiCom;
import repository.installs.IInstallsRepository;
import repository.installs.JSONInstalls;
import repository.rankings.IRankingsRepository;
import repository.rankings.JSONRankings;
import repository.recap.IRecapRepository;
import repository.recap.JSONRecaps;
import repository.teams.ITeamsRepository;
import repository.teams.JSONTeams;

public class DependenciesContainer
{

    private final IInstallsRepository installsRepo;
    private final ITeamsRepository teamsRepo;

    private final IRankingsRepository rankingsRepo;

    private final IRecapRepository recapsRepo ;

    private final IOWAPI owApi;

    private static final DependenciesContainer instance = new DependenciesContainer();

    public static DependenciesContainer getInstance()
    {
        return instance;
    }

    private DependenciesContainer()
    {
        String rootFolder = System.getenv("PERSISTENCE_ROOT");
        installsRepo = new JSONInstalls(rootFolder + "installs.json");
        teamsRepo = new JSONTeams(rootFolder + "teams.json");
        rankingsRepo = new JSONRankings(rootFolder + "rankings.json");
        recapsRepo = new JSONRecaps(rootFolder + "recaps.json");
        owApi = new OwApiCom();
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
