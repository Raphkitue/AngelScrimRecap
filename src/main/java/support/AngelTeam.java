package support;

import static Util.MessageUtils.commandMessage;
import static Util.MessageUtils.sendMessage;
import static model.commands.commands.Team.TEAMS_SHOW;
import static model.commands.commands.Team.TEAM_ADD_ROLE;
import static model.commands.commands.Team.TEAM_ADD_USER;
import static model.commands.commands.Team.TEAM_CLEAN;
import static model.commands.commands.Team.TEAM_CREATE;
import static model.commands.commands.Team.TEAM_DELETE;
import static model.commands.commands.Team.TEAM_REMOVE_USER;
import static model.commands.commands.Team.TEAM_RESET;
import static model.commands.commands.Team.TEAM_SET_CAPTAIN;

import app.DependenciesContainer;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Member;
import discord4j.core.retriever.EntityRetrievalStrategy;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import model.commands.Commander;
import model.commands.Commands;
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
        return commandMessage(event, TEAM_CREATE, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            String teamname = Commander.getMandatoryArgument(command, e.getMessage(), "teamname");
            if (teamname == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "team_put_name");
                return;
            }

            if (teamsRepository.createTeam(new Team(serverId, teamname)))
            { sendMessage(e.getMessage().getChannel(), serverId, "team_created", teamname); }
            else
            { sendMessage(e.getMessage().getChannel(), serverId, "team_already_exists", teamname); }

        });
    }

    public static Mono<Void> onTeamDelete(MessageCreateEvent event)
    {
        return commandMessage(event, TEAM_DELETE, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String teamname = Commander.getMandatoryArgument(command, e.getMessage(), "teamname");

            if (teamsRepository.deleteTeam(Team.getTeamId(teamname, serverId)))
            { sendMessage(e.getMessage().getChannel(), serverId, "team_deleted", teamname); }
            else
            { sendMessage(e.getMessage().getChannel(), serverId, "team_doesnt_exist", teamname); }
        });
    }

    public static Mono<Void> onTeamReset(MessageCreateEvent event)
    {
        return commandMessage(event, TEAM_RESET, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());

            team.getMembers().clear();

            teamsRepository.updateTeam(team);

            displayTeam(event, team);
        });
    }

    private static Team getTeamFromArgument(Commands command, String serverId, String message)
    {
        Team team;
        Optional<String> teamname = Commander.getArgument(command, message, "teamname");

        if (teamname.isPresent())
        { team = teamsRepository.getTeamById(Team.getTeamId(teamname.get(), serverId)); }
        else
        {
            List<Team> teams = teamsRepository.getTeamsForServer(serverId);
            if (teams.size() != 1)
            { team = null; }
            else
            { team = teams.get(0); }
        }
        return team;
    }

    private static void displayTeam(MessageCreateEvent event, Team team)
    {
        sendMessage(event.getMessage().getChannel(), team.getServerId(), "team_display", team.getName()
            , team.getMembers().stream().map(user -> user.getName() + ": " + user.getRole() + "\n").collect(Collectors.joining())
        );
    }

    public static Mono<Void> onTeamsShow(MessageCreateEvent event)
    {
        return commandMessage(event, TEAMS_SHOW, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());

            if (team == null)
            { sendMessage(e.getMessage().getChannel(), serverId, "team_not_found"); }
            else
            { displayTeam(event, team);}

        });
    }

    public static Mono<Void> onTeamAddRole(MessageCreateEvent event)
    {
        return commandMessage(event, TEAM_ADD_ROLE, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String roleId = Commander.getMandatoryArgument(command, e.getMessage(), "@rolename");

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());
            if (team == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "team_not_found");
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
        return commandMessage(event, TEAM_ADD_USER, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String username = Commander.getMandatoryArgument(command, e.getMessage(), "@username");

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());
            if (team == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "team_not_found");
                return;
            }

            Set<User> teamMembers = team.getMembers();

            e.getMessage()
                .getGuild()
                .flatMap(guild -> guild.getMemberById(Snowflake.of(username)))
                .doOnError(throwable -> sendMessage(event.getMessage().getChannel(), serverId, "incorrect_user_tag"))
                .onErrorStop()
                .flatMap(member -> Mono.just(teamMembers.add(new User(member.getId().asString(), member.getUsername(), "player"))))
                .block();

            teamsRepository.updateTeam(team);

            displayTeam(event, team);
        });
    }

    public static Mono<Void> onTeamClean(MessageCreateEvent event)
    {
        return commandMessage(event, TEAM_CLEAN, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());
            if (team == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "team_not_found");
                return;
            }

            Set<User> teamMembers = team.getMembers();
            Guild guild = e.getMessage()
                .getGuild(EntityRetrievalStrategy.REST).block();
            teamMembers = teamMembers.stream().filter(member -> {
                Optional<Member> block = guild.getMemberById(Snowflake.of(member.getUserId()))
                    .onErrorResume(elem -> Mono.empty())
                    .blockOptional();
                return block.isPresent();
            })
            .collect(Collectors.toSet());

            team.setMembers(teamMembers);

            teamsRepository.updateTeam(team);

            displayTeam(event, team);
        });
    }

    public static Mono<Void> onTeamRemoveUser(MessageCreateEvent event)
    {
        return commandMessage(event, TEAM_REMOVE_USER, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String username = Commander.getMandatoryArgument(command, e.getMessage(), "@username");

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());
            if (team == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "team_not_found");
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
        return commandMessage(event, TEAM_SET_CAPTAIN, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String userId = Commander.getMandatoryArgument(command, e.getMessage(), "@username");

            Team team = getTeamFromArgument(command, serverId, e.getMessage().getContent());
            if (team == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "team_not_found");
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
