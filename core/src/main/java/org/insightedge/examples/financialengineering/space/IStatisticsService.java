package org.insightedge.examples.financialengineering.space;

import java.util.List;

import org.openspaces.remoting.Routing;


/**
 * @author Svitlana_Pogrebna
 *
 */
public interface IStatisticsService {
     
     List<StatisticData> get(@Routing String tickerSymbol);
}
