package repository.rankings.recap;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import discord4j.core.GatewayDiscordClient;
import model.rankings.Rankings;
import org.javatuples.Pair;

public interface IRankingsRepository {
    Rankings getRanking(String channelId);

    Rankings getDayRanking(String channelId, Date date);

    Collection<Rankings> getRankings();

    List<Pair<String, String>> getRankingsForServer(String guildId);


    void addRankings(Rankings rankings, String serverId, String channelName);

    void updateRankings(Rankings rankings);
    void updateServs(GatewayDiscordClient client);

    void updateDayRankings(Rankings rankings, Date date);

    boolean rankingsExist(String channelId);

    void deleteRankings(String channelId);
}
