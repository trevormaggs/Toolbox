package progressbar;

/**
 * Interface for receiving progress updates from long-running tasks.
 */
public interface ProgressListener
{
    void onProgressUpdate(int current, int total);
}