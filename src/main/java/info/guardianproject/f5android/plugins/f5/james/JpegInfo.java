// Version 1.0a
// Copyright (C) 1998, James R. Weeks and BioElectroMech.
// Visit BioElectroMech at www.obrador.com. Email James@obrador.com.

// See license.txt for details about the allowed used of this software.
// This software is based in part on the work of the Independent JPEG Group.
// See IJGreadme.txt for details about the Independent JPEG Group's license.

// This encoder is inspired by the Java Jpeg encoder by Florian Raemy,
// studwww.eurecom.fr/~raemy.
// It borrows a great deal of code and structure from the Independent
// Jpeg Group's Jpeg 6a library, Copyright Thomas G. Lane.
// See license.txt for details.

package info.guardianproject.f5android.plugins.f5.james;

import info.guardianproject.f5android.R;
import info.guardianproject.f5android.plugins.f5.F5Buffers;
import info.guardianproject.f5android.stego.StegoProcessThread;
import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * JpegInfo - Given an image, sets default information about it and divides it
 * into its constituant components, downsizing those that need to be.
 */
class JpegInfo {
    String notification_message;

    public Bitmap imageobj;

    public int imageHeight;

    public int imageWidth;

    public int BlockWidth[];

    public int BlockHeight[];

    // the following are set as the default
    public int Precision = 8;

    public int NumberOfComponents = 3;

    public Object Components[];

    public int[] CompID = {
            1, 2, 3 };

    // public int[] HsampFactor = {1, 1, 1};
    // public int[] VsampFactor = {1, 1, 1};
    public int[] HsampFactor = {
            2, 1, 1 };

    public int[] VsampFactor = {
            2, 1, 1 };

    public int[] QtableNumber = {
            0, 1, 1 };

    public int[] DCtableNumber = {
            0, 1, 1 };

    public int[] ACtableNumber = {
            0, 1, 1 };

    public boolean[] lastColumnIsDummy = {
            false, false, false };

    public boolean[] lastRowIsDummy = {
            false, false, false };

    public int Ss = 0;

    public int Se = 63;

    public int Ah = 0;

    public int Al = 0;

    public int compWidth[], compHeight[];

    public int MaxHsampFactor;

    public int MaxVsampFactor;
    
    public F5Buffers f5;
    private StegoProcessThread thread_monitor;

    public JpegInfo(Activity a, final Bitmap image, StegoProcessThread thread_monitor) {
    	f5 = new F5Buffers(a);
    	
        this.Components = new Object[this.NumberOfComponents];
        this.compWidth = new int[this.NumberOfComponents];
        this.compHeight = new int[this.NumberOfComponents];
        this.BlockWidth = new int[this.NumberOfComponents];
        this.BlockHeight = new int[this.NumberOfComponents];
        this.imageobj = image;
        this.imageWidth = image.getWidth();
        this.imageHeight = image.getHeight();
        this.thread_monitor = thread_monitor;
        
        // Comment =
        // "JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech.  ";
        this.notification_message = a.getString(R.string.downsampling_components);
        getYCCArray();
    }
    
    void downsampleCb1(int comp) {
    	Log.d(Jpeg.LOG, "downsampling cb1: ");
    	if(this.thread_monitor.isInterrupted()) { return; }
    	f5.onUpdate(notification_message);
    	
    	int inrow, incol;
        int outrow, outcol;
        float temp;
        int bias;
        inrow = 0;
        incol = 0;
        
        for(outrow=0; outrow<this.compHeight[comp]; outrow++) {
        	bias = 1;
        	for(outcol=0; outcol<this.compWidth[comp]; outcol++) {
        		temp = this.f5.getCb1Value(inrow, incol++);
        		temp += this.f5.getCb1Value(inrow++, incol--);
        		temp += this.f5.getCb1Value(inrow, incol++);
        		temp += this.f5.getCb1Value(inrow--, incol++) + bias;
        		
        		this.f5.setCb2Values(temp/(float) 4.0, outrow, outcol);
        		bias ^= 3;
        	}
        	inrow += 2;
        	incol = 0;
        }
    }
    
    void downsampleCr1(int comp) {
    	if(thread_monitor.isInterrupted()) { return; }
    	
    	Log.d(Jpeg.LOG, "downsampling cr1: ");
    	f5.onUpdate(notification_message);
    	
    	int inrow, incol;
        int outrow, outcol;
        float temp;
        int bias;
        inrow = 0;
        incol = 0;
        
        for(outrow=0; outrow<this.compHeight[comp]; outrow++) {
        	bias = 1;
        	for(outcol=0; outcol<this.compWidth[comp]; outcol++) {
        		temp = this.f5.getCr1Value(inrow, incol++);
        		temp += this.f5.getCr1Value(inrow++, incol--);
        		temp += this.f5.getCr1Value(inrow, incol++);
        		temp += this.f5.getCr1Value(inrow--, incol++) + bias;
        		
        		this.f5.setCr2Values(temp/(float) 4.0, outrow, outcol);
        		bias ^= 3;
        	}
        	inrow += 2;
        	incol = 0;
        }
    }

    float[][] DownSample(final float[][] C, final int comp) {
        int inrow, incol;
        int outrow, outcol;
        float output[][];
        float temp;
        int bias;
        inrow = 0;
        incol = 0;
        output = new float[this.compHeight[comp]][this.compWidth[comp]];
        for (outrow = 0; outrow < this.compHeight[comp]; outrow++) {
            bias = 1;
            for (outcol = 0; outcol < this.compWidth[comp]; outcol++) {
                // System.out.println("outcol="+outcol);
                temp = C[inrow][incol++]; // 00
                temp += C[inrow++][incol--]; // 01
                temp += C[inrow][incol++]; // 10
                temp += C[inrow--][incol++] + bias; // 11 -> 02
                output[outrow][outcol] = temp / (float) 4.0;
                bias ^= 3;
            }
            inrow += 2;
            incol = 0;
        }
        return output;
    }

    /*
     * This method creates and fills three arrays, Y, Cb, and Cr using the input
     * image.
     */

    private void getYCCArray() {
        int r, g, b, y, x;
        
        this.MaxHsampFactor = 1;
        this.MaxVsampFactor = 1;
        for (y = 0; y < this.NumberOfComponents; y++) {
            this.MaxHsampFactor = Math.max(this.MaxHsampFactor, this.HsampFactor[y]);
            this.MaxVsampFactor = Math.max(this.MaxVsampFactor, this.VsampFactor[y]);
        }
        for (y = 0; y < this.NumberOfComponents; y++) {
            this.compWidth[y] = (this.imageWidth % 8 != 0 ? (int) Math.ceil(this.imageWidth / 8.0) * 8
                    : this.imageWidth) / this.MaxHsampFactor * this.HsampFactor[y];
            if (this.compWidth[y] != this.imageWidth / this.MaxHsampFactor * this.HsampFactor[y]) {
                this.lastColumnIsDummy[y] = true;
            }
            // results in a multiple of 8 for compWidth
            // this will make the rest of the program fail for the unlikely
            // event that someone tries to compress an 16 x 16 pixel image
            // which would of course be worse than pointless
            this.BlockWidth[y] = (int) Math.ceil(this.compWidth[y] / 8.0);
            this.compHeight[y] = (this.imageHeight % 8 != 0 ? (int) Math.ceil(this.imageHeight / 8.0) * 8
                    : this.imageHeight) / this.MaxVsampFactor * this.VsampFactor[y];
            if (this.compHeight[y] != this.imageHeight / this.MaxVsampFactor * this.VsampFactor[y]) {
                this.lastRowIsDummy[y] = true;
            }
            this.BlockHeight[y] = (int) Math.ceil(this.compHeight[y] / 8.0);
        }
        
        // XXX: throws outofmemory errors! move to JNI?
        if(this.thread_monitor.isInterrupted()) { return; }
        this.f5.initF5Image(new int[] {this.imageWidth, this.imageHeight}, this.compWidth, this.compHeight);
        //final int values[] = new int[this.imageWidth * this.imageHeight];
        
        int current = 0;
        for(int i=0; i<this.imageHeight; i++) {
        	int[] values = new int[this.imageWidth];
        	imageobj.getPixels(values, 0, this.imageWidth, 0, i, this.imageWidth, 1);
        	
        	this.f5.setPixelValues(values, current);
        	current += this.imageWidth;
        }
        
        imageobj.recycle();
        
        System.gc();
        try {
        	Thread.sleep(100);
        } catch(Exception e) {
        	Log.e(Jpeg.LOG, e.toString());
        	e.printStackTrace();
        }
        
        //final float Y[][] = new float[this.compHeight[0]][this.compWidth[0]];
        int index = 0;
        for (y = 0; y < this.imageHeight; ++y) {
            for (x = 0; x < this.imageWidth; ++x) {
            	int pixel_value = this.f5.getPixelValue(index);
            	
                r = pixel_value >> 16 & 0xff;
                g = pixel_value >> 8 & 0xff;
                b = pixel_value & 0xff;

                // The following three lines are a more correct color conversion
                // but
                // the current conversion technique is sufficient and results in
                // a higher
                // compression rate.
                // Y[y][x] = 16 + (float)(0.8588*(0.299 * (float)r + 0.587 *
                // (float)g + 0.114 * (float)b ));
                // Cb1[y][x] = 128 + (float)(0.8784*(-0.16874 * (float)r -
                // 0.33126 * (float)g + 0.5 * (float)b));
                // Cr1[y][x] = 128 + (float)(0.8784*(0.5 * (float)r - 0.41869 *
                // (float)g - 0.08131 * (float)b));
                this.f5.setYValues((float) (0.299 * r + 0.587 * g + 0.114 * b), y, x);
                this.f5.setCb1Values(128 + (float) (-0.16874 * r - 0.33126 * g + 0.5 * b), y, x);
                this.f5.setCr1Values(128 + (float) (0.5 * r - 0.41869 * g - 0.08131 * b), y, x);
                index++;
            }
        }

        // Need a way to set the H and V sample factors before allowing
        // downsampling.
        // For now (04/04/98) downsampling must be hard coded.
        // Until a better downsampler is implemented, this will not be done.
        // Downsampling is currently supported. The downsampling method here
        // is a simple box filter.

        downsampleCb1(1);
        downsampleCr1(2);
    }
}