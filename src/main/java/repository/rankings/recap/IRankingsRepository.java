package repository.rankings.recap;

import java.util.Collection;
import java.util.Date;
import model.rankings.Player;
import model.rankings.Rankings;

public interface IRankingsRepository
{
    Rankings getRanking(String channelId);
    Rankings getDayRanking(String channelId, Date date);
    Collection<Rankings> getRankings();
    Player getRankingsForServerPlayer(String channelId, String playername);



    void updateRankings(Rankings rankings);
    void updateDayRankings(Rankings rankings, Date date);

    boolean rankingsExist(String channelId);

    void deleteRankings(String channelId);
}
