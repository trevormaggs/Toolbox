package util;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/**
 * Converts hexadecimal Strings and binary representations into byte arrays.
 *
 * <p>
 * Decoding accepts only the ASCII hexadecimal characters {@code 0-9}, {@code A-F}, and {@code a-f}.
 * Non-ASCII Unicode digits and fullwidth letters are rejected.
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
     *        the charset
     */
    private HexByteConversion(Charset charset)
    {
        this.charset = Objects.requireNonNull(charset, "Charset must not be null");
    }

    /**
     * Creates a new codec with the given charset name.
     *
     * @param charsetName
     *        the charset name
     */
    private HexByteConversion(String charsetName)
    {
        this(Charset.forName(charsetName));
    }

    // =========================================================================
    // PUBLIC STATIC ENTRY POINTS
    // =========================================================================

    /**
     * Universal static decoder that accepts a String, byte[], ByteBuffer, or char[]
     * representing hexadecimal values and returns their decoded byte array using the default
     * charset.
     *
     * @param object
     *        A String, byte[], ByteBuffer, or char[] containing hex digits.
     * @return A byte array containing the decoded binary data.
     */
    public static byte[] decode(Object object)
    {
        return decode(object, DEFAULT_CHARSET);
    }

    /**
     * Universal static decoder that accepts a String, byte[], ByteBuffer, or char[] representing
     * hexadecimal values and returns their decoded byte array using the specified charset.
     *
     * @param object
     *        A String, byte[], ByteBuffer, or char[] containing hex digits
     * @param charset
     *        the Charset to use for decoding
     * @return A byte array containing the decoded binary data
     */
    public static byte[] decode(Object object, Charset charset)
    {
        Objects.requireNonNull(object, "Input object must not be null");
        Objects.requireNonNull(charset, "Charset must not be null");

        return new HexByteConversion(charset).decodeInternal(object);
    }

    // =========================================================================
    // PRIVATE INSTANCE METHODS (Hidden implementation details)
    // =========================================================================

    private byte[] decodeInternal(final Object object)
    {
        Objects.requireNonNull(object, "Input object must not be null");

        if (object instanceof String)
        {
            return decodeChars(((String) object).toCharArray());
        }

        if (object instanceof char[])
        {
            return decodeChars((char[]) object);
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

    private byte[] decodeChars(char[] data)
    {
        Objects.requireNonNull(data, "Character array data must not be null");

        byte[] out = new byte[data.length >> 1];

        decodeHex(data, out, 0);

        return out;
    }

    private byte[] decodeBytes(byte[] data)
    {
        Objects.requireNonNull(data, "Byte array data must not be null");

        return decodeChars(new String(data, charset).toCharArray());
    }

    private byte[] decodeBuffer(ByteBuffer buffer)
    {
        Objects.requireNonNull(buffer, "ByteBuffer must not be null");
        byte[] bytes = new byte[buffer.remaining()];

        buffer.duplicate().get(bytes);

        return decodeBytes(bytes);
    }

    /**
     * Converts an array of characters representing hexadecimal values into an array of bytes of
     * those same values. The returned array will be half the length of the passed array, as it
     * takes two characters to represent any given byte. An exception is thrown if the passed char
     * array has an odd number of elements.
     *
     * @param data
     *        An array of characters containing hexadecimal digits.
     * @param result
     *        A byte array to contain the binary data decoded from the supplied char array.
     * @param offset
     *        The position within {@code result} to start writing the decoded bytes.
     * @return The number of bytes written to {@code result}.
     *
     * @throws IllegalArgumentException
     *         Thrown if an odd number of characters or illegal characters are supplied.
     */
    private int decodeHex(char[] data, byte[] result, int offset)
    {
        int len = data.length;

        if ((len & 1) != 0)
        {
            throw new IllegalArgumentException(String.format("Odd number of characters %,d.", len));
        }

        int outLen = len >> 1;

        if (result.length - offset < outLen)
        {
            throw new IllegalArgumentException("Output array is not large enough to accommodate decoded data.");
        }

        for (int i = offset, j = 0; j < len; i++, j += 2)
        {
            int high = toDigit(data[j], j) << 4;
            int low = toDigit(data[j + 1], j + 1);

            result[i] = (byte) (high | low);
        }

        return outLen;
    }

    private int toDigit(char ch, int index)
    {
        int digit;

        if (ch >= '0' && ch <= '9')
        {
            digit = ch - '0';
        }

        else if (ch >= 'A' && ch <= 'F')
        {
            digit = ch - 'A' + 10;
        }

        else if (ch >= 'a' && ch <= 'f')
        {
            digit = ch - 'a' + 10;
        }

        else
        {
            digit = -1;
        }

        if (digit == -1)
        {
            throw new IllegalStateException(String.format("Unrecognised hexadecimal character 0x%02X detected in index %,d.", ch & 0xFFFF, index));
        }

        return digit;
    }

    @Override
    public String toString()
    {
        return super.toString() + "[charsetName=" + this.charset + "]";
    }
}