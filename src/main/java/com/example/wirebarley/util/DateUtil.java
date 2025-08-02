package com.example.wirebarley.util;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

    private static ZoneId defaultZoneId = ZoneId.of("Asia/Seoul");
    private static String formatYYYYMMDD = "yyyyMMdd";

    public static ZoneId getZoneId() {
        return defaultZoneId;
    }

    public static ZonedDateTime now() {
        return ZonedDateTime.now(getZoneId());
    }

    public static String yyyymmdd(ZonedDateTime dateTime) {
        return dateTime.format(DateTimeFormatter.ofPattern(formatYYYYMMDD));
    }
}
