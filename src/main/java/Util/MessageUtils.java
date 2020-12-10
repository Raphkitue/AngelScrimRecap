package Util;

import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.Message;

public class MessageUtils
{

    public static boolean messageStartsWith(MessageCreateEvent event, String command)
    {
        Message message = event.getMessage();
        String content = message.getContent();
        return content.startsWith(command);
    }
}
