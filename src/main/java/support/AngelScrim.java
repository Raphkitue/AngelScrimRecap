package support;

import static Util.MessageUtils.messageStartsWith;
import static discord4j.core.object.entity.channel.Channel.Type.GUILD_TEXT;

import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.lifecycle.ReadyEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Guild;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.User;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.object.presence.Presence;
import discord4j.discordjson.json.ApplicationInfoData;
import discord4j.discordjson.json.ChannelCreateRequest;
import discord4j.discordjson.json.ImmutableChannelCreateRequest;
import discord4j.discordjson.json.MessageCreateRequest;
import discord4j.discordjson.json.OverwriteData;
import discord4j.discordjson.json.gateway.ChannelCreate;
import discord4j.discordjson.possible.Possible;
import io.netty.channel.Channel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import model.CommandsRegistry;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.Logger;
import reactor.util.Loggers;

public class AngelScrim
{

    private static final Map<String, String> guildChannels = new HashMap<>();

    private static final Logger log = Loggers.getLogger(AngelScrim.class);

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

            if (guildChannels.containsKey(event.getGuildId().get().asString()))
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
                    guildChannels.put(event.getGuildId().get().asString(), e.getId().asString());
                    return Mono.empty();
                })
                .block();

        }
        return Mono.empty();
    }

    public static void sendMessage(Mono<MessageChannel> channel, String message)
    {
        channel.flatMap(
            e -> e.createMessage(message)
        ).block();
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

    public static Mono<Void> onScrimMessage(MessageCreateEvent event)
    {
        String command = CommandsRegistry.RECAP_START.getCommand();
        log.info(command);
        if (messageStartsWith(event, command))
        {

            if (!guildChannels.containsKey(event.getGuildId().get().asString()))
            {
                sendMessage(event.getMessage().getChannel(), "Please setup the bot first, see Angel, help");
                return Mono.empty();
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
                    log.info("Created channel for server {}", e.getId().asString());
                    guildChannels.put(event.getGuildId().get().asString(), e.getId().asString());
                    return Mono.empty();
                })
                .block();

        }
        return Mono.empty();
    }
}
