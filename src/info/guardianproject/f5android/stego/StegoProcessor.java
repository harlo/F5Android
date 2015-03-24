package info.guardianproject.f5android.stego;

import java.util.ArrayList;

import info.guardianproject.f5android.Constants.Logger;
import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

public class StegoProcessor {
	private StegoProcessorService sps;
	private ServiceConnection con;
	private Activity ctx;
	
	private static final String LOG = Logger.PROCESS;
	
	public StegoProcessor() {}
	
	public StegoProcessor(Activity ctx) {
		this(ctx, null);
	}
		
	public StegoProcessor(Activity ctx, final StegoProcessThread with_thread) {
		this.ctx = ctx;
		
		con = new ServiceConnection() {
			
			@Override
			public void onServiceConnected(ComponentName name, IBinder service) {
				Log.d(LOG, "COMP NAME: " + name);
				sps = ((StegoProcessorService.LocalBinder) service).getService();
				
				if(with_thread != null) {
					sps.addThread(with_thread);
				}
				
				Log.d(LOG, "OK STARTING STEGO SERVICE IN BKG");
			}

			@Override
			public void onServiceDisconnected(ComponentName name) {
				sps = null;
			}
			
		};
		
		this.ctx.bindService(new Intent(ctx, StegoProcessorService.class), con, Context.BIND_AUTO_CREATE);
	}
	
	public void addThread(StegoProcessThread thread) {
		Log.d(LOG, "ADDING A PROCESS...");
		sps.addThread(thread);
	}

	public void destroy() {
		sps.stopSelf();
		ctx.unbindService(con);
	}
}
