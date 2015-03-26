package info.guardianproject.f5android.stego;

import java.util.ArrayList;

import info.guardianproject.f5android.Constants.Logger;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

public class StegoProcessor {
	private StegoProcessorService sps;
	private ServiceConnection con;
	private Activity ctx;

	private ArrayList<StegoProcessThread> threads = new ArrayList<StegoProcessThread>();
	public int current_thread = -1;

	private static final String LOG = Logger.PROCESS;

	public StegoProcessor() {}

	public StegoProcessor(Activity ctx) {
		this(ctx, null);
	}

	public StegoProcessor(Activity ctx, final StegoProcessThread with_thread) {
		this.ctx = ctx;
		threads = new ArrayList<StegoProcessThread>();

		con = new ServiceConnection() {

			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				sps = ((StegoProcessorService.LocalBinder) service).getService();

				if(with_thread != null) {
					addThread(with_thread, true);
				}
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				sps = null;
			}

		};

		this.ctx.bindService(new Intent(ctx, StegoProcessorService.class), con, Context.BIND_AUTO_CREATE);
	}

	public void addThread(StegoProcessThread thread) {
		addThread(thread, false);
	}

	public void addThread(StegoProcessThread thread, boolean auto_start) {
		threads.add(thread);

		if(auto_start) {
			routeNext();
		}
	}

	public void routeNext() {
		try {
			if(threads.get(current_thread).isInterrupted()) {
				return;
			}
		} catch(ArrayIndexOutOfBoundsException e) {}

		StegoProcessThread t = threads.get(++current_thread);
		Log.d(LOG, "ROUTING NEXT: " + t.getId());

		try {
			t.start();
		} catch(Exception e) {
			Log.d(LOG, e.toString());
		}
	}

	public void cleanUp() {
		threads.clear();

		sps.stopSelf();
		ctx.unbindService(con);
	}

	public void destroy() {
		Log.d(LOG, "USER HAS INVOKED DESTROY ON STEGO PROCESSOR");

		for(StegoProcessThread thread : threads) {
			thread.requestInterrupt();
		}

		cleanUp();
		((StegoProcessorListener) ctx).onProcessorQueueAborted();
	}
}
