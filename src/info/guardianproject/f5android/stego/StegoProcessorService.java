package info.guardianproject.f5android.stego;

import info.guardianproject.f5android.Constants.Logger;

import java.util.ArrayList;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class StegoProcessorService  extends Service {
	private final static String LOG = Logger.PROCESS;
	private final IBinder stego_process = new LocalBinder();

	private ArrayList<StegoProcessThread> threads = new ArrayList<StegoProcessThread>();
	
	public StegoProcessorService() {
		super();
	}

	public class LocalBinder extends Binder {
		StegoProcessorService getService() {
			return StegoProcessorService.this;
		}
	}
	
	public void addThread(StegoProcessThread thread) {
		threads.add(thread);
		threads.get(threads.size() - 1).start();
	}

	@Override
	public void onCreate() {
		threads = new ArrayList<StegoProcessThread>();
		Log.d(LOG, "ON CREATE");
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int start_id) {
		Log.d(LOG, "ON START CMD " + start_id + " : " + intent);
		return START_NOT_STICKY;
	}

	@Override
	public void onDestroy() {
		for(StegoProcessThread thread : threads) {
			thread.requestInterrupt();
		}

		threads.clear();

		Log.d(LOG, "ON DESTROY!");

	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(LOG, "ON BIND!");
		return stego_process;
	}
}