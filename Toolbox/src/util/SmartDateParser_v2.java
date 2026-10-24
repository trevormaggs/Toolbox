package util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.chrono.IsoChronology;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.FormatStyle;
import java.time.format.ResolverStyle;
import java.time.format.SignStyle;
import java.time.format.TextStyle;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;

/**
 * A flexible utility for converting date strings of varying formats into {@link Date}, {@link LocalDateTime}, and {@link ZonedDateTime} objects.
 *
 * <p>
 * <b>Regional Support and Ambiguity: </b>This parser is optimised for <b>Australian/British/Indian (DD/MM/YYYY)</b> date formats. In cases of numerical ambiguity, for example: {@code 01/02/2026}, the parser prioritises the Day-Month-Year interpretation (1st February). This default does <b>not</b> depend on the JVM's default locale.
 * </p>
 *
 * <p>
 * <b>Month-first input (for example US): </b>Callers who know their data is month-first can pass an input {@link Locale} to the overloads that accept one. If, according to the JDK's locale data, that locale's short date pattern starts with the month (for example {@link Locale#US}), numeric dates are read as Month-Day-Year, so {@code 04/05/2026} becomes 5 April. Matching is strict: {@code 23/04/2026} is then rejected rather than silently re-read as day-first. A {@code null} locale, or any locale whose short pattern does not start with the month, keeps the default Day-Month-Year order. Only purely numeric dates are affected; textual months, year-first dates and ISO-8601 strings are unambiguous and parse identically either way. The locale passed to the {@code convertToLocalisedDateTime} methods controls output formatting only and has no effect on how the input is read.
 * </p>
 *
 * <p>
 * <b>Strictness: </b>Parsing is strict. Impossible dates such as {@code 31/02/2026} are rejected rather than being adjusted to the nearest valid date.
 * </p>
 *
 * <p>
 * <b>Time zones: </b>Input that carries an offset or region (ISO-8601) keeps it. Input without one is interpreted in the zone supplied by the caller, or the JVM default zone for the overloads that do not take a {@link ZoneId}. Callers parsing data that originates elsewhere (for example {@code Asia/Kolkata}) should always supply the source zone explicitly.
 * </p>
 *
 * <p>
 * <b>Thread safety: </b>This class is thread-safe. All shared state ({@link Pattern}s and {@link DateTimeFormatter}s) is immutable and created once in the static initialiser, and nothing is cached, so memory use is constant.
 * </p>
 *
 * <table border="1">
 * <caption><b>Supported Date Formats</b></caption>
 * <tr>
 * <th>Standard</th>
 * <th>Format Pattern / Example</th>
 * <th>Description</th>
 * </tr>
 * <tr>
 * <td><b>ISO-8601</b></td>
 * <td>{@code 2026-04-23T20:00[:ss[.SSS]][Z|+05:30|+0530][[Region/Id]]}</td>
 * <td>{@code T}-delimited timestamps with optional seconds, fraction, offset and region.</td>
 * </tr>
 * <tr>
 * <td><b>EXIF / Year first</b></td>
 * <td>{@code yyyy:MM:dd HH:mm:ss}, {@code yyyy-MM-dd}</td>
 * <td>Year-month-day with any of {@code / - : . space} as separator.</td>
 * </tr>
 * <tr>
 * <td><b>Day first</b></td>
 * <td>{@code dd/MM/yyyy}, {@code d MMM yyyy}, {@code 23 April 2026}</td>
 * <td>Numeric or textual month (abbreviated or full, case-insensitive).</td>
 * </tr>
 * <tr>
 * <td><b>Year first, textual month</b></td>
 * <td>{@code yyyy MMM dd}</td>
 * <td>Common in professional software output.</td>
 * </tr>
 * <tr>
 * <td><b>International/US</b></td>
 * <td>{@code MMM dd, yyyy} or {@code MMM dd yyyy}</td>
 * <td>Textual month first (e.g., {@code Jan 19, 2026}).</td>
 * </tr>
 * </table>
 *
 * <p>
 * Every non-ISO date may be followed by an optional time in 24-hour form ({@code H:mm}, {@code H:mm:ss}, {@code H:mm:ss.SSS}) or 12-hour form ({@code h:mm a}, {@code h:mm:ss a}, for example {@code 08:30 PM}).
 * </p>
 *
 * <p>
 * Not supported: two-digit years, timezone abbreviations such as {@code IST}, and offsets on non-ISO input. Input longer than {@value #MAX_INPUT_LENGTH} characters is rejected.
 * </p>
 *
 * @author Trevor Maggs
 * @version 1.5
 * @since 19 January 2026
 */
public final class SmartDateParser_v2
{
    /** Longest input accepted. Real dates are well under this, even with a region id. */
    private static final int MAX_INPUT_LENGTH = 100;

    /** Longest slice of the caller's input echoed back in an exception message. */
    private static final int MAX_ECHOED_LENGTH = 50;

    /** Characters accepted between the date fields of non-ISO input. */
    private static final String[] DATE_SEPARATORS = {"/", "-", ":", " ", "."};

    /**
     * EXIF writers emit all-zero dates when the camera clock was never set. Treated as "no date".
     */
    private static final Pattern ZERO_DATE = Pattern.compile("0{4}\\D0{1,2}\\D0{1,2}(\\D.*)?");

    /** Identifies {@code T}-delimited ISO-8601 input. */
    private static final Pattern ISO_START = Pattern.compile("\\d{4}-\\d{2}-\\d{2}[Tt]");

    private static final TemporalQuery<ZonedDateTime> TO_ZONED = new TemporalQuery<ZonedDateTime>()
    {
        @Override
        public ZonedDateTime queryFrom(TemporalAccessor temporal)
        {
            return ZonedDateTime.from(temporal);
        }
    };

    private static final TemporalQuery<LocalDateTime> TO_LOCAL_DATE_TIME = new TemporalQuery<LocalDateTime>()
    {
        @Override
        public LocalDateTime queryFrom(TemporalAccessor temporal)
        {
            return LocalDateTime.from(temporal);
        }
    };

    private static final TemporalQuery<LocalDate> TO_LOCAL_DATE = new TemporalQuery<LocalDate>()
    {
        @Override
        public LocalDate queryFrom(TemporalAccessor temporal)
        {
            return LocalDate.from(temporal);
        }
    };

    /**
     * ISO local date-time (seconds and fraction optional), followed by an optional offset in either
     * {@code +05:30} or {@code +0530}/{@code +05} form, and an optional bracketed region id.
     */
    private static final DateTimeFormatter ISO_FORMATTER = new DateTimeFormatterBuilder()
            .parseCaseInsensitive()
            .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .optionalStart().appendOffset("+HH:MM:ss", "Z").optionalEnd()
            .optionalStart().appendOffset("+HHmm", "Z").optionalEnd()
            .optionalStart().parseCaseSensitive().appendLiteral('[').appendZoneRegionId().appendLiteral(']').optionalEnd()
            .toFormatter(Locale.ENGLISH)
            .withResolverStyle(ResolverStyle.STRICT)
            .withChronology(IsoChronology.INSTANCE);

    /**
     * Immutable lists of non-ISO patterns, tried in order. They differ only in the numeric layout.
     */
    private static final List<DatePattern> DAY_FIRST_PATTERNS;
    private static final List<DatePattern> MONTH_FIRST_PATTERNS;

    static
    {
        // Field order: y = year, M = month, d = day. The regex is a cheap shape check only;
        // the formatter is the authority on what is actually valid.
        List<DatePattern> ymdNumeric = createPatterns("ymd", false, "\\d{4}%s\\d{1,2}%s\\d{1,2}"); // EXIF
                                                                                                   // /
                                                                                                   // year
                                                                                                   // first
        List<DatePattern> dmyNumeric = createPatterns("dMy", false, "\\d{1,2}%s\\d{1,2}%s\\d{4}"); // AU/UK/IN
                                                                                                   // numeric
        List<DatePattern> mdyNumeric = createPatterns("Mdy", false, "\\d{1,2}%s\\d{1,2}%s\\d{4}"); // US
                                                                                                   // numeric
        List<DatePattern> dmyText = createPatterns("dMy", true, "\\d{1,2}%s\\p{L}{3,9}%s\\d{4}"); // AU/UK/IN
                                                                                                  // textual
        List<DatePattern> ymdText = createPatterns("yMd", true, "\\d{4}%s\\p{L}{3,9}%s\\d{1,2}"); // year
                                                                                                  // first,
                                                                                                  // textual
        List<DatePattern> mdyText = createPatterns("Mdy", true, "\\p{L}{3,9}%s\\d{1,2},?%s\\d{4}"); // US
                                                                                                    // textual

        // Only the numeric day/month layout differs between the two lists
        List<DatePattern> dayFirst = new ArrayList<DatePattern>();
        dayFirst.addAll(ymdNumeric);
        dayFirst.addAll(dmyNumeric);
        dayFirst.addAll(dmyText);
        dayFirst.addAll(ymdText);
        dayFirst.addAll(mdyText);

        List<DatePattern> monthFirst = new ArrayList<DatePattern>();
        monthFirst.addAll(ymdNumeric);
        monthFirst.addAll(mdyNumeric);
        monthFirst.addAll(dmyText);
        monthFirst.addAll(ymdText);
        monthFirst.addAll(mdyText);

        DAY_FIRST_PATTERNS = Collections.unmodifiableList(dayFirst);
        MONTH_FIRST_PATTERNS = Collections.unmodifiableList(monthFirst);
    }

    /**
     * Pairs a regex shape check with the immutable formatters to try for it: first one with an
     * optional 24-hour time (which also covers date-only input), then one with a mandatory 12-hour
     * time and AM/PM marker.
     */
    private static final class DatePattern
    {
        private final Pattern regex;
        private final DateTimeFormatter[] formatters;

        private DatePattern(Pattern regex, DateTimeFormatter optional24HourTime, DateTimeFormatter twelveHourTime)
        {
            this.regex = regex;
            this.formatters = new DateTimeFormatter[]{optional24HourTime, twelveHourTime};
        }
    }

    /**
     * Default constructor will always throw an exception.
     *
     * @throws UnsupportedOperationException
     *         to indicate that instantiation is not supported
     */
    private SmartDateParser_v2()
    {
        throw new UnsupportedOperationException("Not intended for instantiation");
    }

    /**
     * Obtains a {@link Date} after parsing the specified input, interpreting offset-less input in
     * the JVM default time zone.
     *
     * <p>
     * Provided for legacy APIs that require {@link java.util.Date}. Prefer
     * {@link #convertToZonedDateTime(String, ZoneId)} for new code.
     * </p>
     *
     * @param input
     *        the date string to convert
     * @return the parsed timestamp, or {@code null} if the input is null, blank or an EXIF "zero
     *         date" (for example {@code 0000:00:00 00:00:00})
     *
     * @throws IllegalArgumentException
     *         if the input is too long, does not match any supported date format, or is not a valid
     *         date
     */
    public static Date convertToDate(String input)
    {
        return convertToDate(input, ZoneId.systemDefault());
    }

    /**
     * Obtains a {@link Date} after parsing the specified input, interpreting offset-less input in
     * {@code defaultZone}.
     *
     * @param input
     *        the date string to convert
     * @param defaultZone
     *        the zone applied when the input carries no offset or region
     * @return the parsed timestamp, or {@code null} if the input is null, blank or an EXIF "zero
     *         date"
     *
     * @throws IllegalArgumentException
     *         if the input is too long, does not match any supported date format, or is not a valid
     *         date
     * @throws NullPointerException
     *         if {@code defaultZone} is null
     */
    public static Date convertToDate(String input, ZoneId defaultZone)
    {
        return convertToDate(input, defaultZone, null);
    }

    /**
     * Obtains a {@link Date} after parsing the specified input, interpreting offset-less input in
     * {@code defaultZone} and using {@code inputLocale} to decide how ambiguous numeric dates such
     * as {@code 04/05/2026} are read.
     *
     * @param input
     *        the date string to convert
     * @param defaultZone
     *        the zone applied when the input carries no offset or region
     * @param inputLocale
     *        a locale whose short date pattern starts with the month (for example
     *        {@link Locale#US}) reads numeric dates as Month-Day-Year; {@code null} or any other
     *        locale keeps the default Day-Month-Year order
     * @return the parsed timestamp, or {@code null} if the input is null, blank or an EXIF "zero
     *         date"
     *
     * @throws IllegalArgumentException
     *         if the input is too long, does not match any supported date format, or is not a valid
     *         date
     * @throws NullPointerException
     *         if {@code defaultZone} is null
     */
    public static Date convertToDate(String input, ZoneId defaultZone, Locale inputLocale)
    {
        ZonedDateTime zdt = convertToZonedDateTime(input, defaultZone, inputLocale);

        return (zdt != null ? Date.from(zdt.toInstant()) : null);
    }

    /**
     * Parses a raw date string into a {@link LocalDateTime} exactly as written. No time zone is
     * applied, so the result is never shifted by daylight saving gaps or overlaps. If the input
     * carries an offset or region, it is ignored and the written wall-clock time is returned.
     *
     * @param input
     *        the raw date string to parse
     * @return the parsed local date and time (start of day if no time was given), or {@code null}
     *         if the input is null, blank or an EXIF "zero date"
     *
     * @throws IllegalArgumentException
     *         if the input is too long, does not match any supported date format, or is not a valid
     *         date
     */
    public static LocalDateTime convertToLocalDateTime(String input)
    {
        return convertToLocalDateTime(input, null);
    }

    /**
     * Parses a raw date string into a {@link LocalDateTime} exactly as written, using
     * {@code inputLocale} to decide how ambiguous numeric dates such as {@code 04/05/2026} are
     * read. No time zone is applied.
     *
     * @param input
     *        the raw date string to parse
     * @param inputLocale
     *        a locale whose short date pattern starts with the month (for example
     *        {@link Locale#US}) reads numeric dates as Month-Day-Year; {@code null} or any other
     *        locale keeps the default Day-Month-Year order
     * @return the parsed local date and time (start of day if no time was given), or {@code null}
     *         if the input is null, blank or an EXIF "zero date"
     *
     * @throws IllegalArgumentException
     *         if the input is too long, does not match any supported date format, or is not a valid
     *         date
     */
    public static LocalDateTime convertToLocalDateTime(String input, Locale inputLocale)
    {
        TemporalAccessor parsed = parse(input, inputLocale);

        if (parsed == null)
        {
            return null;
        }

        return (parsed instanceof ZonedDateTime) ? ((ZonedDateTime) parsed).toLocalDateTime() : (LocalDateTime) parsed;
    }

    /**
     * Parses a raw date string into a {@link ZonedDateTime}, interpreting offset-less input in the
     * JVM default time zone.
     *
     * @param input
     *        the raw date string, such as {@code 23/04/2026} or {@code 2026-04-23T20:00:00Z}
     * @return the parsed value, or {@code null} if the input is null, blank or an EXIF "zero date"
     *
     * @throws IllegalArgumentException
     *         if the input is too long, does not match any supported date format, or is not a valid
     *         date
     */
    public static ZonedDateTime convertToZonedDateTime(String input)
    {
        return convertToZonedDateTime(input, ZoneId.systemDefault());
    }

    /**
     * Parses a raw date string into a {@link ZonedDateTime}.
     *
     * <p>
     * ISO-8601 input that carries an offset or region keeps it. All other input is interpreted in
     * {@code defaultZone}. Supplying the source zone (for example {@code Asia/Kolkata}) avoids
     * silently depending on the server's zone.
     * </p>
     *
     * @param input
     *        the raw date string
     * @param defaultZone
     *        the zone applied when the input carries no offset or region
     * @return the parsed value, or {@code null} if the input is null, blank or an EXIF "zero date"
     *
     * @throws IllegalArgumentException
     *         if the input is too long, does not match any supported date format, or is not a valid
     *         date;
     *         when a pattern recognised the input but parsing failed, the underlying
     *         {@link DateTimeException} is attached as the cause
     * @throws NullPointerException
     *         if {@code defaultZone} is null
     */
    public static ZonedDateTime convertToZonedDateTime(String input, ZoneId defaultZone)
    {
        return convertToZonedDateTime(input, defaultZone, null);
    }

    /**
     * Parses a raw date string into a {@link ZonedDateTime}, using {@code inputLocale} to decide
     * how ambiguous numeric dates such as {@code 04/05/2026} are read.
     *
     * <p>
     * ISO-8601 input that carries an offset or region keeps it. All other input is interpreted in
     * {@code defaultZone}.
     * </p>
     *
     * @param input
     *        the raw date string
     * @param defaultZone
     *        the zone applied when the input carries no offset or region
     * @param inputLocale
     *        a locale whose short date pattern starts with the month (for example
     *        {@link Locale#US}) reads numeric dates as Month-Day-Year; {@code null} or any other
     *        locale keeps the default Day-Month-Year order
     * @return the parsed value, or {@code null} if the input is null, blank or an EXIF "zero date"
     *
     * @throws IllegalArgumentException
     *         if the input is too long, does not match any supported date format, or is not a valid
     *         date;
     *         when a pattern recognised the input but parsing failed, the underlying
     *         {@link DateTimeException} is attached as the cause
     * @throws NullPointerException
     *         if {@code defaultZone} is null
     */
    public static ZonedDateTime convertToZonedDateTime(String input, ZoneId defaultZone, Locale inputLocale)
    {
        Objects.requireNonNull(defaultZone, "defaultZone");

        TemporalAccessor parsed = parse(input, inputLocale);

        if (parsed == null)
        {
            return null;
        }

        return (parsed instanceof ZonedDateTime) ? (ZonedDateTime) parsed : ((LocalDateTime) parsed).atZone(defaultZone);
    }

    /**
     * Formats a raw date string as a localised, human-readable timestamp using the specified
     * {@link Locale}.
     *
     * <p>
     * The locale controls the <b>output</b> only. The input is always read with the default
     * Day-Month-Year order; to read month-first input, parse it with
     * {@link #convertToZonedDateTime(String, ZoneId, Locale)} and pass the result to
     * {@link #convertToLocalisedDateTime(ZonedDateTime, Locale)}.
     * </p>
     *
     * @param input
     *        the raw date string to parse and format
     * @param locale
     *        the locale used for localisation rules, or {@code null} for the JVM default format
     *        locale
     * @return the localised string, or {@code null} if the input is null, blank or an EXIF "zero
     *         date"
     *
     * @throws IllegalArgumentException
     *         if the input is too long, does not match any supported date format, or is not a valid
     *         date
     */
    public static String convertToLocalisedDateTime(String input, Locale locale)
    {
        return convertToLocalisedDateTime(convertToZonedDateTime(input), locale);
    }

    /**
     * Formats a {@link ZonedDateTime} as a localised, human-readable timestamp using MEDIUM date
     * and SHORT time styles, for example {@code 23 Apr 2026, 8:00 pm} (en_AU).
     *
     * <p>
     * The formatter is created per call on purpose: a cache keyed by caller-supplied locales
     * would be an unbounded map.
     * </p>
     *
     * <p>
     * The exact output is defined by the JDK's locale data and varies between versions, so avoid
     * asserting on the literal string in tests.
     * </p>
     *
     * @param dateTime
     *        the date and time to format
     * @param locale
     *        the locale used for localisation rules, or {@code null} for the JVM default format
     *        locale
     * @return the localised string, or {@code null} if {@code dateTime} is null; callers decide
     *         what to display for a missing value
     */
    public static String convertToLocalisedDateTime(ZonedDateTime dateTime, Locale locale)
    {
        if (dateTime == null)
        {
            return null;
        }

        Locale targetLocale = (locale != null) ? locale : Locale.getDefault(Locale.Category.FORMAT);

        return dateTime.format(DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT).withLocale(targetLocale));
    }

    /**
     * Core parser shared by all public entry points.
     *
     * @param input
     *        the raw, untrimmed caller input; may be null
     * @param inputLocale
     *        decides how ambiguous numeric dates are read; may be null (day-first)
     * @return a {@link ZonedDateTime} if the input carried an offset or region, otherwise a
     *         {@link LocalDateTime}; or {@code null} for null, blank or EXIF zero-date input
     *
     * @throws IllegalArgumentException
     *         if the input is too long or cannot be parsed
     */
    private static TemporalAccessor parse(String input, Locale inputLocale)
    {
        if (input == null)
        {
            return null;
        }

        // Java 8 trim() ignores non-breaking spaces, which often appear in copied text
        String text = input.replace('\u00A0', ' ').trim();

        if (text.isEmpty())
        {
            return null;
        }

        if (text.length() > MAX_INPUT_LENGTH)
        {
            throw new IllegalArgumentException("Date input too long [" + text.length() + " characters, maximum " + MAX_INPUT_LENGTH + "]");
        }

        if (ZERO_DATE.matcher(text).matches())
        {
            return null;
        }

        // The first failure is kept as the cause: ISO is tried first and patterns are ordered by
        // specificity, so it is the most relevant explanation
        DateTimeException firstFailure = null;

        if (ISO_START.matcher(text).lookingAt())
        {
            try
            {
                return ISO_FORMATTER.parseBest(text, TO_ZONED, TO_LOCAL_DATE_TIME);
            }

            catch (DateTimeException exc)
            {
                firstFailure = exc;
            }
        }

        // "Jan 19, 2026" -> "Jan 19 2026"; the formatters do not model the optional comma
        String withoutCommas = (text.indexOf(',') >= 0) ? text.replace(",", "") : text;

        List<DatePattern> patterns = isMonthFirst(inputLocale) ? MONTH_FIRST_PATTERNS : DAY_FIRST_PATTERNS;

        for (DatePattern pattern : patterns)
        {
            if (!pattern.regex.matcher(text).lookingAt())
            {
                continue;
            }

            for (DateTimeFormatter formatter : pattern.formatters)
            {
                try
                {
                    TemporalAccessor parsed = formatter.parseBest(withoutCommas, TO_LOCAL_DATE_TIME, TO_LOCAL_DATE);

                    // Date-only input: use midnight
                    return (parsed instanceof LocalDate) ? ((LocalDate) parsed).atStartOfDay() : parsed;
                }

                catch (DateTimeException exc)
                {
                    if (firstFailure == null)
                    {
                        firstFailure = exc;
                    }
                }
            }
        }

        throw new IllegalArgumentException("Unsupported date format [" + describe(input) + "]", firstFailure);
    }

    /**
     * Creates one pattern per supported date separator for a single layout.
     *
     * @param order
     *        field order, using y, M and d
     * @param textualMonth
     *        {@code true} if the month is a name, {@code false} if it is a number
     * @param regexTemplate
     *        shape check with two {@code %s} placeholders for the separator
     * @return the patterns for this layout, one per separator
     */
    private static List<DatePattern> createPatterns(String order, boolean textualMonth, String regexTemplate)
    {
        List<DatePattern> list = new ArrayList<DatePattern>();

        for (String separator : DATE_SEPARATORS)
        {
            String quoted = Pattern.quote(separator);
            Pattern regex = Pattern.compile(String.format(regexTemplate, quoted, quoted));

            list.add(new DatePattern(regex, buildFormatter(order, textualMonth, separator, false), buildFormatter(order, textualMonth, separator, true)));
        }

        return list;
    }

    /**
     * Decides whether numeric dates should be read month-first, from the first date field of the
     * locale's short date pattern (for example {@code M/d/yy} for US English). The pattern comes
     * from the JDK's locale data, so it can differ between JDK versions.
     *
     * @param locale
     *        the caller's input locale; may be null
     * @return {@code true} only if the locale's short date pattern starts with the month; {@code
     *         false} for null and for every other pattern, which keeps the day-first default
     */
    private static boolean isMonthFirst(Locale locale)
    {
        if (locale == null)
        {
            return false;
        }

        String pattern = DateTimeFormatterBuilder.getLocalizedDateTimePattern(FormatStyle.SHORT, null, IsoChronology.INSTANCE, locale);
        boolean quoted = false;

        for (int i = 0; i < pattern.length(); i++)
        {
            char ch = pattern.charAt(i);

            if (ch == '\'')
            {
                quoted = !quoted;
            }

            else if (!quoted)
            {
                if (ch == 'M' || ch == 'L')
                {
                    return true;
                }

                if (ch == 'd' || ch == 'y' || ch == 'u')
                {
                    return false;
                }
            }
        }

        return false;
    }

    /**
     * Makes caller-supplied text safe to put in an exception message: control characters (such as
     * newlines, which could forge log lines) are replaced and the length is capped.
     *
     * @param input
     *        the raw caller input
     * @return a sanitised, possibly truncated copy that is safe to log
     */
    private static String describe(String input)
    {
        int length = Math.min(input.length(), MAX_ECHOED_LENGTH);
        StringBuilder sb = new StringBuilder(length + 3);

        for (int i = 0; i < length; i++)
        {
            char ch = input.charAt(i);

            sb.append(Character.isISOControl(ch) ? '?' : ch);
        }

        if (input.length() > length)
        {
            sb.append("...");
        }

        return sb.toString();
    }

    /**
     * Builds a strict, case-insensitive formatter for one layout and separator. The 24-hour variant
     * treats the time as optional (so it also handles date-only input); the 12-hour variant
     * requires
     * an AM/PM marker.
     *
     * @param order
     *        field order, using y, M and d
     * @param textualMonth
     *        {@code true} if the month is a name, {@code false} if it is a number
     * @param separator
     *        the literal placed between the date fields
     * @param twelveHour
     *        {@code true} for the mandatory 12-hour time variant, {@code false} for the optional
     *        24-hour time variant
     * @return an immutable, thread-safe formatter
     */
    private static DateTimeFormatter buildFormatter(String order, boolean textualMonth, String separator, boolean twelveHour)
    {
        DateTimeFormatterBuilder builder = new DateTimeFormatterBuilder().parseCaseInsensitive();

        for (int i = 0; i < order.length(); i++)
        {
            if (i > 0)
            {
                builder.appendLiteral(separator);
            }

            switch (order.charAt(i))
            {
                case 'y':
                    builder.appendValue(ChronoField.YEAR, 4);
                break;

                case 'd':
                    builder.appendValue(ChronoField.DAY_OF_MONTH, 1, 2, SignStyle.NOT_NEGATIVE);
                break;

                default:
                    appendMonth(builder, textualMonth);
            }
        }

        if (twelveHour)
        {
            builder.appendLiteral(' ')
                    .appendValue(ChronoField.CLOCK_HOUR_OF_AMPM, 1, 2, SignStyle.NOT_NEGATIVE)
                    .appendLiteral(':')
                    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                    .optionalStart().appendLiteral(':').appendValue(ChronoField.SECOND_OF_MINUTE, 2).optionalEnd()
                    .optionalStart().appendLiteral(' ').optionalEnd()
                    .appendText(ChronoField.AMPM_OF_DAY, TextStyle.SHORT);
        }

        else
        {
            builder.optionalStart()
                    .appendLiteral(' ')
                    .appendValue(ChronoField.HOUR_OF_DAY, 1, 2, SignStyle.NOT_NEGATIVE)
                    .appendLiteral(':')
                    .appendValue(ChronoField.MINUTE_OF_HOUR, 2)
                    .optionalStart()
                    .appendLiteral(':')
                    .appendValue(ChronoField.SECOND_OF_MINUTE, 2)
                    .optionalStart().appendFraction(ChronoField.NANO_OF_SECOND, 0, 9, true).optionalEnd()
                    .optionalEnd()
                    .optionalEnd();
        }

        return builder.toFormatter(Locale.ENGLISH).withResolverStyle(ResolverStyle.STRICT).withChronology(IsoChronology.INSTANCE);
    }

    /**
     * Appends a month field. Textual months accept the full name or the three-letter abbreviation;
     * the full name is attempted first so that, for example, "March" is not consumed as "Mar".
     *
     * @param builder
     *        the builder to append to
     * @param textual
     *        {@code true} for a month name, {@code false} for a one or two digit number
     */
    private static void appendMonth(DateTimeFormatterBuilder builder, boolean textual)
    {
        if (textual)
        {
            builder.optionalStart().appendText(ChronoField.MONTH_OF_YEAR, TextStyle.FULL).optionalEnd()
                    .optionalStart().appendText(ChronoField.MONTH_OF_YEAR, TextStyle.SHORT).optionalEnd();
        }

        else
        {
            builder.appendValue(ChronoField.MONTH_OF_YEAR, 1, 2, SignStyle.NOT_NEGATIVE);
        }
    }
}