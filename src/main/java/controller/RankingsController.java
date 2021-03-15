package controller;

import static Util.LocaleUtils.getLocaleString;
import static Util.MessageUtils.sendEmbed;
import static Util.MessageUtils.sendMessage;

import app.DependenciesContainer;
import controller.view.IRankView;
import discord4j.common.util.Snowflake;
import discord4j.core.GatewayDiscordClient;
import discord4j.core.object.entity.Message;
import discord4j.core.object.entity.channel.MessageChannel;
import discord4j.core.spec.EmbedCreateSpec;
import discord4j.rest.util.Color;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import model.Install;
import model.rankings.Player;
import model.rankings.Rankings;
import net.owapi.IOWAPI;
import org.javatuples.Pair;
import org.javatuples.Quartet;
import reactor.core.publisher.Mono;
import repository.installs.IInstallsRepository;
import repository.rankings.recap.IRankingsRepository;

public class RankingsController
{

    private static final IOWAPI owApi = DependenciesContainer.getInstance().getOwApi();

    private static final Map<String, Timer> systems = new HashMap<>();
    private static GatewayDiscordClient client;

    private static final String SCORE_DOWN = ":arrow_lower_right:";
    private static final String SCORE_UP = ":arrow_upper_right:";

    private static final IInstallsRepository installsRepo = DependenciesContainer.getInstance().getInstallsRepo();
    private static final IRankingsRepository rankingsRepo = DependenciesContainer.getInstance().getRankingsRepo();

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
        return formerElo == newElo ? String.valueOf(newElo) : ((newElo > formerElo ? SCORE_UP : SCORE_DOWN) + newElo);
    }

    public static void displayChanges(String channelId, String serverId, Rankings rankings, Rankings formerRankings)
    {
        MessageChannel messageChannel = (MessageChannel) client.getChannelById(Snowflake.of(channelId)).block();

        if (messageChannel == null) {
            rankingsRepo.deleteRankings(channelId);
            return;
        }

        if (rankings.getLastMessageId() != null && !rankings.getLastMessageId().trim().isEmpty()){
            messageChannel.getMessageById(Snowflake.of(rankings.getLastMessageId()))
                .flatMap(Message::delete)
                .onErrorResume(elem -> Mono.empty())
                .block();
        }

        IRankView rankView = RankViewFactory.getRankView(rankings);
        List<String> collectNames = rankView.getMainLines(rankings, formerRankings);
        List<String> collectRanks = rankView.getSecondLines(rankings, formerRankings);


        Consumer<EmbedCreateSpec> weekly = spec -> {
            spec.setTitle(getLocaleString(serverId, "stats_change"));
            //spec.setDescription("team rankings");
            spec.setColor(Color.of(0x6CAEBE));

            IntStream.range(0, Math.min(collectNames.size(), 25))
                .mapToObj(i -> Pair.with((i + 1) + ": " + collectNames.get(i), collectRanks.get(i)))
                .forEach(pair -> spec.addField(pair.getValue0(), pair.getValue1(), false));
        };

        Message message = sendEmbed(messageChannel, weekly);
        rankings.setLastMessageId(message.getId().asString());
        rankingsRepo.updateRankings(rankings);

    }


    public static void displayScores(String serverId, Rankings rankingsForServer)
    {
        Rankings formerRankings = rankingsForServer.deepClone();

        updateRankings(rankingsForServer);

        // Check who changed
        displayChanges(rankingsForServer.getChannelId(), serverId, rankingsForServer, formerRankings);

        rankingsRepo.updateRankings(rankingsForServer);
    }

    public static void checkForChanges(String channelId)
    {
        Install install = installsRepo.getInstallForServer(channelId);

        if (install.getRankingsId() == null || install.getRankingsId().isEmpty())
        {
            MessageChannel messageChannel = (MessageChannel) client.getChannelById(Snowflake.of(install.getChannelId())).block();
            sendMessage(messageChannel, install.getServerId(), "ranking_system_not_setup");
            return;
        }

        //Request for new elos
        Rankings rankingsForServer = rankingsRepo.getRanking(channelId);
        Rankings formerRankings = rankingsForServer.deepClone();

        IRankView rankView = RankViewFactory.getRankView(rankingsForServer);

        updateRankings(rankingsForServer);

        if (!rankView.displaysEquals(formerRankings, rankingsForServer))
        {
            // Check who changed
            displayChanges(install.getRankingsId(), channelId, rankingsForServer, formerRankings);
        }

        rankingsRepo.updateRankings(rankingsForServer);
    }

}
