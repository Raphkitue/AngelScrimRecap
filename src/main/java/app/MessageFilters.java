package app;

import static Util.MessageUtils.getServerIdFromMessage;

import discord4j.core.event.domain.message.MessageCreateEvent;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.installs.IInstallsRepository;
import repository.teams.ITeamsRepository;
import support.AngelBot;

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
        return event.getMessage().getChannelId()
            .asString().equals(
                installsRepo.getInstallForServer(getServerIdFromMessage(event)).getChannelId()
            );
    }

    public static boolean teamReadyFilter(MessageCreateEvent event)
    {
        return !teamsRepo.getTeamsForServer(getServerIdFromMessage(event)).isEmpty();
    }

}
