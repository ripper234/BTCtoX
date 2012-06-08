package controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import play.jobs.Job;
import play.libs.F;
import play.libs.WS;
import play.mvc.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;

public class Application extends Controller {

    private final static boolean USE_MOCKS = false;

    public static void index() {
        render();
    }

    public static void tobtc() throws ExecutionException, InterruptedException {
        F.Promise<Map<String, BigDecimal>> ratesPromise = getRates();
        F.Promise<BigDecimal> btcRatePromise = getBtcRate();

        F.Tuple<Map<String, BigDecimal>, BigDecimal> result = await(F.Promise.wait2(ratesPromise, btcRatePromise));
        Map<String, BigDecimal> rates = result._1;
        BigDecimal btcRate = result._2;

        render(rates, btcRate);
    }

    public static void frombtc() throws ExecutionException, InterruptedException {
        F.Promise<Map<String, BigDecimal>> ratesPromise = getRates();
        F.Promise<BigDecimal> btcRatePromise = getBtcRate();

        F.Tuple<Map<String, BigDecimal>, BigDecimal> result = await(F.Promise.wait2(ratesPromise, btcRatePromise));
        Map<String, BigDecimal> rates = result._1;
        BigDecimal btcRate = result._2;

        render(rates, btcRate);
    }

    private static F.Promise<BigDecimal> getBtcRate() throws ExecutionException, InterruptedException {
        return new Job<BigDecimal>(){
            @Override
            public BigDecimal doJobWithResult() throws Exception {
                if (USE_MOCKS) {
                    return new BigDecimal(5);
                }

                F.Promise<WS.HttpResponse> async = WS.url("https://mtgox.com/api/1/BTCUSD/ticker").getAsync();

                JsonObject json = async.get().getJson().getAsJsonObject();
                JsonObject jsonObje= json.get("return").getAsJsonObject().get("avg").getAsJsonObject();
                return jsonObje.get("value").getAsBigDecimal();
            }
        }.now();
    }

    private static F.Promise<Map<String, BigDecimal>> getRates() throws ExecutionException, InterruptedException {
        return new Job<Map<String, BigDecimal>>(){
            @Override
            public Map<String, BigDecimal> doJobWithResult() throws Exception {
                HashMap<String, BigDecimal> rates = new HashMap<String, BigDecimal>();
                if (USE_MOCKS) {
                    rates.put("USD", new BigDecimal(1));
                    rates.put("ILS", new BigDecimal(4));

                    return rates;
                }
                F.Promise<WS.HttpResponse> response = WS.url("http://openexchangerates.org/latest.json").getAsync();

                WS.HttpResponse httpResponse = response.get();
                for (Map.Entry<String, JsonElement> entry : ((JsonObject) httpResponse.getJson()).getAsJsonObject("rates").entrySet()) {
                    rates.put(entry.getKey(), entry.getValue().getAsBigDecimal());
                }

                return rates;
            }
        }.now();
    }


    public static void test() {
        String staticUrl = play.mvc.Router.reverseWithCheck("p",
                play.Play.getVirtualFile("/public/stylesheets/main.less"), false);
        renderText("the reverse route is " + staticUrl);
    }
}