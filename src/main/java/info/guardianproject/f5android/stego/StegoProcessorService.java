package info.guardianproject.f5android.stego;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;

public class StegoProcessorService  extends Service {
	private final IBinder stego_process = new LocalBinder();

	public StegoProcessorService() {
		super();
	}

	public class LocalBinder extends Binder {
		StegoProcessorService getService() {
			return StegoProcessorService.this;
		}
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int start_id) {
		return START_NOT_STICKY;
	}

	@Override
	public boolean onUnbind(Intent intent) {
		return super.onUnbind(intent);
	}

	@Override
	public IBinder onBind(Intent intent) {
		return stego_process;
	}
}