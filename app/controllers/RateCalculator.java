package controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import models.BTCWeightedRates;
import models.WeightPeriod;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.btctox.MapUtil;
import org.joda.time.Period;
import org.playutils.CacheUtils;
import play.Play;
import play.jobs.Job;
import play.libs.F;
import play.libs.WS;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

public class RateCalculator {
    private final static Logger logger = LogManager.getLogger(RateCalculator.class);
    private static final String BITCOIN_CHARTS_WEIGHTED_PRICE_API = "http://bitcoincharts.com/t/weighted_prices.json";
    private static final String OPEN_EXCHANGE_API = "http://openexchangerates.org/latest.json";
    private static final String MTGOX_API = "https://mtgox.com/api/1/BTCUSD/ticker";
    private static final String OPEN_EXCHANGE_API_KEY = Play.configuration.getProperty("openExchangeAppId");

    private RateCalculator() {
    }

    private final static boolean USE_MOCKS = false;

    public static void warmCaches() {
        new Job<Void>(){
            @Override
            public void doJob() throws Exception {
                getBtcRate();
                getBTCRateWeighted(WeightPeriod.OneDay);
                getFiatRate();
            }
        }.now();
    }

    public static F.Promise<F.Tuple<Map<String, BigDecimal>, BigDecimal>> gerRates() throws ExecutionException, InterruptedException {
        F.Promise<Map<String, BigDecimal>> ratesPromise = getFiatRate();
        F.Promise<BigDecimal> btcRatePromise = getBtcRate();

        return F.Promise.wait2(ratesPromise, btcRatePromise);
    }

    public static F.Promise<F.Tuple<Map<String, BigDecimal>, BigDecimal>> gerRatesWeighted(WeightPeriod period) throws ExecutionException, InterruptedException {
        F.Promise<Map<String, BigDecimal>> ratesPromise = getFiatRate();
        F.Promise<BigDecimal> btcRatePromise = getBTCRateWeighted(period);

        return F.Promise.wait2(ratesPromise, btcRatePromise);
    }

    private static F.Promise<Map<String, BigDecimal>> getFiatRate() {
        return new Job<Map<String, BigDecimal>>() {
            @Override
            public Map<String, BigDecimal> doJobWithResult() throws Exception {
                return CacheUtils.getCachedOrFresh("fiatRate", Period.minutes(2), new Callable<Map<String, BigDecimal>>() {
                    @Override
                    public Map<String, BigDecimal> call() throws Exception {
                        Map<String, BigDecimal> rates = new LinkedHashMap<String, BigDecimal>();
                        if (USE_MOCKS) {
                            rates.put("USD", new BigDecimal(1));
                            rates.put("ILS", new BigDecimal(4));

                            return rates;
                        }


                        F.Promise<WS.HttpResponse> response1 =
                            CacheUtils.getCachedOrFresh("openExchangeResponse", Period.hours(1), new Callable<F.Promise<WS.HttpResponse>>() {
                                @Override
                                public F.Promise<WS.HttpResponse> call() throws Exception {
                                    return WS.url(OPEN_EXCHANGE_API).setParameter("app_id", OPEN_EXCHANGE_API_KEY).getAsync();
                                }
                            });

                        WS.HttpResponse httpResponse = response1.get();
                        JsonObject json = (JsonObject) httpResponse.getJson();
                        JsonObject jsonRates = json.getAsJsonObject("rates");
                        if (jsonRates == null) {
                            throw new RuntimeException("Failed to get rates from OpenExchange API");
                        }
                        for (Map.Entry<String, JsonElement> entry : jsonRates.entrySet()) {
                            rates.put(entry.getKey(), entry.getValue().getAsBigDecimal());
                        }

                        rates = MapUtil.sortByKey(rates);
                        return rates;
                    }
                });
            }
        }.now();
    }

    private static F.Promise<BigDecimal> getBTCRateWeighted(final WeightPeriod period) {
        Job<BigDecimal> btc = new Job<BigDecimal>() {
            @SuppressWarnings("unchecked")
            @Override
            public BigDecimal doJobWithResult() throws Exception {
                try {
                    if (USE_MOCKS) {
                        return new BigDecimal(4);
                    }

                    // BitcoinCharts only allows us to ping their site every 15 minutes.
                    // Let's cache the results for 16 minutes.
                    BTCWeightedRates rates = CacheUtils.getCachedOrFresh("bitcoinChartsRates", Period.minutes(16),
                            new Callable<BTCWeightedRates>() {
                                @Override
                                public BTCWeightedRates call() throws Exception {
                                    F.Promise<WS.HttpResponse> response = WS.url(BITCOIN_CHARTS_WEIGHTED_PRICE_API).getAsync();
                                    WS.HttpResponse httpResponse = response.get();
                                    return parseBTCWeighted(((JsonObject) (((JsonObject) httpResponse.getJson()).get("USD"))));
                                }
                            });

                    return rates.get(period);
                } catch (Exception e) {
                    logger.info("Exception getting weighted BTC rate: ", e);
                    return null;
                }
            }

        };
        return btc.now();
    }

    private static BTCWeightedRates parseBTCWeighted(JsonObject json) {
        BTCWeightedRates result = new BTCWeightedRates();
        for (WeightPeriod period : WeightPeriod.values()) {
            result.set(period, json.get(period.code).getAsBigDecimal());
        }
        return result;
    }

    private static F.Promise<BigDecimal> getBtcRate() throws ExecutionException, InterruptedException {
        return new Job<BigDecimal>() {
            @Override
            public BigDecimal doJobWithResult() throws Exception {
                return CacheUtils.getCachedOrFresh("btcRate", Period.minutes(2), new Callable<BigDecimal>() {
                    @Override
                    public BigDecimal call() throws Exception {
                        if (USE_MOCKS) {
                            return new BigDecimal(5);
                        }

                        F.Promise<WS.HttpResponse> async = WS.url(MTGOX_API).getAsync();

                        JsonObject json = async.get().getJson().getAsJsonObject();
                        JsonObject jsonObje = json.get("return").getAsJsonObject().get("avg").getAsJsonObject();
                        return jsonObje.get("value").getAsBigDecimal();
                    }
                });
            }
        }.now();
    }
}

