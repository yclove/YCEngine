package com.ycengine.yclibrary.util.thread;

public class StopRunnable implements Runnable {
	public boolean mStop = false;
	
	public void stopRun(){
		mStop = true;
	}

	@Override
	public void run() {}
}
