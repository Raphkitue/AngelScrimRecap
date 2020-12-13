package support;

import static Util.MessageUtils.commandMessage;
import static Util.MessageUtils.sendMessage;

import app.DependenciesContainer;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.retriever.EntityRetrievalStrategy;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import model.commands.Command;
import model.scrims.Team;
import model.scrims.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.teams.ITeamsRepository;

public class AngelTeam
{

    private static final ITeamsRepository teamsRepository = DependenciesContainer.getInstance().getTeamsRepo();

    private static final Logger log = Loggers.getLogger(AngelTeam.class);

    public static Mono<Void> onTeamCreate(MessageCreateEvent event)
    {
        return commandMessage(event, Command.TEAM_CREATE, (command, e) -> {
            String teamname = command.getMandatoryArgument(e.getMessage().getContent(), "teamname");
            if (teamname == null)
            {
                sendMessage(e.getMessage().getChannel(), "Please put a team name");
                return;
            }

            if (teamsRepository.createTeam(new Team(e.getGuildId().get().asString(), teamname)))
            { sendMessage(e.getMessage().getChannel(), "Team " + teamname + " created !"); } else
            { sendMessage(e.getMessage().getChannel(), "Team " + teamname + " already existing on this server !"); }

        });
    }

    public static Mono<Void> onTeamDelete(MessageCreateEvent event)
    {
        return commandMessage(event, Command.TEAM_DELETE, (command, e) -> {
            String teamname = command.getMandatoryArgument(e.getMessage().getContent(), "teamname");

            if (teamsRepository.deleteTeam(Team.getTeamId(teamname, e.getGuildId().get().asString())))
            { sendMessage(e.getMessage().getChannel(), "Team " + teamname + " deleted !"); } else
            { sendMessage(e.getMessage().getChannel(), "Team " + teamname + " didn't exist on this server !"); }
        });
    }

    public static Mono<Void> onTeamReset(MessageCreateEvent event)
    {
        return commandMessage(event, Command.TEAM_RESET, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());

            team.getMembers().clear();

            teamsRepository.updateTeam(team);

            displayTeam(event, team);
        });
    }

    private static Team getTeamFromArgument(Command command, String serverId, String message)
    {
        Team team;
        Optional<String> teamname = command.getArgument(message, "teamname");

        if (teamname.isPresent())
        { team = teamsRepository.getTeamById(Team.getTeamId(teamname.get(), serverId)); } else
        {
            List<Team> teams = teamsRepository.getTeamsForServer(serverId);
            if (teams.size() != 1)
            { team = null; } else
            { team = teams.get(0); }
        }
        return team;
    }

    private static void displayTeam(MessageCreateEvent event, Team team)
    {
        sendMessage(event.getMessage().getChannel(), "Team "
            + team.getName()
            + ":\n"
            + team.getMembers().stream().map(user -> user.getName() + ": " + user.getRole() + "\n").collect(Collectors.joining())
        );
    }

    public static Mono<Void> onTeamsShow(MessageCreateEvent event)
    {
        return commandMessage(event, Command.TEAMS_SHOW, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());

            if (team == null)
            { sendMessage(e.getMessage().getChannel(), "Team couldn't be found or there are multiple teams registered"); } else
            { displayTeam(event, team);}

        });
    }

    public static Mono<Void> onTeamAddRole(MessageCreateEvent event)
    {
        return commandMessage(event, Command.TEAM_ADD_ROLE, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String roleId = command.getMandatoryArgument(e.getMessage().getContent(), "@rolename");

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());
            if (team == null)
            {
                sendMessage(e.getMessage().getChannel(), "Team couldn't be found or there are multiple teams registered");
                return;
            }

            Set<User> teamMembers = team.getMembers();

            e.getMessage()
                .getGuild()
                .flatMapMany(guild -> guild.getMembers(EntityRetrievalStrategy.REST))
                .filter(member -> member.getRoles(EntityRetrievalStrategy.REST).any(role -> role.getId().asString().equals(roleId)).block())
                .flatMap(member -> Mono.just(teamMembers.add(new User(member.getId().asString(), member.getUsername(), "player"))))
                .blockLast();

            teamsRepository.updateTeam(team);

            displayTeam(event, team);
        });
    }

    public static Mono<Void> onTeamAddUser(MessageCreateEvent event)
    {
        return commandMessage(event, Command.TEAM_ADD_USER, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String username = command.getMandatoryArgument(e.getMessage().getContent(), "@username");

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());
            if (team == null)
            {
                sendMessage(e.getMessage().getChannel(), "Team couldn't be found or there are multiple teams registered");
                return;
            }

            Set<User> teamMembers = team.getMembers();

            e.getMessage()
                .getGuild()
                .flatMap(guild -> guild.getMemberById(Snowflake.of(username)))
                .doOnError(throwable -> sendMessage(event.getMessage().getChannel(), "Username is incorrectly tagged"))
                .onErrorStop()
                .flatMap(member -> Mono.just(teamMembers.add(new User(member.getId().asString(), member.getUsername(), "player"))))
                .block();

            teamsRepository.updateTeam(team);

            displayTeam(event, team);
        });
    }

    public static Mono<Void> onTeamRemoveUser(MessageCreateEvent event)
    {
        return commandMessage(event, Command.TEAM_REMOVE_USER, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String username = command.getMandatoryArgument(e.getMessage().getContent(), "@username");

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());
            if (team == null)
            {
                sendMessage(e.getMessage().getChannel(), "Team couldn't be found or there are multiple teams registered");
                return;
            }

            Set<User> teamMembers = team.getMembers();

            e.getMessage()
                .getGuild()
                .flatMap(guild -> guild.getMemberById(Snowflake.of(username)))
                .flatMap(member -> Mono.just(teamMembers.remove(new User(member.getId().asString(), member.getUsername(), "player"))))
                .block();

            teamsRepository.updateTeam(team);

            displayTeam(event, team);
        });
    }

    public static Mono<Void> onTeamSetCaptain(MessageCreateEvent event)
    {
        return commandMessage(event, Command.TEAM_SET_CAPTAIN, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String userId = command.getMandatoryArgument(e.getMessage().getContent(), "@username");

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());
            if (team == null)
            {
                sendMessage(e.getMessage().getChannel(), "Team couldn't be found or there are multiple teams registered");
                return;
            }

            Set<User> teamMembers = team.getMembers();

            Flux.fromIterable(teamMembers)
                .filter(user -> user.getUserId().equals(userId))
                .doOnNext(user -> user.setRole("captain"))
                .subscribe();

            teamsRepository.updateTeam(team);

            displayTeam(event, team);
        });
    }



}
