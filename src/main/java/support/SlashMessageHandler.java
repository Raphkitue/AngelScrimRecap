package support;

import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import exceptions.MissingArgumentException;
import model.commands.Commands;

public interface SlashMessageHandler
{
    void accept(Commands commands, SlashCommandEvent messageCreateEvent);
}
