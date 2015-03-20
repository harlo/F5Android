package info.guardianproject.f5android;

import java.nio.ByteBuffer;

import android.app.Activity;
import android.util.Log;

public class F5Buffers {
	public final static String LOG = "************** PK JNI WRAPPER **************";
	private Activity a;
	private ByteBuffer f5, coeffs, buffer, decode_buffer, permutation;
	
	private native ByteBuffer initImage(int[] dimensions, int[] compWidth, int[] compHeight);
	private native ByteBuffer initCoeffs(int size);
	private native ByteBuffer initHuffmanBuffer(int size);
	private native ByteBuffer initHuffmanDecodeBuffer(int size);
	private native ByteBuffer initPermutation(int size);
	
	private native void setPixelValues(ByteBuffer f5_pointer, int[] values, int v_len, int start);
	private native int getPixelValue(ByteBuffer f5_pointer, int pos);
	
	private native void setYValues(ByteBuffer f5_pointer, float values, int x, int y);
	private native float getYValue(ByteBuffer f5_pointer, int x, int y);
	
	private native void setCr1Values(ByteBuffer f5_pointer, float values, int x, int y);
	private native float getCr1Value(ByteBuffer f5_pointer, int x, int y);
	
	private native void setCb1Values(ByteBuffer f5_pointer, float values, int x, int y);
	private native float getCb1Value(ByteBuffer f5_pointer, int x, int y);
	
	private native void setCr2Values(ByteBuffer f5_pointer, float values, int x, int y);
	private native float getCr2Value(ByteBuffer f5_pointer, int x, int y);
	
	private native void setCb2Values(ByteBuffer f5_pointer, float values, int x, int y);
	private native float getCb2Value(ByteBuffer f5_pointer, int x, int y);
	
	private native void setCoeffValues(ByteBuffer coeffs_pointer, int[] values, int v_len, int start);
	private native int getCoeffValue(ByteBuffer coeffs_pointer, int pos);
	
	private native void setHuffmanBufferValues(ByteBuffer hb_pointer, int[] values, int v_len, int start);
	private native int getHuffmanBufferValue(ByteBuffer hb_pointer, int pos);
	
	private native void setHuffmanDecodeBufferValues(ByteBuffer hdb_pointer, ByteBuffer hb_pointer, int v_len, int start);
	private native int getHuffmanDecodeBufferValue(ByteBuffer hdb_pointer, int pos);
	
	private native void setPermutationValues(ByteBuffer p_pointer, int[] values, int v_len, int start);
	private native int getPermutationValue(ByteBuffer p_pointer, int pos);
	
	
	private native void cleanUpImage(ByteBuffer f5_pointer);
	private native void cleanUpCoeffs(ByteBuffer coeffs_pointer);
	private native void cleanUpHuffmanBuffer(ByteBuffer hb_pointer);
	private native void cleanUpHuffmanDecodeBuffer(ByteBuffer hdb_pointer);
	private native void cleanUpPermutation(ByteBuffer p_pointer);
	
	static {
		System.loadLibrary("F5Buffers");
	}
	
	public interface F5Notification {
		public void onUpdate();
		public void onUpdate(int steps, int interval);
		public void onFailure();
	}
	
	public F5Buffers(Activity a) {
		this.a = a;
	}
	
	public void initF5Image(int[] dimensions, int[] compWidth, int[] compHeight) {
		Log.d(LOG, "initImage");
		f5 = initImage(dimensions, compWidth, compHeight);
		update();
	}
	
	public void initF5Coeffs(int size) {
		Log.d(LOG, "initCoeffs");
		coeffs = initCoeffs(size);
		update();
	}
	
	public void initF5HuffmanBuffer(int size) {
		Log.d(LOG, "initHuffmanBuffer");
		buffer = initHuffmanBuffer(size);
		update();
	}
	
	public void initF5Permutation(int size) {
		Log.d(LOG, "initPermutation");
		permutation = initPermutation(size);
		update();
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
		((F5Notification) a).onUpdate();
		return getHuffmanBufferValue(buffer, pos);
	}
	
	public void setHuffmanDecodeBuffer(int v_len, int start) {
		decode_buffer = this.initHuffmanDecodeBuffer(v_len);
		this.setHuffmanDecodeBufferValues(decode_buffer, buffer, v_len, start);
		update();
	}
	
	public int getHuffmanDecodeBufferValue(int pos) {
		return getHuffmanDecodeBufferValue(decode_buffer, pos);
	}
	
	public void setPermutationValues(int[] values, int start) {
		this.setPermutationValues(permutation, values, values.length, start);
	}
	
	public int getPermutationValues(int pos) {
		return getPermutationValue(permutation, pos);
	}
	
	public void abort() {
		Log.d(LOG, "ABORTING F5 Process");
		cleanUpImage();
		cleanUpCoeffs();
		cleanUpHuffmanBuffer();
		cleanUpHuffmanDecodeBuffer();
		cleanUpPermutation();
		
		((F5Notification) a).onFailure();
	}
	
	public void cleanUpImage() {
		Log.d(LOG, "cleanup image");
		cleanUpImage(f5);
		update();
	}
	
	public void cleanUpCoeffs() {
		Log.d(LOG, "cleanup coeffs");
		cleanUpCoeffs(coeffs);
		update();
	}
	
	public void cleanUpHuffmanBuffer() {
		Log.d(LOG, "cleanup huffman buffer");
		cleanUpHuffmanBuffer(buffer);
		update();
	}
	
	public void cleanUpHuffmanDecodeBuffer() {
		Log.d(LOG, "cleanup decode buffer");
		cleanUpHuffmanDecodeBuffer(decode_buffer);
		update();
	}
	
	public void cleanUpPermutation() {
		Log.d(LOG, "cleanup permutation buffer");
		cleanUpPermutation(permutation);
		update();
	}
	
	public void update() {
		((F5Notification) a).onUpdate();
	}
}
