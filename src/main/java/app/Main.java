package app;

import static support.AngelScrim.readyHandler;

import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;
import support.AngelScrim;
import support.AngelScrim.GetMembers;
import support.EventHandler;

public class Main
{

    public static void main(String[] args)
    {
        GatewayDiscordClient client = DiscordClient.create(System.getenv("token")).login().block();

        List<EventHandler> eventHandlers = new ArrayList<>();

        eventHandlers.add(AngelScrim::logMessages);
        eventHandlers.add(AngelScrim::onSetupMessage);
        eventHandlers.add(AngelScrim::onScrimMessage);
        eventHandlers.add(AngelScrim::onHelp);

        assert client != null;

        Mono.when(
            readyHandler(client),
            commandHandler(client, eventHandlers))
            .block();
    }




    public static Mono<Void> commandHandler(GatewayDiscordClient client, List<EventHandler> eventHandlers)
    {

        return client.on(MessageCreateEvent.class,
            event -> id -> Mono.when(eventHandlers.stream()
                .map(handler -> handler.onMessageCreate(event))
                .collect(Collectors.toList())))
            .then();
    }
}

