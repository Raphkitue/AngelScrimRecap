package app;

import static support.AngelBot.readyHandler;

import com.sun.net.httpserver.HttpServer;
import controller.RankingsController;
import discord4j.core.DiscordClient;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.interaction.SlashCommandEvent;
import discord4j.core.event.domain.message.MessageCreateEvent;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import discord4j.core.object.entity.ApplicationInfo;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import support.*;
import support.slashcommands.RankingsSlashHandlers;

public class Main
{

    private static final Logger log = Loggers.getLogger(Main.class);

    public static GatewayDiscordClient client;
    public static long appId;

    public static void main(String[] args) throws IOException
    {

        String token = System.getenv("DISCORD_TOKEN");
        client = DiscordClient.create(token).login().block();


        List<EventHandler> eventHandlers = new ArrayList<>();
        List<EventHandler> teamCommands = new ArrayList<>();
        List<EventHandler> captainCommands = new ArrayList<>();
        List<EventHandler> channelCommands = new ArrayList<>();

        //eventHandlers.add(AngelBot::logMessages);
        //eventHandlers.add(AngelCompetition::onDebug);
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
        eventHandlers.add(AngelTeam::onTeamSetBtag);
        channelCommands.add(AngelTeam::onTeamAddRole);

        List<SlashEventHandler> slashEvents = new LinkedList<>();

        slashEvents.add(RankingsSlashHandlers::onStart);
        slashEvents.add(RankingsSlashHandlers::onEnroll);
        slashEvents.add(RankingsSlashHandlers::onRemovePlayer);
        slashEvents.add(RankingsSlashHandlers::onDelete);
        slashEvents.add(RankingsSlashHandlers::onUpdate);
        slashEvents.add(RankingsSlashHandlers::onRename);

        //Ajouter elo moyen teams
        //Create a master leaderboard with merged leaderboards

        //Create annotation-based filtering

        //Announcement command
        //Better parse arguments
        //Add doc for command usage places


        assert client != null;
        //AngelCompetition.createCommands(client.getRestClient());
        Main.appId = client.getApplicationInfo().map(ApplicationInfo::getId).block().asLong();

        client.getRestClient().getApplicationService().getGlobalApplicationCommands(appId)
            .map(c -> client.getRestClient().getApplicationService().deleteGlobalApplicationCommand(appId, Long.parseLong(c.id())))
            .then()
            .block();

        AngelCompetition.createGlobalCommands(client.getRestClient());

        RankingsController.initialize(client);

        Mono.when(
            readyHandler(client),
            commandHandler(client, eventHandlers),
            teamMemberMessages(client, teamCommands),
            correctChannelMessages(client, channelCommands),
            captainMessages(client, captainCommands),
            guildSlashCommandHandler(client, slashEvents)
        )
            .subscribe();

        int port = System.getenv("API_PORT") != null ? Integer.parseInt(System.getenv("API_PORT")) : 80;
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(10);
        HttpServer server = HttpServer.create(new InetSocketAddress("0.0.0.0", port), 0);
        APIHandler apiHandler = new APIHandler();
        server.createContext("/", apiHandler.getBaseHandler());
        server.createContext("/settings", apiHandler.getPostRankingsFileHandler());
        server.createContext("/settings/get", apiHandler.getGetRankingsFileHandler());
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

    public static Mono<Void> guildSlashCommandHandler(GatewayDiscordClient client, List<SlashEventHandler> eventHandlers)
    {
        return client.on(SlashCommandEvent.class,
            event -> id -> Mono.when(eventHandlers.stream()
            .map(handler -> handler.onMessageCreate(event))
                .collect(Collectors.toList())
            ))
            .then();
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

