package live;

import java.util.concurrent.Executor;

/**
 * Executor that calls a callback when a thread is finished
 * @author Han
 */
public class CallbackExecutor implements Executor {

    public void execute(final Runnable r) {
        final Thread runner = new Thread(r);
        runner.start();
        if ( r instanceof Callback ) {
            // create a thread to perform the callback
            Thread callerbacker = new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        // block until the running thread is done
                        runner.join();
                        ((Callback)r).callback();
                    }
                    catch ( InterruptedException e ) {
                        // someone doesn't want us running. ok, maybe we give up.
                    }
                }
            });
            callerbacker.start();
        }
    }

}