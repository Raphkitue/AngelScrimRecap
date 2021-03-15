package repository.rankings.recap;

import java.util.Collection;
import model.rankings.Player;
import model.rankings.Rankings;

public interface IRankingsRepository
{
    Rankings getRanking(String channelId);
    Collection<Rankings> getRankings();
    Player getRankingsForServerPlayer(String channelId, String playername);

    void updateRankings(Rankings rankings);

    boolean rankingsExist(String channelId);

    void deleteRankings(String channelId);
}
