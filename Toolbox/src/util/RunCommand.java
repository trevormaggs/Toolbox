package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Utility for executing external system commands and capturing combined output lines. Supports
 * platform-independent command invocation (Unix/Linux/Windows).
 * 
 * <p>
 * Standard Error (stderr) is redirected to Standard Output (stdout) to prevent process deadlocks
 * caused by unbuffered error streams.
 * </p>
 *
 * <p>
 * <b>Note:</b> On Windows, use {@code cmd.exe /c} to run built-in shell commands.
 * </p>
 *
 * @author Trevor Maggs
 * @version 0.5
 * @since 2025-07-11
 */
public final class RunCommand
{
    private final List<String> tokens;
    private List<String> stdoutResults;

    /**
     * Internal constructor that initialises the specified raw string, representing a command.
     * 
     * @param command
     *        the full command string to process
     */
    private RunCommand(String command)
    {
        this.tokens = new ArrayList<>(tokenize(command));
    }

    /**
     * Tokenises a command string into a list of arguments, preserving quoted strings and respecting
     * backslash-escaped characters.
     * 
     * <p>
     * Note: This method does not perform shell-specific expansions such as environment variable
     * substitution, for example: {@code $PATH} or {@code globbing *.txt}.
     * </p>
     *
     * @param commandLine
     *        the full command string to tokenise
     * @return a list of parsed tokens; an empty list if {@code commandLine} is null or empty
     */
    private static List<String> tokenize(String commandLine)
    {
        if (commandLine == null || commandLine.trim().isEmpty())
        {
            return new ArrayList<>();
        }

        boolean escape = false;
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
        List<String> tokens = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        for (int i = 0; i < commandLine.length(); i++)
        {
            char c = commandLine.charAt(i);

            if (escape)
            {
                current.append(c);
                escape = false;
            }

            else if (c == '\\')
            {
                if (inSingleQuotes)
                {
                    current.append(c);
                }

                else
                {
                    escape = true;
                }
            }

            else if (c == '"' && !inSingleQuotes)
            {
                inDoubleQuotes = !inDoubleQuotes;
            }

            else if (c == '\'' && !inDoubleQuotes)
            {
                inSingleQuotes = !inSingleQuotes;
            }

            else if (Character.isWhitespace(c) && !inSingleQuotes && !inDoubleQuotes && current.length() > 0)
            {
                tokens.add(current.toString());
                current.setLength(0);
            }

            else
            {
                current.append(c);
            }
        }

        if (current.length() > 0)
        {
            tokens.add(current.toString());
        }

        return tokens;
    }

    /**
     * Appends a single literal argument to the command. The argument is trimmed and added as a
     * discrete token, ensuring spaces or special characters within the argument do not cause
     * word-splitting.
     * 
     * @param arg
     *        the argument string to add
     * @return this instance for method chaining
     */
    private RunCommand addArgument(String arg)
    {
        if (arg != null && !arg.trim().isEmpty())
        {
            tokens.add(arg.trim());
        }

        return this;
    }

    /**
     * Executes the command and captures its output. Standard Error (stderr) is merged into Standard
     * Output (stdout) to prevent stream deadlocks.
     * 
     * <p>
     * If the process hangs, it will be forcibly terminated after 30 seconds.
     * </p>
     *
     * @return the exit code of the process (typically 0 for success)
     * 
     * @throws IOException
     *         if the execution fails, times out, or is interrupted
     */
    private int execute() throws IOException
    {
        if (tokens.isEmpty())
        {
            throw new IllegalStateException("No command specified.");
        }

        ProcessBuilder pb = new ProcessBuilder(tokens);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        stdoutResults = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
        {
            String line;

            while ((line = reader.readLine()) != null)
            {
                stdoutResults.add(line);
            }

            boolean finished = process.waitFor(30, TimeUnit.SECONDS);

            if (!finished)
            {
                // Kill the zombie process if necessary
                process.destroyForcibly();

                throw new IOException("Command timed out [" + toString() + "]");
            }

            return process.exitValue();
        }
        catch (InterruptedException exc)
        {
            Thread.currentThread().interrupt();

            throw new IOException("Interrupted while executing [" + toString() + "]", exc);
        }
    }

    /**
     * Returns the output captured during execution, combining both Standard Output and Standard
     * Error.
     * 
     * @return an array of output lines; returns an empty array if the command has not yet been
     *         executed or produced no output
     */
    public String[] getStdout()
    {
        return (stdoutResults != null ? stdoutResults.toArray(new String[0]) : new String[0]);
    }

    /**
     * Executes a command string and returns the captured output lines.
     * 
     * @param command
     *        the command line to execute
     * @return an array of combined stdout/stderr lines
     * 
     * @throws IOException
     *         if an I/O error occurs, the process times out, or is interrupted
     */
    public static String[] exec(String command) throws IOException
    {
        return exec(command, new String[0]);
    }

    /**
     * Executes a command string with additional literal arguments and returns the captured output
     * lines.
     * 
     * <p>
     * The {@code args} are added as discrete tokens, which is safer for dynamic input containing
     * spaces or special characters.
     * </p>
     * 
     * @param command
     *        the base command string (tokenised internally)
     * @param args
     *        additional literal arguments to append
     * @return an array of combined stdout/stderr lines
     * 
     * @throws IOException
     *         if an I/O error occurs, the process times out, or is interrupted
     */
    public static String[] exec(String command, String...args) throws IOException
    {
        RunCommand rc = new RunCommand(command);

        for (String part : args)
        {
            rc.addArgument(part);
        }

        int exitCode = rc.execute();

        if (exitCode != 0)
        {
            System.err.println("Command failed with exit code: " + exitCode);
        }

        return rc.getStdout();
    }

    /**
     * Returns a string representation of the reconstructed command line,
     * with tokens joined by spaces.
     * 
     * @return the full command string as it would be presented to the system
     */
    @Override
    public String toString()
    {
        return String.join(" ", tokens);
    }

    public static void main(String[] args) throws IOException
    {
        String command = "cmd.exe /c echo 'Hello world' \"and \\\"quotes\\\"\" unquoted\\ arg";
        String[] result = exec(command);

        for (String line : result)
        {
            System.out.println("[" + line + "]");
        }
    }
}