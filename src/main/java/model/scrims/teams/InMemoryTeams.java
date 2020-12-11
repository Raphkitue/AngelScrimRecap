package model.scrims.teams;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class InMemoryTeams implements ITeamsRepository
{

    Map<String, Team> teams = new HashMap<>();

    @Override
    public List<Team> getTeams()
    {
        return new ArrayList<>(teams.values());
    }

    @Override
    public List<Team> getTeamsForServer(String serverId)
    {
        return teams.values().stream()
            .filter(team -> team.getServerId().equals(serverId))
            .collect(Collectors.toList());
    }

    @Override
    public Team getTeamById(String teamId)
    {
        return teams.getOrDefault(teamId, null);
    }

    @Override
    public Team getTeamByName(String name)
    {
        return teams.values().stream()
            .filter(team -> team.getName().equals(name))
            .findFirst()
            .orElse(null);
    }

    @Override
    public void updateTeam(Team team)
    {
        teams.put(team.getId(), team);
    }

    @Override
    public void deleteTeam(String teamId)
    {
        teams.remove(teamId);
    }
}
