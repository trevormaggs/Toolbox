package util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Converts binary data, character arrays, Strings, and ByteBuffers into hexadecimal Strings.
 *
 * <p>
 * Supports both lowercase {@code (a-f)} and uppercase {@code (A-F)} hexadecimal representations,
 * alongside custom charsets for text-based inputs.
 * </p>
 */
public final class HexEncoder
{
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    // Hex digit lookup tables for performance and casing flexibility
    private static final char[] DIGITS_LOWER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final char[] DIGITS_UPPER = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    private final Charset charset;
    private final boolean upperCase;

    /**
     * Creates a new encoder with the default charset {@link #DEFAULT_CHARSET} and lowercase digits.
     */
    private HexEncoder()
    {
        this.charset = DEFAULT_CHARSET;
        this.upperCase = false;
    }

    /**
     * Creates a new encoder with the given Charset and uppercase setting.
     *
     * @param charset
     *        the charset to use for text inputs; must not be null
     * @param upperCase
     *        {@code true} for uppercase hex digits (A-F), {@code false} for lowercase (a-f)
     */
    private HexEncoder(Charset charset, boolean upperCase)
    {
        this.charset = Objects.requireNonNull(charset, "Charset must not be null");
        this.upperCase = upperCase;
    }

    /**
     * Creates a new encoder with the given charset name and uppercase setting.
     *
     * @param charsetName
     *        the name of the charset to use for text inputs
     * @param upperCase
     *        {@code true} for uppercase hex digits (A-F), {@code false} for lowercase (a-f)
     */
    private HexEncoder(String charsetName, boolean upperCase)
    {
        this(Charset.forName(charsetName), upperCase);
    }

    // =========================================================================
    // PUBLIC STATIC ENTRY POINTS
    // =========================================================================

    /**
     * Universal static encoder that accepts a {@link String}, {@code byte[]}, {@link ByteBuffer},
     * or {@code char[]} and encodes it into a lowercase hexadecimal string using the default charset (UTF-8).
     *
     * @param object
     *        either a String, byte[], ByteBuffer, or char[] to encode
     * @return a lowercase hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the input object is null
     * @throws IllegalArgumentException
     *         если the input type is unsupported
     */
    public static String encode(Object object)
    {
        return encode(object, DEFAULT_CHARSET, false);
    }

    /**
     * Universal static encoder that accepts a {@link String}, {@code byte[]}, {@link ByteBuffer},
     * or {@code char[]} and encodes it into a hexadecimal string using the specified charset and lowercase digits.
     *
     * @param object
     *        the input object to encode
     * @param charset
     *        the Charset to use if the input is textual (String, char[])
     * @return a lowercase hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the input object or charset is null
     * @throws IllegalArgumentException
     *         if the input type is unsupported
     */
    public static String encode(Object object, Charset charset)
    {
        return encode(object, charset, false);
    }

    /**
     * Universal static encoder that accepts a {@link String}, {@code byte[]}, {@link ByteBuffer},
     * or {@code char[]} and encodes it into a hexadecimal string using the specified charset and casing.
     *
     * @param object
     *        the input object to encode
     * @param charset
     *        the Charset to use if the input is textual (String, char[])
     * @param upperCase
     *        {@code true} for uppercase hex digits (A-F), {@code false} for lowercase (a-f)
     * @return a hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the input object or charset is null
     * @throws IllegalArgumentException
     *         if the input type is unsupported
     */
    public static String encode(Object object, Charset charset, boolean upperCase)
    {
        Objects.requireNonNull(charset, "Charset must not be null");
        return new HexEncoder(charset, upperCase).convert(object);
    }

    // =========================================================================
    // PRIVATE INSTANCE METHODS (Hidden implementation details)
    // =========================================================================

    /**
     * Routes and converts the incoming object based on its runtime type into raw bytes, then encodes those bytes into a hexadecimal String.
     *
     * @param object
     *        the input object to encode
     * @return the hexadecimal string representation
     */
    private String convert(final Object object)
    {
        Objects.requireNonNull(object, "Input object must not be null");
        byte[] rawBytes;

        if (object instanceof byte[])
        {
            rawBytes = (byte[]) object;
        }
        
        else if (object instanceof ByteBuffer)
        {
            ByteBuffer buffer = (ByteBuffer) object;
            rawBytes = new byte[buffer.remaining()];
            buffer.duplicate().get(rawBytes);
        }
        
        else if (object instanceof String)
        {
            rawBytes = ((String) object).getBytes(charset);
        }
        
        else if (object instanceof char[])
        {
            rawBytes = new String((char[]) object).getBytes(charset);
        }
        
        else
        {
            throw new IllegalArgumentException("Unsupported input type for hex encoding: " + object.getClass().getName());
        }

        return encodeBytesToString(rawBytes);
    }

    /**
     * Converts a raw byte array into a hexadecimal String using the appropriate digit lookup table.
     *
     * @param data
     *        the raw byte array
     * @return the encoded hexadecimal string
     */
    private String encodeBytesToString(byte[] data)
    {
        Objects.requireNonNull(data, "Byte array data must not be null");

        int l = data.length;
        char[] out = new char[l << 1];
        char[] digits = upperCase ? DIGITS_UPPER : DIGITS_LOWER;
        
        for (int i = 0, j = 0; i < l; i++)
        {
            out[j++] = digits[(0xF0 & data[i]) >>> 4];
            out[j++] = digits[0x0F & data[i]];
        }

        return new String(out);
    }

    @Override
    public String toString()
    {
        return super.toString() + "[charsetName=" + this.charset + ", upperCase=" + this.upperCase + "]";
    }
}