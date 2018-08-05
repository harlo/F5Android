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

// westfeld
// todo:
// switch for multi-volume embedding
// indeterministic embedding
// password switch
package info.guardianproject.f5android.plugins.f5.james;


import info.guardianproject.f5android.plugins.f5.crypt.F5Random;
import info.guardianproject.f5android.plugins.f5.crypt.Permutation;
import info.guardianproject.f5android.stego.StegoProcessThread;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.app.Activity;
import android.graphics.Bitmap;
import android.util.Log;

/**
 * JpegEncoder - The JPEG main program which performs a jpeg compression of an
 * image.
 */
@SuppressWarnings({"unused"})
public class JpegEncoder {
	Thread runner;

	BufferedOutputStream outStream;

	JpegInfo JpegObj;

	Huffman Huf;

	DCT dct;

	int imageHeight, imageWidth;

	int Quality;

	int code;

	public static int[] jpegNaturalOrder = {
		0, 1, 8, 16, 9, 2, 3, 10, 17, 24, 32, 25, 18, 11, 4, 5, 12, 19, 26, 33, 40, 48, 41, 34, 27, 20, 13, 6, 7,
		14, 21, 28, 35, 42, 49, 56, 57, 50, 43, 36, 29, 22, 15, 23, 30, 37, 44, 51, 58, 59, 52, 45, 38, 31, 39, 46,
		53, 60, 61, 54, 47, 55, 62, 63, };

	// westfeld
	InputStream embeddedData = null;

	int n = 0;
	byte[] f5_seed;
	StegoProcessThread thread_monitor;

	public JpegEncoder(Activity a, Bitmap image, int quality, OutputStream out, byte[] f5_seed, StegoProcessThread thread_monitor) {
		/*
		 * Quality of the image. 0 to 100 and from bad image quality, high
		 * compression to good image quality low compression
		 */
		this.Quality = quality;

		/*
		 * Getting picture information It takes the Width, Height and RGB scans
		 * of the image.
		 */
		this.thread_monitor = thread_monitor;
		this.JpegObj = new JpegInfo(a, image, this.thread_monitor);

		this.imageHeight = this.JpegObj.imageHeight;
		this.imageWidth = this.JpegObj.imageWidth;
		this.outStream = new BufferedOutputStream(out);
		this.dct = new DCT(this.Quality);
		this.Huf = new Huffman(this.imageWidth, this.imageHeight);
		this.f5_seed = f5_seed;

	}

	@SuppressWarnings("static-access")
	public boolean Compress() {
		Log.d(thread_monitor.LOG, "NOW COMPRESSING...");

		if(this.thread_monitor.isInterrupted()) { return false; }
		WriteHeaders(this.outStream);

		if(this.thread_monitor.isInterrupted()) { return false; }
		WriteCompressedData(this.outStream);

		if(this.thread_monitor.isInterrupted()) { return false; }
		WriteEOI(this.outStream);

		if(this.thread_monitor.isInterrupted()) { return false; }
		try {
			this.outStream.flush();
			this.JpegObj.f5.cleanUpCoeffs();
			this.JpegObj.f5.cleanUpImage();
			this.JpegObj.f5.cleanUpPermutation();
			return true;
		} catch (final IOException e) {
			Log.e(Jpeg.LOG, "IO Error: " + e.getMessage());
		}

		this.JpegObj.f5.cleanUpCoeffs();
		this.JpegObj.f5.cleanUpImage();
		this.JpegObj.f5.cleanUpPermutation();

		return false;
	}

	public boolean Compress(final InputStream embeddedData) {
		this.embeddedData = embeddedData;
		return Compress();
	}

	public int getQuality() {
		return this.Quality;
	}

	public void setQuality(final int quality) {
		this.dct = new DCT(quality);
	}

	void WriteArray(final byte[] data, final BufferedOutputStream out) {
		final int i;
		int length;
		try {
			length = ((data[2] & 0xFF) << 8) + (data[3] & 0xFF) + 2;
			out.write(data, 0, length);
		} catch (final IOException e) {
			Log.e(Jpeg.LOG, "IO Error: " + e.getMessage());
		}
	}

	public void WriteCompressedData(final BufferedOutputStream outStream) {
		final int offset;
		int i, j, r, c, a, b;
		final int temp = 0;
		int comp, xpos, ypos, xblockoffset, yblockoffset;
		final float dctArray1[][] = new float[8][8];
		double dctArray2[][] = new double[8][8];
		int dctArray3[] = new int[8 * 8];

		/*
		 * This method controls the compression of the image. Starting at the
		 * upper left of the image, it compresses 8x8 blocks of data until the
		 * entire image has been compressed.
		 */

		final int lastDCvalue[] = new int[this.JpegObj.NumberOfComponents];
		final int zeroArray[] = new int[64]; // initialized to hold all zeros
		int Width = 0, Height = 0;
		final int nothing = 0, not;
		int MinBlockWidth, MinBlockHeight;
		// This initial setting of MinBlockWidth and MinBlockHeight is done to
		// ensure they start with values larger than will actually be the case.
		MinBlockWidth = this.imageWidth % 8 != 0 ? (int) (Math.floor(this.imageWidth / 8.0) + 1) * 8 : this.imageWidth;
		MinBlockHeight = this.imageHeight % 8 != 0 ? (int) (Math.floor(this.imageHeight / 8.0) + 1) * 8
				: this.imageHeight;
		for (comp = 0; comp < this.JpegObj.NumberOfComponents; comp++) {
			MinBlockWidth = Math.min(MinBlockWidth, this.JpegObj.BlockWidth[comp]);
			MinBlockHeight = Math.min(MinBlockHeight, this.JpegObj.BlockHeight[comp]);
		}
		xpos = 0;
		// westfeld
		// Before we enter these loops, we initialise the
		// coeff for steganography here:
		int shuffledIndex = 0;
		int coeffCount = 0;
		for (r = 0; r < MinBlockHeight; r++) {
			for (c = 0; c < MinBlockWidth; c++) {
				for (comp = 0; comp < this.JpegObj.NumberOfComponents; comp++) {
					for (i = 0; i < this.JpegObj.VsampFactor[comp]; i++) {
						for (j = 0; j < this.JpegObj.HsampFactor[comp]; j++) {
							coeffCount += 64;
						}
					}
				}
			}
		}

		if(this.thread_monitor.isInterrupted()) { return; }
		this.JpegObj.f5.initF5Coeffs(coeffCount);

		Log.d(Jpeg.LOG, "DCT/quantisation starts");
		Log.d(Jpeg.LOG, this.imageWidth + " x " + this.imageHeight);

		for (r = 0; r < MinBlockHeight; r++) {
			for (c = 0; c < MinBlockWidth; c++) {
				xpos = c * 8;
				ypos = r * 8;
				for (comp = 0; comp < this.JpegObj.NumberOfComponents; comp++) {
					Width = this.JpegObj.BlockWidth[comp];
					Height = this.JpegObj.BlockHeight[comp];

					// XXX: get input arrays from JNI
					//inputArray = (float[][]) this.JpegObj.Components[comp];

					for (i = 0; i < this.JpegObj.VsampFactor[comp]; i++) {
						for (j = 0; j < this.JpegObj.HsampFactor[comp]; j++) {
							xblockoffset = j * 8;
							yblockoffset = i * 8;
							for (a = 0; a < 8; a++) {
								for (b = 0; b < 8; b++) {

									// I believe this is where the dirty line at
									// the bottom of the image is
									// coming from. I need to do a check here to
									// make sure I'm not reading past
									// image data.
									// This seems to not be a big issue right
									// now. (04/04/98)

									// westfeld - dirty line fixed, Jun 6 2000
									int ia = ypos * this.JpegObj.VsampFactor[comp] + yblockoffset + a;
									int ib = xpos * this.JpegObj.HsampFactor[comp] + xblockoffset + b;
									if (this.imageHeight / 2 * this.JpegObj.VsampFactor[comp] <= ia) {
										ia = this.imageHeight / 2 * this.JpegObj.VsampFactor[comp] - 1;
									}
									if (this.imageWidth / 2 * this.JpegObj.HsampFactor[comp] <= ib) {
										ib = this.imageWidth / 2 * this.JpegObj.HsampFactor[comp] - 1;
									}
									// dctArray1[a][b] = inputArray[ypos +
									                                // yblockoffset + a][xpos + xblockoffset +
									                                                     // b];

									//dctArray1[a][b] = inputArray[ia][ib];

									// XXX: this is how we get the value from JNI
									switch(comp) {
									case 0:
										dctArray1[a][b] = this.JpegObj.f5.getYValue(ia, ib);
										break;
									case 1:
										dctArray1[a][b] = this.JpegObj.f5.getCb2Value(ia, ib);
										break;
									case 2:
										dctArray1[a][b] = this.JpegObj.f5.getCr2Value(ia, ib);
										break;
									}
								}
							}
							// The following code commented out because on some
							// images this technique
							// results in poor right and bottom borders.
							// if ((!JpegObj.lastColumnIsDummy[comp] || c <
							// Width - 1) && (!JpegObj.lastRowIsDummy[comp] || r
							// < Height - 1)) {



							dctArray2 = this.dct.forwardDCT(dctArray1);
							dctArray3 = this.dct.quantizeBlock(dctArray2, this.JpegObj.QtableNumber[comp]);

							// }
							// else {
							// zeroArray[0] = dctArray3[0];
							// zeroArray[0] = lastDCvalue[comp];
							// dctArray3 = zeroArray;
							// }
							// westfeld
							// For steganography, all dct
							// coefficients are collected in
							// coeff[] first. We do not encode
							// any Huffman Blocks here (we'll do
							// this later).

							// public static void arraycopy (Object src, int srcPos, Object dst, int dstPos, int length)
							// Copies length elements from the array src, 
							// starting at offset srcPos, into the array dst, 
							// starting at offset dstPos.

							//System.arraycopy(dctArray3, 0, coeff, shuffledIndex, 64);
							int[] coeff = new int[64];
							System.arraycopy(dctArray3, 0, coeff, 0, 64);

							this.JpegObj.f5.setCoeffValues(coeff, shuffledIndex);
							shuffledIndex += 64;

						}
					}
				}
			}
		}

		Log.d(Jpeg.LOG, "got " + coeffCount + " DCT AC/DC coefficients");
		int _changed = 0;
		int _embedded = 0;
		int _examined = 0;
		int _expected = 0;
		int _one = 0;
		int _large = 0;
		int _thrown = 0;
		int _zero = 0;
		for (i = 0; i < coeffCount; i++) {
			if (i % 64 == 0) {
				continue;
			}
			if (this.JpegObj.f5.getCoeffValue(i) == 1) {
				_one++;
			}
			if (this.JpegObj.f5.getCoeffValue(i) == -1) {
				_one++;
			}
			if (this.JpegObj.f5.getCoeffValue(i) == 0) {
				_zero++;
			}
		}
		_large = coeffCount - _zero - _one - coeffCount / 64;
		_expected = _large + (int) (0.49 * _one);
		//
		// System.out.println("zero="+_zero);
		Log.d(Jpeg.LOG, "one=" + _one);
		Log.d(Jpeg.LOG, "large=" + _large);
		//
		Log.d(Jpeg.LOG, "expected capacity: " + _expected + " bits");
		Log.d(Jpeg.LOG, "expected capacity with");
		for (i = 1; i < 8; i++) {
			int usable, changed, n;
			n = (1 << i) - 1;
			usable = _expected * i / n - _expected * i / n % n;
			changed = coeffCount - _zero - coeffCount / 64;
			changed = changed * i / n - changed * i / n % n;
			changed = n * changed / (n + 1) / i;
			//
			changed = _large - _large % (n + 1);
			changed = (changed + _one + _one / 2 - _one / (n + 1)) / (n + 1);
			usable /= 8;
			if (usable == 0) {
				break;
			}
			if (i == 1) {
				Log.d(Jpeg.LOG, "default");
			} else {
				Log.d(Jpeg.LOG, "(1, " + n + ", " + i + ")");
			}
			Log.d(Jpeg.LOG, " code: " + usable + " bytes (efficiency: " + usable * 8 / changed + "." + usable * 80
					/ changed % 10 + " bits per change)");
		}

		// westfeld
		if (this.embeddedData != null) {
			// Now we embed the secret data in the permutated sequence.
			Log.d(Jpeg.LOG, "Permutation starts");
			final F5Random random = new F5Random(this.f5_seed);
			final Permutation permutation = new Permutation(coeffCount, random, this.JpegObj.f5, this.thread_monitor);
			int nextBitToEmbed = 0;
			int byteToEmbed = 0;
			int availableBitsToEmbed = 0;
			// We start with the length information. Well,
			// the length information it is more than one
			// byte, so this first "byte" is 32 bits long.
			try {
				byteToEmbed = this.embeddedData.available();
			} catch (final Exception e) {
				e.printStackTrace();
			}
			Log.d(Jpeg.LOG, "Embedding of " + (byteToEmbed * 8 + 32) + " bits (" + byteToEmbed + "+4 bytes) ");
			// We use the most significant byte for the 1 of n
			// code, and reserve one extra bit for future use.
			if (byteToEmbed > 0x007fffff) {
				byteToEmbed = 0x007fffff;
			}
			// We calculate n now
			for (i = 1; i < 8; i++) {
				int usable;
				final int changed;
				this.n = (1 << i) - 1;
				usable = _expected * i / this.n - _expected * i / this.n % this.n;
				usable /= 8;
				if (usable == 0) {
					break;
				}
				if (usable < byteToEmbed + 4) {
					break;
				}
			}
			final int k = i - 1;
			this.n = (1 << k) - 1;
			switch (this.n) {
			case 0:
				Log.d(Jpeg.LOG, "using default code, file will not fit");
				this.n++;
				break;
			case 1:
				Log.d(Jpeg.LOG, "using default code");
				break;
			default:
				Log.d(Jpeg.LOG, "using (1, " + this.n + ", " + k + ") code");
			}
			byteToEmbed |= k << 24; // store k in the status word
			// Since shuffling cannot hide the distribution, the
			// distribution of all bits to embed is unified by
			// adding a pseudo random bit-string. We continue the random
			// we used for Permutation, initially seeked with password.
			byteToEmbed ^= random.getNextByte();
			byteToEmbed ^= random.getNextByte() << 8;
			byteToEmbed ^= random.getNextByte() << 16;
			byteToEmbed ^= random.getNextByte() << 24;
			nextBitToEmbed = byteToEmbed & 1;
			byteToEmbed >>= 1;
			availableBitsToEmbed = 31;
			_embedded++;
			if (this.n > 1) { // use 1 of n code
				int kBitsToEmbed;
				int extractedBit;
				final int[] codeWord = new int[this.n];
				int hash;
				int startOfN = 0;
				int endOfN = 0;
				boolean isLastByte = false;
				// embed status word first
				for (i = 0; i < coeffCount; i++) {
					shuffledIndex = permutation.getShuffled(i);
					if (shuffledIndex % 64 == 0) {
						continue; // skip DC coefficients
					}
					if (this.JpegObj.f5.getCoeffValue(shuffledIndex) == 0) {
						continue; // skip zeroes
					}
					if (this.JpegObj.f5.getCoeffValue(shuffledIndex) > 0) {
						if ((this.JpegObj.f5.getCoeffValue(shuffledIndex) & 1) != nextBitToEmbed) {
							this.JpegObj.f5.setCoeffValues(new int[] { this.JpegObj.f5.getCoeffValue(shuffledIndex) - 1}, shuffledIndex);
							_changed++;
						}
					} else {
						if ((this.JpegObj.f5.getCoeffValue(shuffledIndex) & 1) == nextBitToEmbed) {
							this.JpegObj.f5.setCoeffValues(new int[] { this.JpegObj.f5.getCoeffValue(shuffledIndex) + 1}, shuffledIndex);
							_changed++;
						}
					}
					if (this.JpegObj.f5.getCoeffValue(shuffledIndex) != 0) {
						// The coefficient is still nonzero. We
						// successfully embedded "nextBitToEmbed".
						// We will read a new bit to embed now.
						if (availableBitsToEmbed == 0) {
							break; // statusword embedded.
						}
						nextBitToEmbed = byteToEmbed & 1;
						byteToEmbed >>= 1;
			availableBitsToEmbed--;
			_embedded++;
					} else {
						_thrown++;
					}
				}
				startOfN = i + 1;
				// now embed the data using 1 of n code
				embeddingLoop: do {
					kBitsToEmbed = 0;
					// get k bits to embed
					for (i = 0; i < k; i++) {
						if (availableBitsToEmbed == 0) {
							// If the byte of embedded text is
							// empty, we will get a new one.
							try {
								if (this.embeddedData.available() == 0) {
									isLastByte = true;
									break;
								}
								byteToEmbed = this.embeddedData.read();
								byteToEmbed ^= random.getNextByte();
							} catch (final Exception e) {
								e.printStackTrace();
								break;
							}
							availableBitsToEmbed = 8;
						}
						nextBitToEmbed = byteToEmbed & 1;
						byteToEmbed >>= 1;
					availableBitsToEmbed--;
					kBitsToEmbed |= nextBitToEmbed << i;
					_embedded++;
					}
					// embed k bits
					do {
						j = startOfN;
						// fill codeWord[] with the indices of the
						// next n non-zero coefficients in coeff[]
						for (i = 0; i < this.n; j++) {
							if (j >= coeffCount) {
								// in rare cases the estimated capacity is too
								// small
								Log.d(Jpeg.LOG, "Capacity exhausted.");
								break embeddingLoop;
							}
							shuffledIndex = permutation.getShuffled(j);
							if (shuffledIndex % 64 == 0) {
								continue; // skip DC coefficients
							}
							if (this.JpegObj.f5.getCoeffValue(shuffledIndex) == 0) {
								continue; // skip zeroes
							}
							codeWord[i++] = shuffledIndex;
						}
						endOfN = j;
						hash = 0;
						for (i = 0; i < this.n; i++) {
							if (this.JpegObj.f5.getCoeffValue(codeWord[i]) > 0) {
								extractedBit = this.JpegObj.f5.getCoeffValue(codeWord[i]) & 1;
							} else {
								extractedBit = 1 - (this.JpegObj.f5.getCoeffValue(codeWord[i]) & 1);
							}
							if (extractedBit == 1) {
								hash ^= i + 1;
							}
						}
						i = hash ^ kBitsToEmbed;
						if (i == 0) {
							break; // embedded without change
						}
						i--;
						if (this.JpegObj.f5.getCoeffValue(codeWord[i]) > 0) {
							this.JpegObj.f5.setCoeffValues(new int[] { this.JpegObj.f5.getCoeffValue(codeWord[i]) - 1}, codeWord[i]);
						} else {
							this.JpegObj.f5.setCoeffValues(new int[] { this.JpegObj.f5.getCoeffValue(codeWord[i]) + 1}, codeWord[i]);
						}
						_changed++;
						if (this.JpegObj.f5.getCoeffValue(codeWord[i]) == 0) {
							_thrown++;
						}
					} while (this.JpegObj.f5.getCoeffValue(codeWord[i]) == 0);
					startOfN = endOfN;
				} while (!isLastByte);
			} else { // default code
				// The main embedding loop follows. It works on the
				// shuffled stream of coefficients.
				for (i = 0; i < coeffCount; i++) {
					shuffledIndex = permutation.getShuffled(i);
					if (shuffledIndex % 64 == 0) {
						continue; // skip DC coefficients
					}
					if (this.JpegObj.f5.getCoeffValue(shuffledIndex) == 0) {
						continue; // skip zeroes
					}
					_examined++;
					if (this.JpegObj.f5.getCoeffValue(shuffledIndex) > 0) {
						if ((this.JpegObj.f5.getCoeffValue(shuffledIndex) & 1) != nextBitToEmbed) {
							this.JpegObj.f5.setCoeffValues(new int[] { this.JpegObj.f5.getCoeffValue(shuffledIndex) - 1}, shuffledIndex);
							_changed++;
						}
					} else {
						if ((this.JpegObj.f5.getCoeffValue(shuffledIndex) & 1) == nextBitToEmbed) {
							this.JpegObj.f5.setCoeffValues(new int[] { this.JpegObj.f5.getCoeffValue(shuffledIndex) + 1}, shuffledIndex);
							_changed++;
						}
					}
					if (this.JpegObj.f5.getCoeffValue(shuffledIndex) != 0) {
						// The coefficient is still nonzero. We
						// successfully embedded "nextBitToEmbed".
						// We will read a new bit to embed now.
						if (availableBitsToEmbed == 0) {
							// If the byte of embedded text is
							// empty, we will get a new one.
							try {
								if (this.embeddedData.available() == 0) {
									break;
								}
								byteToEmbed = this.embeddedData.read();
								byteToEmbed ^= random.getNextByte();
							} catch (final Exception e) {
								e.printStackTrace();
								break;
							}
							availableBitsToEmbed = 8;
						}
						nextBitToEmbed = byteToEmbed & 1;
						byteToEmbed >>= 1;
					availableBitsToEmbed--;
					_embedded++;
					} else {
						_thrown++;
					}
				}
			}
			if (_examined > 0) {
				Log.d(Jpeg.LOG, _examined + " coefficients examined");
			}
			Log.d(Jpeg.LOG, _changed + " coefficients changed (efficiency: " + _embedded / _changed + "."
					+ _embedded * 10 / _changed % 10 + " bits per change)");
			Log.d(Jpeg.LOG, _thrown + " coefficients thrown (zeroed)");
			Log.d(Jpeg.LOG, _embedded + " bits (" + _embedded / 8 + " bytes) embedded");
		}
		Log.d(Jpeg.LOG, "Starting Huffman Encoding.");
		// Do the Huffman Encoding now.
		shuffledIndex = 0;
		for (r = 0; r < MinBlockHeight; r++) {
			for (c = 0; c < MinBlockWidth; c++) {
				for (comp = 0; comp < this.JpegObj.NumberOfComponents; comp++) {
					for (i = 0; i < this.JpegObj.VsampFactor[comp]; i++) {
						for (j = 0; j < this.JpegObj.HsampFactor[comp]; j++) {
							for(int c_=0; c_<64; c_++)
								dctArray3[c_] = this.JpegObj.f5.getCoeffValue(shuffledIndex + c_);

							this.Huf.HuffmanBlockEncoder(outStream, dctArray3, lastDCvalue[comp],
									this.JpegObj.DCtableNumber[comp], this.JpegObj.ACtableNumber[comp]);
							lastDCvalue[comp] = dctArray3[0];
							shuffledIndex += 64;
						}
					}
				}
			}
		}

		this.Huf.flushBuffer(outStream);
	}

	public void WriteEOI(final BufferedOutputStream out) {
		final byte[] EOI = {
				(byte) 0xFF, (byte) 0xD9 };
		WriteMarker(EOI, out);
	}

	public void WriteHeaders(final BufferedOutputStream out) {
		int i, j, index, offset, length;
		int tempArray[];

		// the SOI marker
		final byte[] SOI = {
				(byte) 0xFF, (byte) 0xD8 };
		WriteMarker(SOI, out);

		// The order of the following headers is quiet inconsequential.
		// the JFIF header

		final byte JFIF[] = new byte[18];
		JFIF[0] = (byte) 0xff; // app0 marker
		JFIF[1] = (byte) 0xe0;
		JFIF[2] = (byte) 0x00; // length
		JFIF[3] = (byte) 0x10;
		JFIF[4] = (byte) 0x4a; // "JFIF"
		JFIF[5] = (byte) 0x46;
		JFIF[6] = (byte) 0x49;
		JFIF[7] = (byte) 0x46;
		JFIF[8] = (byte) 0x00;
		JFIF[9] = (byte) 0x01; // 1.01
		JFIF[10] = (byte) 0x01;
		JFIF[11] = (byte) 0x00;
		JFIF[12] = (byte) 0x00;
		JFIF[13] = (byte) 0x01;
		JFIF[14] = (byte) 0x00;
		JFIF[15] = (byte) 0x01;
		JFIF[16] = (byte) 0x00;
		JFIF[17] = (byte) 0x00;

		/*
        if (this.JpegObj.getComment().equals("JPEG Encoder Copyright 1998, James R. Weeks and BioElectroMech.  ")) {
            JFIF[10] = (byte) 0x00; // 1.00
        }
		 */

        // Not Necessary. Few JPEG encoder omit it.
		// Conspicuous without.
		WriteArray(JFIF, out);


		// Comment Header
        // Lie. Claim to be the product of a popular PHP jpeg lib to reduce suspicion.
        String comment = "CREATOR: gd-jpeg v1.0 (using IJG JPEG v62), quality = 90\n  ";

        length = comment.length();
        if (length != 0) {
            final byte COM[] = new byte[length + 4];
            COM[0] = (byte) 0xFF;
            COM[1] = (byte) 0xFE;
            COM[2] = (byte) (length >> 8 & 0xFF);
            COM[3] = (byte) (length & 0xFF);
            java.lang.System.arraycopy(comment.getBytes(), 0, COM, 4, comment.length());
            WriteArray(COM, out);
        }


		// The DQT header
		// 0 is the luminance index and 1 is the chrominance index
		final byte DQT[] = new byte[134];
		DQT[0] = (byte) 0xFF;
		DQT[1] = (byte) 0xDB;
		DQT[2] = (byte) 0x00;
		DQT[3] = (byte) 0x84;
		offset = 4;
		for (i = 0; i < 2; i++) {
			DQT[offset++] = (byte) ((0 << 4) + i);
			tempArray = (int[]) this.dct.quantum[i];
			for (j = 0; j < 64; j++) {
				DQT[offset++] = (byte) tempArray[jpegNaturalOrder[j]];
			}
		}
		WriteArray(DQT, out);

		// Start of Frame Header
		final byte SOF[] = new byte[19];
		SOF[0] = (byte) 0xFF;
		SOF[1] = (byte) 0xC0;
		SOF[2] = (byte) 0x00;
		SOF[3] = (byte) 17;
		SOF[4] = (byte) this.JpegObj.Precision;
		SOF[5] = (byte) (this.JpegObj.imageHeight >> 8 & 0xFF);
		SOF[6] = (byte) (this.JpegObj.imageHeight & 0xFF);
		SOF[7] = (byte) (this.JpegObj.imageWidth >> 8 & 0xFF);
		SOF[8] = (byte) (this.JpegObj.imageWidth & 0xFF);
		SOF[9] = (byte) this.JpegObj.NumberOfComponents;
		index = 10;
		for (i = 0; i < SOF[9]; i++) {
			SOF[index++] = (byte) this.JpegObj.CompID[i];
			SOF[index++] = (byte) ((this.JpegObj.HsampFactor[i] << 4) + this.JpegObj.VsampFactor[i]);
			SOF[index++] = (byte) this.JpegObj.QtableNumber[i];
		}
		WriteArray(SOF, out);

		// The DHT Header
		byte DHT1[], DHT2[], DHT3[], DHT4[];
		int bytes, temp, oldindex, intermediateindex;
		length = 2;
		index = 4;
		oldindex = 4;
		DHT1 = new byte[17];
		DHT4 = new byte[4];
		DHT4[0] = (byte) 0xFF;
		DHT4[1] = (byte) 0xC4;
		for (i = 0; i < 4; i++) {
			bytes = 0;
			DHT1[index++ - oldindex] = (byte) this.Huf.bits.elementAt(i)[0];
			for (j = 1; j < 17; j++) {
				temp = this.Huf.bits.elementAt(i)[j];
				DHT1[index++ - oldindex] = (byte) temp;
				bytes += temp;
			}
			intermediateindex = index;
			DHT2 = new byte[bytes];
			for (j = 0; j < bytes; j++) {
				DHT2[index++ - intermediateindex] = (byte) this.Huf.val.elementAt(i)[j];
			}
			DHT3 = new byte[index];
			java.lang.System.arraycopy(DHT4, 0, DHT3, 0, oldindex);
			java.lang.System.arraycopy(DHT1, 0, DHT3, oldindex, 17);
			java.lang.System.arraycopy(DHT2, 0, DHT3, oldindex + 17, bytes);
			DHT4 = DHT3;
			oldindex = index;
		}
		DHT4[2] = (byte) (index - 2 >> 8 & 0xFF);
		DHT4[3] = (byte) (index - 2 & 0xFF);
		WriteArray(DHT4, out);

		// Start of Scan Header
		final byte SOS[] = new byte[14];
		SOS[0] = (byte) 0xFF;
		SOS[1] = (byte) 0xDA;
		SOS[2] = (byte) 0x00;
		SOS[3] = (byte) 12;
		SOS[4] = (byte) this.JpegObj.NumberOfComponents;
		index = 5;
		for (i = 0; i < SOS[4]; i++) {
			SOS[index++] = (byte) this.JpegObj.CompID[i];
			SOS[index++] = (byte) ((this.JpegObj.DCtableNumber[i] << 4) + this.JpegObj.ACtableNumber[i]);
		}
		SOS[index++] = (byte) this.JpegObj.Ss;
		SOS[index++] = (byte) this.JpegObj.Se;
		SOS[index++] = (byte) ((this.JpegObj.Ah << 4) + this.JpegObj.Al);
		WriteArray(SOS, out);

	}

	void WriteMarker(final byte[] data, final BufferedOutputStream out) {
		try {
			out.write(data, 0, 2);
		} catch (final IOException e) {
			Log.e(Jpeg.LOG, "IO Error: " + e.getMessage());
			// XXX: CATCH THIS ERROR?
		}
	}
}
