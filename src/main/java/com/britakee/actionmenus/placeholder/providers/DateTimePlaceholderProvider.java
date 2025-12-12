package com.britakee.actionmenus.placeholder.providers;

import com.britakee.actionmenus.placeholder.PlaceholderContext;
import com.britakee.actionmenus.placeholder.PlaceholderProvider;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;

/**
 * Provides date and time related placeholders.
 * 
 * Supported placeholders:
 * - time_hour: Current hour (0-23)
 * - time_hour12: Current hour (1-12)
 * - time_minute: Current minute
 * - time_second: Current second
 * - time_ampm: AM or PM
 * - date_day: Day of month
 * - date_month: Month (1-12)
 * - date_month_name: Month name
 * - date_year: Year
 * - date_day_of_week: Day of week name
 * - date_formatted: Formatted date string
 * - time_formatted: Formatted time string
 * - datetime_formatted: Full datetime string
 * - timestamp: Unix timestamp
 */
public class DateTimePlaceholderProvider implements PlaceholderProvider {
    
    @Override
    public String getIdentifier() {
        return "time";
    }
    
    @Override
    public String resolve(String placeholder, PlaceholderContext context) {
        LocalDateTime now = LocalDateTime.now();
        String lowerPlaceholder = placeholder.toLowerCase();
        
        // Remove prefixes if present
        if (lowerPlaceholder.startsWith("time_")) {
            lowerPlaceholder = lowerPlaceholder.substring(5);
        } else if (lowerPlaceholder.startsWith("date_")) {
            lowerPlaceholder = lowerPlaceholder.substring(5);
        } else if (lowerPlaceholder.startsWith("datetime_")) {
            lowerPlaceholder = lowerPlaceholder.substring(9);
        }
        
        return switch (lowerPlaceholder) {
            // Time
            case "hour", "hours", "h" -> String.format("%02d", now.getHour());
            case "hour12", "hours12", "h12" -> {
                int hour = now.getHour() % 12;
                yield String.format("%02d", hour == 0 ? 12 : hour);
            }
            case "minute", "minutes", "m" -> String.format("%02d", now.getMinute());
            case "second", "seconds", "s" -> String.format("%02d", now.getSecond());
            case "ampm", "am_pm" -> now.getHour() < 12 ? "AM" : "PM";
            case "millisecond", "ms" -> String.valueOf(now.get(ChronoField.MILLI_OF_SECOND));
            
            // Date
            case "day", "d", "day_of_month" -> String.format("%02d", now.getDayOfMonth());
            case "month", "month_number" -> String.format("%02d", now.getMonthValue());
            case "month_name" -> now.getMonth().toString();
            case "month_short" -> now.getMonth().toString().substring(0, 3);
            case "year", "y" -> String.valueOf(now.getYear());
            case "year_short" -> String.valueOf(now.getYear() % 100);
            case "day_of_week" -> now.getDayOfWeek().toString();
            case "day_of_week_short" -> now.getDayOfWeek().toString().substring(0, 3);
            case "day_of_year" -> String.valueOf(now.getDayOfYear());
            case "week_of_year" -> String.valueOf(now.get(ChronoField.ALIGNED_WEEK_OF_YEAR));
            
            // Formatted
            case "formatted", "time_formatted" -> now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            case "date_formatted" -> now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            case "datetime", "datetime_formatted" -> now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            case "iso" -> now.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            case "us_date" -> now.format(DateTimeFormatter.ofPattern("MM/dd/yyyy"));
            case "eu_date" -> now.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));
            case "12hour" -> now.format(DateTimeFormatter.ofPattern("hh:mm:ss a"));
            case "24hour" -> now.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            
            // Timestamp
            case "timestamp", "unix" -> String.valueOf(System.currentTimeMillis() / 1000);
            case "timestamp_ms", "unix_ms" -> String.valueOf(System.currentTimeMillis());
            
            // Minecraft time (could be enhanced with actual world time)
            case "minecraft_time", "mc_time" -> {
                if (context.hasPlayer()) {
                    long worldTime = context.getPlayer().level().getDayTime() % 24000;
                    int hours = (int) ((worldTime / 1000 + 6) % 24);
                    int minutes = (int) ((worldTime % 1000) * 60 / 1000);
                    yield String.format("%02d:%02d", hours, minutes);
                }
                yield "00:00";
            }
            case "minecraft_day", "mc_day" -> {
                if (context.hasPlayer()) {
                    yield String.valueOf(context.getPlayer().level().getDayTime() / 24000);
                }
                yield "0";
            }
            case "is_day" -> {
                if (context.hasPlayer()) {
                    long time = context.getPlayer().level().getDayTime() % 24000;
                    yield String.valueOf(time >= 0 && time < 12000);
                }
                yield "true";
            }
            case "is_night" -> {
                if (context.hasPlayer()) {
                    long time = context.getPlayer().level().getDayTime() % 24000;
                    yield String.valueOf(time >= 12000);
                }
                yield "false";
            }
            
            // Timezone
            case "timezone", "tz" -> ZoneId.systemDefault().getId();
            case "timezone_offset" -> now.atZone(ZoneId.systemDefault())
                    .getOffset().getId();
            
            default -> null;
        };
    }
    
    @Override
    public String[] getPlaceholders() {
        return new String[] {
                "hour", "hour12", "minute", "second", "ampm", "millisecond",
                "day", "month", "month_name", "month_short", "year", "year_short",
                "day_of_week", "day_of_week_short", "day_of_year", "week_of_year",
                "formatted", "datetime", "iso", "us_date", "eu_date", "12hour", "24hour",
                "timestamp", "timestamp_ms",
                "minecraft_time", "minecraft_day", "is_day", "is_night",
                "timezone", "timezone_offset"
        };
    }
}
