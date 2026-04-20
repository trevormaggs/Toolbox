package util;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility for generating cryptographic hashes (MD5, SHA-1, SHA-256) for files. Optimised to handle
 * large files efficiently via buffered streaming to prevent memory exhaustion.
 * 
 * <p>
 * This utility is essential for verifying file integrity during batch move or copy operations.
 * </p>
 *
 * @author Trevor Maggs
 * @version 0.2
 * @since 2 April 2026 (Originally created April 2017)
 */
public final class FileChecksum
{
    /**
     * Private constructor to prevent instantiation.
     * 
     * @throws UnsupportedOperationException
     *         always
     */
    private FileChecksum()
    {
        throw new UnsupportedOperationException("Not intended for instantiation");
    }

    /**
     * Gets the MD5 checksum of the specified file.
     * 
     * @param pfile
     *        the Path representing the file to be checked for integrity
     * @return the computed checksum in an upper-case hexadecimal format
     * 
     * @throws NoSuchAlgorithmException
     *         if the MD5 algorithm is not available in the environment
     * @throws IOException
     *         if an I/O error occurs during file access
     */
    public static String getFileMD5checksum(Path pfile) throws NoSuchAlgorithmException, IOException
    {
        return generateChecksum(MessageDigest.getInstance("MD5"), pfile);
    }

    /**
     * Gets the MD5 checksum of the specified file.
     * 
     * @param file
     *        the file to be checked for integrity
     * @return the computed checksum in an upper-case hexadecimal format
     * 
     * @throws NoSuchAlgorithmException
     *         if there is a problem when attempting to compute with the hashing algorithm
     * @throws IOException
     *         if there is an I/O error
     */
    public static String getFileMD5checksum(String file) throws NoSuchAlgorithmException, IOException
    {
        return getFileMD5checksum(Paths.get(file));
    }

    /**
     * Gets the SHA1 checksum of the specified file.
     * 
     * @param pfile
     *        the Path representing the file to be checked for integrity
     * @return the computed checksum in an upper-case hexadecimal format
     * 
     * @throws NoSuchAlgorithmException
     *         if there is a problem when attempting to compute with the hashing algorithm
     * @throws IOException
     *         if there is an I/O error
     */
    public static String getFileSHA1checksum(Path pfile) throws NoSuchAlgorithmException, IOException
    {
        return generateChecksum(MessageDigest.getInstance("SHA-1"), pfile);
    }

    /**
     * Gets the SHA1 checksum of the specified file.
     * 
     * @param file
     *        the file to be checked for integrity
     * @return the computed checksum in an upper-case hexadecimal format
     * 
     * @throws NoSuchAlgorithmException
     *         if there is a problem when attempting to compute with the hashing algorithm
     * @throws IOException
     *         if there is an I/O error
     */
    public static String getFileSHA1checksum(String file) throws NoSuchAlgorithmException, IOException
    {
        return getFileSHA1checksum(Paths.get(file));
    }

    /**
     * Gets the SHA256 checksum of the specified file.
     *
     * @param pfile
     *        the Path representing the file to be checked for integrity
     * @return the computed checksum in an upper-case hexadecimal format
     * 
     * @throws NoSuchAlgorithmException
     *         if there is a problem when attempting to compute with the hashing algorithm
     * @throws IOException
     *         if there is an I/O error
     */
    public static String getFileSHA256checksum(Path pfile) throws NoSuchAlgorithmException, IOException
    {
        return generateChecksum(MessageDigest.getInstance("SHA-256"), pfile);
    }

    /**
     * Gets the SHA256 checksum of the specified file.
     * 
     * @param file
     *        the file to be checked for integrity
     * @return the computed checksum in an upper-case hexadecimal format
     * 
     * @throws NoSuchAlgorithmException
     *         if there is a problem when attempting to compute with the hashing algorithm
     * @throws IOException
     *         if there is an I/O error
     */
    public static String getFileSHA256checksum(String file) throws NoSuchAlgorithmException, IOException
    {
        return getFileSHA256checksum(Paths.get(file));
    }

    /**
     * Performs the hash computation to obtain the checksum based on the specified hashing
     * algorithm.
     * 
     * @param digest
     *        the MessageDigest object representing the specified hashing algorithm (MD5, SHA-1, or
     *        SHA-256)
     * @param pfile
     *        the Path object representing the file to be check-summed
     * @return the computed checksum in an upper-case hexadecimal format
     * 
     * @throws IOException
     *         if an I/O error occurs while reading the file stream
     */
    private static String generateChecksum(MessageDigest digest, Path pfile) throws IOException
    {
        StringBuilder sb = new StringBuilder();

        try (InputStream is = Files.newInputStream(pfile))
        {
            int read;
            byte[] buffer = new byte[8192];

            while ((read = is.read(buffer)) != -1)
            {
                digest.update(buffer, 0, read);
            }

            byte[] hashBytes = digest.digest();

            for (byte b : hashBytes)
            {
                sb.append(String.format("%02X", b));
            }
        }

        return sb.toString();
    }
}