package net.owapi;

import org.javatuples.Quartet;

public interface IOWAPI
{
     Quartet<Long, Long, Long, Long> getPlayerElos(String battletag);
}
