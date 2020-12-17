package Util;

import static Util.LocaleUtils.getLocaleString;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import java.util.function.BiConsumer;
import model.commands.Command;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class MessageUtils
{

    private static final Logger log = Loggers.getLogger(MessageUtils.class);

    public static Message sendMessage(Mono<MessageChannel> channel, String serverId, String ressourceId, String ...args)
    {
        return channel.flatMap(
            e -> e.createMessage(getLocaleString(serverId, ressourceId, args))
        ).block();
    }

    public static Message sendMessage(MessageChannel channel, String serverId, String ressourceId, String ...args)
    {
        return channel.createMessage(getLocaleString(serverId, ressourceId, args)).block();
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
