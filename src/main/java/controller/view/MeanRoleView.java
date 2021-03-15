package controller.view;

import static controller.RankingsController.eloProgressEmoji;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import model.rankings.Player;
import model.rankings.Rankings;
import org.javatuples.Pair;

public class MeanRoleView implements IRankView
{

    @Override
    public boolean displaysEquals(Rankings formerRanks, Rankings newerRanks)
    {
        return getUniqueStats(newerRanks).equals(getUniqueStats(formerRanks));
    }

    private String getUniqueStats(Rankings ranks)
    {
        return ranks.getRanks().stream().map(player ->
            player.getBattletag()
                + getPlayerMean(player)
        )
            .collect(Collectors.joining());
    }

    private long getPlayerMean(Player player)
    {
        return (player.getSupportElo() + player.getDamageElo() + player.getTankElo()) / 3;
    }

    @Override
    public List<String> getMainLines(Rankings ranks, Rankings formerRankings)
    {
        return ranks.getRanks().stream()
            .sorted(Comparator.comparingLong(this::getPlayerMean).reversed())
            .map(e -> Pair.with(e, formerRankings.getPlayerRanks(e.getBattletag()).orElse(new Player())))
            .map(e -> e.getValue0().getBattletag()
                + " (*" + eloProgressEmoji(getPlayerMean(e.getValue1()), getPlayerMean(e.getValue0())) + "*) "
                + (e.getValue0().isPrivate() ? "PRIVATE PROFILE" : ""))
            .collect(Collectors.toList());
    }

    @Override
    public List<String> getSecondLines(Rankings ranks, Rankings formerRanks)
    {
        return ranks.getRanks().stream()
            .sorted(Comparator.comparingLong(this::getPlayerMean).reversed())
            .map(e -> Pair.with(e, formerRanks.getPlayerRanks(e.getBattletag()).orElse(new Player())))
            .map(e -> "Tank " + eloProgressEmoji(e.getValue1().getTankElo(), e.getValue0().getTankElo())
                + ", Damage " + eloProgressEmoji(e.getValue1().getDamageElo(), e.getValue0().getDamageElo())
                + ", Support " + eloProgressEmoji(e.getValue1().getSupportElo(), e.getValue0().getSupportElo()))
            .collect(Collectors.toList());
    }
}