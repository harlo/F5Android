package info.guardianproject.f5android;

import james.Jpeg;
import james.JpegEncoder;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;

public class Embed {
	Activity a;
	Bitmap image = null;
	FileOutputStream dataOut = null;
    File file, outFile, root_dir;
    JpegEncoder jpg;
    int i, Quality = 80;
    // Check to see if the input file name has one of the extensions:
    // .tif, .gif, .jpg
    // If not, print the standard use info.
    boolean haveInputImage = false;
    String embFileName = null;
    String comment = "JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech.  ";
    String inFileName = null;
    String outFileName = null;
    
    public Embed(Activity a, String inFileName) {
    	this(a, inFileName, null, null);
    }
    
    public Embed(Activity a, String inFileName, String outFileName, String password) {
    	this.a = a;
    	this.inFileName = inFileName;
    	this.file = new File(this.inFileName);
    	
    	root_dir = new File(Environment.getExternalStorageDirectory(), "jpegStegoTest");
    	if(!root_dir.exists())
    		root_dir.mkdir();
    	
    	if(outFileName == null) {
    		this.outFile = new File(root_dir, this.file.getName().replace(".jpg", "_embed.jpg"));
    		this.outFileName = this.outFile.getAbsolutePath();
    	} else {
    		this.outFileName = outFileName;
    		this.outFile = new File(outFileName);
    	}
    	
    	this.embFileName = new File(root_dir, "test_embed.txt").getAbsolutePath();
    	
    	i = 1;
    	while(outFile.exists()) {
    		this.outFile = new File(outFileName.substring(0, this.outFileName.lastIndexOf(".")) + i++ + ".jpg");
    		if(i > 100)
    			return;
    	}
    	
    	if(this.file.exists()) {
    		try {
    			dataOut = new FileOutputStream(outFile);
    		} catch(final IOException e) {}
    		
    		image = BitmapFactory.decodeFile(this.inFileName);
    		jpg = new JpegEncoder(image, Quality, dataOut, comment);
    		
    		try {
    			jpg.Compress(new FileInputStream(embFileName), password);
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
