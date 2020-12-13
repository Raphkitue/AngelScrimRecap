package Util;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.Channel;
import discord4j.core.object.entity.channel.MessageChannel;
import java.util.function.BiConsumer;
import model.commands.Command;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;

public class MessageUtils
{

    private static final Logger log = Loggers.getLogger(MessageUtils.class);

    public static boolean messageStartsWith(MessageCreateEvent event, String command)
    {
        Message message = event.getMessage();
        String content = message.getContent();
        return content.startsWith(command);
    }

    public static Message sendMessage(Mono<MessageChannel> channel, String message)
    {
        return channel.flatMap(
            e -> e.createMessage(message)
        ).block();
    }

    public static Message sendMessage(MessageChannel channel, String message)
    {
        return channel.createMessage(message).block();
    }


    public static String getServerIdFromMessage(MessageCreateEvent messageCreateEvent)
    {
        return messageCreateEvent.getGuildId().orElse(Snowflake.of(0)).asString();
    }

    public static Mono<Void> commandMessage(MessageCreateEvent event, Command requiredCommand, BiConsumer<Command, MessageCreateEvent> consumer)
    {
        String command = requiredCommand.getCommand();
        if (messageStartsWith(event, command))
        {
            log.info("Parsing :" + command);
            consumer.accept(requiredCommand, event);
        }
        return Mono.empty();
    }

}
