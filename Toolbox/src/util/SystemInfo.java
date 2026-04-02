package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A comprehensive utility for retrieving detailed system information, including operating system
 * distribution, versioning, network identity, and hardware architecture.
 *
 * <p>
 * This utility significantly extends the standard {@code System.getProperty} capabilities. While
 * standard Java 8 often identifies Windows 11 as "Windows 10", this class resolves the underlying
 * build numbers to provide accurate marketing names. It also performs deep-scanning of Linux
 * filesystem release files to identify specific distributions such as Ubuntu, RHEL, and Amazon
 * Linux.
 * </p>
 *
 * <p>
 * <b>Key Features:</b>
 * </p>
 * 
 * <ul>
 * <li><b>Precise Windows Mapping:</b> Distinguishes between Workstation and Server variants, such
 * as Windows 10 vs. Server 2016.</li>
 * <li><b>Linux Flavor Detection:</b> Scans {@code /etc/os-release} and other distribution-specific
 * files.</li>
 * <li><b>Network Diagnostics:</b> Provides short-name hostname resolution and IP address
 * discovery.</li>
 * <li><b>Resource Efficiency:</b> Uses lazy-style static initialization to minimize memory
 * footprint on non-Linux systems.</li>
 * </ul>
 *
 * @author Trevor Maggs
 * @version 0.4
 * @since 1 April 2026
 */
public final class SystemInfo
{
    private static final String OS_NAME;
    private static final String OS_ARCH;
    private static final String OS_VERSION;
    private static final SystemProperties sysInfo;
    private static final List<LinuxRelease> LINUX_RELEASES;
    private static final Pattern WIN_VER_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

    static
    {
        OS_ARCH = System.getProperty("os.arch").toLowerCase();
        OS_NAME = System.getProperty("os.name").toLowerCase();
        OS_VERSION = System.getProperty("os.version").toLowerCase();

        sysInfo = new SystemProperties();
        sysInfo.version = OS_VERSION;
        sysInfo.architecture = OS_ARCH;

        /*
         * Initialise Linux detection patterns. This is populated only
         * if the running OS is identified as Linux.
         */
        if (isLinux())
        {
            String suseRegex = "^\\s*VERSION\\s+=\\s+([0-9\\.]+)";

            List<LinuxRelease> list = new ArrayList<>();
            // Latest systems (Ubuntu/Alpine/Amazon Linux)
            list.add(new LinuxRelease("/etc/os-release", "^ID=[\"']?([^\"']+)[\"']?$", Platform.UNKNOWN));
            // Debian/Ubuntu
            list.add(new LinuxRelease("/etc/debian_version", "(\\d+(?:\\.\\d+)*)", Platform.DEBIAN));
            list.add(new LinuxRelease("/etc/debian_release", "(\\d+(?:\\.\\d+)*)", Platform.DEBIAN));
            // SuSE / SLES / Novell
            list.add(new LinuxRelease("/etc/SuSE-release", suseRegex, Platform.SUSE));
            list.add(new LinuxRelease("/etc/sles-release", suseRegex, Platform.SUSE));
            list.add(new LinuxRelease("/etc/novell-release", suseRegex, Platform.SUSE));
            // CentOS / Fedora
            list.add(new LinuxRelease("/etc/centos-release", "(\\d+(?:\\.\\d+)*)", Platform.CENTOS));
            list.add(new LinuxRelease("/etc/fedora-release", "Fedora[^\\d]+(\\d+)", Platform.FEDORA));
            // Red Hat variants
            list.add(new LinuxRelease("/etc/redhat-release", "\\s*CentOS.*(\\d+(?:\\.\\d+)*)", Platform.CENTOS));
            list.add(new LinuxRelease("/etc/redhat-release", "\\s*Oracle VM.*(\\d+(?:\\.\\d+)*)", Platform.OVM));
            list.add(new LinuxRelease("/etc/redhat-release", "Red Hat Enterprise Linux.+?(\\d+(?:\\.\\d+)*)", Platform.RHEL));

            LINUX_RELEASES = Collections.unmodifiableList(list);
        }

        else
        {
            LINUX_RELEASES = Collections.emptyList();
        }

        try
        {
            detectPlatform();
        }

        catch (IOException exc)
        {
            sysInfo.platform = Platform.UNKNOWN;
        }

        readLocalHostName();
        readLocalIPaddress();
        readJavaArchitectureInfo();
    }

    /**
     * Inner container used to store resolved system metadata.
     */
    private static class SystemProperties
    {
        private String hostname;
        private String ip_address;
        private String architecture;
        private int data_model;
        private String version;
        private Platform platform;

        @Override
        public String toString()
        {
            StringBuilder line = new StringBuilder();

            line.append("--- System Information ---\n");
            line.append(String.format("  %-30s %s%n", "[Hostname]", hostname));
            line.append(String.format("  %-30s %s%n", "[IP Address]", ip_address));
            line.append(String.format("  %-30s %s%n", "[Architecture]", architecture));
            line.append(String.format("  %-30s %s%n", "[Architecture Data Model]", data_model));
            line.append(String.format("  %-30s %s%n", "[OS Version]", version));
            line.append(String.format("  %-30s %s%n", "[OS Name]", platform));
            line.append(String.format("  %-30s %s%n", "[Real Name]", platform.getRealName()));

            return line.toString();
        }
    }

    /**
     * Represents a Linux distribution release file and the logic to parse it.
     */
    private static class LinuxRelease
    {
        final String file;
        final Pattern pattern;
        final Platform plat;

        LinuxRelease(String f, String regex, Platform p)
        {
            this.file = f;
            this.pattern = Pattern.compile(regex);
            this.plat = p;
        }
    }

    /**
     * Private constructor to prevent instantiation.
     * 
     * @throws UnsupportedOperationException
     *         always.
     */
    private SystemInfo()
    {
        throw new UnsupportedOperationException("Not intended for instantiation");
    }

    /**
     * Returns the resolved Platform constant for the current system.
     * 
     * @return the {@link Platform} enum constant
     */
    public static Platform getPlatform()
    {
        return sysInfo.platform;
    }

    /**
     * Returns the short hostname of the computer.
     * 
     * @return the short hostname, such as "server01"
     */
    public static String getHostname()
    {
        return sysInfo.hostname;
    }

    /**
     * Returns the local IP address.
     * 
     * @return the IPv4 address string
     */
    public static String getIPaddress()
    {
        return sysInfo.ip_address;
    }

    /**
     * Returns the marketing name of the Operating System.
     * 
     * @return the real name string, such as "Windows 11" or "Ubuntu"
     */
    public static String getOperatingSystemRealName()
    {
        return sysInfo.platform.getRealName();
    }

    /**
     * Returns the OS version as a raw string.
     * 
     * @return the version string
     */
    public static String getOsVersion()
    {
        return sysInfo.version;
    }

    /**
     * Normalises the OS version into a numerical double value.
     * 
     * <p>
     * For example, "5.15.0-76-generic" is converted to 5.15.
     * </p>
     * 
     * @return the numerical version, or 0.0 if parsing fails
     */
    public static double getOsVersionDigit()
    {
        String ver = getOsVersion();

        if (ver != null && !ver.isEmpty())
        {
            // Handles "5.15.0-76-generic" -> "5.15.076" -> split into ["5", "15", "076"]
            String[] parts = ver.replaceAll("[^0-9.]", "").split("\\.");

            try
            {
                if (parts.length >= 2)
                {
                    return Double.parseDouble(parts[0] + "." + parts[1]);
                }

                else if (parts.length == 1 && !parts[0].isEmpty())
                {
                    return Double.parseDouble(parts[0] + ".0");
                }
            }

            catch (NumberFormatException exc)
            {
                /*
                 * In case of any unexpected "marketing strings"
                 * that may be non-standard. This is a safety net.
                 */
            }
        }

        return 0.0;
    }

    /**
     * Gets the architecture name. For example, x86, amd64, etc.
     *
     * @return the architecture name
     */
    public static String getArchitecture()
    {
        return OS_ARCH;
    }

    /**
     * Returns the architecture data model of the JRE, either 32-bit or 64-bit.
     *
     * @return 32-bit or 64-bit as an integer value
     */
    public static int getArchitectureDataModel()
    {
        return sysInfo.data_model;
    }

    /**
     * Checks whether the platform is a Power PC hardware.
     *
     * @return boolean true if the hardware is a Power PC system
     */
    public static boolean isPowerPcHardware()
    {
        return OS_ARCH.contains("ppc");
    }

    /**
     * Checks whether the platform is a SPARC hardware.
     *
     * @return boolean true if the hardware is a SPARC type
     */
    public static boolean isSparcHardware()
    {
        return OS_ARCH.contains("sparc");
    }

    /**
     * Checks whether the running OS is based on UNIX.
     *
     * @return true if the OS family is Unix-based (Linux, AIX, etc.).
     */
    public static boolean isUnixSystem()
    {
        return sysInfo.platform.getOSFamily().isUnixBased();
    }

    /**
     * Checks whether AIX is the current operating system.
     *
     * @return boolean true if AIX is running
     */
    public static boolean isAIX()
    {
        return (OS_NAME.startsWith("aix"));
    }

    /**
     * Checks whether FreeBSD is the current operating system.
     *
     * @return boolean true if FreeBSD is running
     */
    public static boolean isFreeBSD()
    {
        return (OS_NAME.startsWith("freebsd"));
    }

    /**
     * Checks whether HP-UX is the current operating system.
     *
     * @return boolean true if HP-UX is running
     */
    public static boolean isHPUX()
    {
        return (OS_NAME.startsWith("hp-ux"));
    }

    /**
     * Checks whether Solaris is the current operating system.
     *
     * @return boolean true if Solaris is running
     */
    public static boolean isSolaris()
    {
        return (OS_NAME.startsWith("solaris") || OS_NAME.startsWith("sunos") || OS_NAME.startsWith("sun os"));
    }

    /**
     * Checks whether Windows is running.
     *
     * @return boolean true if Windows is running
     */
    public static boolean isWindows()
    {
        return (OS_NAME.startsWith("windows"));
    }

    /**
     * Checks whether Linux is running.
     *
     * @return boolean true if Linux is running
     */
    public static boolean isLinux()
    {
        return (OS_NAME.startsWith("linux"));
    }

    /**
     * Checks if the current OS matches a specific Platform.
     * 
     * @param osName
     *        the platform to check
     * @return boolean {@code true if the specified operating system is present
     */
    public static boolean isRunningOS(Platform osName)
    {
        return isRunningOS(EnumSet.of(osName));
    }

    /**
     * Checks if the current OS matches any Platform in the set.
     * 
     * @param osNameSet
     *        the set of platforms
     * @return {@code true} if the current platform is contained within the set
     *
     * @throws IllegalArgumentException
     *         if the specified set is null or empty
     */
    public static boolean isRunningOS(EnumSet<Platform> osNameSet)
    {
        if (osNameSet == null || osNameSet.isEmpty())
        {
            throw new IllegalArgumentException("Undefined EnumSet values");
        }

        return (osNameSet.contains(sysInfo.platform));
    }

    /**
     * Queries the AIX operating system to dynamically obtain its current OS version. Note: it
     * executes {@code oslevel} to find AIX version.
     *
     * @return the OS version in the form of String. If unable to compute, it returns "0.0"
     * 
     * @throws IOException
     *         if unable to obtain the information
     */
    private static String readVersionAIX() throws IOException
    {
        // Pre-compile the pattern to match AIX versioning (e.g., 7.1 from 7.1.0.0)
        // Matches optional whitespace, captures major.minor, ignores the rest
        String regex = "^\\s*(\\d+\\.\\d+).*$";
        Pattern pattern = Pattern.compile(regex);

        try
        {
            RunCommand rc = RunCommand.run("/usr/bin/oslevel");

            if (rc.getExitCode() == 0)
            {
                /*
                 * Example in AIX:
                 * # /usr/bin/oslevel
                 * 7.1.0.0
                 */
                for (String line : rc.getStdout())
                {
                    Matcher m = pattern.matcher(line);

                    if (m.find())
                    {
                        return m.group(1);
                    }
                }
            }
        }

        catch (IOException exc)
        {
            throw new IOException("AIX query failed", exc);
        }

        return "0.0";
    }

    /**
     * Queries the Solaris operating system to dynamically obtain its current OS version. Note: it
     * executes {@code uname -r} to find Solaris version.
     *
     * @return the OS version in the form of String. If unable to compute, it returns "0.0"
     * 
     * @throws IOException
     *         if unable to obtain the information
     */
    private static String readVersionSolaris() throws IOException
    {
        String regex = "^\\s*(\\d+(?:\\.\\d+)*)\\s*$";
        Pattern pattern = Pattern.compile(regex);

        try
        {
            RunCommand rc = RunCommand.run("/usr/bin/uname", "-r");

            if (rc.getExitCode() == 0)
            {
                /*
                 * Example in Solaris:
                 * au10qap8qftels2 $ uname -r
                 * 5.10
                 */
                for (String line : rc.getStdout())
                {
                    Matcher m = pattern.matcher(line);

                    if (m.find())
                    {
                        return m.group(1);
                    }
                }
            }
        }

        catch (IOException exc)
        {
            throw new IOException("Solaris query failed", exc);
        }

        return "0.0";
    }

    /**
     * Identifies specific Windows marketing versions based on kernel version and build number.
     *
     * @return the named Platform constant
     * 
     * @throws IOException
     *         if unable to obtain the information
     */
    private static Platform readSystemInfoWindows()
    {
        boolean isServer = OS_NAME.contains("server");
        double actualVer = getOsVersionDigit();

        if (Double.compare(actualVer, 5.2) == 0)
        {
            return isServer ? Platform.WIN2003 : Platform.WINXP64;
        }

        if (Double.compare(actualVer, 10.0) == 0)
        {
            int build = resolveWindowsBuild();

            if (isServer)
            {
                if (build >= 20348)
                {
                    return Platform.WIN2022;
                }

                else if (build >= 17763)
                {
                    return Platform.WIN2019;
                }

                else
                {
                    // build >= 14393
                    return Platform.WIN2016;
                }
            }

            else
            {
                return (build >= 22000) ? Platform.WIN11 : Platform.WIN10;
            }
        }

        for (Platform os : Platform.values())
        {
            if (os.getOSFamily() == Platform.OSFamily.WINDOWS)
            {
                if (Double.compare(os.getVersion(), actualVer) == 0 && os.isServer() == isServer)
                {
                    return os;
                }
            }
        }

        return Platform.UNKNOWN;
    }

    /**
     * Resolves the Windows Build Number by executing the native 'ver' command.
     * *
     * <p>
     * This is required to distinguish between Windows 10 and Windows 11, as
     * both report a major version of 10.0 in standard system properties.
     * </p>
     * * @return the build number (e.g. 22621 for Win 11), or -1 if it cannot be resolved
     */
    private static int resolveWindowsBuild()
    {
        try
        {
            RunCommand rc = RunCommand.run("cmd /c ver");

            if (rc.getExitCode() == 0 && rc.getStdout().length > 0)
            {
                for (String line : rc.getStdout())
                {
                    if (line == null || line.trim().isEmpty())
                    {
                        continue;
                    }

                    // Format: 10.0.[BUILD].patch
                    // Example: "Microsoft Windows [Version 10.0.22621.1105]"
                    Matcher m = WIN_VER_PATTERN.matcher(line);

                    if (m.find())
                    {
                        // Group 3 is the Build Number (e.g., 19045, 22621)
                        return Integer.parseInt(m.group(3));
                    }
                }
            }
        }

        catch (Exception exc)
        {
            // Do nothing
        }

        return -1;
    }

    /**
     * Iterates through release files to identify the Linux distribution and version.
     *
     * @return the named Platform constant
     * 
     * @throws IOException
     *         if unable to obtain the information
     */
    private static Platform readSystemInfoLinux()
    {
        Platform plat = Platform.UNKNOWN;

        for (LinuxRelease info : LINUX_RELEASES)
        {
            Path releaseFile = Paths.get(info.file);

            if (Files.notExists(releaseFile))
            {
                continue;
            }

            try (BufferedReader br = Files.newBufferedReader(releaseFile, StandardCharsets.UTF_8))
            {
                String line;
                String regex = null;
                String osrelease = null;

                while ((line = br.readLine()) != null)
                {
                    Matcher matcher = info.pattern.matcher(line);

                    if (matcher.find())
                    {
                        if (info.plat == Platform.UNKNOWN)
                        {
                            // For example: ID=ubuntu
                            plat = Platform.fromShortName(matcher.group(1));
                        }

                        else
                        {
                            plat = info.plat;
                            regex = matcher.group(1);
                        }
                    }

                    if (line.startsWith("VERSION_ID="))
                    {
                        osrelease = line.split("=")[1].replaceAll("[\"']", "");
                    }
                }

                if (plat != Platform.UNKNOWN)
                {
                    if (osrelease != null)
                    {
                        sysInfo.version = osrelease;
                    }

                    else if (regex != null)
                    {
                        sysInfo.version = regex;
                    }

                    break;
                }
            }

            catch (IOException exc)
            {
            }
        }

        return plat;
    }

    /**
     * Orchestrates the high-level platform detection logic.
     */
    private static void detectPlatform() throws IOException
    {
        if (isAIX())
        {
            sysInfo.platform = Platform.AIX;
            sysInfo.version = readVersionAIX();
        }

        else if (isFreeBSD())
        {
            sysInfo.platform = Platform.FREEBSD;
            sysInfo.version = "0.0";
        }

        else if (isHPUX())
        {
            sysInfo.platform = Platform.HPUX;
            sysInfo.version = "0.0";
        }

        else if (isSolaris())
        {
            sysInfo.platform = Platform.SOLARIS;
            sysInfo.version = readVersionSolaris();
        }

        else if (isWindows())
        {
            sysInfo.platform = readSystemInfoWindows();
            sysInfo.version = String.valueOf(sysInfo.platform.getVersion());
        }

        else if (isLinux())
        {
            sysInfo.platform = readSystemInfoLinux();
        }

        else
        {
            sysInfo.platform = Platform.UNKNOWN;
            sysInfo.version = "0.0";
        }
    }

    /**
     * Resolves the short hostname via Java API with a shell fallback.
     *
     * <p>
     * Prefers the Java Networking API. If a Fully Qualified Domain Name (FQDN) is returned, this
     * method truncates it to provide the short hostname only, for example: 'server01' instead of
     * 'server01.network.com'.
     * </p>
     *
     * <p>
     * If that hostname cannot be obtained from the Java Networking API, it will fallback by
     * executing {@code hostname} command directly in the OS environment. If that host name,
     * however, cannot be obtained, a string "localhost" will be provided.
     * </p>
     */
    private static void readLocalHostName()
    {
        try
        {
            String fqdn = InetAddress.getLocalHost().getHostName();

            // Example: update oc7456161242.ibm.com to oc7456161242
            sysInfo.hostname = (fqdn != null && fqdn.contains(".")) ? fqdn.split("\\.")[0] : fqdn;
        }

        catch (UnknownHostException exc1)
        {
            try
            {
                // Fallback by running a command directly in the OS environment
                RunCommand rc = RunCommand.run("hostname");

                if (rc.getExitCode() == 0 && rc.getStdout().length > 0)
                {
                    sysInfo.hostname = rc.getStdout()[0].split("\\.")[0];
                }
            }

            catch (IOException exc2)
            {
                sysInfo.hostname = "localhost";
            }
        }
    }

    /**
     * Retrieves the local IP address. If the information cannot be obtained, a string "127.0.0.1"
     * will be provided.
     */
    private static void readLocalIPaddress()
    {
        try
        {
            sysInfo.ip_address = InetAddress.getLocalHost().getHostAddress();
        }

        catch (UnknownHostException exc)
        {
            sysInfo.ip_address = "127.0.0.1";
        }
    }

    /**
     * Reads the JRE architecture data model (32 vs 64 bit).
     */
    private static void readJavaArchitectureInfo()
    {
        sysInfo.architecture = OS_ARCH;

        try
        {
            sysInfo.data_model = Integer.parseInt(System.getProperty("sun.arch.data.model", "64"));
        }

        catch (NumberFormatException exc)
        {
            sysInfo.data_model = 64;
        }
    }
}