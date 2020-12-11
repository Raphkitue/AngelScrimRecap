package model.scrims.teams;

import java.util.List;

public interface ITeamsRepository
{
    List<Team> getTeams();
    List<Team> getTeamsForServer(String serverId);

    Team getTeamById(String teamId);
    Team getTeamByName(String name);

    void updateTeam(Team team);

    void deleteTeam(String teamId);
}
