package org.insightedge.examples.financialengineering.space;

import java.io.Serializable;

/**
 * @author Svitlana_Pogrebna
 *
 */
public class StatisticData implements Serializable {

    private static final long serialVersionUID = 1L;

    public StatisticData() {}
    
    private String date;
    private Double individualIndex;
    private Double marketIndex;
    private Double open;
    private Double close;
    private Double high;
    private Double low;
    
    public StatisticData(String date, Double individualIndex, Double marketIndex, Double open, Double close, Double high, Double low) {
        super();
        this.date = date;
        this.individualIndex = individualIndex;
        this.marketIndex = marketIndex;
        this.open = open;
        this.close = close;
        this.high = high;
        this.low = low;
    }
    public String getDate() {
        return date;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public Double getIndividualIndex() {
        return individualIndex;
    }
    public void setIndividualIndex(Double individualIndex) {
        this.individualIndex = individualIndex;
    }
    public Double getMarketIndex() {
        return marketIndex;
    }
    public void setMarketIndex(Double marketIndex) {
        this.marketIndex = marketIndex;
    }
    
    public Double getOpen() {
        return open;
    }
    public void setOpen(Double open) {
        this.open = open;
    }
    public Double getClose() {
        return close;
    }
    public void setClose(Double close) {
        this.close = close;
    }
    public Double getHigh() {
        return high;
    }
    public void setHigh(Double high) {
        this.high = high;
    }
    public Double getLow() {
        return low;
    }
    public void setLow(Double low) {
        this.low = low;
    }
}
