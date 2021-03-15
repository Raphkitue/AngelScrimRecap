package controller.view;

import java.util.List;
import model.rankings.Rankings;

public interface IRankView
{
    boolean displaysEquals(Rankings formerRanks, Rankings newerRanks);
    List<String> getMainLines(Rankings ranks, Rankings formerRankings);
    List<String> getSecondLines(Rankings ranks, Rankings formerRanks);
}
