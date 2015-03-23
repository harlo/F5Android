package info.guardianproject.stego;

import android.util.Log;

public class StegoProcessThread extends Thread implements ThreadMonitorListener {
	private Thread thread_monitor;
	private static String LOG;
	
	private static final String default_log_name = "***************** info.guardianproject.stego (Process Thread) **************";
	
	public StegoProcessThread() {
		this(default_log_name);
	}
	
	public StegoProcessThread(String log_name) {
		LOG = log_name;
	}
	
	@Override
	public void run() {
		thread_monitor = Thread.currentThread();
		Log.d(LOG, "THREAD ID: " + thread_monitor.getId());
	}
	
	@Override
	public void requestInterrupt() {
		thread_monitor.interrupt();
		Log.d(LOG, "REQUESTING INTERRUPT!");
	}

}
