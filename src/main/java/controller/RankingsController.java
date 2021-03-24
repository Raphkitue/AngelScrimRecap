package controller;

import static Util.LocaleUtils.getLocaleString;
import static Util.MessageUtils.sendEmbed;

import app.DependenciesContainer;
import controller.view.IRankView;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.text.DateFormat;
import java.time.Duration;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import model.rankings.Player;
import model.rankings.Rankings;
import model.scrims.Team;
import net.owapi.IOWAPI;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import reactor.core.publisher.Mono;
import repository.rankings.recap.IRankingsRepository;
import repository.teams.ITeamsRepository;

public class RankingsController
{

    private static final IOWAPI owApi = DependenciesContainer.getInstance().getOwApi();

    private static final Map<String, Timer> systems = new HashMap<>();
    private static GatewayDiscordClient client;

    private static final String SCORE_DOWN = ":arrow_lower_right:";
    private static final String SCORE_UP = ":arrow_upper_right:";

    private static final IRankingsRepository rankingsRepo = DependenciesContainer.getInstance().getRankingsRepo();
    private static final ITeamsRepository teamsRepo = DependenciesContainer.getInstance().getTeamsRepo();

    public static void initialize(GatewayDiscordClient client)
    {
        RankingsController.client = client;
        rankingsRepo.getRankings().forEach(r -> createTask(r.getChannelId()));
    }

    public static void createTask(String channelId)
    {
        long firstStart = 5000;
        long period = Duration.ofMinutes(30).toMillis();

        systems.computeIfAbsent(channelId, k -> {
            Timer timerLeaderboard = new Timer();
            timerLeaderboard.schedule(new TimerTask()
            {
                @Override
                public void run()
                {
                    checkForChanges(channelId);
                }
            }, firstStart, period);
            return timerLeaderboard;
        });
    }

    private static void updateRankings(Rankings ranks)
    {
        //Update elos
        Map<String, Player> collect = ranks.getRanks()
            .stream().map(player -> {
                Quartet<Long, Long, Long, Long> playerElos = owApi.getPlayerElos(player.getBattletag());
                if (playerElos == null)
                {
                    player.setPrivate(true);
                    return player;
                }
                player.setPrivate(false);
                player.setTankElo(playerElos.getValue0() != 0 ? playerElos.getValue0() : player.getTankElo());
                player.setDamageElo(playerElos.getValue1() != 0 ? playerElos.getValue1() : player.getDamageElo());
                player.setSupportElo(playerElos.getValue2() != 0 ? playerElos.getValue2() : player.getSupportElo());
                player.setOpenQElo(playerElos.getValue3() != 0 ? playerElos.getValue3() : player.getOpenQElo());
                return player;
            }).collect(Collectors.toMap(Player::getBattletag, e -> e));

        ranks.setServerRanks(collect);
    }

    public static String eloProgressEmoji(long formerElo, long newElo)
    {
        return formerElo == newElo ? String.format("%04d", newElo) : (String.format("%04d", formerElo) + (newElo > formerElo ? SCORE_UP : SCORE_DOWN) + String.format("%04d", newElo));
    }

    public static void displayChanges(String serverId, Rankings rankings, Rankings formerRankings)
    {
        MessageChannel messageChannel = (MessageChannel) client.getChannelById(Snowflake.of(rankings.getChannelId())).block();

        if (messageChannel == null)
        {
            rankingsRepo.deleteRankings(rankings.getChannelId());
            return;
        }

        Rankings dayStartRankings = rankingsRepo.getDayRanking(rankings.getChannelId(), new Date());
        if (dayStartRankings == null)
        {
            dayStartRankings = formerRankings;
        }


        if (rankings.getLastMessageId() != null && !rankings.getLastMessageId().trim().isEmpty())
        {
            messageChannel.getMessageById(Snowflake.of(rankings.getLastMessageId()))
                .flatMap(Message::delete)
                .onErrorResume(elem -> Mono.empty())
                .block();
        }

        IRankView rankView = RankViewFactory.getRankView(rankings);
        List<String> collectNames = rankView.getMainLines(rankings, dayStartRankings);
        List<String> collectRanks = rankView.getSecondLines(rankings, dayStartRankings);

        List<Team> teamsForServer = teamsRepo.getTeamsForServer(serverId);
        List<Pair<String, Double>> teamElos = rankView.getTeamRank(teamsForServer, rankings);

        Consumer<EmbedCreateSpec> weekly = spec -> {
            spec.setTitle(getLocaleString(serverId, "stats_change", DateFormat.getDateInstance().format(new Date())));
            spec.setDescription(teamElos.stream().map(elem -> elem.getValue0() + ": " + elem.getValue1()).collect(Collectors.joining(" - ")));
            spec.setColor(Color.of(0x6CAEBE));

            IntStream.range(0, Math.min(collectNames.size(), 25))
                .mapToObj(i -> Pair.with((i + 1) + ": " + collectNames.get(i), collectRanks.get(i)))
                .forEach(pair -> spec.addField(pair.getValue0(), pair.getValue1(), false));
        };

        Message message = sendEmbed(messageChannel, weekly);
        rankings.setLastMessageId(message.getId().asString());
        rankingsRepo.updateRankings(rankings);
        rankingsRepo.updateDayRankings(dayStartRankings, new Date());

    }


    public static void displayScores(String serverId, String channel)
    {
        Rankings rankingsForServer = rankingsRepo.getRanking(channel);
        Rankings formerRankings = rankingsForServer.deepClone();

        updateRankings(rankingsForServer);

        displayChanges(serverId, rankingsForServer, formerRankings);

        rankingsRepo.updateRankings(rankingsForServer);
    }

    public static void checkForChanges(String channelId)
    {
        //Request for new elos
        Rankings rankingsForServer = rankingsRepo.getRanking(channelId);
        Rankings formerRankings = rankingsForServer.deepClone();

        IRankView rankView = RankViewFactory.getRankView(rankingsForServer);

        updateRankings(rankingsForServer);

        if (!rankView.displaysEquals(formerRankings, rankingsForServer))
        {
            // Check who changed
            displayChanges(channelId, rankingsForServer, formerRankings);
        }

        rankingsRepo.updateRankings(rankingsForServer);
    }

}
