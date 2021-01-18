package repository.rankings.recap;

import java.util.Collection;
import model.rankings.Ranking;
import model.rankings.Rankings;

public interface IRankingsRepository
{
    Rankings getRankingsForServer(String serverId);
    Collection<Rankings> getRankings();
    Ranking getRankingsForServerPlayer(String serverId, String playername);

    void updateRankings(Rankings rankings);

    boolean rankingsExist(String serverid);

    void deleteRankings(String serverId);
}
