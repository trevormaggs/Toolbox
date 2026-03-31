package util;

public enum Platform
{
    MACOS("macOS", 0.0, false, Family.MAC),

    // Windows workstations
    WIN11("Windows 11", 10.0, false, Family.WINDOWS),
    WIN10("Windows 10", 10.0, false, Family.WINDOWS),
    WIN81("Windows 8.1", 6.3, false, Family.WINDOWS),
    WIN8("Windows 8", 6.2, false, Family.WINDOWS),
    WIN7("Windows 7", 6.1, false, Family.WINDOWS),
    WINVISTA("Windows Vista", 6.0, false, Family.WINDOWS),
    WINXP64("Windows XP", 5.2, false, Family.WINDOWS),
    WINXP("Windows XP", 5.1, false, Family.WINDOWS),
    WINME("Windows Me", 4.90, false, Family.WINDOWS),
    WIN98("Windows 98", 4.10, false, Family.WINDOWS),
    WIN95("Windows 95", 4.0, false, Family.WINDOWS),

    // Windows servers
    WIN2022("Windows Server 2022", 10.0, true, Family.WINDOWS),
    WIN2019("Windows Server 2019", 10.0, true, Family.WINDOWS),
    WIN2016("Windows Server 2016", 10.0, true, Family.WINDOWS),
    WIN2012R2("Windows Server 2012 R2", 6.3, true, Family.WINDOWS),
    WIN2012("Windows Server 2012", 6.2, true, Family.WINDOWS),
    WIN2008R2("Windows Server 2008 R2", 6.1, true, Family.WINDOWS),
    WIN2008("Windows Server 2008", 6.0, true, Family.WINDOWS),
    WIN2003("Windows 2003", 5.2, true, Family.WINDOWS),
    WIN2000("Windows 2000", 5.0, true, Family.WINDOWS),
    WINNT4("Windows NT", 4.0, true, Family.WINDOWS),
    NT351("Windows NT", 3.51, true, Family.WINDOWS),
    NT35("Windows NT", 3.5, true, Family.WINDOWS),
    NT31("Windows NT", 3.1, true, Family.WINDOWS),

    // Linux Distributions (No generic "Linux" needed)
    AMZN("Amazon Linux", 0.0, true, Family.LINUX),
    ALPINE("Alpine Linux", 0.0, true, Family.LINUX),
    CENTOS("CentOS", 0.0, true, Family.LINUX),
    DEBIAN("Debian", 0.0, true, Family.LINUX),
    FEDORA("Fedora", 0.0, true, Family.LINUX),
    OVM("Oracle VM", 0.0, true, Family.LINUX),
    RHEL("Red Hat", 0.0, true, Family.LINUX),
    SUSE("SuSE", 0.0, true, Family.LINUX),
    SLES("Sles", 0.0, true, Family.LINUX),
    UBN("Ubuntu", 0.0, true, Family.LINUX),

    // Unix Variants
    AIX("AIX", 0.0, true, Family.UNIX),
    FREEBSD("FreeBSD", 0.0, true, Family.UNIX),
    HPUX("HP-UX", 0.0, true, Family.UNIX),
    SOLARIS("Solaris", 0.0, true, Family.UNIX),
    UNKNOWN("Unknown", 0.0, false, Family.OTHER);

    public enum Family
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
    private final Family family;

    Platform(String name, double ver, boolean type, Family family)
    {
        this.realName = name;
        this.version = ver;
        this.isServer = type;
        this.family = family;
    }

    /**
     * Returns the name of the Operating System mapped to by this named constant.
     *
     * @return real name of the Operating System
     */
    public String getRealName()
    {
        return realName;
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
        return isServer;
    }

    public Family getFamily()
    {
        return family;
    }

    /**
     * Universal version lookup. Works for any OS that defines a version.
     */
    public static Platform fromOSversion2(double osVer, boolean isServer)
    {
        if (osVer > 0)
        {
            for (Platform plat : Platform.values())
            {
                // Match both the numerical version and the role (Server vs Workstation)
                // This allows us to distinguish between Win 10 (10.0, false)
                // and Win Server 2016 (10.0, true).
                if (Double.compare(plat.getVersion(), osVer) == 0 && plat.isServer() == isServer)
                {
                    return plat;
                }
            }
        }

        return UNKNOWN;
    }

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

                case "red hat":
                case "rhel":
                    searchName = "rhel";
                break;
            }

            for (Platform os : values())
            {
                // Check Enum Name (UBN) OR Real Name (Ubuntu)
                if (os.name().equalsIgnoreCase(searchName) || os.getRealName().equalsIgnoreCase(searchName))
                {
                    return os;
                }
            }
        }

        return UNKNOWN;
    }
}