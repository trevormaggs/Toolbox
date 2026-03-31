package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class SystemInfo2
{
    // https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/SystemUtils.java
    private static final String OS_NAME;
    private static final String OS_ARCH;
    private static final String OS_VERSION;
    private static final SystemProperties sysInfo;

    static
    {
        sysInfo = new SystemProperties();

        OS_ARCH = System.getProperty("os.arch").toLowerCase();
        OS_NAME = System.getProperty("os.name").toLowerCase();
        OS_VERSION = System.getProperty("os.version").toLowerCase();

        sysInfo.version = OS_VERSION;
        sysInfo.architecture = OS_ARCH;

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
     * Default constructor will always throw an exception.
     *
     * @throws UnsupportedOperationException
     *         to indicate that instantiation is not supported
     */
    private SystemInfo2()
    {
        throw new UnsupportedOperationException("Not intended for instantiation");
    }

    /**
     * Returns the current running Operating System.
     *
     * Note, this method is similar to the {@code getOSName()} method, except it returns the
     * {@code Platform} type.
     *
     * @return constant name based on the OperatingSystem type
     */
    public static Platform getPlatform()
    {
        return sysInfo.platform;
    }

    /**
     * Gets the hostname of the computer system.
     *
     * @return the current hostname
     */
    public static String getHostname()
    {
        return sysInfo.hostname;
    }

    /**
     * Gets the IP address.
     *
     * @return the configured IP address
     */
    public static String getIPaddress()
    {
        return sysInfo.ip_address;
    }

    /**
     * Gets the current running Operating System name.
     *
     * @return the OS name as an Platform enum constant, such as {@code WIN10}, {@code RHEL},
     *         {@code WIN2016} etc
     */
    public static Platform getOperatingSystem()
    {
        return sysInfo.platform;
    }

    /**
     * Gets the real OS variant or flavour name, such as {@code Windows 2000}, {@code Fedora},
     * {@code Windows 11}, {@codeRed Hat} etc.
     *
     * @return the real operating system name
     */
    public static String getOperatingSystemRealName()
    {
        return sysInfo.platform.getRealName();
    }

    /**
     * Gets the OS version in the string format.
     *
     * @return OS version
     */
    public static String getOsVersion()
    {
        return sysInfo.version;
    }

    /**
     * Gets the OS version in the form of real number (double).
     *
     * @return OS version
     */
    public static double getOsVersionDigit()
    {
        String ver = getOsVersion();

        if (ver != null && !ver.isEmpty())
        {
            // This handles "5.15.0-76-generic" -> "5.15.076" -> split into ["5", "15", "076"]
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
     * Returns the Architecture Data Model of the JRE, either 32-bit or 64-bit.
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
     * @return boolean true if the platform is running UNIX
     */
    public static boolean isUnixSystem()
    {
        return sysInfo.platform.getFamily().isUnixBased();
    }

    public static boolean isAIX()
    {
        return (OS_NAME.startsWith("aix"));
    }

    public static boolean isFreeBSD()
    {
        return (OS_NAME.startsWith("freebsd"));
    }

    public static boolean isHPUX()
    {
        return (OS_NAME.startsWith("hp-ux"));
    }

    public static boolean isSolaris()
    {
        return (OS_NAME.startsWith("solaris") || OS_NAME.startsWith("sunos") || OS_NAME.startsWith("sun os"));
    }

    public static boolean isWindows()
    {
        return (OS_NAME.startsWith("windows") && sysInfo.platform.getFamily() == Platform.Family.WINDOWS);
    }

    public static boolean isLinux()
    {

        return (OS_NAME.startsWith("linux") && sysInfo.platform.getFamily() == Platform.Family.LINUX);
    }

    /**
     * Queries the computer system to determine whether the specified operating system is running.
     *
     * @param osName
     *        the operating system name to check for. The name is a constant derived from the
     *        {@code Platform} enumeration class. For example,
     *        {@code SystemInfo.isRunningOS(DEBIAN)} will return true if the Debian operating system
     *        is found running
     *
     * @return boolean true if the specified operating system is present
     * @throws IllegalArgumentException
     *         if any parameters are unrecognised
     */
    public static boolean isRunningOS(Platform osName)
    {
        return isRunningOS(EnumSet.of(osName));
    }

    /**
     * Evaluates if the current operating system matches against the specified set.
     *
     * @param osNameSet
     *        A set of {@link Platform} constants to check against
     * @return {@code true} if the current platform is contained within the set
     *
     * @throws IllegalArgumentException
     *         if the specified set is null or empty
     */
    public static boolean isRunningOS(EnumSet<Platform> osNameSet)
    {
        // https://www.superglobals.net/java-bitmask-enum-example
        // https://howtodoinjava.com/java/enum/java-enum-string-example

        if (osNameSet == null || osNameSet.isEmpty())
        {
            throw new IllegalArgumentException("Undefined EnumSet values");
        }

        try
        {
            return (osNameSet.contains(sysInfo.platform));
        }

        catch (IllegalArgumentException exc)
        {
            throw new IllegalArgumentException("Unrecognised enum constant [" + sysInfo.platform.toString() + "]", exc);
        }
    }

    /**
     * Queries the AIX operating system to obtain the basic system information.
     *
     * @throws IOException
     *         if it is unable to obtain the information
     */
    private static String readVersionAIX() throws IOException
    {
        // Pre-compile the pattern to match AIX versioning (e.g., 7.1 from 7.1.0.0)
        // Matches optional whitespace, captures major.minor, ignores the rest
        String regex = "^\\s*(\\d+\\.\\d+).*$";
        Pattern pattern = Pattern.compile(regex);

        try
        {
            String[] out = RunCommand.exec("/usr/bin/oslevel");

            /*
             * Example in AIX:
             * # /usr/bin/oslevel
             * 7.1.0.0
             */
            for (String line : out)
            {
                Matcher matcher = pattern.matcher(line);

                if (matcher.find())
                {
                    return matcher.group(1);
                }
            }
        }

        catch (IOException exc)
        {
            throw new IOException("Unable to query the AIX Operating System", exc);
        }

        return "0.0";
    }

    /**
     * Queries the Solaris operating system to obtain the basic system information.
     *
     * @throws IOException
     *         if it is unable to obtain the information
     */
    private static String readVersionSolaris() throws IOException
    {
        String regex = "^\\s*(\\d+(?:\\.\\d+)*)\\s*$";
        Pattern pattern = Pattern.compile(regex);

        try
        {
            String[] out = RunCommand.exec("/usr/bin/uname", "-r");

            /*
             * Example in Solaris:
             * au10qap8qftels2 $ uname -r
             * 5.10
             */
            for (String line : out)
            {
                Matcher matcher = pattern.matcher(line);

                if (matcher.find())
                {
                    return matcher.group(1);
                }
            }
        }

        catch (IOException exc)
        {
            throw new IOException("Unable to query the Solaris Operating System", exc);
        }

        return "0.0";
    }

    private static Platform readSystemInfoWindows()
    {
        boolean isServer = OS_NAME.contains("server");
        double actualVer = getOsVersionDigit();

        if (Double.compare(actualVer, 5.2) == 0)
        {
            return isServer ? Platform.WIN2003 : Platform.WINXP64;
        }

        else if (Double.compare(actualVer, 10.0) == 0)
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

        // Fallback for Legacy version matching for all other Legacy OS (7, 8, 2012, etc.)
        for (Platform os : Platform.values())
        {
            if (os.getFamily() == Platform.Family.WINDOWS)
            {
                if (Double.compare(os.getVersion(), actualVer) == 0 && os.isServer() == isServer)
                {
                    return os;
                }
            }
        }

        return Platform.UNKNOWN;
    }

    private static int resolveWindowsBuild()
    {
        try
        {
            String[] output = RunCommand.exec("cmd /c ver");

            if (output != null && output.length > 0)
            {
                // Format: 10.0.[BUILD].patch
                // Example: "Microsoft Windows [Version 10.0.22621.1105]"
                Pattern pattern = Pattern.compile("(\\d+)\\.(\\d+)\\.(\\d+)(?:\\.(\\d+))?");

                for (String line : output)
                {
                    if (line == null || line.isEmpty())
                    {
                        continue;
                    }

                    Matcher matcher = pattern.matcher(line);

                    if (matcher.find())
                    {
                        // Group 3 is the Build Number (e.g., 19045, 22621)
                        return Integer.parseInt(matcher.group(3));
                    }
                }
            }
        }

        catch (Exception exc)
        {
        }

        return -1;
    }

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
            sysInfo.version = String.valueOf(sysInfo.platform.getVersion());
        }

        else
        {
            sysInfo.platform = Platform.UNKNOWN;
            sysInfo.version = "0.0";
        }
    }

    /**
     * Resolves the local hostname.
     *
     * <p>
     * Prefers the Java Networking API. If a Fully Qualified Domain Name (FQDN) is returned, this
     * method truncates it to provide the short hostname only, for example: 'server01' instead of
     * 'server01.network.com'.
     * </p>
     *
     * <p>
     * If that hostname cannot be obtained from the Java Networking API, it will
     * alternatively execute the {@code hostname} command directly in the OS environment. If that
     * host name, however, cannot be obtained, a string "localhost" will be provided.
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
                String[] output = RunCommand.exec("hostname");

                if (output != null && output.length > 0)
                {
                    sysInfo.hostname = output[0].split("\\.")[0];
                }
            }

            catch (IOException exc2)
            {
                sysInfo.hostname = "localhost";
            }
        }
    }

    /**
     * Retrieves the IP address from the computer system. If the information cannot be obtained, a
     * string "UNKNOWN" will be provided.
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
     * Fetches the basic architecture type and Java architecture data model.
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

    /**
     * Queries the Linux operating system to obtain basic system information.
     *
     * @throws IOException
     *         if it is unable to obtain the information
     */
    private static Platform readSystemInfoLinux()
    {
        class LinuxRelease
        {
            final String file;
            final Pattern pattern;
            final Platform meta;

            LinuxRelease(String f, String regex, Platform m)
            {
                file = f;
                pattern = Pattern.compile(regex);
                meta = m;
            }
        }

        Platform plat = Platform.UNKNOWN;

        // 1. Define our Release models (Logic moved into a helper list)
        List<LinuxRelease> releases = new java.util.ArrayList<>();

        // Debian/Ubuntu
        releases.add(new LinuxRelease("/etc/debian_version", "(\\d+(?:\\.\\d+)*)", Platform.DEBIAN));
        releases.add(new LinuxRelease("/etc/debian_release", "(\\d+(?:\\.\\d+)*)", Platform.DEBIAN));

        // SuSE / SLES / Novell
        String suseRegex = "^\\s*VERSION\\s+=\\s+([0-9\\.]+)";
        releases.add(new LinuxRelease("/etc/SuSE-release", suseRegex, Platform.SUSE));
        releases.add(new LinuxRelease("/etc/sles-release", suseRegex, Platform.SUSE));
        releases.add(new LinuxRelease("/etc/novell-release", suseRegex, Platform.SUSE));

        // CentOS / Fedora
        releases.add(new LinuxRelease("/etc/centos-release", "(\\d+(?:\\.\\d+)*)", Platform.CENTOS));
        releases.add(new LinuxRelease("/etc/fedora-release", "Fedora[^\\d]+(\\d+)", Platform.FEDORA));

        // RedHat variations (Priority ordered)
        releases.add(new LinuxRelease("/etc/redhat-release", "\\s*CentOS.*(\\d+(?:\\.\\d+)*)", Platform.CENTOS));
        releases.add(new LinuxRelease("/etc/redhat-release", "\\s*Oracle VM.*(\\d+(?:\\.\\d+)*)", Platform.OVM));
        releases.add(new LinuxRelease("/etc/redhat-release", "Red Hat Enterprise Linux.+?(\\d+(?:\\.\\d+)*)", Platform.RHEL));

        // Fallback for modern systems (Ubuntu/Alpine/Amazon Linux)
        releases.add(new LinuxRelease("/etc/os-release", "^ID=[\"']?([^\"']+)[\"']?$", Platform.UNKNOWN));

        for (LinuxRelease info : releases)
        {
            Path releaseFile = Paths.get(info.file);

            if (Files.exists(releaseFile))
            {
                try (BufferedReader br = Files.newBufferedReader(releaseFile, StandardCharsets.UTF_8))
                {
                    String line;
                    Platform foundPlatform = Platform.UNKNOWN;
                    String foundVersion = "unknown";

                    while ((line = br.readLine()) != null)
                    {
                        Matcher matcher = info.pattern.matcher(line);

                        // Check for the Platform/Version pattern match
                        if (matcher.find())
                        {
                            if (info.meta == Platform.UNKNOWN)
                            {
                                // This is likely /etc/os-release where we parse ID=
                                foundPlatform = Platform.fromAbbreviation(matcher.group(1));
                            }

                            else
                            {
                                // Legacy file match (e.g., /etc/centos-release)
                                sysInfo.version = matcher.group(1);
                                plat = info.meta;
                            }
                        }

                        // Modern os-release version parsing
                        if (line.startsWith("VERSION_ID="))
                        {
                            foundVersion = line.split("=")[1].replaceAll("[\"']", "");
                        }
                    }

                    if (foundPlatform != Platform.UNKNOWN)
                    {
                        // Only update version if we actually found a VERSION_ID line
                        if (!foundVersion.equals("unknown"))
                        {
                            sysInfo.version = foundVersion;
                        }

                        plat = foundPlatform;
                    }
                }

                catch (IOException exc)
                {
                    // Silently fail to next file, or log error
                }
            }

            if (plat != Platform.UNKNOWN)
            {
                break;
            }
        }

        return plat;
    }
}