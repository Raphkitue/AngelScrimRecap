package model.rankings;

import Util.Jsonable;
import org.json.simple.JSONObject;

public class Ranking implements Jsonable
{
    private String battletag;
    private String mainRole;
    private long tankElo;
    private long damageElo;
    private long supportElo;
    private long openQElo;

    public Ranking()
    {
    }

    public Ranking(String battletag, String mainRole, int tankElo, int damageElo, int supportElo, int openQElo)
    {
        this.battletag = battletag;
        this.mainRole = mainRole;
        this.tankElo = tankElo;
        this.damageElo = damageElo;
        this.supportElo = supportElo;
        this.openQElo = openQElo;
    }

    public String getBattletag()
    {
        return battletag;
    }

    public String getMainRole()
    {
        return mainRole;
    }

    public long getTankElo()
    {
        return tankElo;
    }

    public void setTankElo(long tankElo)
    {
        this.tankElo = tankElo;
    }

    public long getDamageElo()
    {
        return damageElo;
    }

    public long getMainRoleElo()
    {
        switch (Roles.from(mainRole))
        {
            case OFF_HEAL:
            case MAIN_HEAL:
                return supportElo;
            case DPS_PROJ:
            case DPS_HITSCAN:
                return damageElo;
            case MAIN_TANK:
            case OFF_TANK:
                return tankElo;
            default:
                return 0;
        }
    }

    public void setDamageElo(long damageElo)
    {
        this.damageElo = damageElo;
    }

    public long getSupportElo()
    {
        return supportElo;
    }

    public void setSupportElo(long supportElo)
    {
        this.supportElo = supportElo;
    }

    public long getOpenQElo()
    {
        return openQElo;
    }

    public void setOpenQElo(long openQElo)
    {
        this.openQElo = openQElo;
    }

    @Override
    public JSONObject toJson()
    {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("battletag", battletag);
        jsonObject.put("mainRole", mainRole);
        jsonObject.put("tankElo", tankElo);
        jsonObject.put("damageElo", damageElo);
        jsonObject.put("supportElo", supportElo);
        jsonObject.put("openQElo", openQElo);
        return jsonObject;

    }

    @Override
    public Jsonable fromJson(JSONObject jsonObject)
    {
        battletag = (String) jsonObject.get("battletag");
        mainRole = (String) jsonObject.get("mainRole");
        tankElo = (long) jsonObject.get("tankElo");
        damageElo = (long) jsonObject.get("damageElo");
        supportElo = (long) jsonObject.get("supportElo");
        openQElo = (long) jsonObject.get("openQElo");
        return this;
    }

    @Override
    public String toString()
    {
        return "Ranking{" +
            "battletag='" + battletag + '\'' +
            ", mainRole='" + mainRole + '\'' +
            ", tankElo=" + tankElo +
            ", damageElo=" + damageElo +
            ", supportElo=" + supportElo +
            ", openQElo=" + openQElo +
            '}';
    }
}
