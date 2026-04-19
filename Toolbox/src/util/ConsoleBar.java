package util;

/**
 * A lightweight console utility for rendering a real-time progress bar.
 * 
 * <p>
 * This utility uses the carriage return ({@code \r}) character to update a single line in the
 * console, providing a dynamic visual representation of task completion without flooding the
 * standard output with new lines.
 * </p>
 * 
 * @author Trevor Maggs
 * @version 0.2
 * @since 19 February 2020
 */
public final class ConsoleBar
{
    private static int lastPercent = -1;

    /**
     * Private constructor to prevent instantiation.
     * 
     * @throws UnsupportedOperationException
     *         always
     */
    private ConsoleBar()
    {
        throw new UnsupportedOperationException("Not intended for instantiation");
    }

    /**
     * Updates the console progress bar based on the provided progress ratio.
     * 
     * <p>
     * The bar is fixed at 50 characters wide. When {@code current} reaches {@code total}, a line
     * separator is automatically appended to finalise the output.
     * </p>
     * 
     * @param current
     *        the current completion count
     * @param total
     *        the target total count
     */
    public static void updateProgressBar(int current, int total)
    {
        if (total < 1)
        {
            return;
        }

        int percent = (int) ((double) current / total * 100);

        if (percent == lastPercent && current < total)
        {
            return;
        }

        lastPercent = percent;

        int width = 50;
        int filledWidth = (percent * width) / 100;

        StringBuilder sb = new StringBuilder(width + 20);

        sb.append("\r[");

        for (int i = 0; i < width; i++)
        {
            if (i < filledWidth)
            {
                sb.append("=");
            }

            else if (i == filledWidth && current < total)
            {
                sb.append(">");
            }

            else
            {
                sb.append(" ");
            }
        }

        System.out.printf("] %3d%%", percent);

        if (current >= total)
        {
            lastPercent = -1;
            System.out.println();
        }
    }
}