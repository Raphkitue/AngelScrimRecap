package support.interactions.handlers;

import discord4j.discordjson.json.InteractionApplicationCommandCallbackData;
import discord4j.discordjson.json.InteractionResponseData;
import discord4j.rest.interaction.GuildInteraction;
import discord4j.rest.interaction.InteractionHandler;
import discord4j.rest.interaction.InteractionResponse;
import org.reactivestreams.Publisher;
import reactor.util.Logger;
import reactor.util.Loggers;
import support.AngelCompetition;

public class NewLeaderboardInteration implements InteractionHandler {

    private final GuildInteraction interaction;
    private static final Logger log = Loggers.getLogger(NewLeaderboardInteration.class);

    public NewLeaderboardInteration(GuildInteraction interaction) {
        this.interaction = interaction;
        log.info("Yes");
    }

    @Override
    public InteractionResponseData response() {
        log.info("We are answering");
        return InteractionResponseData.builder().data(InteractionApplicationCommandCallbackData.builder()
        .content("Oui super").build()).build();
    }

    @Override
    public Publisher<?> onInteractionResponse(InteractionResponse response) {
        log.info("AH oui oui uoi");
        return null;
    }
}
