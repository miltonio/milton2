package io.milton.simpleton;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author bradm (zfc1502)
 */
public class Stage<V extends Runnable> implements Runnable, Closeable{

    private static final Logger log = LoggerFactory.getLogger(Stage.class);
    final String name;
    final LinkedBlockingQueue<V> queue;
//    final BlockingQueue<V> admissionControl;
    final List<Thread> threads;
    final int capacity;
    final int maxThreads;
    final boolean blockOnAdd;

    int threadCounter;

    public Stage(String name, int capacity, int maxThreads, boolean blockOnAdd) {
        this.name = name;
        this.capacity = capacity;
        this.blockOnAdd = blockOnAdd;
        this.maxThreads = maxThreads;
        queue = new LinkedBlockingQueue<V>(capacity);
//        admissionControl = new LinkedBlockingQueue<V>(capacity);
        threads = new ArrayList<Thread>();
        for( int i=0; i<maxThreads; i++) {
            addThread();
        }
    }

    protected void addThread() {        
        Thread t = new Thread(this,"Stage-" + name + "-" + threadCounter++);
        threads.add( t );
        log.debug(name + " added thread: " + threads.size());
        t.start();
    }

    public String getName() {
        return name;
    }

    public void enqueue(V v) {
        log.debug("queue size: " + queue.size() + " capacity: " + capacity);
        if( queue.size() > capacity/2 && threads.size()<maxThreads ) {
            addThread();
        }
        try {
            if( blockOnAdd ) {
                queue.put(v);
            } else {
                queue.add(v);
            }
//            admissionControl.put(v);
        } catch (InterruptedException ex) {
            log.warn("interrupted", ex);
            return ;
        }
        //log.debug(name + "------------------ enqueue. queue=" + queue.size() + " threads=" + threads.size());
    }

//    private void dequeue(V v) {
//        log.debug("admit size1: " + admissionControl.size());
//        boolean b = admissionControl.remove(v);
//        if( !b ) throw new RuntimeException("wasmt in queue: " + this.name + " - " + v.getClass());
//        log.debug("admit size2: " + admissionControl.size());
//        log.debug(name + "------------------ dequeue. queue=" + queue.size() + " admit=" + admissionControl.size() +  " threads=" + threads.size());
//    }

    @Override
    public void run() {
        try {
            boolean done = false;
            while (!done) {
                V v = queue.take();
                try {
                    v.run();
                } catch(Exception e) {
                    log.error( "exception processing: " + v.getClass(), e);
                } finally {
//                    dequeue(v);
                }
            }
            threads.remove(Thread.currentThread());
            log.debug(name + " thread stopped: " + threads.size());
        } catch (InterruptedException ex) {
            log.warn("interrupted", ex);
        } catch(Exception e) {
            log.error("exception has killed stage", e);
        }
    }

    public void close() throws IOException {
        for( Thread t : threads ) {
            t.interrupt();
        }
    }
}
