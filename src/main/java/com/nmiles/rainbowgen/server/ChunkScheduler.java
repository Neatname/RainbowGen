package com.nmiles.rainbowgen.server;

import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.glassfish.grizzly.CompletionHandler;
import org.glassfish.grizzly.websockets.DataFrame;
import org.glassfish.grizzly.websockets.WebSocket;

public class ChunkScheduler {
	
	private static ChunkScheduler instance = null;
	
	private List<SocketQueue> queue;
	
	private ReentrantReadWriteLock queueLock;
	
	private ExecutorService executor;
	
	private BatchRunner runner;
	
	private BatchStarter starter;
	
	private ChunkScheduler (){
		queue = new LinkedList<>();
		queueLock = new ReentrantReadWriteLock(true);
		runner = new BatchRunner();
		executor = Executors.newSingleThreadExecutor();
		starter = new BatchStarter();
	}
	
	public class BatchRunner implements Runnable{
		
		private BatchRunner(){
			// this class is stateless
		}
		
		private synchronized void sendBatch(){
			boolean sentOne = false;
			try{
				while (!sentOne){
					queueLock.readLock().lock();
					
					if (queue.isEmpty()){
						return;
					}
					ListIterator<SocketQueue> iter = queue.listIterator();
					SocketQueue current;
					String message;
					while (iter.hasNext()) {
						current = iter.next();
						message = current.messages.poll();
						if (message != null){
							current.socket.send(message).addCompletionHandler(starter);
							sentOne = true;
							break;
						}
					}
					while (iter.hasNext()) {
						current = iter.next();
						message = current.messages.poll();
						if (message != null){
							current.socket.send(message);
						}
					}
					
					queueLock.readLock().unlock();
				}
			} catch (Exception e){
				e.printStackTrace();
			} finally {
				queueLock.readLock().unlock();
			}
		}

		@Override
		public void run() {
			sendBatch();
		}
	}
	
	public class BatchStarter implements CompletionHandler<DataFrame> {
		
		@Override
		public void completed(DataFrame arg0) {
			executor.submit(runner);
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
	}
	
	public static ChunkScheduler getInstance(){
		if (instance == null){
			instance = new ChunkScheduler();
		}
		return instance;
	}
	
	public ConcurrentLinkedQueue <String> register(WebSocket socket){
		boolean shouldStartRunner = false;
		ConcurrentLinkedQueue <String> retQueue = null;
		try {
			queueLock.writeLock().lock();
			if (queue.isEmpty()){
				shouldStartRunner = true;
			}
			SocketQueue newQueue = new SocketQueue(socket);
			retQueue = newQueue.messages;
			queue.add(newQueue);
			System.out.println("Registered sockets: " + queue.size());
		} catch (Exception e){
			System.out.println("Exception while registering WebSocket: " + e.getMessage());
		} finally {
			queueLock.writeLock().unlock();
		}
		if (shouldStartRunner){
			executor.submit(runner);
		}
		return retQueue;
	}
	
	public void remove(WebSocket socket){
		try {
			queueLock.writeLock().lock();
			ListIterator<SocketQueue> i = queue.listIterator();
			if (!i.hasNext()){
				return;
			}
			SocketQueue current;
			while (i.hasNext()){
				current = i.next();
				if (current.socket == socket){
					i.remove();
				}
			}
			System.out.println("Registered sockets: " + queue.size());
		} catch (Exception e){
			System.out.println("Exception while removing WebSocket: " + e.getMessage());
		} finally {
			queueLock.writeLock().unlock();
		}
	}
	
	private class SocketQueue {
		private WebSocket socket;
		
		private ConcurrentLinkedQueue <String> messages;
		
		private SocketQueue(WebSocket socket){
			this.socket = socket;
			messages = new ConcurrentLinkedQueue<>();
		}
	}
}
