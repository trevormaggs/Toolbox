package util;

public enum Platform
{
    MACOS("macOS", 0.0, false),

    // Windows workstations
    WIN11("Windows 11", 10.0, false),
    WIN10("Windows 10", 10.0, false),
    WIN81("Windows 8.1", 6.3, false),
    WIN8("Windows 8", 6.2, false),
    WIN7("Windows 7", 6.1, false),
    WINVISTA("Windows Vista", 6.0, false),
    WINXP64("Windows XP", 5.2, false), /* Windows XP Professional x64 Edition */
    WINXP("Windows XP", 5.1, false),
    WINME("Windows Me", 4.90, false),
    WIN98("Windows 98", 4.10, false),
    WIN95("Windows 95", 4.0, false),

    // Windows servers
    WIN2022("Windows Server 2022", 10.0, true),
    WIN2019("Windows Server 2019", 10.0, true),
    WIN2016("Windows Server 2016", 10.0, true),
    WIN2012R2("Windows Server 2012 R2", 6.3, true),
    WIN2012("Windows Server 2012", 6.2, true),
    WIN2008R2("Windows Server 2008 R2", 6.1, true),
    WIN2008("Windows Server 2008", 6.0, true),
    WIN2003("Windows 2003", 5.2, true),
    WIN2000("Windows 2000", 5.0, true),
    WINNT4("Windows NT", 4.0, true),
    NT351("Windows NT", 3.51, true),
    NT35("Windows NT", 3.5, true),
    NT31("Windows NT", 3.1, true),

    // Linux
    LINUX("Linux", 0.0, true),
    AMZN("Amazon Linux", 0.0, true),
    ALPINE("Alpine Linux", 0.0, true),
    CENTOS("CentOS", 0.0, true),
    DEBIAN("Debian", 0.0, true),
    FEDORA("Fedora", 0.0, true),
    OVM("Oracle VM", 0.0, true),
    RHEL("Red Hat", 0.0, true),
    SUSE("SuSE", 0.0, true),
    SLES("Sles", 0.0, true),
    UBN("Ubuntu", 0.0, true),

    // Unix
    AIX("AIX", 0.0, true),
    FREEBSD("FreeBSD", 0.0, true),
    HPUX("HP-UX", 0.0, true),
    SOLARIS("Solaris", 0.0, true),
    SUNOS("Sun OS", 0.0, true),

    UNKNOWN("Unknown", 0.0, false);
    /*
     * See https://www.techthoughts.info/windows-version-numbers
     * Note, Windows Server 2003 R2 also exists. Use native code to check.
     * Refer to MSDN for coding details.
     */

    private String realName;
    private double version;
    private boolean deviceType;

    Platform(String name, double ver, boolean type)
    {
        realName = name;
        version = ver;
        deviceType = type;
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
        return deviceType;
    }

    /**
     * Searches for a matching Operating System enumeration value based on the specified full real
     * name string, such as {@code Windows Server 2008 R2}.
     *
     * @param osName
     *        the Operating System in its full real name format such as "Windows Server 2016"
     *        and "Fedora"
     * @return an constant value if a match is found, otherwise {@code UNKNOWN}
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

    public static Platform fromOSversion(double osVer, boolean isServer)
    {
        if (osVer > 0)
        {
            for (Platform plat : Platform.values())
            {
                if (osVer == plat.getVersion() && isServer == plat.isServer())
                {
                    return plat;
                }
            }
        }

        return UNKNOWN;
    }
}