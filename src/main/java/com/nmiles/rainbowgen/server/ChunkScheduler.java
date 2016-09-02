package com.nmiles.rainbowgen.server;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;

/**
 * This is an asynchronous, thread-safe scheduler for a WebSocket server. The
 * purpose of this class is to fairly send messages to WebSocket clients. This
 * is necessary to stop a single WebSocket from queueing up too many messages
 * and blocking other WebSockets from sending messages until the first
 * WebSocket's messages have all been sent. To use it, register WebSocket
 * connections with register() and use the return value, a
 * ConcurrentLinkedQueue, to enqueue messages to be sent.
 * 
 * The implementing program MUST call remove() for each WebSocket that it has
 * registered. The WebSocket's onClose() method is a perfect place to do this.
 * In the interest of performance, no mechanisms are in place for checking
 * WebSockets registered with this class to make sure they are still active. Not
 * calling remove() WILL result in memory leaking and performance degradation.
 * 
 * Messages for the same WebSocket are guaranteed to be sent in the proper
 * order, but messages for different WebSockets have no such guarantee. For
 * instance, if WebSocket 1 enqueues two messages, then WebSocket 2 enqueues two
 * messages, the order of messages sent may be: both WebSocket 1 messages, then
 * both WebSocket 2 messages; both WebSocket 2 messages, then both WebSocket 1
 * messages; WebSocket 1's first message, both of WebSocket 2's messages, then
 * WebSocket 1's second message, or any other variation that preserves
 * individual WebSockets' message order.
 * 
 * @author Nathan Miles
 *
 */
public class ChunkScheduler implements CompletionHandler<DataFrame>, Runnable {
    /** The singleton instance of this class */
    private static ChunkScheduler instance = null;

    /**
     * The list of all registered WebSockets and their associated message queues
     */
    private List<SocketQueue> socketList;

    /**
     * The lock for the socketList. It allows any number of concurrent reads,
     * but only one thread may read at once, and it may only write when no other
     * thread is reading.
     */
    private ReentrantReadWriteLock listLock;

    /**
     * A single-thread executor service that is used for actually sending
     * messages.
     */
    private ExecutorService executor;

    /**
     * Initializes the instance of the ChunkScheduler
     */
    private ChunkScheduler() {
        socketList = new LinkedList<>();
        listLock = new ReentrantReadWriteLock(true);
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Steps through the socketList and sends one message from each queue to its
     * WebSocket
     */
    private synchronized void sendBatch() {
        boolean sentOne = false;
        try {
            // loop until we've sent at least one message
            while (!sentOne) {
                listLock.readLock().lock();

                if (socketList.isEmpty()) {
                    return;
                }
                ListIterator<SocketQueue> iter = socketList.listIterator();
                SocketQueue current;
                String message;
                // send the first message
                while (iter.hasNext()) {
                    current = iter.next();
                    message = current.messages.poll();
                    if (message != null) {
                        /*
                         * Add a completion handler to the first message sent.
                         * This class extends CompletionHandler, so we may
                         * notify this class on completion.
                         */
                        current.socket.send(message).addCompletionHandler(this);
                        sentOne = true;
                        break;
                    }
                }
                // We don't care about these messages so they don't get handlers
                while (iter.hasNext()) {
                    current = iter.next();
                    message = current.messages.poll();
                    if (message != null) {
                        current.socket.send(message);
                    }
                }

                listLock.readLock().unlock();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            listLock.readLock().unlock();
        }
    }

    /**
     * Invoked when this class is run by the ExecutorService. It starts a batch
     * of messages.
     */
    @Override
    public void run() {
        sendBatch();
    }

    /**
     * Invoked when the first message of each batch is finished sending. When
     * invoked, this method submits this class to the ExecutorService's queue to
     * send another batch.
     */
    @Override
    public void completed(DataFrame arg0) {
        executor.submit(this);
    }

    @Override
    public void cancelled() {
        // do nothing
    }

    @Override
    public void failed(Throwable arg0) {
        // do nothing
    }

    @Override
    public void updated(DataFrame arg0) {
        // do nothing
    }

    /**
     * Gets the singleton instance of the class
     * 
     * @return The instance
     */
    public static ChunkScheduler getInstance() {
        if (instance == null) {
            instance = new ChunkScheduler();
        }
        return instance;
    }

    /**
     * Registers a WebSocket with the ChunkScheduler. After it is registered, a
     * ConcurrentLinkedQueue is returned that is used by the calling method to
     * enqueue messages. To enqueue messages, simply use the queue as normal,
     * calling add() to add messages to the tail of the queue.
     * 
     * @param socket
     *            The WebSocket to register
     * @return The ConcurrentLinkedQueue that the calling method must use to
     *         submit messages
     */
    public ConcurrentLinkedQueue<String> register(WebSocket socket) {
        boolean shouldStartRunner = false;
        ConcurrentLinkedQueue<String> retQueue = null;
        try {
            listLock.writeLock().lock();
            if (socketList.isEmpty()) {
                shouldStartRunner = true;
            }
            SocketQueue newQueue = new SocketQueue(socket);
            retQueue = newQueue.messages;
            socketList.add(newQueue);
            System.out.println("Registered sockets: " + socketList.size());
        } catch (Exception e) {
            System.out.println("Exception while registering WebSocket: " + e.getMessage());
            e.printStackTrace();
        } finally {
            listLock.writeLock().unlock();
        }
        if (shouldStartRunner) {
            executor.submit(this);
        }
        return retQueue;
    }

    /**
     * Removes a WebSocket from the ChunkScheduler. This method MUST be called
     * for every WebSocket connection that has been closed, as this class does
     * no checks on registered WebSockets to ensure they are still connected.
     * 
     * @param socket
     *            The WebSocket to remove
     */
    public void remove(WebSocket socket) {
        try {
            listLock.writeLock().lock();
            ListIterator<SocketQueue> i = socketList.listIterator();
            if (!i.hasNext()) {
                return;
            }
            SocketQueue current;
            while (i.hasNext()) {
                current = i.next();
                if (current.socket == socket) {
                    i.remove();
                }
            }
            System.out.println("Registered sockets: " + socketList.size());
        } catch (Exception e) {
            System.out.println("Exception while removing WebSocket: " + e.getMessage());
            e.printStackTrace();
        } finally {
            listLock.writeLock().unlock();
        }
    }

    /**
     * A wrapper for a WebSocket and its associated ConcurrentLinkedQueue for
     * holding messages.
     * 
     * @author Nathan Miles
     *
     */
    private class SocketQueue {
        /** The WebSocket */
        private WebSocket socket;

        /** The queue */
        private ConcurrentLinkedQueue<String> messages;

        /**
         * Constructs a new SocketQueue for the given WebSocket
         * 
         * @param socket
         *            The WebSocket
         */
        private SocketQueue(WebSocket socket) {
            this.socket = socket;
            messages = new ConcurrentLinkedQueue<>();
        }
    }
}
