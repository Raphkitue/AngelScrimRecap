package Util;

import static Util.LocaleUtils.getLocaleString;

import com.google.common.base.Splitter;
import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;
import model.commands.Command;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class MessageUtils
{

    private static final Logger log = Loggers.getLogger(MessageUtils.class);

    public static Message sendMessage(Mono<MessageChannel> channel, String serverId, String ressourceId, String... args)
    {
        return getPartedMessage(getLocaleString(serverId, ressourceId, args))
            .map(msg -> channel.flatMap(
                e -> e.createMessage(msg)
            ).block())
            .reduce((first, second) -> second).orElse(null);

    }

    public static Message sendMessage(MessageChannel channel, String serverId, String ressourceId, String... args)
    {
        return getPartedMessage(getLocaleString(serverId, ressourceId, args))
            .map(msg -> channel.createMessage(msg).block())
            .reduce((first, second) -> second).orElse(null);

    }

    public static Message sendEmbed(MessageChannel channel, String serverId, Consumer<EmbedCreateSpec> embedSpecific, String ressourceId, String... args)
    {
        return getPartedMessage(getLocaleString(serverId, ressourceId, args))
            .map(msg -> channel.createEmbed(spec -> embedSpecific.accept(spec.setDescription(msg))).block())
            .reduce((first, second) -> second).orElse(null);

    }

    public static Stream<String> getPartedMessage(String message)
    {
        return StreamSupport.stream(Splitter.fixedLength(2000).split(message).spliterator(), false);
    }


    public static String getServerIdFromMessage(MessageCreateEvent messageCreateEvent)
    {
        return messageCreateEvent.getGuildId().orElse(Snowflake.of(0)).asString();
    }

    public static Mono<Void> commandMessage(MessageCreateEvent event, Command requiredCommand, BiConsumer<Command, MessageCreateEvent> consumer)
    {
        if (requiredCommand.validate(event.getMessage().getContent()))
        {
            log.debug("Parsing :" + event.getMessage().getContent());
            consumer.accept(requiredCommand, event);
        }
        return Mono.empty();
    }

}
