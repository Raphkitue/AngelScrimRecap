package support;

import static Util.LocaleUtils.getLocaleString;
import static Util.MessageUtils.commandMessage;
import static Util.MessageUtils.messageStartsWith;
import static Util.MessageUtils.sendMessage;

import app.DependenciesContainer;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.GuildChannel;
import discord4j.core.object.reaction.ReactionEmoji;
import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import model.commands.Command;
import repository.installs.IInstallsRepository;
import model.Install;
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
        return commandMessage(event, Command.SETUP, (command, ev) -> {

            log.info("Creating channel for server");
            String serverId = event.getGuildId().get().asString();

            String channelId = command.getArgument(ev.getMessage().getContent(), "#channel").orElse(null);
            if (channelId != null)
            {
                GuildChannel channel = ev.getGuild().flatMap(guild -> guild.getChannelById(Snowflake.of(channelId))).block();
                log.info("Created set to ", channel.getName());
                Install installForServer = installsRepo.getInstallForServer(serverId);

                if (installForServer != null)
                { installForServer.setChannelId(channel.getId().asString()); } else
                { installsRepo.updateInstall(new Install(serverId, channel.getId().asString())); }

                sendMessage(ev.getMessage().getChannel(), getLocaleString(serverId,"channel_set_success", channel.getName()));
                return;
            }

            if (installsRepo.installExists(serverId))
            {
                sendMessage(ev.getMessage().getChannel(), getLocaleString(serverId,"channel_set_fail"));
                return;
            }

            event.getMessage().getGuild().flatMap(e -> {
                log.info("Created channel for server {}", e.getId().asString());
                return e.createTextChannel(spec ->
                    spec.setTopic("Scrim recaps")
                        .setName("angel-scrim-recap")
                        .setReason("New channel for Scrim")
                );
            })
                .flatMap(e -> {
                    log.info(getLocaleString(serverId,"channel_set_success", e.getId().asString()));
                    installsRepo.updateInstall(new Install(event.getGuildId().get().asString(), e.getId().asString()));
                    return Mono.empty();
                })
                .block();

        });
    }

    public static Mono<Void> onSetupVod(MessageCreateEvent event)
    {
        return setupSetting(event, Command.SETUP_VOD, "#channel", (Install::setVodId));
    }

    public static Mono<Void> onSetupLang(MessageCreateEvent event)
    {
        return setupSetting(event, Command.SETUP_LANGUAGE, "lang", (Install::setLang));
    }

    public static Mono<Void> onSetupDelay(MessageCreateEvent event)
    {
        return setupSetting(event, Command.SETUP_DELAY, "delay", (Install::setVoteDelay));
    }

    public static Mono<Void> onSetupRecap(MessageCreateEvent event)
    {
        return setupSetting(event, Command.SETUP_RECAP, "#channel", (Install::setRecapsId));
    }

    private static Mono<Void> setupSetting(MessageCreateEvent event, Command comm, String argname, BiConsumer<Install, String> action)
    {
        return commandMessage(event, comm, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String argument = command.getMandatoryArgument(e.getMessage().getContent(), argname);

            Install installForServer = installsRepo.getInstallForServer(serverId);
            if (installForServer == null)
            {
                sendMessage(e.getMessage().getChannel(), getLocaleString(installForServer, "server_not_setup"));
                return;
            }

            action.accept(installForServer, argument);

            installsRepo.updateInstall(installForServer);
            e.getMessage().addReaction(ReactionEmoji.unicode("✅")).block();
        });
    }

    public static Mono<Void> onHelp(MessageCreateEvent event)
    {
        return commandMessage(event, Command.HELP, (command, ev) -> {
            StringBuilder sb = new StringBuilder();

            sb.append("Here is the help for Angel bot: \n");
            sb.append("Call the bot with \"Angel,\" and add one of the following commands \n");

            Arrays.stream(Command.values())
                .forEach(e -> {
                    sb.append(" - ")
                        .append(e.getCommand());
                    e.getArguments().forEach(f -> sb.append(" ").append(f.toString()));
                    sb.append(": ")
                        .append(e.getDescription())
                        .append('\n');
                });

            sendMessage(event.getMessage().getChannel(), sb.toString());
        });

    }


}
