package info.guardianproject.f5android;

import java.nio.ByteBuffer;

import android.util.Log;

public class F5Buffers {
	public final static String LOG = "************** PK JNI WRAPPER **************";
	private ByteBuffer f5, coeffs, buffer;
	
	public native ByteBuffer initImage(int[] dimensions, int[] compWidth, int[] compHeight);
	public native ByteBuffer initCoeffs(int size);
	public native ByteBuffer initHuffmanBuffer(int size);
	
	public native void setPixelValues(ByteBuffer f5_pointer, int[] values, int v_len, int start);
	public native int getPixelValue(ByteBuffer f5_pointer, int pos);
	
	public native void setYValues(ByteBuffer f5_pointer, float values, int x, int y);
	public native float getYValue(ByteBuffer f5_pointer, int x, int y);
	
	public native void setCr1Values(ByteBuffer f5_pointer, float values, int x, int y);
	public native float getCr1Value(ByteBuffer f5_pointer, int x, int y);
	
	public native void setCb1Values(ByteBuffer f5_pointer, float values, int x, int y);
	public native float getCb1Value(ByteBuffer f5_pointer, int x, int y);
	
	public native void setCr2Values(ByteBuffer f5_pointer, float values, int x, int y);
	public native float getCr2Value(ByteBuffer f5_pointer, int x, int y);
	
	public native void setCb2Values(ByteBuffer f5_pointer, float values, int x, int y);
	public native float getCb2Value(ByteBuffer f5_pointer, int x, int y);
	
	
	public native void setCoeffValues(ByteBuffer coeffs_pointer, int[] values, int v_len, int start);
	public native int getCoeffValue(ByteBuffer coeffs_pointer, int pos);
	
	
	public native void setHuffmanBufferValues(ByteBuffer hb_pointer, int[] values, int v_len, int start);
	public native int getHuffmanBufferValue(ByteBuffer hb_pointer, int pos);
	
	public native void cleanUpImage(ByteBuffer f5_pointer);
	public native void cleanUpCoeffs(ByteBuffer coeffs_pointer);
	public native void cleanUpHuffmanBuffer(ByteBuffer hb_pointer);
	
	static {
		System.loadLibrary("F5Buffers");
	}
	
	public F5Buffers() {}
	
	public void initF5Image(int[] dimensions, int[] compWidth, int[] compHeight) {
		Log.d(LOG, "initImage");
		f5 = initImage(dimensions, compWidth, compHeight);
	}
	
	public void initF5Coeffs(int size) {
		Log.d(LOG, "initCoeffs");
		coeffs = initCoeffs(size);
	}
	
	public void initF5HuffmanBuffer(int size) {
		Log.d(LOG, "initHuffmanBuffer");
		buffer = initHuffmanBuffer(size);
	}
	
	public void setPixelValues(int[] values, int start) {
		//Log.d(LOG, "setPixelValues");
		setPixelValues(f5, values, values.length, start);
	}
	
	public int getPixelValue(int pos) {
		//Log.d(LOG, "getPixelValues");
		return getPixelValue(f5, pos);
	}
	
	public void setYValues(float values, int x, int y) {
		//Log.d(LOG, "setYValues");
		setYValues(f5, values, x, y);
	}
	
	public float getYValue(int x, int y) {
		//Log.d(LOG, "getYValues");
		return getYValue(f5, x, y);
	}
	
	public void setCr1Values(float values, int x, int y) {
		//Log.d(LOG, "setCr1Values");
		setCr1Values(f5, values, x, y);
	}
	
	public float getCr1Value(int x, int y) {
		//Log.d(LOG, "getCr1Values");
		return getCr1Value(f5, x, y);
	}
	
	public void setCr2Values(float values, int x, int y) {
		//Log.d(LOG, "setCr2Values");
		setCr2Values(f5, values, x, y);
	}
	
	public float getCr2Value(int x, int y) {
		//Log.d(LOG, "getCr2Values");
		return getCr2Value(f5, x, y);
	}
	
	public void setCb1Values(float values, int x, int y) {
		//Log.d(LOG, "setCb1Values");
		setCb1Values(f5, values, x, y);
	}
	
	public float getCb1Value(int x, int y) {
		//Log.d(LOG, "getCb1Values");
		return getCb1Value(f5, x, y);
	}
	
	public void setCb2Values(float values, int x, int y) {
		//Log.d(LOG, "setCb2Values");
		setCb2Values(f5, values, x, y);
	}
	
	public float getCb2Value(int x, int y) {
		//Log.d(LOG, "getCb2Values");
		return getCb2Value(f5, x, y);
	}
	
	public void setCoeffValues(int[] values, int start) {
		//Log.d(LOG, "setCoeffValues");
		setCoeffValues(coeffs, values, values.length, start);
	}
	
	public int getCoeffValue(int pos) {
		//Log.d(LOG, "getCoeffValues");
		return getCoeffValue(coeffs, pos);
	}
	
	public void setHuffmanBufferValues(int[] values, int start) {
		//Log.d(LOG, "setPixelValues");
		setHuffmanBufferValues(buffer, values, values.length, start);
	}
	
	public int getHuffmanBufferValue(int pos) {
		//Log.d(LOG, "getPixelValues");
		return getHuffmanBufferValue(buffer, pos);
	}
	
	public void cleanUpImage() {
		Log.d(LOG, "cleanup image");
		cleanUpImage(f5);
	}
	
	public void cleanUpCoeffs() {
		Log.d(LOG, "cleanup coeffs");
		cleanUpCoeffs(coeffs);
	}
	
	public void cleanUpHuffmanBuffer() {
		Log.d(LOG, "cleanup image");
		cleanUpHuffmanBuffer(buffer);
	}
}
