package Framework.Client;

/**
 * Monitors user inactivity and triggers disconnection when timeout is exceeded.
 * 
 * Runs as a daemon thread that periodically checks elapsed time since the last user activity.
 * Sets a timeout flag when the inactivity threshold is reached, allowing the client to
 * gracefully disconnect.
 * 
 * @author Isaac Terés Espallargas
 */
public class InactivityWatcher extends Thread {
    
    private final long timeoutMillis;
    // Using volatile to ensure visibility across threads(Client and this)
    private volatile long lastActivityTime;
    private volatile boolean running = true;
    private volatile boolean timedOut = false;

    /**
     * Constructs an InactivityWatcher with a specified timeout duration.
     * 
     * @param timeoutMillis the inactivity timeout in milliseconds
     */
    public InactivityWatcher(long timeoutMillis) {
        this.setDaemon(true);
        this.timeoutMillis = timeoutMillis;
        this.lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Resets the inactivity timer to the current time.
     * 
     * Call this method whenever the user performs an action to prevent timeout.
     */
    public void resetTimer() {
        lastActivityTime = System.currentTimeMillis();
    }

    /**
     * Stops the inactivity watcher thread.
     * 
     * Signals the thread to stop running and interrupts it if necessary.
     */
    public void stopWatcher() {
        running = false;
        this.interrupt();
    }

    /**
     * Checks if the inactivity timeout has been exceeded.
     * 
     * @return true if timeout has occurred, false otherwise
     */
    public boolean hasTimedOut() {
        return timedOut;
    }

    /**
     * Executes the inactivity monitoring loop.
     * 
     * Periodically checks elapsed time since last activity and sets the timeout flag
     * when the threshold is exceeded. Reduces CPU usage by checking every 5 seconds.
     */
    public void run() {
        while (running) {
            try {
                // To avoid too many checks, reducing CPU usage
                Thread.sleep(5 * 1000);
                
                long inactiveTime = System.currentTimeMillis() - lastActivityTime;
                
                if (inactiveTime >= timeoutMillis) {
                    timedOut = true;
                    running = false;
                }
            } catch (InterruptedException e) {
                // The thread was interrupted due to the stop request
                break;
            }
        }
    }
}