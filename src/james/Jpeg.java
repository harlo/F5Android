// Copyright (C) 1998, James R. Weeks and BioElectroMech.
// Visit BioElectroMech at www.obrador.com. Email James@obrador.com.

// This software is based in part on the work of the Independent JPEG Group.
// See license.txt for details about the allowed used of this software.
// See IJGreadme.txt for details about the Independent JPEG Group's license.

package james; // westfeld

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class Jpeg {
	Activity a;
	File root_dir = new File(Environment.getExternalStorageDirectory(), "F5Android");
	File inFile, outFile;
	FileOutputStream dataOut = null;
	JpegEncoder jpg;
	Bitmap image;
	BitmapFactory.Options opts;
	int Quality;
	
	public static final int DEFAULT_QUALITY = 80;
	public static final String LOG = "***************** JPEG-STEGO ******************";
	
	public Jpeg(Activity a, String in_file_name, String out_file_name) {
		this(a, in_file_name, DEFAULT_QUALITY, null);
	}
	
	public Jpeg(Activity a, String in_file_name) {
		this(a, in_file_name, DEFAULT_QUALITY, null);
	}
	
	public Jpeg(Activity a, String in_file_name, int quality) {
		this(a, in_file_name, quality, null);
	}
	
    public Jpeg(Activity a, String in_file_name, int quality, String out_file_name) {
    	if(!root_dir.exists())
    		root_dir.mkdir();
    	
    	this.a = a;
    	String string = new String();
    	inFile = new File(in_file_name);
    	
    	if(in_file_name.endsWith(".jpg") && !in_file_name.endsWith(".tif") && !in_file_name.endsWith(".gif")) {
    		StandardUsage();
    	}
    	
    	if(out_file_name == null) {
    		string = inFile.getName().substring(0, inFile.getName().lastIndexOf(".")) + ".jpg";
    	} else {
    		string = out_file_name;
    		if(string.endsWith(".tif") || string.endsWith(".gif")) {
    			string = string.substring(0, string.lastIndexOf("."));
    		}
    		
    		if(!string.endsWith(".jpg")) {
    			string = string.concat(".jpg");
    		}
    	}
    	
    	outFile = new File(root_dir, string);
    	
    	
		if(inFile.exists()) {
			try {
				dataOut = new FileOutputStream(outFile);
			} catch(final IOException e) {
				Log.e(LOG, e.toString());
				e.printStackTrace();
			}
			Quality = quality;
			
			opts = new BitmapFactory.Options();
			opts.inDither = false;
			opts.inPurgeable = true;
			opts.inInputShareable = true;
			opts.inTempStorage = new byte[32 * 1024];
			
			//image = BitmapFactory.decodeFile(in_file_name, opts);
			image = BitmapFactory.decodeFile(in_file_name);
			jpg = new JpegEncoder(image, Quality, dataOut, "");
			
		} else {
			// TODO: could not find the in file-- throw error
			Log.e(LOG, "could not find the inFile? (" + inFile.getAbsolutePath() + ")");
			return;
		}
    }
    
    public void compress(InputStream embedFile) {
    	jpg.Compress(embedFile);
		try {
			dataOut.close();
		} catch(final IOException e) {}
		
    }
    
    public void setComment(String comment) {
    	jpg.setComment(comment);
    }
    
    public static void StandardUsage() {
    	// throw helpful error here.
    }
}
