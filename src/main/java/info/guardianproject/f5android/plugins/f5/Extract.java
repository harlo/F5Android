package info.guardianproject.f5android.plugins.f5;

import info.guardianproject.f5android.plugins.PluginNotificationListener;
import info.guardianproject.f5android.plugins.f5.crypt.F5Random;
import info.guardianproject.f5android.plugins.f5.crypt.Permutation;
import info.guardianproject.f5android.plugins.f5.james.Jpeg;
import info.guardianproject.f5android.plugins.f5.ortega.HuffmanDecode;
import info.guardianproject.f5android.stego.StegoProcessThread;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import android.app.Activity;
import android.util.Log;

public class Extract extends StegoProcessThread {
	private File f; // carrier file
	private Activity a;
	private byte[] f5_seed;

	private byte[] carrier; // carrier data

	//private int[] coeff; // dct values

	private ByteArrayOutputStream fos; // embedded file (output file)

	private static byte[] deZigZag = {
		0, 1, 5, 6, 14, 15, 27, 28, 2, 4, 7, 13, 16, 26, 29, 42, 3, 8, 12, 17, 25, 30, 41, 43, 9, 11, 18, 24, 31,
		40, 44, 53, 10, 19, 23, 32, 39, 45, 52, 54, 20, 22, 33, 38, 46, 51, 55, 60, 21, 34, 37, 47, 50, 56, 59, 61,
		35, 36, 48, 49, 57, 58, 62, 63 };

	public interface ExtractionListener {
		public void onExtractionResult(ByteArrayOutputStream baos);
	}

	public Extract(Activity a, String f, byte[] f5_seed) {
		this(a, new File(f), f5_seed);
	}

	public Extract(Activity a, File f, byte[] f5_seed) {
		super(Jpeg.LOG);

		fos = new ByteArrayOutputStream();
		this.f = f;
		this.a = a;
		this.f5_seed = f5_seed;
	}

	public void run() {
		super.run();

		try {
			final FileInputStream fis = new FileInputStream(this.f);
			extract(fis, (int) f.length(), fos, f5_seed);
		} catch (IOException e) {
			Log.e(Jpeg.LOG, e.toString());
			e.printStackTrace();

			((PluginNotificationListener) a).onFailure();
		} catch (InterruptedException e) {
			Log.e(Jpeg.LOG, e.toString());
			e.printStackTrace();
			
			((PluginNotificationListener) a).onFailure();
		} catch(NullPointerException e) {
			Log.e(Jpeg.LOG, e.toString());
			e.printStackTrace();

			((PluginNotificationListener) a).onFailure();
		} catch (ArrayIndexOutOfBoundsException e) {
			Log.e(Jpeg.LOG, e.toString());
			e.printStackTrace();
			
			((PluginNotificationListener) a).onFailure();
		}

	}

	public void extract(InputStream fis, int flength, ByteArrayOutputStream fos, byte[] f5_seed) throws IOException, InterruptedException {
		carrier = new byte[flength];
		fis.read(carrier);

		final HuffmanDecode hd = new HuffmanDecode(a, carrier, this);
		Log.d(Jpeg.LOG, "Huffman decoding starts");
		//coeff = hd.decode();
		int coeff_length = hd.decode();

		Log.d(Jpeg.LOG, "Permutation starts");
		final F5Random random = new F5Random(f5_seed);
		final Permutation permutation = new Permutation(coeff_length, random, hd.f5, this);

		Log.d(Jpeg.LOG, coeff_length + " indices shuffled");
		int extractedByte = 0;
		int availableExtractedBits = 0;
		int extractedFileLength = 0;
		int nBytesExtracted = 0;
		int shuffledIndex = 0;
		int extractedBit;
		int i;

		Log.d(Jpeg.LOG, "Extraction starts");
		if(isInterrupted()) { return; }
		// extract length information
		for (i = 0; availableExtractedBits < 32; i++) {
			shuffledIndex = permutation.getShuffled(i);
			if (shuffledIndex % 64 == 0) {
				continue; // skip DC coefficients
			}
			shuffledIndex = shuffledIndex - shuffledIndex % 64 + deZigZag[shuffledIndex % 64];
			if (hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) == 0) {
				continue; // skip zeroes
			}
			if (hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) > 0) {
				extractedBit = hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) & 1;
			} else {
				extractedBit = 1 - (hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) & 1);
			}
			extractedFileLength |= extractedBit << availableExtractedBits++;
		}
		// remove pseudo random pad
		extractedFileLength ^= random.getNextByte();
		extractedFileLength ^= random.getNextByte() << 8;
		extractedFileLength ^= random.getNextByte() << 16;
		extractedFileLength ^= random.getNextByte() << 24;
		int k = extractedFileLength >> 24;
		k %= 32;
		final int n = (1 << k) - 1;
		extractedFileLength &= 0x007fffff;

		Log.d(Jpeg.LOG, "Length of embedded file: " + extractedFileLength + " bytes");
		if(isInterrupted()) { return; }
		availableExtractedBits = 0;
		if (n > 0) {
			int startOfN = i;
			int hash;

			Log.d(Jpeg.LOG, "(1, " + n + ", " + k + ") code used");
			extractingLoop: do {
				// 1. read n places, and calculate k bits
				hash = 0;
				int code = 1;
				for (i = 0; code <= n; i++) {
					// check for pending end of coeff
					if (startOfN + i >= coeff_length) {
						break extractingLoop;
					}
					shuffledIndex = permutation.getShuffled(startOfN + i);
					if (shuffledIndex % 64 == 0) {
						continue; // skip DC coefficients
					}
					shuffledIndex = shuffledIndex - shuffledIndex % 64 + deZigZag[shuffledIndex % 64];
					if (hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) == 0) {
						continue; // skip zeroes
					}
					if (hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) > 0) {
						extractedBit = hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) & 1;
					} else {
						extractedBit = 1 - (hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) & 1);
					}
					if (extractedBit == 1) {
						hash ^= code;
					}
					code++;
				}
				startOfN += i;
				// 2. write k bits bytewise
				for (i = 0; i < k; i++) {
					extractedByte |= (hash >> i & 1) << availableExtractedBits++;
					if (availableExtractedBits == 8) {
						// remove pseudo random pad
						extractedByte ^= random.getNextByte();
						fos.write((byte) extractedByte);
						extractedByte = 0;
						availableExtractedBits = 0;
						nBytesExtracted++;
						// check for pending end of embedded data
						if (nBytesExtracted == extractedFileLength) {
							break extractingLoop;
						}
					}
				}
			} while (true);
		} else {
			Log.d(Jpeg.LOG, "Default code used");
			for (; i < coeff_length; i++) {
				shuffledIndex = permutation.getShuffled(i);
				if (shuffledIndex % 64 == 0) {
					continue; // skip DC coefficients
				}
				shuffledIndex = shuffledIndex - shuffledIndex % 64 + deZigZag[shuffledIndex % 64];
				if (hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) == 0) {
					continue; // skip zeroes
				}
				if (hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) > 0) {
					extractedBit = hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) & 1;
				} else {
					extractedBit = 1 - (hd.f5.getHuffmanDecodeBufferValue(shuffledIndex) & 1);
				}
				extractedByte |= extractedBit << availableExtractedBits++;
				if (availableExtractedBits == 8) {
					// remove pseudo random pad
					extractedByte ^= random.getNextByte();
					fos.write((byte) extractedByte);
					extractedByte = 0;
					availableExtractedBits = 0;
					nBytesExtracted++;
					if (nBytesExtracted == extractedFileLength) {
						break;
					}
				}
			}
		}

		if (nBytesExtracted < extractedFileLength) {
			Log.d(Jpeg.LOG, "Incomplete file: only " + nBytesExtracted + " of " + extractedFileLength + " bytes extracted");
			((PluginNotificationListener) a).onFailure();
		} else {
			((ExtractionListener) a).onExtractionResult(fos);
		}

		hd.f5.cleanUpHuffmanBuffer();
		hd.f5.cleanUpHuffmanDecodeBuffer();

	}
}
