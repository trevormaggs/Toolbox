package progressbar;

/**
 * A stateful ProgressListener that renders a progress bar to the console. Mimics JProgressBar behaviour with configurable min and max values.
 * 
 * @author Trevor Maggs
 * @version 0.3
 * @since 15 April 2026
 */
public final class ConsoleBarAdvanced implements ProgressListener
{
    private static final int BAR_WIDTH = 50;

    private final int min;
    private final int max;
    private int lastPercent = -1;

    /**
     * Constructs a ConsoleBar with a default range of 0 to 100.
     */
    public ConsoleBarAdvanced()
    {
        this(0, 100);
    }

    /**
     * Constructs a ConsoleBar with a specific range.
     * * @param min the starting value
     * 
     * @param max
     *        the ending value
     */
    public ConsoleBarAdvanced(int min, int max)
    {
        this.min = min;
        this.max = max;
    }

    @Override
    public void onProgressUpdate(int current, int total)
    {
        // We use the constructor's 'max' if 'total' isn't provided dynamically
        int effectiveTotal = (total > 0) ? total : this.max;

        if (effectiveTotal <= min) return;

        // Calculate percentage based on the range (current - min) / (max - min)
        int percent = (int) (((double) (current - min) / (effectiveTotal - min)) * 100);

        // Efficiency: Only render if percentage changed
        if (percent == lastPercent && current < effectiveTotal)
        {
            return;
        }
        
        lastPercent = percent;

        render(current, effectiveTotal, percent);

        if (current >= effectiveTotal)
        {
            System.out.println();
            lastPercent = -1;
        }
    }

    private void render(int current, int total, int percent)
    {
        int filledWidth = (percent * BAR_WIDTH) / 100;
        StringBuilder sb = new StringBuilder(BAR_WIDTH + 10);
        sb.append("\r[");

        for (int i = 0; i < BAR_WIDTH; i++)
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

        sb.append("] %3d%%");
        
        System.out.printf(sb.toString(), percent);
    }
}