package repository.teams;

import java.util.List;
import model.scrims.Team;

public interface ITeamsRepository
{
    List<Team> getTeams();
    List<Team> getTeamsForServer(String serverId);

    Team getTeamById(String teamId);
    Team getTeamByName(String name);

    void updateTeam(Team team);

    boolean createTeam(Team team);

    boolean deleteTeam(String teamId);
}
