package org.insightedge.examples.financialengineering.space;

import com.gigaspaces.query.aggregators.GroupByAggregator;
import com.gigaspaces.query.aggregators.GroupByResult;
import com.gigaspaces.query.aggregators.GroupByValue;
import com.j_spaces.core.client.SQLQuery;
import org.insightedge.examples.financialengineering.model.InvestmentReturn;
import org.insightedge.examples.financialengineering.model.MarketReturn;
import org.insightedge.examples.financialengineering.model.TickData;
import org.openspaces.core.GigaSpace;
import org.openspaces.core.context.GigaSpaceContext;
import org.openspaces.remoting.RemotingService;

import java.util.ArrayList;
import java.util.List;

import static org.openspaces.extensions.QueryExtension.average;
import static org.openspaces.extensions.QueryExtension.groupBy;

/**
 * @author Svitlana_Pogrebna
 *
 */
@RemotingService
public class StatisticsService implements IStatisticsService {

    @GigaSpaceContext(name = "gigaSpace")
    private GigaSpace gigaSpace;

    @Override
    public List<StatisticData> get(String tickerSymbol) {
        SQLQuery<TickData> query = new SQLQuery<>(TickData.class, "symbol = ?");
        query.setParameters(tickerSymbol);

        GroupByResult tickDataResult = groupBy(gigaSpace, query, new GroupByAggregator().select(
                average("open"), 
                average("close"), 
                average("high"), 
                average("low")).groupBy("dateAsStr"));

        SQLQuery<InvestmentReturn> query1 = new SQLQuery<>(InvestmentReturn.class, "symbol = ?");
        query1.setParameters(tickerSymbol);
        GroupByResult investmentReturnResult = groupBy(gigaSpace, query1, new GroupByAggregator().select(average("percentageRateOfReturn")).groupBy("dateAsStr"));

        SQLQuery<MarketReturn> query2 = new SQLQuery<>(MarketReturn.class, "symbol = ?");
        query2.setParameters(tickerSymbol);
        GroupByResult marketReturnResult = groupBy(gigaSpace, query2, new GroupByAggregator().select(average("percentageRateOfReturn")).groupBy("dateAsStr"));

        List<StatisticData> result = new ArrayList<>(Math.max(investmentReturnResult.size(), marketReturnResult.size()));

        for (GroupByValue tickGroup : tickDataResult) {
            String dateAsStr = (String) tickGroup.getKey().get("dateAsStr");
            Double avgOpen = tickGroup.getDouble("avg(open)");
            Double avgClose = tickGroup.getDouble("avg(close)");
            Double avgHigh = tickGroup.getDouble("avg(high)");
            Double avgLow = tickGroup.getDouble("avg(low)");

            GroupByValue individualGroup = investmentReturnResult.get(dateAsStr);
            Double individualIndex = individualGroup == null ? null : individualGroup.getDouble("avg(percentageRateOfReturn)");

            GroupByValue marketGroup = marketReturnResult.get(dateAsStr);
            Double marketIndex = marketGroup == null ? null : marketGroup.getDouble("avg(percentageRateOfReturn)");
            result.add(new StatisticData(dateAsStr, individualIndex, marketIndex, avgOpen, avgClose, avgHigh, avgLow));
        }

        return result;
    }
}