package model.rankings;

import org.javatuples.Pair;

import java.util.Arrays;

public enum Roles
{
    OFF_HEAL("off-heal", "Off Heal"),
    MAIN_HEAL("main-heal", "Main Heal"),
    OFF_TANK("off-tank", "Off Tank"),
    MAIN_TANK("main-tank", "Main Tank"),
    DPS_PROJ("dps-proj", "DPS (Projectiles)"),
    DPS_HITSCAN("dps-hitscan", "DPS (Hitscan)");

    private final String name;
    private final String friendlyName;

    Roles(String name, String friendlyName)
    {
        this.name = name;
        this.friendlyName = friendlyName;
    }

    public static Roles from(String role)
    {
        return Arrays.stream(Roles.values())
            .filter(roles -> roles.name.equals(role))
            .findFirst()
            .orElse(null);
    }

    public String getName() {
        return name;
    }

    public String getFriendlyName() {
        return friendlyName;
    }

    public Pair<String, String> getPair()
    {
        return Pair.with(name, friendlyName);
    }
}
