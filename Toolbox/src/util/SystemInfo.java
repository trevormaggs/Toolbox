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

/**
 * Provides a centralised utility for retrieving core system metadata, including hardware
 * architecture, network identity, and operating system specifics.
 * 
 * <p>
 * Data is captured once during class initialisation and stored in an internal immutable-like state
 * to ensure high-performance retrieval and consistency across the application lifecycle.
 * </p>
 * 
 * <p>
 * Following list of possible values you can obtain.
 * </p>
 *
 * <ul>
 * <li>Operating system name</li>
 * <li>Alternative operating system name</li>
 * <li>Operating system version</li>
 * <li>Operating system version in real numeric format</li>
 * <li>Host name</li>
 * <li>IP address</li>
 * <li>Architecture type ie x86, amd64 etc</li>
 * <li>Architecture data model, 32-bit or 64-bit</li>
 * </ul>
 *
 *
 * <p>
 * <b>Change History</b>
 * </p>
 * 
 * <ul>
 * <li>Trevor Maggs created on 6 April 2016</li>
 * <li>Changed from Host to SystemInfo static only class on 12 April 2019</li>
 * <li>Improved logic in many methods. 29 March 2026</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @version 0.3
 * @since 29 March 2026
 */
public final class SystemInfo
{
    // https://github.com/apache/commons-lang/blob/master/src/main/java/org/apache/commons/lang3/SystemUtils.java
    private static final String OS_NAME;
    private static final String OS_ARCH;
    private static final String OS_VERSION;

    private static final boolean IS_PPC;
    private static final boolean IS_SPARC;

    private static final boolean IS_AIX;
    private static final boolean IS_FREEBSD;
    private static final boolean IS_HPUX;
    private static final boolean IS_LINUX;
    private static final boolean IS_OPENBSD;
    private static final boolean IS_SOLARIS;
    private static final boolean IS_UNIX;
    private static final boolean IS_WINDOWS;

    private static final SystemProperties sysInfo;

    static
    {
        sysInfo = new SystemProperties();

        OS_ARCH = System.getProperty("os.arch").toLowerCase();
        OS_NAME = System.getProperty("os.name").toLowerCase();
        OS_VERSION = System.getProperty("os.version").toLowerCase();

        IS_AIX = OS_NAME.startsWith("aix");
        IS_FREEBSD = OS_NAME.startsWith("freebsd");
        IS_HPUX = OS_NAME.startsWith("hp-ux");
        IS_LINUX = OS_NAME.startsWith("linux");
        IS_OPENBSD = OS_NAME.startsWith("openbsd");
        IS_SOLARIS = OS_NAME.startsWith("solaris") || OS_NAME.startsWith("sunos") || OS_NAME.startsWith("sun os");
        IS_UNIX = IS_AIX || IS_FREEBSD || IS_HPUX || IS_LINUX || IS_OPENBSD || IS_SOLARIS;
        IS_WINDOWS = OS_NAME.startsWith("windows");

        IS_PPC = OS_ARCH.contains("ppc");
        IS_SPARC = OS_ARCH.contains("sparc");

        collectSystemInfo();
    }

    private static class SystemProperties
    {
        private String hostname;
        private String ip_address;
        private String architecture;
        private byte data_model;
        private String version;
        private OperatingSystem platform;

        private String getPlatformName()
        {
            return platform.toString();
        }

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
            line.append(String.format("  %-30s %s%n", "[OS Name]", getPlatformName()));
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
    private SystemInfo()
    {
        throw new UnsupportedOperationException("Not intended for instantiation");
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
     * @return the abbreviated OS name
     */
    public static String getOSName()
    {
        return sysInfo.getPlatformName();
    }

    /**
     * Gets the real OS variant or flavour name, such as Windows 2000, Fedora, CentOS etc.
     *
     * @return the real Operating System name
     */
    public static String getOperatingSystemRealName()
    {
        return sysInfo.platform.getRealName();
    }

    /**
     * Returns the current running Operating System.
     *
     * Note, this method is similar to the {@code getOSName()} method, except it returns the
     * {@code OperatingSystem} type.
     *
     * @return constant name based on the OperatingSystem type
     */
    public static OperatingSystem getPlatformName()
    {
        return sysInfo.platform;
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
        return (sysInfo.version.matches("-?\\d+(\\.\\d+)?") ? Double.parseDouble(sysInfo.version) : 0);
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
     * @return 32-bit or 64-bit as a byte value
     */
    public static byte getArchitectureDataModel()
    {
        return sysInfo.data_model;
    }

    /**
     * Queries the computer system to determine whether the specified operating system is running.
     *
     * @param osName
     *        the operating system name to check for. The name is a constant derived from the
     *        {@code OperatingSystem} enumeration class. For example,
     *        {@code SystemInfo.isRunningOS(DEBIAN)} will return true if the Debian operating system
     *        is found running
     *
     * @return boolean true if the specified operating system is present
     * @throws IllegalArgumentException
     *         if any parameters are unrecognised
     */
    public static boolean isRunningOS(OperatingSystem osName)
    {
        return isRunningOS(EnumSet.of(osName));
    }

    /**
     * Evaluates if the current operating system matches against the specified set.
     * 
     * @param osNameSet
     *        A set of {@link OperatingSystem} constants to check against.
     * @return {@code true} if the current platform is contained within the set.
     * 
     * @throws IllegalArgumentException
     *         if the specified set is null or empty
     */
    public static boolean isRunningOS(EnumSet<OperatingSystem> osNameSet)
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
     * Checks whether the platform is a Power PC hardware.
     *
     * @return boolean true if the hardware is a Power PC system
     */
    public static boolean isPowerPcHardware()
    {
        return IS_PPC;
    }

    /**
     * Checks whether the platform is a SPARC hardware.
     *
     * @return boolean true if the hardware is a SPARC type
     */
    public static boolean isSparcHardware()
    {
        return IS_SPARC;
    }

    /**
     * Checks whether the running OS is based on UNIX.
     *
     * @return boolean true if the platform is running UNIX
     */
    public static boolean isUnixSystem()
    {
        return IS_UNIX;
    }

    /**
     * Checks whether AIX is the current operating system.
     *
     * @return boolean true if AIX is running
     */
    public static boolean isAIX()
    {
        return IS_AIX;
    }

    /**
     * Checks whether FreeBSD is the current operating system.
     *
     * @return boolean true if FreeBSD is running
     */
    public static boolean isFreeBSD()
    {
        return IS_FREEBSD;
    }

    /**
     * Checks whether HP-UX is the current operating system.
     *
     * @return boolean true if HP-UX is running
     */
    public static boolean isHPUX()
    {
        return IS_HPUX;
    }

    /**
     * Checks whether Linux is running.
     *
     * @return boolean true if Linux is running
     */
    public static boolean isLinux()
    {
        return IS_LINUX;
    }

    /**
     * Checks whether Solaris is the current operating system.
     *
     * @return boolean true if Solaris is running
     */
    public static boolean isSolaris()
    {
        return IS_SOLARIS;
    }

    /**
     * Checks whether Windows is running.
     *
     * @return boolean true if Windows is running
     */
    public static boolean isWindows()
    {
        return IS_WINDOWS;
    }

    /**
     * Displays the system information.
     */
    public static void displaySystemInfo()
    {
        System.out.println(sysInfo);
    }

    /*-------------------- PRIVATE STATIC METHODS -------------------- */

    /**
     * Resolves the local hostname.
     * 
     * <p>
     * Prefers the Java Networking API. If a Fully Qualified Domain Name (FQDN) is returned, this
     * method truncates it to provide the short hostname only, for example: 'server01' instead of
     * 'server01.network.com'.
     * </p>
     */
    private static void readLocalHostName()
    {
        try
        {
            String fqdn = InetAddress.getLocalHost().getHostName();

            // Example: update oc7456161242.ibm.com to oc7456161242
            sysInfo.hostname = fqdn.split("\\.")[0];
        }

        catch (UnknownHostException exc)
        {
            // Try the alternative solution if possible
            readLocalHostNameByCommand();
        }
    }

    /**
     * If the {@code readLocalHostName} method cannot obtain the host name for any reason, it will
     * alternatively execute the {@code hostname} command directly on the computer system in an
     * attempt to fetch the required name.
     *
     * If that host name, however, cannot be obtained, a string "UNKNOWN" will be provided.
     */
    private static void readLocalHostNameByCommand()
    {
        try
        {
            String fqdn = RunCommand.exec("hostname")[0];

            sysInfo.hostname = fqdn.split("\\.")[0];
        }

        catch (IOException exc)
        {
            sysInfo.hostname = "UNKNOWN";
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
            sysInfo.ip_address = "UNKNOWN";
        }
    }

    /**
     * Fetches the basic architecture type and Java architecture data model.
     */
    private static void readJavaArchitectureInfo()
    {
        sysInfo.architecture = OS_ARCH;
        sysInfo.data_model = Byte.valueOf(System.getProperty("sun.arch.data.model"));
    }

    /**
     * Queries the AIX operating system to obtain the basic system information.
     *
     * @throws IOException
     *         if it is unable to obtain the information
     */
    private static void readSystemInfoAIX() throws IOException
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
                    sysInfo.platform = OperatingSystem.AIX;
                    sysInfo.version = matcher.group(1);

                    return;
                }
            }
        }
        catch (IOException exc)
        {
            throw new IOException("Unable to query the AIX Operating System", exc);
        }
    }

    /**
     * Queries the FreeBSD operating system to obtain the basic system information. At present, it
     * is not fully implemented yet.
     */
    private static void readSystemInfoFreebsd()
    {
        sysInfo.platform = OperatingSystem.FREEBSD;
        sysInfo.version = "0";
    }

    /**
     * Queries the HP-UX operating system to obtain the basic system information. At present, it is
     * not fully implemented yet.
     */
    private static void readSystemInfoHPUX()
    {
        sysInfo.platform = OperatingSystem.HPUX;
        sysInfo.version = "0";
    }

    /**
     * Queries the Linux operating system to obtain basic system information.
     *
     * @throws IOException
     *         if it is unable to obtain the information
     */
    private static void readSystemInfoLinux() throws IOException
    {
        class LinuxRelease
        {
            final String file;
            final Pattern pattern;
            final OperatingSystem meta;

            LinuxRelease(String f, String regex, OperatingSystem m)
            {
                file = f;
                pattern = Pattern.compile(regex);
                meta = m;
            }
        }

        /*
         * # cat /etc/redhat-release
         * Red Hat Enterprise Linux Server release 6.7 (Santiago)
         *
         * # cat /etc/redhat-release
         * Red Hat Enterprise Linux Workstation release 7.7 (Maipo)
         *
         * # cat /etc/SuSE-release
         * SUSE Linux Enterprise Server 10 (x86_64)
         * VERSION = 10
         * PATCHLEVEL = 4
         *
         * # cat /etc/centos-release
         * CentOS release 6.3 (Final)
         *
         * # cat /etc/fedora-release
         * Fedora release 24 (Twenty Four)
         *
         * # cat /etc/redhat-release
         * Oracle VM server release 3.3.2
         */

        List<LinuxRelease> releases = new java.util.ArrayList<>();

        // Debian/Ubuntu
        releases.add(new LinuxRelease("/etc/debian_version", "(\\d+(?:\\.\\d+)*)", OperatingSystem.DEBIAN));
        releases.add(new LinuxRelease("/etc/debian_release", "(\\d+(?:\\.\\d+)*)", OperatingSystem.DEBIAN));

        // SuSE / SLES / Novell
        String suseRegex = "^\\s*VERSION\\s+=\\s+([0-9\\.]+)";
        releases.add(new LinuxRelease("/etc/SuSE-release", suseRegex, OperatingSystem.SUSE));
        releases.add(new LinuxRelease("/etc/sles-release", suseRegex, OperatingSystem.SUSE));
        releases.add(new LinuxRelease("/etc/novell-release", suseRegex, OperatingSystem.SUSE));

        // CentOS / Fedora
        releases.add(new LinuxRelease("/etc/centos-release", "(\\d+(?:\\.\\d+)*)", OperatingSystem.CENTOS));
        releases.add(new LinuxRelease("/etc/fedora-release", "Fedora[^\\d]+(\\d+)", OperatingSystem.FEDORA));

        // RedHat variations (Priority ordered)
        releases.add(new LinuxRelease("/etc/redhat-release", "\\s*CentOS.*(\\d+(?:\\.\\d+)*)", OperatingSystem.CENTOS));
        releases.add(new LinuxRelease("/etc/redhat-release", "\\s*Oracle VM.*(\\d+(?:\\.\\d+)*)", OperatingSystem.OVM));
        releases.add(new LinuxRelease("/etc/redhat-release", "Red Hat Enterprise Linux.+?(\\d+(?:\\.\\d+)*)", OperatingSystem.RHEL));

        // Fallback for modern systems (Ubuntu/Alpine/Amazon Linux)
        releases.add(new LinuxRelease("/etc/os-release", "^ID=[\"']?([^\"']+)[\"']?$", OperatingSystem.UNKNOWN));

        for (LinuxRelease info : releases)
        {
            Path releaseFile = Paths.get(info.file);

            if (Files.exists(releaseFile))
            {
                try (BufferedReader br = Files.newBufferedReader(releaseFile, StandardCharsets.UTF_8))
                {
                    String line;
                    boolean foundPlatform = false;
                    String foundVersion = "unknown";

                    while ((line = br.readLine()) != null)
                    {
                        Matcher matcher = info.pattern.matcher(line);

                        if (matcher.find())
                        {
                            if (info.meta == OperatingSystem.UNKNOWN)
                            {
                                sysInfo.platform = OperatingSystem.fromAbbreviation(matcher.group(1));
                                foundPlatform = true;
                            }

                            else
                            {
                                sysInfo.platform = info.meta;
                                sysInfo.version = matcher.group(1);
                                return;
                            }
                        }

                        if (line.startsWith("VERSION_ID="))
                        {
                            foundVersion = line.split("=")[1].replaceAll("[\"']", "");

                            if (foundPlatform)
                            {
                                sysInfo.version = foundVersion;
                                return;
                            }
                        }
                    }

                    if (foundPlatform)
                    {
                        sysInfo.version = foundVersion;
                        return;
                    }
                }

                catch (IOException exc)
                {
                    throw new IOException("Unable to read release file [" + releaseFile + "] from Linux", exc);
                }
            }
        }
    }

    /**
     * Queries the Solaris operating system to obtain the basic system information.
     *
     * @throws IOException
     *         if it is unable to obtain the information
     */
    private static void readSystemInfoSolaris() throws IOException
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
                    sysInfo.platform = OperatingSystem.SOLARIS;
                    sysInfo.version = matcher.group(1);

                    return;
                }
            }
        }
        catch (IOException exc)
        {
            throw new IOException("Unable to query the Solaris Operating System", exc);
        }
    }

    /**
     * Queries the Windows operating system to obtain the basic system information.
     *
     * <p>
     * Note, the version of each Windows operating system is uniquely assigned a numerical
     * identifier. Refer to the list of the known Windows versions below.
     * </p>
     *
     * <p>
     * <b>Server versions</b>
     * </p>
     *
     * <ul>
     * <li>Windows Server 2019 = ver 10.0</li>
     * <li>Windows Server 2016 = ver 10.0</li>
     * <li>Windows Server 2012 R2 = ver 6.3</li>
     * <li>Windows Server 2012 = ver 6.2</li>
     * <li>Windows Server 2008 R2 = ver 6.1</li>
     * <li>Windows Server 2008 = ver 6.0</li>
     * <li>Windows Server 2003 R2 = ver 5.2</li>
     * <li>Windows Server 2003 = ver 5.2</li>
     * <li>Windows Server 2000 = ver 5.0</li>
     * <li>Windows NT 4.0 = ver 4.0</li>
     * <li>Windows NT 3.51 = ver 3.51</li>
     * <li>Windows NT 3.5 = ver 3.50</li>
     * <li>Windows NT 3.1 = ver 3.10</li>
     * </ul>
     *
     * <p>
     * <b>Workstation versions</b>
     * </p>
     *
     * <ul>
     * <li>Windows 10 = ver 10.0</li>
     * <li>Windows 8.1 = ver 6.3</li>
     * <li>Windows 8 = ver 6.2</li>
     * <li>Windows 7 = ver 6.1</li>
     * <li>Windows Vista = ver 6.0</li>
     * <li>Windows XP Pro x64 = ver 5.2</li>
     * <li>Windows XP = ver 5.1</li>
     * <li>Windows 2000 = ver 5.0</li>
     * <li>Windows ME = ver 4.9</li>
     * <li>Windows 98 = ver 4.1</li>
     * <li>Windows 95 = ver 4.0</li>
     * </ul>
     *
     * @see <a href=
     *      "https://docs.microsoft.com/en-us/windows/win32/sysinfo/operating-system-version">Windows
     *      OS Versions</a>
     *
     * @throws IOException
     *         if it is unable to obtain the information
     */
    private static void readSystemInfoWindows() throws IOException
    {
        double actualVer = (OS_VERSION.matches("-?\\d+(\\.\\d+)?") ? Double.parseDouble(OS_VERSION) : 0);

        for (OperatingSystem os : OperatingSystem.values())
        {
            if (os.getVersion() == actualVer && os.getRealName().equalsIgnoreCase(OS_NAME))
            {
                sysInfo.platform = os;
                sysInfo.version = OS_VERSION;

                return;
            }
        }

        throw new IOException("Unable to query the Windows Operating System");
    }

    /**
     * Manages the collection of system properties.
     * 
     * <p>
     * This method attempts to resolve identity and OS traits through a hierarchy of Java System
     * properties and native shell commands. If a specific OS-level query fails, the error is logged
     * to stderr and the platform defaults to {@link OperatingSystem#UNKNOWN}.
     * </p>
     */
    private static void collectSystemInfo()
    {
        try
        {
            readLocalHostName();
            readLocalIPaddress();
            readJavaArchitectureInfo();

            if (IS_AIX)
            {
                readSystemInfoAIX();
            }

            else if (IS_FREEBSD)
            {
                readSystemInfoFreebsd();
            }

            else if (IS_HPUX)
            {
                readSystemInfoHPUX();
            }

            else if (IS_LINUX)
            {
                readSystemInfoLinux();
            }

            else if (IS_SOLARIS)
            {
                readSystemInfoSolaris();
            }

            else if (IS_WINDOWS)
            {
                readSystemInfoWindows();
            }
        }

        catch (IOException exc)
        {
            sysInfo.platform = OperatingSystem.UNKNOWN;
            System.err.println(exc.getMessage());
        }
    }
}