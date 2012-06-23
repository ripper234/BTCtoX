package models;

public enum WeightPeriod {
    OneDay("24h"),
    SevenDays("7d"),
    ThirtyDays("30d");

    public final String code;

    WeightPeriod(String code) {
        this.code = code;
    }

    public static WeightPeriod tryParse(String code) {
        for (WeightPeriod value : values()) {
            if (value.code.equals(code)) {
                return value;
            }
        }
        return null;
    }
}
