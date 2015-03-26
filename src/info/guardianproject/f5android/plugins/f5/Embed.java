package info.guardianproject.f5android.plugins.f5;

import info.guardianproject.f5android.plugins.PluginNotificationListener;
import info.guardianproject.f5android.plugins.f5.james.Jpeg;
import info.guardianproject.f5android.plugins.f5.james.JpegEncoder;
import info.guardianproject.f5android.stego.StegoProcessThread;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class Embed extends StegoProcessThread {
	Activity a;
	Bitmap image = null;
	FileOutputStream dataOut = null;
	File file, outFile, root_dir;
	JpegEncoder jpg;
	int i, Quality = 90;
	// Check to see if the input file name has one of the extensions:
	// .tif, .gif, .jpg
	// If not, print the standard use info.
	boolean haveInputImage = false;

	String inFileName = null;
	String outFileName = null;
	public String secret_message = null;
	String dump_dir = null;

	private byte[] f5_seed;

	public interface EmbedListener {
		public void onEmbedded(File outFile);
	}

	public Embed(Activity a, String inFileName, String secret_message, byte[] f5_seed) {
		this(a, "F5Android", inFileName, null, secret_message, f5_seed);
	}

	public Embed(Activity a, String dump_dir, String inFileName, String secret_message, byte[] f5_seed) {
		this(a, dump_dir, inFileName, null, secret_message, f5_seed);    	
	}

	public Embed(Activity a, String dump_dir, String inFileName, String outFileName, String secret_message, byte[] f5_seed) {
		super(Jpeg.LOG);

		this.a = a;
		this.inFileName = inFileName;
		this.file = new File(this.inFileName);
		this.secret_message = secret_message;
		this.f5_seed = f5_seed;
		this.dump_dir = dump_dir;
	}

	public void run(){
		super.run();
		
		if(isInterrupted()) { return; }
		
		root_dir = new File(Environment.getExternalStorageDirectory(), dump_dir);
		if(!root_dir.exists())
			root_dir.mkdir();

		if(outFileName == null) {
			String extension = this.file.getName().substring(this.file.getName().lastIndexOf(".") - 1);
			this.outFile = new File(root_dir, this.file.getName().replace(extension, "_embed.jpg"));
			this.outFileName = this.outFile.getAbsolutePath();
		} else {
			this.outFile = new File(outFileName);
		}

		i = 1;
		while(outFile.exists()) {
			this.outFile = new File(root_dir, outFile.getName().substring(0, outFile.getName().lastIndexOf(".")) + "_" + i++ + ".jpg");
			if(i > 100)
				return;
		}
		
		if(isInterrupted()) { return; }
		try {
			embed();
		} catch(Exception e) {
			Log.e(Jpeg.LOG, e.toString());
			e.printStackTrace();

			((PluginNotificationListener) a).onFailure();
		}
	}

	private void embed() {
		if(this.file.exists()) {
			try {
				dataOut = new FileOutputStream(outFile);
			} catch(final IOException e) {}
			
			image = BitmapFactory.decodeFile(this.inFileName);
			jpg = new JpegEncoder(a, image, Quality, dataOut, f5_seed, this);
			
			if(isInterrupted()) { return; }
			try {
				if(jpg.Compress(new ByteArrayInputStream(secret_message.getBytes()))) {
					((EmbedListener) a).onEmbedded(outFile);
				}
			} catch(final Exception e) {
				Log.e(Jpeg.LOG, e.toString());
				e.printStackTrace();
			}

			try {
				dataOut.close();
			} catch(final IOException e) {
				Log.e(Jpeg.LOG, e.toString());
				e.printStackTrace();
			}
		}


	}
}
