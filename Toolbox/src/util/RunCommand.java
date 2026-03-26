package util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

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
 * <b>Note:</b> On Windows, use "cmd.exe /c" to run built-in shell commands.
 * </p>
 *
 * @author Trevor Maggs
 * @version 0.4
 * @since 11 July 2025
 */
public final class RunCommand
{
    /** The individual tokens of the command to be executed. */
    private final List<String> commandParts;

    /** The captured lines from the process output stream. */
    private List<String> stdoutResults;

    /**
     * Handles parsing of raw command strings into discrete tokens internally.
     */
    public static class CommandTokenizer
    {
        /**
         * Tokenises a command string, preserving quoted strings and escaped characters.
         *
         * @param commandLine
         *        the full command string to tokenise
         * @return a list of parsed tokens
         */
        private static List<String> tokenize(String commandLine)
        {
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
    }

    /**
     * Internal constructor to initialise the command parts list.
     */
    private RunCommand()
    {
        this.commandParts = new ArrayList<>();
    }

    /**
     * Internal constructor that initialises the command with a raw string.
     * 
     * @param commandLine
     *        the initial command string to parse
     */
    private RunCommand(String commandLine)
    {
        this();
        addCommand(commandLine);
    }

    /**
     * Internal constructor that initialises the command and appends an initial argument.
     * 
     * @param command
     *        the base command to parse
     * @param arg
     *        the first argument to append
     */
    private RunCommand(String command, String arg)
    {
        this();
        addCommand(command);
        addArgument(arg);
    }

    /**
     * Creates a new instance from a command string.
     * 
     * @param command
     *        the base command (may include arguments)
     * @return a new RunCommand instance
     */
    public static RunCommand newInstance(String command)
    {
        return new RunCommand(command);
    }

    /**
     * Creates a new instance with a command and one initial argument.
     * 
     * @param command
     *        the base command
     * @param arg
     *        the first argument
     * @return a new RunCommand instance
     */
    public static RunCommand newInstance(String command, String arg)
    {
        return new RunCommand(command, arg);
    }

    /**
     * Executes a command string and returns the captured output lines.
     * 
     * @param command
     *        the full command string
     * @return array of output lines (stdout and stderr combined)
     * 
     * @throws IOException
     *         if execution fails
     */
    public static String[] runAndCapture(String command) throws IOException
    {
        RunCommand rc = RunCommand.newInstance(command);
        rc.execute();
        return rc.getStdout();
    }

    /**
     * Adds a single argument to the command. Use this to safely add arguments
     * that contain spaces or special characters.
     * 
     * @param arg
     *        the argument string
     * @return this instance for method chaining
     */
    public RunCommand addArgument(String arg)
    {
        if (arg != null && !arg.trim().isEmpty())
        {
            commandParts.add(arg.trim());
        }
        return this;
    }

    /**
     * Adds a single integer as an argument.
     * 
     * @param arg
     *        the argument value
     * @return this instance for method chaining
     */
    public RunCommand addArgument(int arg)
    {
        return addArgument(String.valueOf(arg));
    }

    /**
     * Executes the command and captures output. Standard error is merged into stdout.
     *
     * @return the exit code of the process
     * 
     * @throws IOException
     *         if execution fails or is interrupted
     */
    public int execute() throws IOException
    {
        if (commandParts.isEmpty())
        {
            throw new IllegalStateException("No command specified.");
        }

        stdoutResults = new ArrayList<>();

        ProcessBuilder pb = new ProcessBuilder(commandParts);

        pb.redirectErrorStream(true);

        Process process = pb.start();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream())))
        {
            String line;

            while ((line = reader.readLine()) != null)
            {
                stdoutResults.add(line);
            }

            return process.waitFor();
        }

        catch (InterruptedException exc)
        {
            Thread.currentThread().interrupt();

            throw new IOException("Interrupted while executing [" + getCommandAsString() + "]", exc);
        }
    }

    /**
     * Returns the full command as a space-delimited string for logging.
     * 
     * @return the command string
     */
    public String getCommandAsString()
    {
        return String.join(" ", commandParts);
    }

    /**
     * Returns the output captured during execution.
     * 
     * @return array of combined stdout/stderr lines, or empty array if not yet executed.
     */
    public String[] getStdout()
    {
        return (stdoutResults != null ? stdoutResults.toArray(new String[0]) : new String[0]);
    }

    /**
     * Gets a specific line from the captured output.
     * 
     * @param index
     *        the line index
     * @return the line at the specified index
     * 
     * @throws IndexOutOfBoundsException
     *         if index is invalid
     * @throws IllegalStateException
     *         if the process has not been executed yet
     */
    public String getStdoutLine(int index)
    {
        if (stdoutResults == null)
        {
            throw new IllegalStateException("Process has not been executed yet.");
        }

        return stdoutResults.get(index);
    }

    /**
     * Parses the initial command string into tokens and adds them to the parts list.
     * 
     * @param command
     *        the raw command string to parse
     * 
     * @throws IllegalArgumentException
     *         if command is null or blank
     */
    private void addCommand(String command)
    {
        if (command == null || command.trim().isEmpty())
        {
            throw new IllegalArgumentException("Command cannot be null or blank");
        }

        this.commandParts.addAll(CommandTokenizer.tokenize(command));
    }

    /**
     * Demonstration entry point for the RunCommand utility.
     * 
     * @param args
     *        command line arguments (not used)
     * @throws IOException
     *         if command execution fails
     */
    public static void main(String[] args) throws IOException
    {
        String command = "cmd.exe /c echo 'Hello world' \"and \\\"quotes\\\"\" unquoted\\ arg";
        String[] result = runAndCapture(command);

        for (String line : result)
        {
            System.out.println("[" + line + "]");
        }
    }
}