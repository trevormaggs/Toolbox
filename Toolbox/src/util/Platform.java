package util;

/**
 * Defines a mapping of Operating System constants to their human-readable names, kernel versions,
 * and server/workstation classifications.
 *
 * <p>
 * This enumeration provides a standardised way to identify Platforms across different families,
 * such as Windows, Linux, Unix, and macOS. It is capable of resolving ambiguities in standard
 * system properties, such as distinguishing between Windows 10 and Windows Server 2016, or
 * detecting specific Linux flavours like CentOS, RHEL, and Ubuntu.
 * </p>
 *
 * <p>
 * For Windows, this maps internal version numbers, such as 6.1 and 10.0 etc, combined with build
 * numbers and product types to familiar marketing names, such as Windows 7 and Windows Server
 * 2022.
 * </p>
 *
 * <p>
 * <b>Change logs:</b>
 * </p>
 *
 * <ul>
 * <li>Added on 10 February 2021</li>
 * <li>Updated March 2024: Added support for Windows 11, Windows Server 2022, and advanced Linux
 * distribution detection</li>
 * <li>Updated March 2026: Big improvements in logic in several methods, including detecting the
 * difference between Win10 and Win 11, plus Windows Server 2025</li>
 * </ul>
 *
 * @author Trevor Maggs
 * @version 0.2
 * @since 10 February 2021
 * @see <a target="_top" href=
 *      "http://hg.openjdk.java.net/jdk8u/jdk8u/jdk/file/tip/src/windows/native/java/lang/java_props_md.c">OpenJDK
 *      java_props_md.c: Native Windows OS name strings</a>
 */
public enum Platform
{
    MACOS("macOS", 0.0, false, OSFamily.MAC),

    // Windows workstations
    WIN11("Windows 11", 10.0, false, OSFamily.WINDOWS),
    WIN10("Windows 10", 10.0, false, OSFamily.WINDOWS),
    WIN81("Windows 8.1", 6.3, false, OSFamily.WINDOWS),
    WIN8("Windows 8", 6.2, false, OSFamily.WINDOWS),
    WIN7("Windows 7", 6.1, false, OSFamily.WINDOWS),
    WINVISTA("Windows Vista", 6.0, false, OSFamily.WINDOWS),
    WINXP64("Windows XP", 5.2, false, OSFamily.WINDOWS),
    WINXP("Windows XP", 5.1, false, OSFamily.WINDOWS),
    WINME("Windows Me", 4.90, false, OSFamily.WINDOWS),
    WIN98("Windows 98", 4.10, false, OSFamily.WINDOWS),
    WIN95("Windows 95", 4.0, false, OSFamily.WINDOWS),

    // Windows servers
    WIN2025("Windows Server 2025", 10.0, true, OSFamily.WINDOWS),
    WIN2022("Windows Server 2022", 10.0, true, OSFamily.WINDOWS),
    WIN2019("Windows Server 2019", 10.0, true, OSFamily.WINDOWS),
    WIN2016("Windows Server 2016", 10.0, true, OSFamily.WINDOWS),
    WIN2012R2("Windows Server 2012 R2", 6.3, true, OSFamily.WINDOWS),
    WIN2012("Windows Server 2012", 6.2, true, OSFamily.WINDOWS),
    WIN2008R2("Windows Server 2008 R2", 6.1, true, OSFamily.WINDOWS),
    WIN2008("Windows Server 2008", 6.0, true, OSFamily.WINDOWS),
    WIN2003("Windows 2003", 5.2, true, OSFamily.WINDOWS),
    WIN2000("Windows 2000", 5.0, true, OSFamily.WINDOWS),
    WINNT4("Windows NT", 4.0, true, OSFamily.WINDOWS),
    NT351("Windows NT", 3.51, true, OSFamily.WINDOWS),
    NT35("Windows NT", 3.5, true, OSFamily.WINDOWS),
    NT31("Windows NT", 3.1, true, OSFamily.WINDOWS),

    // Linux Distributions (No generic "Linux" needed)
    AMZN("Amazon Linux", 0.0, true, OSFamily.LINUX),
    ALPINE("Alpine Linux", 0.0, true, OSFamily.LINUX),
    CENTOS("CentOS", 0.0, true, OSFamily.LINUX),
    DEBIAN("Debian", 0.0, true, OSFamily.LINUX),
    FEDORA("Fedora", 0.0, true, OSFamily.LINUX),
    OVM("Oracle VM", 0.0, true, OSFamily.LINUX),
    RHEL("Red Hat", 0.0, true, OSFamily.LINUX),
    SUSE("SuSE", 0.0, true, OSFamily.LINUX),
    SLES("Sles", 0.0, true, OSFamily.LINUX),
    UBN("Ubuntu", 0.0, true, OSFamily.LINUX),

    // Unix Variants
    AIX("AIX", 0.0, true, OSFamily.UNIX),
    FREEBSD("FreeBSD", 0.0, true, OSFamily.UNIX),
    HPUX("HP-UX", 0.0, true, OSFamily.UNIX),
    SOLARIS("Solaris", 0.0, true, OSFamily.UNIX),
    UNKNOWN("Unknown", 0.0, false, OSFamily.OTHER);

    public enum OSFamily
    {
        WINDOWS, LINUX, UNIX, MAC, OTHER;

        public boolean isUnixBased()
        {
            return this == LINUX || this == UNIX || this == MAC;
        }
    }

    private final String realName;
    private final double version;
    private final boolean isServer;
    private final OSFamily osfamily;

    private Platform(String name, double ver, boolean type, OSFamily family)
    {
        this.realName = name;
        this.version = ver;
        this.isServer = type;
        this.osfamily = family;
    }

    /**
     * Returns the familiar marketing name of the Operating System mapped to by this named constant.
     *
     * @return the real name of the Operating System
     */
    public String getRealName()
    {
        return realName;
    }

    /**
     * Returns the version of the Operating System mapped to by this named constant.
     *
     * @return the OS version, for example: 10.0, 6.3, etc. Note: for non-Windows Platforms, this
     *         returns {@code 0.0}. Use {@link SystemInfo} for dynamic version retrieval on other
     *         Platforms
     */
    public double getVersion()
    {
        return version;
    }

    /**
     * Returns a boolean value to indicate whether the computer system is a server or a workstation
     * type.
     *
     * @return {@code true} if the system is a server type, or {@code false} for a workstation type
     */
    public boolean isServer()
    {
        return isServer;
    }

    /**
     * Returns the broad operating system family, such as Windows, Linux, or Unix, that this
     * specific Platform belongs to.
     *
     * <p>
     * This classification is useful for identifying shared characteristics across different
     * distributions or versions, such as POSIX compliance.
     * </p>
     *
     * @return the {@code OSFamily} classification
     */
    public OSFamily getOSFamily()
    {
        return osfamily;
    }

    /**
     * Searches for a matching Operating System constant based on a numerical version and device
     * role.
     *
     * <p>
     * This allows for distinguishing between OS releases that share the same version number but
     * have different roles, such as Windows 10 (10.0, false) and Windows Server 2016 (10.0, true).
     * </p>
     *
     * @param osVer
     *        the numerical version of the Operating System
     * @param isServer
     *        {@code true} if the OS is a server type, or {@code false} for workstations
     * @return the matching {@code Platform} constant, or {@link #UNKNOWN} if no match is found
     */
    public static Platform fromOSversion(double osVer, boolean isServer)
    {
        if (osVer > 0)
        {
            for (Platform plat : Platform.values())
            {
                if (Double.compare(plat.getVersion(), osVer) == 0 && plat.isServer() == isServer)
                {
                    return plat;
                }
            }
        }

        return UNKNOWN;
    }

    /**
     * Searches for a matching Operating System enumeration value based on the familiar marketing
     * name string, such as {@code Windows Server 2008 R2}.
     *
     * @param osName
     *        the Operating System name in its short or abbreviated name format
     * @return an enumeration value if a match is found; otherwise {@link #UNKNOWN}
     */
    public static Platform fromRealName(String osName)
    {
        if (osName != null && !osName.isEmpty())
        {
            for (Platform plat : Platform.values())
            {
                if (osName.equalsIgnoreCase(plat.getRealName()))
                {
                    return plat;
                }
            }
        }

        return UNKNOWN;
    }

    /**
     * Searches for a matching Operating System enumeration value based on the specified abbreviated
     * name, such as {@code ubuntu}, {@code amzn}, or {@code WIN10}.
     *
     * @param shortName
     *        the Operating System name in its short or abbreviated name format
     * @return an enumeration value if a match is found; otherwise {@link #UNKNOWN}
     */
    public static Platform fromShortName(String osName)
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

                case "red hat":
                case "rhel":
                    searchName = "rhel";
                break;
            }

            for (Platform os : values())
            {
                if (os.name().equalsIgnoreCase(searchName) || os.getRealName().equalsIgnoreCase(searchName))
                {
                    return os;
                }
            }
        }

        return UNKNOWN;
    }
}