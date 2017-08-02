package org.insightedge.examples.financialengineering.controller;

import org.insightedge.examples.financialengineering.space.IStatisticsService;
import org.insightedge.examples.financialengineering.space.StatisticData;
import org.openspaces.remoting.ExecutorProxy;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

/**
 * @author Svitlana_Pogrebna
 *
 */
@Controller
public class CharacteristicLineController {

    @ExecutorProxy
    private IStatisticsService statisticsService;

    @RequestMapping(value = "/securities/{security}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody List<StatisticData> load(@PathVariable("security") String security) {
        return statisticsService.get(security);
    }

}
