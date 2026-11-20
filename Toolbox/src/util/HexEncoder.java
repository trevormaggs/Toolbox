package util;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Encodes byte arrays, ByteBuffers, character arrays, and Strings into hexadecimal strings.
 *
 * <p>
 * Supports lowercase ({@code a-f}) and uppercase ({@code A-F}) hexadecimal representations. Text-based inputs can be encoded using a specified charset, with UTF-8 used by default.
 * </p>
 */
public final class HexEncoder
{
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
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
     * Encodes a byte array into a hexadecimal string using the configured letter case.
     *
     * @param data
     *        the byte array to encode
     * @return the hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the byte array is null
     */
    public String encode(byte[] data)
    {
        return toHexString(data, upperCase);
    }

    /**
     * Encodes a character array into a hexadecimal string using the configured charset and letter
     * case.
     *
     * @param data
     *        the character array to encode; must not be null
     * @return the hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the character array is null
     */
    public String encode(char[] data)
    {
        Objects.requireNonNull(data, "Character array must not be null");

        ByteBuffer buffer = charset.encode(CharBuffer.wrap(data));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return toHexString(bytes, upperCase);
    }

    /**
     * Encodes the remaining bytes of a ByteBuffer into a hexadecimal string.
     *
     * <p>
     * The buffer's position and limit remain unchanged.
     * </p>
     *
     * @param buffer
     *        the ByteBuffer to encode; must not be null
     * @return the hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the buffer is null
     */
    public String encode(ByteBuffer buffer)
    {
        Objects.requireNonNull(buffer, "Buffer must not be null");

        ByteBuffer duplicate = buffer.duplicate();
        byte[] data = new byte[duplicate.remaining()];
        duplicate.get(data);

        return toHexString(data, upperCase);
    }

    /**
     * Encodes a string into a hexadecimal string using the configured charset.
     *
     * @param data
     *        the string to encode; must not be null
     * @return the hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the string is null
     */
    public String encode(String data)
    {
        Objects.requireNonNull(data, "String value must not be null");

        return toHexString(data.getBytes(charset), upperCase);
    }

    // =========================================================================
    // PUBLIC STATIC ENTRY POINTS
    // =========================================================================

    /**
     * Creates an encoder using UTF-8 and lowercase hexadecimal digits.
     *
     * @return a new HexEncoder instance
     */
    public static HexEncoder getInstance()
    {
        return new HexEncoder();
    }

    /**
     * Creates an encoder using the specified charset and hexadecimal letter case.
     *
     * @param charsetName
     *        the charset to use when converting characters into bytes. It must not be null
     * @param upperCase
     *        {@code true} for uppercase hexadecimal digits (A-F) or {@code false} for lowercase
     *        hexadecimal digits (a-f)
     * 
     * @return a new HexEncoder instance
     */
    public static HexEncoder getInstance(String charsetName, boolean upperCase)
    {
        return new HexEncoder(charsetName, upperCase);
    }

    /**
     * Creates an encoder using the specified charset and hexadecimal letter case.
     *
     * @param charset
     *        the charset to use when converting characters into bytes. It must not be null
     * @param upperCase
     *        {@code true} for uppercase hexadecimal digits (A-F) or {@code false} for lowercase
     *        hexadecimal digits (a-f)
     * 
     * @return a new HexEncoder instance
     */
    public static HexEncoder getInstance(Charset charset, boolean upperCase)
    {
        return new HexEncoder(charset, upperCase);
    }

    /**
     * Encodes a character array into a hexadecimal string using the specified charset.
     *
     * @param data
     *        the character array to encode. It must not be null
     * @param charset
     *        the charset to use when converting characters into bytes. It must not be null
     * @param upperCase
     *        {@code true} for uppercase hexadecimal digits (A-F),
     *        {@code false} for lowercase hexadecimal digits (a-f)
     * @return a hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the character array or charset is null
     */
    public static String encode(char[] data, Charset charset, boolean upperCase)
    {
        Objects.requireNonNull(data, "Character array must not be null");
        Objects.requireNonNull(charset, "Charset must not be null");

        ByteBuffer buffer = charset.encode(CharBuffer.wrap(data));
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);

        return encode(bytes, upperCase);
    }

    /**
     * Encodes a string into a hexadecimal string using the specified charset.
     *
     * @param data
     *        the string to encode. It must not be null
     * @param charset
     *        the charset to use when converting the string into bytes. It must not be null
     * @param upperCase
     *        {@code true} for uppercase hexadecimal digits (A-F),
     *        {@code false} for lowercase hexadecimal digits (a-f)
     * @return a hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the string or charset is null
     */
    public static String encode(String data, Charset charset, boolean upperCase)
    {
        Objects.requireNonNull(data, "String value must not be null");
        Objects.requireNonNull(charset, "Charset must not be null");

        return encode(data.getBytes(charset), upperCase);
    }

    /**
     * Encodes a byte array into a hexadecimal string.
     *
     * @param data
     *        the byte array to encode. It must not be null
     * @param upperCase
     *        {@code true} for uppercase hexadecimal digits (A-F),
     *        {@code false} for lowercase hexadecimal digits (a-f)
     * @return a hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the byte array is null
     */
    public static String encode(byte[] data, boolean upperCase)
    {
        return toHexString(data, upperCase);
    }

    /**
     * Encodes the remaining bytes of a ByteBuffer into a hexadecimal string.
     *
     * <p>
     * The buffer's position and limit remain unchanged.
     * </p>
     *
     * @param buffer
     *        the ByteBuffer to encode. It must not be null
     * @param upperCase
     *        {@code true} for uppercase hexadecimal digits (A-F),
     *        {@code false} for lowercase hexadecimal digits (a-f)
     * @return a hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the buffer is null
     */
    public static String encode(ByteBuffer buffer, boolean upperCase)
    {
        Objects.requireNonNull(buffer, "Buffer must not be null");

        ByteBuffer duplicate = buffer.duplicate();
        byte[] data = new byte[duplicate.remaining()];
        duplicate.get(data);

        return toHexString(data, upperCase);
    }

    /**
     * Encodes a character array into a hexadecimal string using UTF-8.
     *
     * @param data
     *        the character array to encode. It must not be null
     * @param upperCase
     *        {@code true} for uppercase hexadecimal digits (A-F),
     *        {@code false} for lowercase hexadecimal digits (a-f)
     * @return a hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the character array is null
     */
    public static String encode(char[] data, boolean upperCase)
    {
        return encode(data, DEFAULT_CHARSET, upperCase);
    }

    /**
     * Encodes a string into a hexadecimal string using UTF-8.
     *
     * @param data
     *        the string to encode. It must not be null
     * @param upperCase
     *        {@code true} for uppercase hexadecimal digits (A-F),
     *        {@code false} for lowercase hexadecimal digits (a-f)
     * @return a hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the string is null
     */
    public static String encode(String data, boolean upperCase)
    {
        return encode(data, DEFAULT_CHARSET, upperCase);
    }

    /**
     * Encodes a string into a lowercase hexadecimal string using the specified charset.
     * 
     * @param data
     *        the string to encode. It must not be null
     * @param charset
     *        the charset to use when converting the string into bytes; must not be null
     * @return a lowercase hexadecimal string representation
     * 
     * @throws NullPointerException
     *         if the string or charset is null
     */
    public static String encode(String data, Charset charset)
    {
        return encode(data, charset, false);
    }

    /**
     * Converts a byte array into a hexadecimal string.
     * 
     * @param data
     *        the byte array to encode
     * @param upperCase
     *        {@code true} for uppercase hexadecimal digits (A-F),
     *        {@code false} for lowercase hexadecimal digits (a-f)
     * @return the hexadecimal string
     */
    private static String toHexString(byte[] data, boolean upperCase)
    {
        Objects.requireNonNull(data, "Data must not be null");

        if (data.length > Integer.MAX_VALUE / 2)
        {
            throw new IllegalArgumentException("Input data is too large to encode");
        }

        char[] digits = upperCase ? DIGITS_UPPER : DIGITS_LOWER;
        char[] result = new char[data.length << 1];

        for (int i = 0, j = 0; i < data.length; i++)
        {
            result[j++] = digits[(data[i] & 0xF0) >>> 4];
            result[j++] = digits[data[i] & 0x0F];
        }

        return new String(result);
    }
}