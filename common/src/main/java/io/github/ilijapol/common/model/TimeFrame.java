package io.github.ilijapol.common.model;

public enum TimeFrame {
    ONE_MINUTE("1m", 60),
    FIVE_MINUTES("5m", 300),
    FIFTEEN_MINUTES("15m", 900),
    THIRTY_MINUTES("30m", 1800),
    ONE_HOUR("1h", 3600),
    FOUR_HOURS("4h", 14400),
    ONE_DAY("1d", 86400);

    private final String code;
    private final int seconds;

    TimeFrame(String code, int seconds) {
        this.code = code;
        this.seconds = seconds;
    }

    public int getSeconds() { return seconds; }

    public static TimeFrame parse(String input) {
        switch (input) {
            case "1m": return ONE_MINUTE;
            case "5m": return FIVE_MINUTES;
            case "15m": return FIFTEEN_MINUTES;
            case "30m": return THIRTY_MINUTES;
            case "1h": return ONE_HOUR;
            case "4h": return FOUR_HOURS;
            case "1d": return ONE_DAY;
            default: throw new IllegalArgumentException("Unknown time frame: " + input);
        }
    }
}