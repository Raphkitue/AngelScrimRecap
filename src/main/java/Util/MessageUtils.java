package Util;

import static Util.LocaleUtils.getLocaleString;

import com.google.common.base.Splitter;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import exceptions.MissingArgumentException;

import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import model.commands.Commander;
import model.commands.Commands;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import support.MessageHandler;
import support.SlashMessageHandler;

public class MessageUtils {

    private static final Logger log = Loggers.getLogger(MessageUtils.class);

    public static Message sendMessage(Mono<MessageChannel> channel, String serverId, String ressourceId, String... args) {
        return getPartedMessage(getLocaleString(serverId, ressourceId, args))
            .map(msg -> channel.flatMap(
                e -> e.createMessage(msg)
            ).block())
            .reduce((first, second) -> second).orElse(null);

    }

    public static Message sendMessage(MessageChannel channel, String serverId, String ressourceId, String... args) {
        return getPartedMessage(getLocaleString(serverId, ressourceId, args))
            .map(msg -> channel.createMessage(msg).block())
            .reduce((first, second) -> second).orElse(null);

    }

    public static Message sendEmbed(MessageChannel channel, Consumer<EmbedCreateSpec> embedSpecific) {
        return channel.createEmbed(embedSpecific).block();

    }

    public static Stream<String> getPartedMessage(String message) {
        return StreamSupport.stream(Splitter.fixedLength(2000).split(message).spliterator(), false);
    }


    public static String getServerIdFromMessage(MessageCreateEvent messageCreateEvent) {
        return messageCreateEvent.getGuildId().orElse(Snowflake.of(0)).asString();
    }

    public static Mono<Void> commandMessage(MessageCreateEvent event, Commands requiredCommand, MessageHandler consumer) {
        if (Commander.validate(requiredCommand, event.getMessage().getContent())) {
            log.debug("Parsing :" + event.getMessage().getContent());
            try {
                consumer.accept(requiredCommand, event);
            } catch (MissingArgumentException e) {
                event.getGuild()
                    .flatMap(guild -> guild.getChannelById(Snowflake.of(e.getChannelId())))
                    .filter(channel -> channel instanceof MessageChannel)
                    .flatMap(channel -> ((MessageChannel) channel).createMessage(getLocaleString(e.getServerId(), "missing_argument", e.getArgname())))
                    .block();
                return null;
            }
        }
        return Mono.empty();
    }

    public static Mono<Void> slashCommandMessage(SlashCommandEvent event, Commands requiredCommand, SlashMessageHandler consumer) {
        if (event.getCommandName().equals(requiredCommand.getSlashCommand())) {
            log.debug("Parsing :" + event.getCommandName());
            consumer.accept(requiredCommand, event);
        }
        return Mono.empty();
    }

}
