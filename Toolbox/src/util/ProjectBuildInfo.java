package util;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Identifies the active {@code JAR} library file from which the current class is running. This
 * utility obtains the file name and the last compilation date and time, effectively capturing the
 * build date of the library.
 * 
 * <p>
 * This class captures the {@code JAR} name accurately at runtime; however, if a {@code JAR} is not
 * used (such as during IDE development), it assumes the name of the current running class instead.
 * </p>
 * 
 * <p>
 * Change Log:
 * </p>
 * 
 * <ul>
 * <li>Initial creation on 4 September 2023</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @since 4 September 2023
 */
public final class ProjectBuildInfo
{
    private Path fpath;
    private Instant buildInstant;

    /**
     * Internal constructor that identifies the source resource (JAR or .class) for the specified
     * class and extracts its build metadata.
     * 
     * @param runningClass
     *        the class used to resolve the resource location
     */
    private ProjectBuildInfo(Class<?> runningClass)
    {
        URL resource = runningClass.getResource(runningClass.getSimpleName() + ".class");

        if (resource == null)
        {
            throw new IllegalStateException("Could not find class resource for [" + runningClass.getName() + "]");
        }

        try
        {
            readBuildInfo(resource);
        }

        catch (URISyntaxException | IOException exc)
        {
            throw new IllegalStateException("Error accessing build resource [" + resource.getPath() + "]", exc);
        }
    }

    /**
     * Returns the full file system path to the active resource (JAR or class file).
     * 
     * @return the full path as a {@link Path} object
     */
    public Path getFullPath()
    {
        return fpath;
    }

    /**
     * Returns the file name of the JAR library or the current running class resource. Unlike
     * {@link #getFullPath()}, this includes only the final name element and its extension, for
     * example: {@code MyLibrary.jar} or {@code MyClass.class}.
     * 
     * @return the unmodified file name of the resource as a {@link Path} object
     */
    public Path getFileName()
    {
        return fpath.getFileName();
    }

    /**
     * Returns a human-readable string representation of the build date and time. The format used is
     * {@code dd/MM/yyyy @ hh:mm a}, for example: 27/03/2026 @ 07:45 PM.
     * 
     * @return the formatted build date string, or {@code "Unknown"} if the timestamp could not be
     *         determined
     */
    public String getBuildDate()
    {
        if (buildInstant == null)
        {
            return "Unknown";
        }

        return LocalDateTime.ofInstant(buildInstant, ZoneId.systemDefault()).format(DateTimeFormatter.ofPattern("dd/MM/yyyy @ hh:mm a"));
    }

    /**
     * Returns the build date-time as a legacy {@link Date} object. This provides backwards
     * compatibility with older APIs while representing the same point in time as the captured build
     * resource.
     * 
     * <p>
     * Note: While the internal state uses {@link Instant} precision, this method returns a value
     * truncated to millisecond precision as required by the {@code Date} class.
     * </p>
     * 
     * @return a {@link Date} object representing the last modification or compilation time of the
     *         resource
     */
    public Date getFullBuildDate()
    {
        return Date.from(buildInstant);
    }

    /**
     * Returns a string representation that specifies all relevant values collected by the
     * constructor, providing a comprehensive summary of the resource's details.
     * 
     * @return a formatted textual representation of details of this resource, including all
     *         relevant values
     */
    @Override
    public String toString()
    {
        StringBuilder sb = new StringBuilder();

        sb.append(String.format("[%s]%n", getClass().getSimpleName()));
        sb.append(String.format("   %-20s : %s%n", "Latest Build Date", getBuildDate()));
        sb.append(String.format("   %-20s : %s%n", "Full Path", getFullPath()));
        sb.append(String.format("   %-20s : %s%n", "File Name", getFileName()));

        return sb.toString();
    }

    /**
     * Creates a new instance of {@code ProjectBuildInfo} for the specified class. This static
     * factory method identifies the underlying resource (JAR or class file) and extracts its build
     * metadata, including the file path and last compilation time.
     * 
     * @param currentClass
     *        the class used to resolve the active resource location
     * @return an instance of {@link ProjectBuildInfo} containing the resource location and build
     *         metadata
     */
    public static ProjectBuildInfo getInstance(Class<?> currentClass)
    {
        return new ProjectBuildInfo(currentClass);
    }

    /**
     * Internal helper to resolve the resource path and extract build metadata from the specified
     * URL.
     * 
     * <p>
     * If the URL points to a JAR file, this method extracts the manifest timestamp. Otherwise, it
     * resolves to the local class file and uses its filesystem modification time.
     * </p>
     * 
     * @param resource
     *        the URL of the active class or JAR resource to be evaluated
     * 
     * @throws URISyntaxException
     *         if the URL cannot be converted to a valid URI for path resolution
     * @throws IOException
     *         if the JAR file or filesystem resource cannot be accessed
     */
    private void readBuildInfo(URL resource) throws URISyntaxException, IOException
    {
        String protocol = resource.getProtocol();

        if (protocol.equals("file"))
        {
            fpath = Paths.get(resource.toURI());
            buildInstant = Files.getLastModifiedTime(fpath).toInstant();
        }

        else if (protocol.equals("jar"))
        {
            String path = resource.getPath();

            /*
             * Extract the path only with the "file:" prefix removed. For example on Windows:
             * 
             * Before -> file:/E:/download/ProxyFilterGUI.jar!/proxy/ProxyFilterFrame.class
             * After -> /E:/download/ProxyFilterGUI.jar
             */
            path = path.substring(path.indexOf(":") + 1, path.indexOf("!"));

            String decodedPath = decodePath(path);

            fpath = Paths.get(decodedPath);
            buildInstant = getJarManifestTimestamp(decodedPath);
        }

        else if (protocol.equals("rsrc"))
        {
            // Eclipse Jar-in-Jar loader workaround
            String jarCmd = System.getProperty("sun.java.command", "");
            String jarPath = jarCmd.split("\\s+")[0];

            fpath = Paths.get(decodePath(jarPath));
            buildInstant = getJarManifestTimestamp(fpath.toString());
        }

        else
        {
            throw new UnsupportedOperationException("Unsupported protocol [" + protocol + "]");
        }
    }

    /**
     * Decodes a URL-encoded path string into a standard file system path. This method specifically
     * handles the leading slash often prepended to absolute Windows paths by the ClassLoader, for
     * example: converting {@code /C:/} to {@code C:/}).
     * 
     * @param urlPath
     *        the URL-encoded path string to be decoded
     * @return the decoded file system path string
     * 
     * @throws IOException
     *         if the decoding process fails or the encoding is unsupported
     */
    private String decodePath(String urlPath) throws IOException
    {
        if (urlPath.startsWith("/") && urlPath.indexOf(":") == 2)
        {
            urlPath = urlPath.substring(1);
        }

        return URLDecoder.decode(urlPath, "UTF-8");
    }

    /**
     * Retrieves the build time-stamp from the {@code META-INF/MANIFEST.MF} entry within the
     * specified JAR file.
     * 
     * <p>
     * If the manifest entry cannot be obtained, the method resolves to the last modified time of
     * the JAR file itself on the file system.
     * </p>
     * 
     * @param jarPath
     *        the absolute file system path to the JAR resource
     * @return an {@code Instant} representing the build time, accurate to the limits of the
     *         JAR/File System metadata
     * 
     * @throws IOException
     *         if an I/O error occurs while accessing the JAR file or its entries
     */
    private Instant getJarManifestTimestamp(String jarPath) throws IOException
    {
        try (JarFile jarFile = new JarFile(jarPath))
        {
            JarEntry manifest = jarFile.getJarEntry("META-INF/MANIFEST.MF");

            if (manifest != null)
            {
                return Instant.ofEpochMilli(manifest.getTime());
            }

            return Files.getLastModifiedTime(Paths.get(jarPath)).toInstant();
        }
    }
    
    
    /**
     * Returns the short name of the JAR library or the current running class resource, with the
     * {@code .class} extension name removed, providing a concise identifier for the resource.
     * 
     * @return the short name of the resource as a {@link Path} object, without the {@code .class}
     *         extension
     */
    public Path getShortFileName()
    {
        String ext = ".class";
        String str = fpath.getFileName().toString();

        if (str.endsWith(ext))
        {
            str = str.substring(0, str.length() - ext.length());
        }

        return Paths.get(str);
    }
}