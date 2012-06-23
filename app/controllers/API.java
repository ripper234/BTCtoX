package controllers;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import play.libs.F;
import play.mvc.Controller;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class API extends Controller {
    private static final Logger logger = LogManager.getLogger(Controller.class);

    public static void tobtc(BigDecimal amount, String currencyCode) throws ExecutionException, InterruptedException {
        if (amount == null) {
            renderJSON(APIResult.error("Null amount"));
            throw new RuntimeException("Never gets here");
        }
        if (currencyCode == null) {
            renderJSON(APIResult.error("Null currencyCode"));
            throw new RuntimeException("Never gets here");
        }
        F.Tuple<Map<String, BigDecimal>, BigDecimal> results = await(RateCalculator.gerRates());
        Map<String, BigDecimal> rates = results._1;
        BigDecimal btcRate = results._2;

        BigDecimal currencyRate = rates.get(currencyCode);
        if (currencyRate == null) {
            renderJSON(APIResult.error("Can't find currency code"));
            throw new RuntimeException("Never gets here");
        }

        BigDecimal result = amount.divide(currencyRate, 5, RoundingMode.UP).divide(btcRate, 5, RoundingMode.UP);

        logger.info(String.format("Got API request (tobtc), amount=%s, currencyCode=%s, result=%s", amount, currencyCode, result));
        renderJSON(APIResult.success(result));
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
        F.Tuple<Map<String, BigDecimal>, BigDecimal> results = await(RateCalculator.gerRates());
        Map<String, BigDecimal> rates = results._1;
        BigDecimal btcRate = results._2;

        BigDecimal currencyRate = rates.get(currencyCode);
        if (currencyRate == null) {
            renderJSON(APIResult.error("Can't find currency code"));
            throw new RuntimeException("Never gets here");
        }
        BigDecimal result = amount.multiply(currencyRate).multiply(btcRate);
        logger.info(String.format("Got API request (frombtc), amount=%s, currencyCode=%s, result=%s", amount, currencyCode, result));
        renderJSON(APIResult.success(result));
    }

    public static void tobtcWeighted(BigDecimal amount, String currencyCode, String period) throws ExecutionException, InterruptedException {
        if (amount == null) {
            renderJSON(APIResult.error("Null amount"));
            throw new RuntimeException("Never gets here");
        }
        if (currencyCode == null) {
            renderJSON(APIResult.error("Null currencyCode"));
            throw new RuntimeException("Never gets here");
        }
        if (period == null) {
            renderJSON(APIResult.error("Null period"));
            throw new RuntimeException("Never gets here");
        }
        WeightPeriod periodParsed = WeightPeriod.tryParse(period);
        if (periodParsed == null) {
            renderJSON(APIResult.error("Unable to parse period: " + period));
            throw new RuntimeException("Never gets here");
        }

        F.Tuple<Map<String, BigDecimal>, BigDecimal> results = await(RateCalculator.gerRatesWeighted(periodParsed));
        Map<String, BigDecimal> rates = results._1;
        BigDecimal btcRate = results._2;

        BigDecimal currencyRate = rates.get(currencyCode);
        if (currencyRate == null) {
            renderJSON(APIResult.error("Can't find currency code"));
            throw new RuntimeException("Never gets here");
        }

        BigDecimal result = amount.divide(currencyRate, 5, RoundingMode.UP).divide(btcRate, 5, RoundingMode.UP);

        logger.info(String.format("Got API request (tobtc), amount=%s, currencyCode=%s, result=%s", amount, currencyCode, result));
        renderJSON(APIResult.success(result));
    }

    public static void frombtcWeighted(BigDecimal amount, String currencyCode, String period) throws ExecutionException, InterruptedException {
        if (amount == null) {
            renderJSON(APIResult.error("Null amount"));
            throw new RuntimeException("Never gets here");
        }
        if (currencyCode == null) {
            renderJSON(APIResult.error("Null currencyCode"));
            throw new RuntimeException("Never gets here");
        }
        WeightPeriod periodParsed = WeightPeriod.tryParse(period);
        if (periodParsed == null) {
            renderJSON(APIResult.error("Unable to parse period: " + period));
            throw new RuntimeException("Never gets here");
        }

        F.Tuple<Map<String, BigDecimal>, BigDecimal> results = await(RateCalculator.gerRatesWeighted(periodParsed));
        Map<String, BigDecimal> rates = results._1;
        BigDecimal btcRate = results._2;

        BigDecimal currencyRate = rates.get(currencyCode);
        if (currencyRate == null) {
            renderJSON(APIResult.error("Can't find currency code"));
            throw new RuntimeException("Never gets here");
        }
        BigDecimal result = amount.multiply(currencyRate).multiply(btcRate);
        logger.info(String.format("Got API request (frombtc), amount=%s, currencyCode=%s, result=%s", amount, currencyCode, result));
        renderJSON(APIResult.success(result));
    }
}
