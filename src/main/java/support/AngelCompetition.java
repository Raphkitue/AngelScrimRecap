package support;

import static Util.MessageUtils.commandMessage;
import static Util.MessageUtils.sendEmbed;
import static Util.MessageUtils.sendMessage;

import app.DependenciesContainer;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.event.domain.message.MessageCreateEvent;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import java.time.Duration;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import model.Install;
import model.commands.Command;
import model.rankings.Ranking;
import model.rankings.Rankings;
import model.rankings.Roles;
import net.owapi.IOWAPI;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import reactor.core.publisher.Mono;
import reactor.util.Logger;
import reactor.util.Loggers;
import repository.installs.IInstallsRepository;
import repository.rankings.recap.IRankingsRepository;

public class AngelCompetition
{

    private static final Logger log = Loggers.getLogger(AngelCompetition.class);

    private static final IOWAPI owApi = DependenciesContainer.getInstance().getOwApi();
    private static final IInstallsRepository installsRepo = DependenciesContainer.getInstance().getInstallsRepo();
    private static final IRankingsRepository rankingsRepo = DependenciesContainer.getInstance().getRankingsRepo();

    private static final Map<String, Timer> systems = new HashMap<>();
    private static GatewayDiscordClient client;

    private static final String SCORE_DOWN = "\uD83E\uDC56";
    private static final String SCORE_UP = "\uD83E\uDC55";

    public static void initialize(GatewayDiscordClient client)
    {
        AngelCompetition.client = client;
        rankingsRepo.getRankings().forEach(r -> createTask(r.getServerId()));
    }

    private static void createTask(String serverId)
    {
        Timer timerLeaderboard = new Timer();
        long firstStart = 5000;
        long period = Duration.ofMinutes(30).toMillis();

        if (!systems.containsKey(serverId))
        {
            timerLeaderboard.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    checkForChanges(serverId);
                }
            }, firstStart + 10000, period);

            systems.put(serverId, timerLeaderboard);
        }
    }

    private static String compareElos(long formerElo, long newElo)
    {
        return formerElo == newElo ? String.valueOf(newElo) : ((newElo > formerElo ? SCORE_UP : SCORE_DOWN) + newElo);
    }

    private static void displayChanges(String channelId, String serverId, Rankings rankings, Rankings formerRankings)
    {
        MessageChannel messageChannel = (MessageChannel) client.getChannelById(Snowflake.of(channelId)).block();

        List<String> collectNames = rankings.getRanks().stream()
            .sorted(Comparator.comparingLong(Ranking::getMainRoleElo).reversed())
            .map(e -> Pair.with(e, formerRankings.getPlayerRanks(e.getBattletag()).orElse(new Ranking())))
            .map(e -> e.getValue0().getBattletag()
                + " (*" + compareElos(e.getValue1().getMainRoleElo(), e.getValue0().getMainRoleElo()) + "*) "
                + (e.getValue0().isPrivate() ? "PRIVATE PROFILE" : ""))
            .collect(Collectors.toList());

        List<String> collectRanks = rankings.getRanks().stream()
            .sorted(Comparator.comparingLong(Ranking::getMainRoleElo).reversed())
            .map(e -> Pair.with(e, formerRankings.getPlayerRanks(e.getBattletag()).orElse(new Ranking())))
            .map(e -> "Tank " + compareElos(e.getValue1().getTankElo(), e.getValue0().getTankElo())
                + ", Damage " + compareElos(e.getValue1().getDamageElo(), e.getValue0().getDamageElo())
                + ", Support " + compareElos(e.getValue1().getSupportElo(), e.getValue0().getSupportElo()))
            .collect(Collectors.toList());

        Consumer<EmbedCreateSpec> weekly = spec -> {
            spec.setTitle("Stats change");

            IntStream.range(0, Math.min(collectNames.size(), 25))
                .mapToObj(i -> Pair.with((i + 1) + ": " + collectNames.get(i), collectRanks.get(i)))
                .forEach(pair -> spec.addField(pair.getValue0(), pair.getValue1(), false));
        };

        sendEmbed(messageChannel, serverId, weekly, "ranking_system_weekly");
    }


    private static void displayScores(String serverId)
    {
        Install install = installsRepo.getInstallForServer(serverId);

        if (install.getRankingsId() == null || install.getRankingsId().isEmpty())
        {
            MessageChannel messageChannel = (MessageChannel) client.getChannelById(Snowflake.of(install.getChannelId())).block();
            sendMessage(messageChannel, install.getServerId(), "ranking_system_not_setup");
            return;
        }

        //Request for new elos
        Rankings rankingsForServer = rankingsRepo.getRankingsForServer(serverId);
        Rankings formerRankings = rankingsForServer.deepClone();

        updateRankings(rankingsForServer);

        // Check who changed
        displayChanges(install.getRankingsId(), serverId, rankingsForServer, formerRankings);

        rankingsRepo.updateRankings(rankingsForServer);
    }

    private static void checkForChanges(String serverId)
    {
        Install install = installsRepo.getInstallForServer(serverId);

        if (install.getRankingsId() == null || install.getRankingsId().isEmpty())
        {
            MessageChannel messageChannel = (MessageChannel) client.getChannelById(Snowflake.of(install.getChannelId())).block();
            sendMessage(messageChannel, install.getServerId(), "ranking_system_not_setup");
            return;
        }

        //Request for new elos
        Rankings rankingsForServer = rankingsRepo.getRankingsForServer(serverId);
        Rankings formerRankings = rankingsForServer.deepClone();

        updateRankings(rankingsForServer);

        if (!rankingsForServer.getUniqueStats().equals(formerRankings.getUniqueStats()))
        {
            // Check who changed
            displayChanges(install.getRankingsId(), serverId, rankingsForServer, formerRankings);
        }

        rankingsRepo.updateRankings(rankingsForServer);
    }

    private static void updateRankings(Rankings rankingsForServer)
    {
        //Update elos
        Map<String, Ranking> collect = rankingsForServer.getRanks()
            .stream().map(ranking -> {
                Quartet<Long, Long, Long, Long> playerElos = owApi.getPlayerElos(ranking.getBattletag());
                if (playerElos == null)
                {
                    ranking.setPrivate(true);
                    return ranking;
                }
                ranking.setPrivate(false);
                ranking.setTankElo(playerElos.getValue0() != 0 ? playerElos.getValue0() : ranking.getTankElo());
                ranking.setDamageElo(playerElos.getValue1() != 0 ? playerElos.getValue1() : ranking.getDamageElo());
                ranking.setSupportElo(playerElos.getValue2() != 0 ? playerElos.getValue2() : ranking.getSupportElo());
                ranking.setOpenQElo(playerElos.getValue3() != 0 ? playerElos.getValue3() : ranking.getOpenQElo());
                return ranking;
            }).collect(Collectors.toMap(Ranking::getBattletag, e -> e));

        rankingsForServer.setServerRanks(collect);
    }


    public static Mono<Void> onStartRankings(MessageCreateEvent event)
    {
        return commandMessage(event, Command.RANKINGS_START, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            //Start task for weekly summary + task to check for changes

            if (rankingsRepo.rankingsExist(serverId))
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_system_already_created");
            }

            Rankings rankings = new Rankings(serverId);
            rankingsRepo.updateRankings(rankings);

            createTask(serverId);

            StringBuilder sb = new StringBuilder();

            Arrays.stream(Command.values())
                .filter(f -> f.getCommand().contains("rankings"))
                .filter(f -> !f.equals(Command.RANKINGS_START))
                .forEach(f -> {
                    sb.append(" - ")
                        .append(f.getCommand());
                    f.getArguments().forEach(g -> sb.append(" ").append(g.toString()));
                    sb.append(": ")
                        .append(f.getDescription(serverId))
                        .append('\n');
                });

            sendMessage(event.getMessage().getChannel(), serverId, "ranking_system_enabled", sb.toString());

        });
    }

    public static Mono<Void> onRankingsEnroll(MessageCreateEvent event)
    {
        return commandMessage(event, Command.RANKINGS_ENROLL, (command, e) -> {
            String serverId = e.getGuildId().get().asString();
            String battletag = command.getMandatoryArgument(e.getMessage().getContent(), "battletag");
            String mainRole = command.getMandatoryArgument(e.getMessage().getContent(), "mainrole");

            if (Roles.from(mainRole) == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "invalid_role");
                return;
            }

            //Start task for weekly summary + task to check for changes
            if (!rankingsRepo.rankingsExist(serverId))
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_system_not_created");
                return;
            }

            if (owApi.getPlayerElos(battletag) == null)
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_system_incorrect_profile");
                return;
            }

            Rankings rankings = rankingsRepo.getRankingsForServer(serverId);
            Ranking ranking = new Ranking(battletag, mainRole, 0, 0, 0, 0, false);
            rankings.setRanking(ranking);

            rankingsRepo.updateRankings(rankings);

            sendMessage(event.getMessage().getChannel(), serverId, "ranking_system_player_enrolled", battletag);

        });
    }

    public static Mono<Void> onRankingsConf(MessageCreateEvent event)
    {
        return commandMessage(event, Command.RANKINGS_CONF, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

        });
    }

    public static Mono<Void> onRankingsUpdate(MessageCreateEvent event)
    {

        return commandMessage(event, Command.RANKINGS_UPDATE, (command, e) -> {
            String serverId = e.getGuildId().get().asString();

            if (!rankingsRepo.rankingsExist(serverId))
            {
                sendMessage(e.getMessage().getChannel(), serverId, "ranking_system_not_created");
                return;
            }

            displayScores(serverId);
            //displayWeeklyUpdate(serverId);
        });
    }


}
