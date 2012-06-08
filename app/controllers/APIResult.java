package controllers;

import java.math.BigDecimal;

public class APIResult {
    public final boolean success;
    public final String message;
    public final BigDecimal rate;

    public APIResult(boolean success, String message, BigDecimal rate) {
        this.success = success;
        this.message = message;
        this.rate = rate;
    }

    public static APIResult error(String message) {
        return new APIResult(false, message, null);
    }

    public static APIResult success(BigDecimal rate) {
        return new APIResult(true, null, rate);
    }
}
