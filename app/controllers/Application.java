package controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

        Map<String, BigDecimal> rates = ratesPromise.get();
        BigDecimal btcRate = btcRatePromise.get();

        render(rates, btcRate);
    }

    public static void frombtc() throws ExecutionException, InterruptedException {
        F.Promise<Map<String, BigDecimal>> ratesPromise = getRates();
        F.Promise<BigDecimal> btcRatePromise = getBtcRate();

        Map<String, BigDecimal> rates = ratesPromise.get();
        BigDecimal btcRate = btcRatePromise.get();

        render(rates, btcRate);
    }

    private static F.Promise<BigDecimal> getBtcRate() throws ExecutionException, InterruptedException {
        F.Promise<BigDecimal> promise = new F.Promise<BigDecimal>();

        if (USE_MOCKS) {
            promise.invoke(new BigDecimal(5));
        } else {
            F.Promise<WS.HttpResponse> async = WS.url("https://mtgox.com/api/1/BTCUSD/ticker").getAsync();

            // TODO - async it
            JsonObject json = async.get().getJson().getAsJsonObject();

            JsonObject jsonObje= json.get("return").getAsJsonObject().get("avg").getAsJsonObject();
            BigDecimal rate = jsonObje.get("value").getAsBigDecimal();
            promise.invoke(rate);
        }

        
        return promise;
    }

    private static F.Promise<Map<String, BigDecimal>> getRates() throws ExecutionException, InterruptedException {
        HashMap<String, BigDecimal> rates = new HashMap<String, BigDecimal>();
        if (USE_MOCKS) {
            rates.put("USD", new BigDecimal(1));
            rates.put("ILS", new BigDecimal(4));

            F.Promise<Map<String, BigDecimal>> promise = new F.Promise<Map<String, BigDecimal>>();
            promise.invoke(rates);
            return promise;
        } else {
            F.Promise<WS.HttpResponse> response = WS.url("http://openexchangerates.org/latest.json").getAsync();

            // TODO - asynch it - http://stackoverflow.com/questions/10941206/building-complex-promises-in-play
            WS.HttpResponse httpResponse = response.get();
            JsonElement json = httpResponse.getJson();

            for (Map.Entry<String, JsonElement> entry : ((JsonObject) httpResponse.getJson()).getAsJsonObject("rates").entrySet()) {
                rates.put(entry.getKey(), entry.getValue().getAsBigDecimal());
            }

            F.Promise<Map<String, BigDecimal>> promise = new F.Promise<Map<String, BigDecimal>>();
            promise.invoke(rates);
            return promise;
        }
    }


    public static void test() {
        String staticUrl = play.mvc.Router.reverseWithCheck("p",
                play.Play.getVirtualFile("/public/stylesheets/main.less"), false);
        renderText("the reverse route is " + staticUrl);
    }
}