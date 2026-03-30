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
    private static final SystemProperties sysInfo;

    private static final String OS_NAME;
    private static final String OS_ARCH;
    private static final String OS_VERSION;

    private static final boolean IS_PPC;
    private static final boolean IS_SPARC;

    public static final boolean IS_AIX;
    public static final boolean IS_FREEBSD;
    public static final boolean IS_HPUX;
    public static final boolean IS_LINUX;
    public static final boolean IS_SOLARIS;
    public static final boolean IS_UNIX;
    public static final boolean IS_WINDOWS;

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
        IS_SOLARIS = OS_NAME.startsWith("solaris") || OS_NAME.startsWith("sunos") || OS_NAME.startsWith("sun os");
        IS_UNIX = IS_AIX || IS_FREEBSD || IS_HPUX || IS_LINUX || IS_SOLARIS;
        IS_WINDOWS = OS_NAME.startsWith("windows");

        IS_PPC = OS_ARCH.contains("ppc");
        IS_SPARC = OS_ARCH.contains("sparc");

        collectSystemInfo();
    }

    private static void collectSystemInfo()
    {
        readLocalHostName();
        readLocalIPaddress();
        readJavaArchitectureInfo();

        sysInfo.architecture = OS_ARCH;
        sysInfo.version = OS_VERSION;
        sysInfo.platform = autoDetect();

        try
        {
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
            sysInfo.platform = Platform.UNKNOWN;
        }
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
    public static Platform getOperatingSystemName()
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
        String ver = sysInfo.version;

        if (ver != null && !ver.isEmpty())
        {

            try
            {
                return Double.parseDouble(ver.split("\\.")[0] + "." + (ver.contains(".") ? ver.split("\\.")[1] : "0"));
            }

            catch (Exception exc)
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
     * Detects and returns the OperatingSystem constant for the current host. This handles the
     * version 10.0 ambiguity between Windows 10, 11, and modern Servers.
     */
    private static Platform autoDetect()
    {
        String os = System.getProperty("os.name").toLowerCase();
        boolean isServer = os.contains("server");

        if (os.contains("windows"))
        {
            int build = resolveWindowsBuild();

            // Disambiguate Windows 11 (Build 22000+) from Windows 10
            if (build >= 22000)
            {
                return isServer ? Platform.WIN2022 : Platform.WIN11;
            }

            else if (os.contains("10"))
            {
                return Platform.fromOSversion(10, isServer); // Or 2019/2022 checks
            }

            else if (os.contains("8.1"))
            {
                return Platform.fromOSversion(8.1, isServer);

            }

            else if (os.contains("7"))
            {
                return Platform.fromOSversion(7, isServer);
            }
        }

        for (Platform p : Platform.values())
        {
            if (os.contains(p.getRealName()))
            {
                return p;
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
                Matcher matcher = pattern.matcher(output[0]);

                if (matcher.find())
                {
                    return Integer.parseInt(matcher.group(3));
                }
            }
        }

        catch (Exception exc)
        {
        }

        return -1;
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
                    sysInfo.platform = Platform.AIX;
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
     * is not fully implemented yet. May be deleted as it is not common these days.
     */
    private static void readSystemInfoFreebsd()
    {
        sysInfo.platform = Platform.FREEBSD;
        sysInfo.version = "0";
    }

    /**
     * Queries the HP-UX operating system to obtain the basic system information. At present, it is
     * not fully implemented yet. May be deleted as it is not common these days.
     */
    private static void readSystemInfoHPUX()
    {
        sysInfo.platform = Platform.HPUX;
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
            final Platform meta;

            LinuxRelease(String f, String regex, Platform m)
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
                    boolean foundPlatform = false;
                    String foundVersion = "unknown";

                    while ((line = br.readLine()) != null)
                    {
                        Matcher matcher = info.pattern.matcher(line);

                        if (matcher.find())
                        {
                            if (info.meta == Platform.UNKNOWN)
                            {
                                sysInfo.platform = fromAbbreviation(matcher.group(1));
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
                    sysInfo.platform = Platform.SOLARIS;
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
        boolean isServer = OS_NAME.contains("server");
        sysInfo.version = OS_VERSION;

        if (actualVer == 10.0)
        {
            int build = resolveWindowsBuild();

            if (isServer)
            {
                if (build >= 20348)
                {
                    sysInfo.platform = Platform.WIN2022;
                }

                else if (build >= 17763)
                {
                    sysInfo.platform = Platform.WIN2019;
                }

                else if (build >= 14393)
                {
                    sysInfo.platform = Platform.WIN2016;
                }

                else
                {
                    sysInfo.platform = Platform.WIN2012; // Fallback for early kernel reports
                }
            }

            else
            {
                // Workstation logic
                sysInfo.platform = (build >= 22000 ? Platform.WIN11 : Platform.WIN10);
            }

            return;
        }

        // Standard version matching for older OS (7, 8, XP, etc.)
        for (Platform os : Platform.values())
        {
            if (os.getVersion() == actualVer && os.isServer() == isServer)
            {
                sysInfo.platform = os;
                return;
            }
        }
    }

    /**
     * Searches for a matching Operating System enumeration value based on the specified abbreviated
     * name or constant string, such as {@code ubuntu}, {@code amzn}, or {@code WIN10}.
     *
     * @param osName
     *        the Operating System name in its short or abbreviated name format
     * @return an enumeration value if a match is found, otherwise {@code UNKNOWN}
     */
    public static Platform fromAbbreviation(String osName)
    {
        if (osName != null && !osName.isEmpty())
        {
            String searchName = osName.toLowerCase().trim();

            switch (searchName)
            {
                case "ubuntu":
                    searchName = "ubn";
                break;

                case "raspbian":
                    searchName = "debian";
                break;

                case "ol":
                    searchName = "ovm";
                break;
            }

            for (Platform os : Platform.values())
            {
                if (os.name().equalsIgnoreCase(searchName))
                {
                    return os;
                }
            }
        }

        return Platform.UNKNOWN;
    }
}