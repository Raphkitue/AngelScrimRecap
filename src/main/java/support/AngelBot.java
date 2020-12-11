package support;

import static Util.MessageUtils.messageStartsWith;
import static Util.MessageUtils.sendMessage;

import app.DependenciesContainer;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import model.CommandsRegistry;
import model.installs.IInstallsRepository;
import model.installs.Install;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class AngelBot
{

    private static final IInstallsRepository installsRepo = DependenciesContainer.getInstance().getInstallsRepo();

    private static final Logger log = Loggers.getLogger(AngelBot.class);

    public static Mono<Void> readyHandler(GatewayDiscordClient client)
    {
        return client.on(ReadyEvent.class)
            .doOnNext(ready -> log.info("Logged in as {}", ready.getSelf().getUsername()))
            .then();
    }

    public static Mono<Void> logMessages(MessageCreateEvent event)
    {
        log.info(event.getMessage().getContent());
        return Mono.empty();
    }

    public static Mono<Void> onSetupMessage(MessageCreateEvent event)
    {
        String command = CommandsRegistry.SETUP.getCommand();
        if (messageStartsWith(event, command))
        {
            log.info("Creating channel for server");

            if (installsRepo.installExists(event.getGuildId().get().asString()))
            { return Mono.empty(); }

            event.getMessage().getGuild().flatMap(e -> {
                log.info("Created channel for server {}", e.getId().asString());
                return e.createTextChannel(spec ->
                    spec.setTopic("Scrim recaps")
                        .setName("angel-scrim-recap")
                        .setReason("New channel for Scrim")
                );
            })
                .flatMap(e -> {
                    log.info("Created channel for server {}", e.getId().asString());
                    installsRepo.updateInstall(new Install(event.getGuildId().get().asString(), e.getId().asString()));
                    return Mono.empty();
                })
                .block();

        }
        return Mono.empty();
    }

    public static Mono<Void> onHelp(MessageCreateEvent event)
    {
        String command = CommandsRegistry.HELP.getCommand();
        log.info(command);
        if (messageStartsWith(event, command))
        {
            StringBuilder sb = new StringBuilder();

            sb.append("Here is the help for Angel bot: \n");
            sb.append("Call the bot with \"Angel,\" and add one of the following commands \n");

            Arrays.stream(CommandsRegistry.values())
                .forEach(e -> sb.append(" - ").append(e.getCommand()).append(": " ).append(e.getDescription()).append('\n'));

            sendMessage(event.getMessage().getChannel(), sb.toString());
        }
        return Mono.empty();
    }


}
