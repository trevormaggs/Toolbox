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
 * @since 2 April 2026
 */
public final class RunCommand
{
    private final List<String> tokens;
    private List<String> stdoutResults;
    private int exitCode;

    /**
     * Internal constructor that initialises the specified raw string, representing a command.
     * 
     * @param command
     *        the full command string to process
     */
    public RunCommand(String command)
    {
        this.stdoutResults = new ArrayList<>();
        this.tokens = tokenize(command);
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
    public RunCommand addArgument(String arg)
    {
        if (arg != null && !arg.trim().isEmpty())
        {
            tokens.add(arg.trim());
        }

        return this;
    }

    public int getExitCode()
    {
        return exitCode;
    }

    /**
     * Returns the output captured during execution.
     * 
     * @return an array of strings representing each line of output. Returns an empty array if the
     *         command has not been executed yet or if it produced no output
     */
    public String[] getStdout()
    {
        return stdoutResults.toArray(new String[0]);
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
    public int execute() throws IOException
    {
        if (tokens.isEmpty())
        {
            throw new IllegalStateException("No command specified");
        }

        ProcessBuilder pb = new ProcessBuilder(tokens);
        pb.redirectErrorStream(true);
        Process process = pb.start();

        stdoutResults = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
        {
            while (process.isAlive() || reader.ready())
            {
                if (reader.ready())
                {
                    String line = reader.readLine();

                    if (line != null)
                    {
                        stdoutResults.add(line);
                    }
                }

                else
                {
                    if (!process.waitFor(10, TimeUnit.SECONDS))
                    {
                        // Kill the zombie process if necessary
                        process.destroyForcibly();

                        throw new IOException("Command timed out after 10 seconds");
                    }

                    // Break after one last check for remaining buffer
                    if (!reader.ready())
                    {
                        break;
                    }
                }

                exitCode = process.exitValue();
            }
        }

        catch (InterruptedException exc)
        {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while executing [" + this + "]", exc);
        }

        return exitCode;
    }

    /**
     * Static factory method that runs the command and returns the instance.
     * 
     * @param command
     *        the base command string (tokenised internally)
     * @return an array of combined stdout/stderr lines
     * 
     * @throws IOException
     *         if an I/O error occurs, the process times out, or is interrupted
     */
    public static RunCommand run(String command) throws IOException
    {
        RunCommand rc = new RunCommand(command);

        rc.execute();

        return rc;
    }

    /**
     * Static factory method that runs the command and returns the instance.
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
    public static RunCommand run(String command, String...args) throws IOException
    {
        RunCommand rc = new RunCommand(command);

        for (String part : args)
        {
            rc.addArgument(part);
        }

        rc.execute();

        return rc;
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
        List<String> tokens = new ArrayList<>();

        if (commandLine == null || commandLine.trim().isEmpty())
        {
            return tokens;
        }

        boolean escape = false;
        boolean inSingleQuotes = false;
        boolean inDoubleQuotes = false;
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

            else if (Character.isWhitespace(c) && !inSingleQuotes && !inDoubleQuotes)
            {
                if (current.length() > 0)
                {
                    tokens.add(current.toString());
                    current.setLength(0);
                }
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

    public static void main(String[] args) throws IOException
    {
        // Example usage
        // RunCommand result = RunCommand.run("cmd.exe /c echo Hello World");
        RunCommand result = RunCommand.run("cmd.exe /c echo 'Hello world' \"and \\\"quotes\\\"\" unquoted\\ arg");

        if (result.getExitCode() == 0)
        {
            System.out.println("Success! Output lines: " + result.getStdout().length);

            for (String line : result.getStdout())
            {
                System.out.println("[" + line + "]");
            }
        }

        else
        {
            System.err.println("Failed with code: " + result.getExitCode());
        }
    }
}