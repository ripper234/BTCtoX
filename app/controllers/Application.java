package controllers;

import play.*;
import play.libs.F;
import play.mvc.*;

import java.math.BigDecimal;
import java.util.*;
import java.util.concurrent.ExecutionException;

import models.*;

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void tobtc() throws ExecutionException, InterruptedException {
        F.Promise<Map<String, BigDecimal>> ratesPromise = getRates();
        F.Promise<BigDecimal> btcToUsdPromise = getBtcRate();

        Map<String, BigDecimal> rates = ratesPromise.get();
        BigDecimal btcToUsd = btcToUsdPromise.get();

        render(rates, btcToUsd);
    }

    public static void frombtc() {
        render();
    }

    private static F.Promise<BigDecimal> getBtcRate() {
        F.Promise<BigDecimal> Promise = new F.Promise<BigDecimal>();
        Promise.invoke(new BigDecimal(5));
        
        return Promise;
    }

    private static F.Promise<Map<String, BigDecimal>> getRates() {
        HashMap<String, BigDecimal> rates = new HashMap<String, BigDecimal>();
        rates.put("USD", new BigDecimal(1));
        rates.put("ILS", new BigDecimal(4));
        
        F.Promise<Map<String, BigDecimal>> promise = new F.Promise<Map<String, BigDecimal>>();
        promise.invoke(rates);
        return promise;
    }

}