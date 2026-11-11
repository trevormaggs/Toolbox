package util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Converts hexadecimal Strings, character arrays, byte arrays, and ByteBuffers into binary byte
 * arrays.
 *
 * <p>
 * Decoding accepts only the ASCII hexadecimal characters {@code 0-9}, {@code A-F}, and {@code a-f}.
 * Non-ASCII Unicode digits, fullwidth letters, and odd-length inputs are rejected with an
 * exception. Supports leading hash notation (e.g., RGB values like {@code #3478F0}).
 * </p>
 */
public final class HexByteConversion
{
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private final Charset charset;

    /**
     * Creates a new codec with the default charset {@link #DEFAULT_CHARSET}.
     */
    private HexByteConversion()
    {
        this.charset = DEFAULT_CHARSET;
    }

    /**
     * Creates a new codec with the given Charset.
     *
     * @param charset
     *        the charset to use for decoding binary representations; must not be null
     */
    private HexByteConversion(Charset charset)
    {
        this.charset = Objects.requireNonNull(charset, "Charset must not be null");
    }

    /**
     * Creates a new codec with the given charset name.
     *
     * @param charsetName
     *        the name of the charset to use for decoding
     */
    private HexByteConversion(String charsetName)
    {
        this(Charset.forName(charsetName));
    }

    // =========================================================================
    // PUBLIC STATIC ENTRY POINTS
    // =========================================================================

    /**
     * Universal static decoder that accepts a {@link String}, {@code byte[]}, {@link ByteBuffer},
     * or {@code char[]} representing hexadecimal values and returns their decoded byte array using
     * the default charset (UTF-8).
     *
     * @param object
     *        either a String, byte[], ByteBuffer, or char[] containing hex digits
     * @return an array of bytes containing the decoded binary data
     * 
     * @throws NullPointerException
     *         if the input object is null
     * @throws IllegalArgumentException
     *         if the input type is unsupported, has an odd length, or contains invalid characters
     */
    public static byte[] decode(Object object)
    {
        return decode(object, DEFAULT_CHARSET);
    }

    /**
     * Universal static decoder that accepts a {@link String}, {@code byte[]}, {@link ByteBuffer},
     * or {@code char[]} representing hexadecimal values and returns their decoded byte array using
     * the specified charset.
     *
     * @param object
     *        either a String, byte[], ByteBuffer, or char[] containing hex digits
     * @param charset
     *        the Charset to use for decoding binary inputs
     * @return an array of bytes containing the decoded binary data
     * 
     * @throws NullPointerException
     *         if the input object or charset is null
     * @throws IllegalArgumentException
     *         if the input type is unsupported, has an odd length, or contains invalid characters
     */
    public static byte[] decode(Object object, Charset charset)
    {
        Objects.requireNonNull(charset, "Charset must not be null");
        return new HexByteConversion(charset).convert(object);
    }

    // =========================================================================
    // PRIVATE INSTANCE METHODS (Hidden implementation details)
    // =========================================================================

    /**
     * Routes and converts the incoming object based on its runtime type into a decoded byte array.
     *
     * @param object
     *        the input object to convert
     * @return the decoded byte array
     */
    private byte[] convert(final Object object)
    {
        Objects.requireNonNull(object, "Input object must not be null");

        if (object instanceof String)
        {
            return decodeCharArray(normalize((String) object));
        }

        if (object instanceof char[])
        {
            return decodeCharArray(normalize((char[]) object));
        }

        if (object instanceof byte[])
        {
            return decodeBytes((byte[]) object);
        }

        if (object instanceof ByteBuffer)
        {
            return decodeBuffer((ByteBuffer) object);
        }

        throw new IllegalArgumentException("Unsupported input type for hex decoding: " + object.getClass().getName());
    }

    /**
     * Normalises a String by stripping a leading '#' if present and converting it to a character
     * array. For example, RGB colours typically denote something like "#3478F0"
     * 
     * @param data
     *        the input string
     * @return the normalised character array
     */
    private char[] normalize(String data)
    {
        Objects.requireNonNull(data, "String data must not be null");
        return normalize(data.trim().toCharArray());
    }

    /**
     * Normalises a character array by stripping a leading '#' if present.
     *
     * @param data
     *        the input character array
     * @return the normalised character array
     */
    private char[] normalize(char[] data)
    {
        Objects.requireNonNull(data, "Character array data must not be null");

        if (data.length > 0 && data[0] == '#')
        {
            char[] trimmed = new char[data.length - 1];
            System.arraycopy(data, 1, trimmed, 0, trimmed.length);

            return trimmed;
        }

        return data;
    }

    /**
     * Decodes a normalised character array into a raw byte array.
     *
     * @param data
     *        the character array containing hex digits
     * @return the decoded byte array
     */
    private byte[] decodeCharArray(char[] data)
    {
        Objects.requireNonNull(data, "Character array data must not be null");

        byte[] result = new byte[data.length >> 1];
        decodeHex(data, result, 0);

        return result;
    }

    /**
     * Decodes a byte array by interpreting its bytes through the configured charset.
     *
     * @param data
     *        the byte array containing hex character representations
     * @return the decoded binary byte array
     */
    private byte[] decodeBytes(byte[] data)
    {
        Objects.requireNonNull(data, "Byte array data must not be null");
        return decodeCharArray(new String(data, charset).toCharArray());
    }

    /**
     * Decodes a ByteBuffer into a binary byte array.
     *
     * @param buffer
     *        the ByteBuffer containing hex data
     * @return the decoded binary byte array
     */
    private byte[] decodeBuffer(ByteBuffer buffer)
    {
        Objects.requireNonNull(buffer, "ByteBuffer must not be null");

        byte[] bytes = new byte[buffer.remaining()];
        buffer.duplicate().get(bytes);

        return decodeBytes(bytes);
    }

    /**
     * Retrieves the result by computing the specified input containing hexadecimal values and
     * producing an array of bytes with those same values.
     * 
     * Since it takes two characters to represent any given byte, the returned array will be half
     * the length of that input array.
     *
     * @param data
     *        an array of characters containing hexadecimal digits
     * @param result
     *        a byte array to contain the binary data decoded from the specified char array
     * @param offset
     *        the position within {@code result} to start writing the decoded bytes
     * @return the number of bytes written to {@code result}
     *
     * @throws IllegalArgumentException
     *         if the number of characters is odd or the output array is too small
     */
    private int decodeHex(char[] data, byte[] result, int offset)
    {
        int len = data.length;

        if ((len & 1) != 0)
        {
            throw new IllegalArgumentException(String.format("Odd number of characters %,d.", len));
        }

        int resultLen = len >> 1;

        if (result.length - offset < resultLen)
        {
            throw new IllegalArgumentException("Output array is not large enough to accommodate decoded data.");
        }

        for (int i = offset, j = 0; j < len; i++, j += 2)
        {
            int high = toInteger(data[j]) << 4;
            int low = toInteger(data[j + 1]);

            result[i] = (byte) (high | low);
        }

        return resultLen;
    }

    /**
     * Converts a hexadecimal character to its integer value equivalent.
     *
     * @param ch
     *        the character to convert (0-9, A-F, a-f)
     * @return the integer value of the hex digit (0-15)
     * 
     * @throws IllegalStateException
     *         if an unrecognised character is encountered
     */
    private int toInteger(char ch)
    {
        if (ch >= '0' && ch <= '9')
        {
            return ch - '0';
        }

        if (ch >= 'A' && ch <= 'F')
        {
            return ch - 'A' + 10;
        }

        if (ch >= 'a' && ch <= 'f')
        {
            return ch - 'a' + 10;
        }

        throw new IllegalStateException(String.format("Unrecognised hexadecimal character 0x%02X detected.", ch & 0xFFFF));
    }

    @Override
    public String toString()
    {
        return super.toString() + "[charsetName=" + this.charset + "]";
    }
}