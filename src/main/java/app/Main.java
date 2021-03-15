package app;

import static support.AngelBot.readyHandler;

import com.sun.net.httpserver.HttpServer;
import controller.RankingsController;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import support.AngelBot;
import support.AngelCompetition;
import support.AngelRecap;
import support.AngelTeam;
import support.EventHandler;

public class Main
{

    private static final Logger log = Loggers.getLogger(Main.class);

    public static void main(String[] args) throws IOException
    {

        String token = System.getenv("DISCORD_TOKEN");
log.info(token);

        GatewayDiscordClient client = DiscordClient.create(token).login().block();

        List<EventHandler> eventHandlers = new ArrayList<>();
        List<EventHandler> teamCommands = new ArrayList<>();
        List<EventHandler> captainCommands = new ArrayList<>();
        List<EventHandler> channelCommands = new ArrayList<>();

        //eventHandlers.add(AngelBot::logMessages);
        eventHandlers.add(AngelBot::onSetupMessage);
        eventHandlers.add(AngelBot::onSetupDelay);
        eventHandlers.add(AngelBot::onSetupLang);
        eventHandlers.add(AngelBot::onHelp);
        teamCommands.add(AngelRecap::onRecapAddLine);
        teamCommands.add(AngelRecap::onRecapAddReplay);

        captainCommands.add(AngelRecap::onRecapStart);
        captainCommands.add(AngelRecap::onRecapFinish);

        channelCommands.add(AngelCompetition::onStartRankings);
        channelCommands.add(AngelCompetition::onRankingsConf);
        channelCommands.add(AngelCompetition::onRankingsRemove);
        eventHandlers.add(AngelCompetition::onRankingsEnroll);
        channelCommands.add(AngelCompetition::onRankingsDelete);
        channelCommands.add(AngelCompetition::onRankingsUpdate);

        channelCommands.add(AngelTeam::onSetupVod);
        channelCommands.add(AngelTeam::onSetupRecap);
        channelCommands.add(AngelTeam::onTeamCreate);
        channelCommands.add(AngelTeam::onTeamShowAll);
        channelCommands.add(AngelTeam::onTeamDelete);
        channelCommands.add(AngelTeam::onTeamClean);
        channelCommands.add(AngelTeam::onTeamReset);
        channelCommands.add(AngelTeam::onTeamSetCaptain);
        channelCommands.add(AngelTeam::onTeamsShow);
        eventHandlers.add(AngelTeam::onTeamRemoveUser);
        eventHandlers.add(AngelTeam::onTeamAddUser);
        channelCommands.add(AngelTeam::onTeamAddRole);


        //Ajouter elo moyen teams
        //Create a master leaderboard with merged leaderboards

        //Create annotation-based filtering

        //Announcement command
        //Better parse arguments
        //Add doc for command usage places


        assert client != null;

        RankingsController.initialize(client);

        Mono.when(
            readyHandler(client),
            commandHandler(client, eventHandlers),
            teamMemberMessages(client, teamCommands),
            correctChannelMessages(client, channelCommands),
            captainMessages(client, captainCommands)
        )
            .subscribe();

        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", 8001), 0);
        server.createContext("/", new DummyHandler());
        server.setExecutor(threadPoolExecutor);
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
        return baseCommandHandler(client, eventHandlers, MessageFilters::teamReadyFilter, MessageFilters::teamFilter);
    }
    public static Mono<Void> captainMessages(GatewayDiscordClient client, List<EventHandler> eventHandlers)
    {
        return baseCommandHandler(client, eventHandlers, MessageFilters::teamReadyFilter, MessageFilters::captainFilter);
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

