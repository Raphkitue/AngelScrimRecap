package Util;

import discord4j.common.util.Snowflake;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import reactor.core.publisher.Mono;

public class MessageUtils
{

    public static boolean messageStartsWith(MessageCreateEvent event, String command)
    {
        Message message = event.getMessage();
        String content = message.getContent();
        return content.startsWith(command);
    }

    public static void sendMessage(Mono<MessageChannel> channel, String message)
    {
        channel.flatMap(
            e -> e.createMessage(message)
        ).block();
    }

    public static String getServerIdFromMessage(MessageCreateEvent messageCreateEvent)
    {
        return messageCreateEvent.getGuildId().orElse(Snowflake.of(0)).asString();
    }
}
