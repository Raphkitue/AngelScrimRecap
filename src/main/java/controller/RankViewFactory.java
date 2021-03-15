package controller;

import static support.AngelCompetition.RankingsMode.MAIN_ROLE;
import static support.AngelCompetition.RankingsMode.MAIN_ROLE_AND_OPEN_Q;
import static support.AngelCompetition.RankingsMode.MEAN_ROLES;
import static support.AngelCompetition.RankingsMode.MEAN_ROLES_AND_OPEN_Q;

import controller.view.IRankView;
import controller.view.MainRoleOpenQView;
import controller.view.MainRoleView;
import controller.view.MeanRoleOpenQView;
import controller.view.MeanRoleView;
import model.rankings.Rankings;

public class RankViewFactory
{

    public static IRankView getRankView(Rankings rankings)
    {
        if (MAIN_ROLE.getValue().equalsIgnoreCase(rankings.getMode()))
        {
            return new MainRoleView();
        }
        else if (MAIN_ROLE_AND_OPEN_Q.getValue().equalsIgnoreCase(rankings.getMode()))
        {
            return new MainRoleOpenQView();
        }
        else if (MEAN_ROLES.getValue().equalsIgnoreCase(rankings.getMode()))
        {
            return new MeanRoleView();
        }
        else if (MEAN_ROLES_AND_OPEN_Q.getValue().equalsIgnoreCase(rankings.getMode()))
        {
            return new MeanRoleOpenQView();
        } else {
            return new MainRoleView();
        }
    }
}
