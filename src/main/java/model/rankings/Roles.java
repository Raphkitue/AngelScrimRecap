package model.rankings;

import java.util.Arrays;

public enum Roles
{
    OFF_HEAL("off-heal"),
    MAIN_HEAL("main-heal"),
    OFF_TANK("off-tank"),
    MAIN_TANK("main-tank"),
    DPS_PROJ("dps-proj"),
    DPS_HITSCAN("dps-hitscan");

    private final String name;

    Roles(String name)
    {
        this.name = name;
    }

    public static Roles from(String role)
    {
        return Arrays.stream(Roles.values())
            .filter(roles -> roles.name.equals(role))
            .findFirst()
            .orElse(null);
    }
}
