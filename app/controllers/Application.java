package controllers;

import play.libs.F;
import play.mvc.Controller;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class Application extends Controller {

    public static void index() {
        render();
    }

    public static void api() {
        render();
    }

    public static void tobtc() throws ExecutionException, InterruptedException {
        F.Tuple<Map<String, BigDecimal>, BigDecimal> result = await(RateCalculator.gerRates());
        Map<String, BigDecimal> rates = result._1;
        BigDecimal btcRate = result._2;

        render(rates, btcRate);
    }

    public static void frombtc() throws ExecutionException, InterruptedException {
        F.Tuple<Map<String, BigDecimal>, BigDecimal> result = await(RateCalculator.gerRates());
        Map<String, BigDecimal> rates = result._1;
        BigDecimal btcRate = result._2;

        render(rates, btcRate);
    }
}
