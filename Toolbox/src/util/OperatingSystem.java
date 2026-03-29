package util;

/**
 * Defines a mapping of Operating System constants to their human-readable names, kernel versions,
 * and server/workstation classifications.
 * 
 * <p>
 * This utility is particularly useful for handling legacy Windows versioning logic where the system
 * property version string needs to be mapped to a marketing name, for example: mapping version 6.1
 * to Windows 7.
 * </p>
 * 
 * <p>
 * <b>Change logs:</b>
 * </p>
 * 
 * <ul>
 * <li>Added on 10 February 2021</li>
 * </ul>
 * 
 * @author Trevor Maggs
 * @version 0.1
 * @since 10 February 2021
 * @see <a target="_top" href=
 *      "http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/tip/src/windows/native/java/lang/java_props_md.c">See
 *      source code java_props_md.c for actual Windows OS name strings</a>
 */
public enum OperatingSystem
{
    /* Also see https://www.techthoughts.info/windows-version-numbers */
    MACOS("macOS", 0.0, false),
    AMZN("Amazon Linux", 0.0, true),
    ALPINE("Alpine Linux", 0.0, true),
    LINUX("Linux", 0.0, true),
    AIX("AIX", 0.0, true),
    CENTOS("CentOS", 0.0, true),
    DEBIAN("Debian", 0.0, true),
    FEDORA("Fedora", 0.0, true),
    FREEBSD("FreeBSD", 0.0, true),
    HPUX("HP-UX", 0.0, true),
    OVM("Oracle VM", 0.0, true),
    RHEL("Red Hat", 0.0, true),
    SOLARIS("Solaris", 0.0, true),
    SUNOS("Sun OS", 0.0, true),
    SUSE("SuSE", 0.0, true),
    SLES("Sles", 0.0, true),
    UBN("Ubuntu", 0.0, true),
    WIN2022("Windows Server 2022", 10.0, true),
    WIN2019("Windows Server 2019", 10.0, true),
    WIN2016("Windows Server 2016", 10.0, true),
    WIN11("Windows 11", 10.0, false),
    WIN10("Windows 10", 10.0, false),
    WIN2012R2("Windows Server 2012 R2", 6.3, true),
    WIN81("Windows 8.1", 6.3, false),
    WIN2012("Windows Server 2012", 6.2, true),
    WIN8("Windows 8", 6.2, false),
    WIN2008R2("Windows Server 2008 R2", 6.1, true),
    WIN7("Windows 7", 6.1, false),
    WIN2008("Windows Server 2008", 6.0, true),
    WINVISTA("Windows Vista", 6.0, false),
    WIN2003("Windows 2003", 5.2, true),
    WINXP64("Windows XP", 5.2, false), /* Windows XP Professional x64 Edition */
    WINXP("Windows XP", 5.1, false),
    WIN2000("Windows 2000", 5.0, true),
    WINME("Windows Me", 4.90, false),
    WIN98("Windows 98", 4.10, false),
    WINNT4("Windows NT", 4.0, true),
    WIN95("Windows 95", 4.0, false),
    NT351("Windows NT", 3.51, true),
    NT35("Windows NT", 3.5, true),
    NT31("Windows NT", 3.1, true),
    UNKNOWN("Unknown", 0.0, false);
    /*
     * Note, Windows Server 2003 R2 also exists. Use native code to check.
     * Refer to MSDN for coding details.
     */

    private String realname;
    private double version;
    private boolean devicetype;

    OperatingSystem(String name, double ver, boolean type)
    {
        realname = name;
        version = ver;
        devicetype = type;
    }

    /**
     * Returns the name of the Operating System mapped to by this named constant.
     * 
     * @return real name of the Operating System
     */
    public String getRealName()
    {
        return realname;
    }

    /**
     * Returns the version of the Operating System mapped to by this named constant.
     * 
     * @return the OS version (e.g., 10.0, 6.3). Note: For non-Windows platforms, this returns
     *         {@code 0.0}. Use {@code SystemInfo} for dynamic version retrieval on other platforms.
     */
    public double getVersion()
    {
        return version;
    }

    /**
     * Returns a boolean value that determines whether the computer system is a server or a
     * workstation type.
     * 
     * @return a true value represents a server type, while a false value represents a workstation
     *         type
     */
    public boolean isServer()
    {
        return devicetype;
    }

    /**
     * Determines if this operating system belongs to the Windows family.
     * 
     * @return {@code true} if the OS is a Windows or Windows NT variant
     */
    public boolean isWindows()
    {
        return this.name().startsWith("WIN") || this.name().startsWith("NT");
    }

    /**
     * Determines if this operating system is a Unix or Unix-like variant.
     * 
     * @return {@code true} if the OS is not Windows and is not {@link #UNKNOWN}
     */
    public boolean isUnix()
    {
        return !isWindows() && this != UNKNOWN;
    }

    /**
     * Searches for a matching Operating System enumeration value based on the specified abbreviated
     * name or constant string, such as {@code ubuntu}, {@code amzn}, or {@code WIN10}.
     *
     * @param osName
     *        the Operating System name in its short or abbreviated name format
     * @return an enumeration value if a match is found, otherwise {@code UNKNOWN}
     */
    public static OperatingSystem fromAbbreviation(String osName)
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

            for (OperatingSystem os : OperatingSystem.values())
            {
                if (os.name().equalsIgnoreCase(searchName))
                {
                    return os;
                }
            }
        }

        return UNKNOWN;
    }

    /**
     * Searches for a matching Operating System enumeration value based on the specified full real
     * name string, such as {@code Windows Server 2008 R2}.
     *
     * @param osName
     *        the Operating System name in its full real name format
     * @return an enumeration value if a match is found, otherwise {@code UNKNOWN}
     */
    public static OperatingSystem fromRealName(String osName)
    {
        if (osName != null)
        {
            for (OperatingSystem os : OperatingSystem.values())
            {
                if (os.getRealName().equalsIgnoreCase(osName))
                {
                    return os;
                }
            }
        }

        return UNKNOWN;
    }

    @Deprecated
    public static OperatingSystem fromAbbreviation2(String osName)
    {
        if (osName != null)
        {
            for (OperatingSystem os : OperatingSystem.values())
            {
                if (os.name().equalsIgnoreCase(osName))
                {
                    return os;
                }
            }
        }

        return UNKNOWN;
    }
}