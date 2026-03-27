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
 * 
 * This utility class determines the active {@code JAR} library file from which the current class is
 * running. It identifies the {@code JAR} file name and retrieves the last compilation date and
 * time, effectively capturing the build date of the library.
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
 * <li>Initial creation on 4 September 4 2023</li>
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
     * Constructs a private {@code ProjectBuildInfo} object to analyse the active resource, which
     * can be either a JAR library or the current running class resource. This retrieves the last
     * successful compilation or build date of the resource.
     * 
     * Note that this constructor is not intended for direct use. Instead, use the public static
     * factory method to indirectly invoke this constructor.
     * 
     * @param runningClass
     *        the current running class resource being analysed
     */
    private ProjectBuildInfo(Class<?> runningClass)
    {
        URL resource = runningClass.getResource(runningClass.getSimpleName() + ".class");

        if (resource == null)
        {
            throw new IllegalStateException("Could not find class resource for " + runningClass.getName());
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
     * @return the full path of the resource as a {@link Path} object
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
     * Returns the build date-time as a legacy {@link Date} object. This provides compatibility with
     * older APIs while representing the same moment in time as the captured build resource.
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
     * Retrieves a resource that specifies the build date stamp of the last successful compilation
     * for your Java development project. This public static factory method provides a convenient
     * way to access the build date information.
     * 
     * @param currentClass
     *        the current running class resource
     * 
     * @return an instance of ProjectBuildDate containing the resource name and build date
     *         information
     */
    public static ProjectBuildInfo getInstance(Class<?> currentClass)
    {
        return new ProjectBuildInfo(currentClass);
    }
    
    /**
     * Retrieves the resource name and the {@code Instant} build time-stamp of the last successful
     * compilation associated with the specified URL resource. If the resource is a JAR library, it
     * will be identified and its build information obtained. Otherwise, the current running class
     * resource is assumed.
     * 
     * @param resource
     *        the URL instance of the active class resource
     * 
     * @throws URISyntaxException
     *         if the URI for the specified resource cannot be obtained
     * @throws IOException
     *         if an I/O error occurs during the retrieval process
     */
    private void readBuildInfo(URL resource) throws URISyntaxException, IOException
    {
        String protocol = resource.getProtocol();

        if ("file".equals(protocol))
        {
            fpath = Paths.get(resource.toURI());
            buildInstant = Files.getLastModifiedTime(fpath).toInstant();
        }

        else if ("jar".equals(protocol))
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

        else if ("rsrc".equals(protocol))
        {
            // Eclipse Jar-in-Jar loader workaround
            String jarCmd = System.getProperty("sun.java.command", "");
            String jarPath = jarCmd.split("\\s+")[0];

            fpath = Paths.get(decodePath(jarPath));
            buildInstant = getJarManifestTimestamp(fpath.toString());
        }

        else
        {
            throw new UnsupportedOperationException("Unsupported protocol: " + protocol);
        }
    }

    /**
     * Retrieves the build time-stamp from the manifest file (META-INF/MANIFEST.MF) within the
     * specified JAR file. If the manifest entry is missing, the method falls back to the last
     * modified time of the JAR file itself.
     * 
     * @param jarPath
     *        the absolute file system path to the JAR resource
     * @return an {@code Instant} representing the time-stamp of the build
     * 
     * @throws IOException
     *         if an I/O error occurs during the file access or JAR entry retrieval
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
}