package app;

import static util.MessageUtils.getServerIdFromMessage;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.installs.IInstallsRepository;
import repository.teams.ITeamsRepository;

public class MessageFilters
{

    private static final IInstallsRepository installsRepo = DependenciesContainer.getInstance().getInstallsRepo();
    private static final ITeamsRepository teamsRepo = DependenciesContainer.getInstance().getTeamsRepo();

    private static final Logger log = Loggers.getLogger(MessageFilters.class);

    public static boolean installedFilter(MessageCreateEvent event)
    {
        return installsRepo.installExists(getServerIdFromMessage(event));
    }

    public static boolean inCorrectChannelFilter(MessageCreateEvent event)
    {
        return installedFilter(event) && event.getMessage().getChannelId()
            .asString().equals(
                installsRepo.getInstallForServer(getServerIdFromMessage(event)).getChannelId()
            );
    }

    public static boolean captainFilter(MessageCreateEvent event)
    {
        return teamsRepo.getTeamsForServer(event.getGuildId().get().asString())
            .stream()
            .flatMap(team -> team.getMembers().stream())
            .filter(user -> user.getUserId().equals(event.getMessage().getAuthor().get().getId().asString()))
            .anyMatch(user -> user.getRole().equals("captain"));
    }

    public static boolean teamReadyFilter(MessageCreateEvent event)
    {
        return !teamsRepo.getTeamsForServer(getServerIdFromMessage(event)).isEmpty();
    }

    public static boolean teamFilter(MessageCreateEvent event)
    {
        return teamsRepo.getTeamsForServer(getServerIdFromMessage(event)).stream()
            .flatMap(team -> team.getMembers().stream())
            .anyMatch(user -> user.getUserId().equals(event.getMessage().getAuthor().get().getId().asString()));
    }

}
