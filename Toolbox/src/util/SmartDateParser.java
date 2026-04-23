package util;

import java.time.LocalDateTime;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * A flexible utility for converting date strings of varying formats into {@link Date} and
 * {@link ZonedDateTime} objects.
 * 
 * <p>
 * <b>Regional Support and Ambiguity: </b>This parser is optimised for <b>Australian/British
 * (DD/MM/YYYY)</b> date formats. In cases of numerical ambiguity, for example: {@code 01/02/2026},
 * the parser prioritises the Day-Month-Year interpretation (1st February).
 * </p>
 * 
 * <table border="1">
 * <caption><b>Supported Date Formats - Date Parsing Standards</b></caption>
 * <tr>
 * <th>Standard</th>
 * <th>Format Pattern / Example</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><b>EXIF Standard</b></td>
 * <td>{@code yyyy:MM:dd HH:mm:ss}</td>
 * <td>Standard camera metadata format using colons as delimiters.</td>
 * </tr>
 * <tr>
 * <td><b>ISO-8601</b></td>
 * <td>{@code yyyy-MM-dd'T'HH:mm:ss.SSS}</td>
 * <td>Supports {@code T} delimited timestamps with optional sub-seconds.</td>
 * </tr>
 * <tr>
 * <td><b>International/US</b></td>
 * <td>{@code MMM dd, yyyy} or {@code MMM dd yyyy}</td>
 * <td>Supports textual month patterns (e.g., {@code Jan 19, 2026}).</td>
 * </tr>
 * </table>
 * 
 * @author Trevor Maggs
 * @version 1.2
 * @since 19 January 2026
 */
public final class SmartDateParser
{
    private static final String[] DATE_SEPARATORS = {"/", "-", ":", " ", "."};
    private static final String[] TIME_FORMATS = {" HH:mm:ss", " HH:mm", ""};
    private static final List<DatePattern> MAP_PATTERN = new ArrayList<>();

    static
    {
        Map<String, String> regexMap = new LinkedHashMap<String, String>();

        // For Australian Date formats
        regexMap.put("y[sep]M[sep]d", "\\d{4}[sep]\\d{1,2}[sep]\\d{1,2}"); // EXIF Standard
        regexMap.put("d[sep]M[sep]y", "\\d{1,2}[sep]\\d{1,2}[sep]\\d{4}"); // AU Numerical
        regexMap.put("d[sep]MMM[sep]y", "\\d{1,2}[sep]\\w{3}[sep]\\d{4}"); // AU Textual
        regexMap.put("y[sep]MMM[sep]d", "\\d{4}[sep]\\w{3}[sep]\\d{1,2}"); // Pro-software Standard
        regexMap.put("MMM[sep]d[sep]y", "\\w{3}[sep]\\d{1,2},?[sep]\\d{4}"); // US format

        // ISO-8601 / T-Format (Strict Pattern)
        MAP_PATTERN.add(new DatePattern("\\d{4}-\\d{1,2}-\\d{1,2}T\\d{1,2}:\\d{1,2}:\\d{1,2}.*", "yyyy-M-d'T'HH:mm:ss", true));

        for (Map.Entry<String, String> entry : regexMap.entrySet())
        {
            for (String sep : DATE_SEPARATORS)
            {
                String pattern = entry.getKey().replace("[sep]", sep);
                String regex = entry.getValue().replace("[sep]", sep) + ".*";
                MAP_PATTERN.add(new DatePattern(regex, pattern, false));
            }
        }
    }

    /**
     * Container for mapping a regex identification string to a specific DateTime pattern.
     */
    private static class DatePattern
    {
        final String regex;
        final String formatPattern;
        final boolean isFullDateTime;

        private DatePattern(String regex, String formatPattern, boolean isFullDateTime)
        {
            this.regex = regex;
            this.formatPattern = formatPattern;
            this.isFullDateTime = isFullDateTime;
        }
    }

    /**
     * Default constructor will always throw an exception.
     *
     * @throws UnsupportedOperationException
     *         to indicate that instantiation is not supported
     */
    private SmartDateParser()
    {
        throw new UnsupportedOperationException("Not intended for instantiation");
    }

    /**
     * Obtains a {@link Date} object after parsing the specified date input.
     * 
     * <p>
     * This method is provided for backward compatibility with legacy APIs that require
     * {@link java.util.Date}. For modern logic, {@link #convertToZonedDateTime(String)} is
     * preferred.
     * </p>
     * 
     * @param input
     *        the date string to convert
     * @return a {@link Date} object representing the parsed timestamp
     * 
     * @throws IllegalArgumentException
     *         if the input is null or does not match any known date format
     */
    public static Date convertToDate(String input)
    {
        ZonedDateTime zdt = convertToZonedDateTime(input);

        return Date.from(zdt.toInstant());
    }

    /**
     * Attempts to parse a raw date string into a {@link ZonedDateTime} object by matching it
     * against a collection of supported date and time patterns.
     * 
     * <p>
     * This method prioritises ISO-8601 full date-time formats before falling back to common
     * regional patterns (e.g., Australian DD/MM/YYYY). If a pattern matches but lacks timezone
     * data, the system's default {@link ZoneId} is applied.
     * </p>
     * 
     * @param input
     *        the raw date string to be parsed, such as "23/04/2026" or "2026-04-23T20:00:00Z"
     * @return a valid {@code ZonedDateTime} representing the input
     * 
     * @throws IllegalArgumentException
     *         if the input is null or does not match any known format
     */
    public static ZonedDateTime convertToZonedDateTime(String input)
    {
        if (input != null && !input.trim().isEmpty())
        {
            String normalised = input.trim();

            for (DatePattern map : MAP_PATTERN)
            {
                if (normalised.matches(map.regex))
                {
                    try
                    {
                        if (map.isFullDateTime)
                        {
                            return parseZonedISO_8601(normalised);
                        }

                        LocalDateTime ldt = parseToLocalDateTime(normalised, map.formatPattern);

                        if (ldt != null)
                        {
                            return ldt.atZone(ZoneId.systemDefault());
                        }
                    }

                    catch (Exception exc)
                    {
                        // Fall through to next pattern
                    }
                }
            }
        }

        throw new IllegalArgumentException("Unsupported ZonedDateTime format [" + input + "]");
    }

    /**
     * Parses ISO-8601 strings with a preference for preserving existing offsets using
     * {@link DateTimeFormatter#ISO_OFFSET_DATE_TIME}.
     * 
     * @param input
     *        the ISO-8601 string
     * @return a {@link ZonedDateTime} object; if no offset is present, the result is normalised to
     *         the system's default time zone.
     */
    private static ZonedDateTime parseZonedISO_8601(String input)
    {
        try
        {
            return ZonedDateTime.parse(input, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }

        catch (DateTimeParseException exc)
        {
            String normalised = input.replaceAll("(\\.\\d+)?(Z|[+-]\\d{2}:?\\d{2})?$", "");

            return LocalDateTime.parse(normalised, DateTimeFormatter.ISO_LOCAL_DATE_TIME).atZone(ZoneId.systemDefault());
        }
    }

    /**
     * Internal helper to parse strings into {@link LocalDateTime} by testing suffixes.
     * 
     * @param input
     *        normalised date string
     * @param pattern
     *        base date pattern
     * @return parsed {@link LocalDateTime}, or {@code null} if unsuccessful
     */
    private static LocalDateTime parseToLocalDateTime(String input, String pattern)
    {
        String normalisedInput = input.replace(",", "");
        String normalisedPattern = pattern.replace(",", "");

        for (String suffix : TIME_FORMATS)
        {
            try
            {
                String fullPattern = normalisedPattern + suffix;
                DateTimeFormatter dtf = DateTimeFormatter.ofPattern(fullPattern, Locale.ENGLISH);

                if (fullPattern.contains("HH"))
                {
                    return LocalDateTime.parse(normalisedInput, dtf);
                }

                else
                {
                    return LocalDate.parse(normalisedInput, dtf).atStartOfDay();
                }
            }

            catch (DateTimeParseException exc)
            {
                /* Fallback through to next time pattern and try again */
            }
        }

        return null;
    }
}