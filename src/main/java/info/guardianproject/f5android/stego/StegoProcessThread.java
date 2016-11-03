package info.guardianproject.f5android.stego;

import info.guardianproject.f5android.Constants.Logger;

public class StegoProcessThread extends Thread implements ThreadMonitorListener {
	public static String LOG;
	
	private static final String default_log_name = Logger.THREAD;
	
	public StegoProcessThread() {
		this(default_log_name);
	}
	
	public StegoProcessThread(String log_name) {
		LOG = log_name;
	}
	
	
	@Override
	public void requestInterrupt() {
		interrupt();
	}
	
	@Override
	public boolean isInterrupted() {
		// XXX: Might want to do something here?
		return super.isInterrupted();
	}
}
