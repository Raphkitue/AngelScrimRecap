package support;

import static Util.MessageUtils.messageStartsWith;
import static Util.MessageUtils.sendMessage;

import app.DependenciesContainer;
import discord4j.core.event.domain.message.MessageCreateEvent;
import model.CommandsRegistry;
import model.installs.IInstallsRepository;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class AngelScrim
{
    private static final IInstallsRepository installsRepo = DependenciesContainer.getInstance().getInstallsRepo();

    private static final Logger log = Loggers.getLogger(AngelScrim.class);

    public static Mono<Void> onScrimMessage(MessageCreateEvent event)
    {
        String command = CommandsRegistry.RECAP_START.getCommand();
        log.info(command);
        if (messageStartsWith(event, command))
        {

        }
        return Mono.empty();
    }
}
