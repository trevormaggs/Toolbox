package progressbar;

import javax.swing.JProgressBar;

public class SwingProgressAdapter implements ProgressListener
{
    private final JProgressBar progressBar;

    public SwingProgressAdapter(JProgressBar progressBar)
    {
        this.progressBar = progressBar;
    }

    @Override
    public void onProgressUpdate(int current, int total)
    {
        // Already running on EDT thanks to ProgressWorker's process() method
        progressBar.setMaximum(total);
        progressBar.setValue(current);
    }
}