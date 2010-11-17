package org.apache.cassandra.casserole.ui;

import javax.swing.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class OffSwingWorker {
    private static ExecutorService service = new ThreadPoolExecutor(5, 30, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>());
    
    public static Future submit(final Runnable offEdt, final Runnable onEdt) {
        Callable c = new Callable() {
            public Object call() throws Exception
            {
                offEdt.run(); // what about exceptions?
                if (onEdt != null)
                    SwingUtilities.invokeLater(onEdt);
                return null;
            }
        };
        return service.submit(c);
    }
}
