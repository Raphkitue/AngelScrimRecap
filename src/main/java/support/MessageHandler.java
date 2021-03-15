package support;

import discord4j.core.event.domain.message.MessageCreateEvent;
import exceptions.MissingArgumentException;
import model.commands.Commands;

public interface MessageHandler
{
    void accept(Commands commands, MessageCreateEvent messageCreateEvent) throws MissingArgumentException;
}
