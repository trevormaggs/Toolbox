package progressbar;

import javax.swing.SwingWorker;
import java.util.List;

/**
 * Optimized for Java 8. Uses publish/process to ensure thread safety
 * and UI responsiveness.
 */
public class ProgressWorker extends SwingWorker<Void, ProgressWorker.ProgressUpdate>
{

    private final List<ProgressListener> listeners;

    /**
     * Simple container for progress data (Replaces Java 14+ Records)
     */
    public static class ProgressUpdate
    {
        public final int current;
        public final int total;

        public ProgressUpdate(int current, int total)
        {
            this.current = current;
            this.total = total;
        }
    }

    public ProgressWorker(List<ProgressListener> listeners)
    {
        this.listeners = listeners;
    }

    @Override
    protected Void doInBackground() throws Exception
    {
        int total = 100;

        for (int i = 1; i <= total; i++)
        {
            // Simulate your file processing or heavy lifting
            Thread.sleep(50);

            // Pass the data to the UI thread
            publish(new ProgressUpdate(i, total));
        }
        return null;
    }

    @Override
    protected void process(List<ProgressUpdate> chunks)
    {
        // This logic is now automatically thread-safe on the EDT.
        // Chunks contains all updates since the last 'process' call.
        // We only care about the most recent update.
        if (chunks == null || chunks.isEmpty()) return;

        ProgressUpdate latest = chunks.get(chunks.size() - 1);

        for (ProgressListener listener : listeners)
        {
            listener.onProgressUpdate(latest.current, latest.total);
        }
    }

    @Override
    protected void done()
    {
        System.out.println("Task completed successfully!");
    }
}