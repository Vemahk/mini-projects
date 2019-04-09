package me.vem.art.thread;

import java.util.LinkedList;
import java.util.List;

public class IterThread extends Thread{

	private static IterThread instance;
	public static IterThread getInstance() {
		if(instance == null)
			instance = new IterThread(1);
		return instance;
	}
	
	public static IterThread getInstance(int i) {
		if(instance == null)
			instance = new IterThread(i);
		return instance;
	}
	
	
	
	private List<BidThread> bidders;
	
	private IterThread(int bidThreads) {
		super("Fractle Iteration Thread");
		bidders = new LinkedList<>();
		for(int i=0;i<bidThreads;i++)
			spawnThread();
	}
	
	private void spawnThread() {
		bidders.add(new BidThread());
	}
	
	private class BidThread extends Thread{
		
	}
}
