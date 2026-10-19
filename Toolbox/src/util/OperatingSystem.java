package util;

import java.util.Objects;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Defines supported operating systems and maps each enumeration constant to its display name,
 * version number, and server/workstation classification.
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
    LNX("Linux", 0.0, true),
    AIX("AIX", 0.0, true),
    CENTOS("CentOS", 0.0, true),
    DEBIAN("Debian", 0.0, true),
    FEDORA("Fedora", 0.0, true),
    FREEBSD("FreeBSD", 0.0, true),
    HPUX("HP-UX", 0.0, true),
    OVM("Oracle VM", 0.0, true),
    RHEL("Red Hat", 0.0, true),
    SOLARIS("Solaris", 0.0, true),
    SUNOS("SunOS", 0.0, true),
    SUSE("SuSE", 0.0, true),
    SLES("SLES", 0.0, true),
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
    WIN2003("Windows Server 2003", 5.2, true),
    WINXP64("Windows XP", 5.2, false), /* Windows XP Professional x64 Edition */
    WINXP("Windows XP", 5.1, false),
    WIN2000("Windows 2000", 5.0, true),
    WINME("Windows Me", 4.90, false),
    WIN98("Windows 98", 4.10, false),
    WINNT4("Windows NT", 4.0, true),
    WIN95("Windows 95", 4.0, false),
    NT351("Windows NT", 3.51, true),
    NT35("Windows NT", 3.5, true),
    NT31("Windows NT", 3.1, true);
    /*
     * Note, Windows Server 2003 R2 also exists. Use native code to check.
     * Refer to MSDN for coding details.
     */

    private final String realName;
    private final double version;
    private final boolean server;

    OperatingSystem(String name, double ver, boolean type)
    {
        realName = name;
        version = ver;
        server = type;
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
     * @return the Operating System version. Note, if the Operating System is not Windows, a value
     *         of zero will be returned. To get the current OS version for other OS types, use the
     *         {@code SystemInfo} static class
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
        return server;
    }

    /**
     * Returns the operating system enumeration value with the specified name, ignoring case. The
     * name is the enum constant name, such as {@code WIN2012R2} or {@code WIN10}.
     *
     * @param osName
     *        the abbreviated operating system name
     * @return the matching operating system, or {@code null} if no match is found
     * 
     * @throws NullPointerException
     *         if {@code osName} is {@code null}
     */
    public static OperatingSystem getName(String osName)
    {
        Objects.requireNonNull(osName, "Input operating system name cannot be null");

        for (OperatingSystem os : OperatingSystem.values())
        {
            if (os.name().equalsIgnoreCase(osName))
            {
                return os;
            }
        }

        return null;
    }

    /**
     * Returns the first operating system enumeration value whose full real name contains a match
     * for the specified regular expression, ignoring case.
     *
     * @param osName
     *        the regular expression to match against the full real names of the operating systems
     * @return the first matching operating system, or {@code null} if no match is found
     *
     * @throws NullPointerException
     *         if {@code osName} is {@code null}
     * @throws IllegalArgumentException
     *         if the regular expression has a syntax error
     */
    public static OperatingSystem getNameExtended(String osName)
    {
        Objects.requireNonNull(osName, "Input operating system name cannot be null");

        final Pattern pattern;

        try
        {
            pattern = Pattern.compile(osName, Pattern.CASE_INSENSITIVE);
        }

        catch (PatternSyntaxException exc)
        {
            throw new IllegalArgumentException("Pattern syntax error in [" + osName + "] regular expression", exc);
        }

        for (OperatingSystem os : OperatingSystem.values())
        {
            if (pattern.matcher(os.getRealName()).find())
            {
                return os;
            }
        }

        return null;
    }
}