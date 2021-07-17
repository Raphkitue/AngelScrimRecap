package support.interactions.handlers;

import discord4j.discordjson.json.InteractionResponseData;
import discord4j.rest.interaction.GuildInteraction;
import discord4j.rest.interaction.InteractionHandler;
import discord4j.rest.interaction.InteractionResponse;
import org.reactivestreams.Publisher;

public class NewLeaderboardInteration implements InteractionHandler {

    private final GuildInteraction interaction;

    public NewLeaderboardInteration(GuildInteraction interaction) {
        this.interaction = interaction;
    }

    @Override
    public InteractionResponseData response() {
        return null;
    }

    @Override
    public Publisher<?> onInteractionResponse(InteractionResponse response) {
        return null;
    }
}
