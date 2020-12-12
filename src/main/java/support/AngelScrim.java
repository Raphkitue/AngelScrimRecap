package support;

import static Util.MessageUtils.messageStartsWith;

import app.DependenciesContainer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import model.commands.Command;
import repository.installs.IInstallsRepository;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class AngelScrim
{
    private static final IInstallsRepository installsRepo = DependenciesContainer.getInstance().getInstallsRepo();

    private static final Logger log = Loggers.getLogger(AngelScrim.class);

    public static Mono<Void> onScrimMessage(MessageCreateEvent event)
    {
        String command = Command.RECAP_START.getCommand();
        if (messageStartsWith(event, command))
        {
            log.info(command);

        }
        return Mono.empty();
    }
}
