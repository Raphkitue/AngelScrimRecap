package controller.view;

import java.util.List;
import model.rankings.Rankings;
import model.scrims.Team;
import org.javatuples.Pair;

public interface IRankView
{
    boolean displaysEquals(Rankings formerRanks, Rankings newerRanks);
    List<String> getMainLines(Rankings ranks, Rankings formerRankings);
    List<String> getSecondLines(Rankings ranks, Rankings formerRanks);
    List<Pair<String, Double>> getTeamRank(List<Team> team, Rankings rankings);
}
