package controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.btctox.MapUtil;
import play.jobs.Job;
import play.libs.F;
import play.libs.WS;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RateCalculator {
    private final static Logger logger = LogManager.getLogger(RateCalculator.class);
    private static final String BITCOIN_CHARTS_WEIGHTED_PRICE_API = "http://bitcoincharts.com/t/weighted_prices.json";
    private static final String OPEN_EXCHANGE_API = "http://openexchangerates.org/latest.json";
    private static final String MTGOX_API = "https://mtgox.com/api/1/BTCUSD/ticker";

    private RateCalculator() {
    }

    private final static boolean USE_MOCKS = false;

    static F.Promise<F.Tuple<Map<String, BigDecimal>, BigDecimal>> gerRates() throws ExecutionException, InterruptedException {
        F.Promise<Map<String, BigDecimal>> ratesPromise = getFiatRate();
        F.Promise<BigDecimal> btcRatePromise = getBtcRate();

        return F.Promise.wait2(ratesPromise, btcRatePromise);
    }

    static F.Promise<F.Tuple<Map<String, BigDecimal>, BigDecimal>> gerRatesWeighted(WeightPeriod period) throws ExecutionException, InterruptedException {
        F.Promise<Map<String, BigDecimal>> ratesPromise = getFiatRate();
        F.Promise<BigDecimal> btcRatePromise = getBTCRateWeighted(period);

        return F.Promise.wait2(ratesPromise, btcRatePromise);
    }

    private static F.Promise<Map<String, BigDecimal>> getFiatRate() {
        return new Job<Map<String, BigDecimal>>() {
            @Override
            public Map<String, BigDecimal> doJobWithResult() throws Exception {
                Map<String, BigDecimal> rates = new LinkedHashMap<String, BigDecimal>();
                if (USE_MOCKS) {
                    rates.put("USD", new BigDecimal(1));
                    rates.put("ILS", new BigDecimal(4));

                    return rates;
                }
                F.Promise<WS.HttpResponse> response1 = WS.url(OPEN_EXCHANGE_API).getAsync();

                WS.HttpResponse httpResponse = response1.get();
                for (Map.Entry<String, JsonElement> entry : ((JsonObject) httpResponse.getJson()).getAsJsonObject("rates").entrySet()) {
                    rates.put(entry.getKey(), entry.getValue().getAsBigDecimal());
                }

                rates = MapUtil.sortByKey(rates);
                return rates;
            }
        }.now();
    }

    private static F.Promise<BigDecimal> getBTCRateWeighted(final WeightPeriod period) {
        Job<BigDecimal> btc = new Job<BigDecimal>() {
            @Override
            public BigDecimal doJobWithResult() throws Exception {
                try {
                    if (USE_MOCKS) {
                        return new BigDecimal(4);
                    }
                    F.Promise<WS.HttpResponse> response = WS.url(BITCOIN_CHARTS_WEIGHTED_PRICE_API).getAsync();
                    WS.HttpResponse httpResponse = response.get();
                    JsonObject btcRates = (JsonObject) ((JsonObject) httpResponse.getJson()).get("USD");
                    JsonElement jsonElement = btcRates.get(period.code);
                    return jsonElement.getAsBigDecimal();
                } catch (Exception e) {
                    logger.info("Exception getting weighted BTC rate: ", e);
                    return null;
                }
            }
        };
        return btc.now();
    }

    private static F.Promise<BigDecimal> getBtcRate() throws ExecutionException, InterruptedException {
        return new Job<BigDecimal>() {
            @Override
            public BigDecimal doJobWithResult() throws Exception {
                if (USE_MOCKS) {
                    return new BigDecimal(5);
                }

                F.Promise<WS.HttpResponse> async = WS.url(MTGOX_API).getAsync();

                JsonObject json = async.get().getJson().getAsJsonObject();
                JsonObject jsonObje = json.get("return").getAsJsonObject().get("avg").getAsJsonObject();
                return jsonObje.get("value").getAsBigDecimal();
            }
        }.now();
    }
}
