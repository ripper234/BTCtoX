package controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import org.btctox.MapUtil;
import play.jobs.Job;
import play.libs.F;
import play.libs.WS;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class RateCalculator {
    private RateCalculator() {
    }

    private final static boolean USE_MOCKS = false;

    static F.Promise<F.Tuple<Map<String, BigDecimal>, BigDecimal>> gerRates() throws ExecutionException, InterruptedException {
        F.Promise<Map<String, BigDecimal>> ratesPromise = new Job<Map<String, BigDecimal>>() {
            @Override
            public Map<String, BigDecimal> doJobWithResult() throws Exception {
                Map<String, BigDecimal> rates = new LinkedHashMap<String, BigDecimal>();
                if (USE_MOCKS) {
                    rates.put("USD", new BigDecimal(1));
                    rates.put("ILS", new BigDecimal(4));

                    return rates;
                }
                F.Promise<WS.HttpResponse> response1 = WS.url("http://openexchangerates.org/latest.json").getAsync();

                WS.HttpResponse httpResponse = response1.get();
                for (Map.Entry<String, JsonElement> entry : ((JsonObject) httpResponse.getJson()).getAsJsonObject("rates").entrySet()) {
                    rates.put(entry.getKey(), entry.getValue().getAsBigDecimal());
                }

                rates = MapUtil.sortByKey(rates);
                return rates;
            }
        }.now();
        F.Promise<BigDecimal> btcRatePromise = getBtcRate();

        return F.Promise.wait2(ratesPromise, btcRatePromise);
    }

    private static F.Promise<BigDecimal> getBtcRate() throws ExecutionException, InterruptedException {
        return new Job<BigDecimal>() {
            @Override
            public BigDecimal doJobWithResult() throws Exception {
                if (USE_MOCKS) {
                    return new BigDecimal(5);
                }

                F.Promise<WS.HttpResponse> async = WS.url("https://mtgox.com/api/1/BTCUSD/ticker").getAsync();

                JsonObject json = async.get().getJson().getAsJsonObject();
                JsonObject jsonObje = json.get("return").getAsJsonObject().get("avg").getAsJsonObject();
                return jsonObje.get("value").getAsBigDecimal();
            }
        }.now();
    }
}
