package org.insightedge.examples.financialengineering.controller;

import com.j_spaces.core.client.SQLQuery;
import org.insightedge.examples.financialengineering.model.TickerSymbol;
import org.openspaces.core.GigaSpace;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * @author Svitlana_Pogrebna
 *
 */
@Controller
@RequestMapping("/")
public class HomeController {

    @Autowired
    private GigaSpace gigaSpace;

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public String get(Model model) {
        SQLQuery<TickerSymbol> query = new SQLQuery<>(TickerSymbol.class, "");
        TickerSymbol[] securities = gigaSpace.readMultiple(query);
        model.addAttribute("securities", securities);
        return "index";
    }
}
