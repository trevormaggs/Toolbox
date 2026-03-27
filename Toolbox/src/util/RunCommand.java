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
     */
    private RunCommand(String command)
    {
        this.tokens = new ArrayList<>(tokenize(command));
    }

    /**
     * Creates a new instance from a single command string.
     * Handles spaces and quotes via internal tokenisation.
     */
    public static RunCommand newInstance(String command)
    {
        if (command == null || command.trim().isEmpty())
        {
            throw new IllegalArgumentException("Command cannot be null or blank");
        }
        
        return new RunCommand(command);
    }

    /**
     * Creates a new instance using a base command and additional tokens.
     * 
     * @param command
     *        The base command string (tokenised internally)
     * @param args
     *        Additional arguments added as literal tokens
     * @return a new RunCommand instance
     */
    public static RunCommand newInstance(String command, String...args)
    {
        RunCommand rc = new RunCommand(command);
        
        for (String part : args)
        {
            rc.addArgument(part);
        }
        
        return rc;
    }

    /**
     * Shorthand to execute a command string and return captured output lines immediately.
     */
    public static String[] exec(String command) throws IOException
    {
        RunCommand rc = RunCommand.newInstance(command);
        
        rc.execute();
        return rc.getStdout();
    }
    
    /**
     * Adds a single argument to the command. Use this to safely add arguments that contain spaces
     * or special characters.
     * 
     * @param arg
     *        the argument string
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
            
            return process.waitFor();
        }
        
        catch (InterruptedException exc)
        {
            Thread.currentThread().interrupt();
            throw new IOException("Interrupted while executing [" + getCommandAsString() + "]", exc);
        }
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
     * Returns the full command as a space-delimited string for logging.
     * 
     * @return the command string
     */
    public String getCommandAsString()
    {
        return String.join(" ", tokens);
    }

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