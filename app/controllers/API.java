package controllers;

import play.libs.F;
import play.mvc.Controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class API extends Controller {
    public static void tobtc(BigDecimal amount, String currencyCode) throws ExecutionException, InterruptedException {
        if (amount == null) {
            renderJSON(APIResult.error("Null amount"));
            throw new RuntimeException("Never gets here");
        }
        if (currencyCode == null) {
            renderJSON(APIResult.error("Null currencyCode"));
            throw new RuntimeException("Never gets here");
        }
        F.Tuple<Map<String, BigDecimal>, BigDecimal> result = await(RateCalculator.gerRates());
        Map<String, BigDecimal> rates = result._1;
        BigDecimal btcRate = result._2;

        BigDecimal currencyRate = rates.get(currencyCode);
        if (currencyRate == null) {
            renderJSON(APIResult.error("Can't find currency code"));
            throw new RuntimeException("Never gets here");
        }
        renderJSON(APIResult.success(amount.divide(currencyRate, 5, RoundingMode.UP).divide(btcRate, 5, RoundingMode.UP)));
    }

    public static void frombtc(BigDecimal amount, String currencyCode) throws ExecutionException, InterruptedException {
        if (amount == null) {
            renderJSON(APIResult.error("Null amount"));
            throw new RuntimeException("Never gets here");
        }
        if (currencyCode == null) {
            renderJSON(APIResult.error("Null currencyCode"));
            throw new RuntimeException("Never gets here");
        }
        F.Tuple<Map<String, BigDecimal>, BigDecimal> result = await(RateCalculator.gerRates());
        Map<String, BigDecimal> rates = result._1;
        BigDecimal btcRate = result._2;

        BigDecimal currencyRate = rates.get(currencyCode);
        if (currencyRate == null) {
            renderJSON(APIResult.error("Can't find currency code"));
            throw new RuntimeException("Never gets here");
        }
        renderJSON(APIResult.success(amount.multiply(currencyRate).multiply(btcRate)));
    }
}
