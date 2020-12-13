package app;

import static support.AngelBot.readyHandler;

import com.sun.net.httpserver.HttpServer;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;
import support.AngelBot;
import support.AngelScrim;
import support.AngelTeam;
import support.EventHandler;

public class Main
{
    public static void main(String[] args) throws IOException
    {
        GatewayDiscordClient client = DiscordClient.create(System.getProperty("token")).login().block();


        List<EventHandler> eventHandlers = new ArrayList<>();
        List<EventHandler> teamCommands = new ArrayList<>();
        List<EventHandler> channelCommands = new ArrayList<>();

        eventHandlers.add(AngelBot::logMessages);
        eventHandlers.add(AngelBot::onSetupMessage);
        eventHandlers.add(AngelBot::onSetupVod);
        eventHandlers.add(AngelBot::onSetupRecap);
        eventHandlers.add(AngelBot::onHelp);

        teamCommands.add(AngelScrim::onScrimStart);
        teamCommands.add(AngelScrim::onRecapAddLine);
        teamCommands.add(AngelScrim::onRecapAddReplay);
        teamCommands.add(AngelScrim::onRecapFinish);

        channelCommands.add(AngelTeam::onTeamCreate);
        channelCommands.add(AngelTeam::onTeamDelete);
        channelCommands.add(AngelTeam::onTeamAddRole);
        channelCommands.add(AngelTeam::onTeamAddUser);
        channelCommands.add(AngelTeam::onTeamReset);
        channelCommands.add(AngelTeam::onTeamRemoveUser);
        channelCommands.add(AngelTeam::onTeamSetCaptain);
        channelCommands.add(AngelTeam::onTeamsShow);

        assert client != null;

        Mono.when(
            readyHandler(client),
            commandHandler(client, eventHandlers),
            teamMemberMessages(client, teamCommands),
            correctChannelMessages(client, channelCommands)
        )
            .subscribe();

        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8001), 0);
        server.start();
    }


    public static Mono<Void> commandHandler(GatewayDiscordClient client, List<EventHandler> eventHandlers)
    {
        return client.on(MessageCreateEvent.class,
            event -> id -> Mono.when(eventHandlers.stream()
                .map(handler -> handler.onMessageCreate(event))
                .collect(Collectors.toList())))
            .then();
    }

    public static Mono<Void> setupCommandHandler(GatewayDiscordClient client, List<EventHandler> eventHandlers)
    {
        return baseCommandHandler(client, eventHandlers, MessageFilters::installedFilter);
    }

    public static Mono<Void> correctChannelMessages(GatewayDiscordClient client, List<EventHandler> eventHandlers)
    {
        return baseCommandHandler(client, eventHandlers, MessageFilters::inCorrectChannelFilter);
    }

    public static Mono<Void> teamMemberMessages(GatewayDiscordClient client, List<EventHandler> eventHandlers)
    {
        return baseCommandHandler(client, eventHandlers, MessageFilters::inCorrectChannelFilter, MessageFilters::teamReadyFilter, MessageFilters::teamFilter);
    }


    @SafeVarargs
    public static Mono<Void> baseCommandHandler(GatewayDiscordClient client, List<EventHandler> eventHandlers, Predicate<MessageCreateEvent> ...predicates)
    {

        return client.on(MessageCreateEvent.class,
            event -> id -> Mono.when(eventHandlers.stream()
                .filter(e -> Arrays.stream(predicates).allMatch(p -> p.test(event)))
                .map(handler -> handler.onMessageCreate(event))
                .collect(Collectors.toList())))
            .then();
    }
}

