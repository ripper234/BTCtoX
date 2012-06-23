package models;

import java.math.BigDecimal;
import java.util.Map;

import static com.google.common.collect.Maps.newHashMap;

public class BTCWeightedRates {
    private Map<WeightPeriod, BigDecimal> weights = newHashMap();
    public BigDecimal get(WeightPeriod period) {
        return weights.get(period);
    }
    public void set (WeightPeriod period, BigDecimal rate) {
        weights.put(period, rate);
    }
}
