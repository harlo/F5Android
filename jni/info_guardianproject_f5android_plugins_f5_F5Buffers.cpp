#include "info_guardianproject_f5android_plugins_f5_F5Buffers.h"
#include <jni.h>
#include <stdlib.h>
#include <stdio.h>
#include <android/log.h>
#include <string.h>

class F5Permutation
{
public:
	int* buffer;

	F5Permutation() {};
	F5Permutation(int hb_size) {
		this->buffer = new int[hb_size];
	}
	~F5Permutation() {};
};

class F5HuffmanDecodeBuffer
{
public:
	int* buffer;

	F5HuffmanDecodeBuffer() {};
	F5HuffmanDecodeBuffer(int hb_size) {
		this->buffer = new int[hb_size];
	}
	~F5HuffmanDecodeBuffer() {};
};

class F5HuffmanBuffer
{
public:
	int* buffer;

	F5HuffmanBuffer() {};
	F5HuffmanBuffer(int hb_size) {
		this->buffer = new int[hb_size];
	}
	~F5HuffmanBuffer() {};
};

class F5Coeffs
{
public:
	int* coeffs;

	F5Coeffs() {};
	F5Coeffs(int coeffs_size) {
		this->coeffs = new int[coeffs_size];
	}
	~F5Coeffs() {};
};

class F5Image
{
public:
	int* dimensions;
	int* comp_width;
	int* comp_height;
	int* pixel_array;
	float** y_array;
	float** cb1_array;
	float** cb2_array;
	float** cr1_array;
	float** cr2_array;


	F5Image() {};
	F5Image(int* dimensions, int* comp_width, int* comp_height) {
		this->dimensions = dimensions;
		this->comp_width = comp_width;
		this->comp_height = comp_height;
		this->pixel_array = new int[dimensions[0] * dimensions[1]];

		int i;
		this->y_array = new float *[comp_height[0]];
		for(i=0; i<comp_height[0]; i++)
			this->y_array[i] = new float[comp_width[0]];

		this->cr1_array = new float *[comp_height[0]];
		for(i=0; i<comp_height[0]; i++)
			this->cr1_array[i] = new float[comp_width[0]];

		this->cb1_array = new float *[comp_height[0]];
		for(i=0; i<comp_height[0]; i++)
			this->cb1_array[i] = new float[comp_width[0]];

		this->cb2_array = new float *[comp_height[1]];
		for(i=0; i<comp_height[1]; i++)
			this->cb2_array[i] = new float[comp_width[1]];

		this->cr2_array = new float *[comp_height[2]];
		for(i=0; i<comp_height[2]; i++)
			this->cr2_array[i] = new float[comp_width[2]];
	};
	~F5Image() {};

};

JNIEXPORT jobject JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_initImage
(JNIEnv *env, jobject obj, jintArray dimensions, jintArray comp_width, jintArray comp_height)
{
	int* dimensions_ = env->GetIntArrayElements(dimensions, NULL);
	int* comp_width_ = env->GetIntArrayElements(comp_width, NULL);
	int* comp_height_ = env->GetIntArrayElements(comp_height, NULL);

	F5Image* f5 = new F5Image(dimensions_, comp_width_, comp_height_);

	env->ReleaseIntArrayElements(dimensions, dimensions_, 0);
	env->ReleaseIntArrayElements(comp_width, comp_width_, 0);
	env->ReleaseIntArrayElements(comp_height, comp_height_, 0);

	return env->NewDirectByteBuffer((void*) f5, sizeof(F5Image));
}

JNIEXPORT jobject JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_initCoeffs
(JNIEnv *env, jobject obj, int coeffs_size)
{
	F5Coeffs* f5 = new F5Coeffs(coeffs_size);
	return env->NewDirectByteBuffer((void*) f5, sizeof(F5Coeffs));
}

JNIEXPORT jobject JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_initHuffmanBuffer
(JNIEnv *env, jobject obj, int hb_size)
{
	F5HuffmanBuffer* f5 = new F5HuffmanBuffer(hb_size);
	return env->NewDirectByteBuffer((void*) f5, sizeof(F5HuffmanBuffer));
}

JNIEXPORT jobject JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_initHuffmanDecodeBuffer
  (JNIEnv *env, jobject obj, int hdb_size)
{
	F5HuffmanDecodeBuffer* f5 = new F5HuffmanDecodeBuffer(hdb_size);
	return env->NewDirectByteBuffer((void*) f5, sizeof(F5HuffmanDecodeBuffer));
}

JNIEXPORT jobject JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_initPermutation
  (JNIEnv *env, jobject obj, int p_size)
{
	F5Permutation* f5 = new F5Permutation(p_size);
	return env->NewDirectByteBuffer((void*) f5, sizeof(F5Permutation));
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_setPixelValues
(JNIEnv *env, jobject obj, jobject pntr, jintArray values, int v_len, int start)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	int* values_ = env->GetIntArrayElements(values, NULL);

	int p = 0;
	for(; p<v_len;) {
		f5->pixel_array[p + start] = values_[p];
		p++;
	}

	env->ReleaseIntArrayElements(values, values_, 0);
}

JNIEXPORT jint JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_getPixelValue
(JNIEnv *env, jobject obj, jobject pntr, int pos)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	return f5->pixel_array[pos];
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_setYValues
(JNIEnv *env, jobject obj, jobject pntr, float value, int x, int y)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	f5->y_array[x][y] = value;
}

JNIEXPORT jfloat JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_getYValue
(JNIEnv *env, jobject obj, jobject pntr, int x, int y)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	return f5->y_array[x][y];
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_setCr1Values
(JNIEnv *env, jobject obj, jobject pntr, float value, int x, int y)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	f5->cr1_array[x][y] = value;
}

JNIEXPORT jfloat JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_getCr1Value
(JNIEnv *env, jobject obj, jobject pntr, int x, int y)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	return f5->cr1_array[x][y];
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_setCb1Values
(JNIEnv *env, jobject obj, jobject pntr, float value, int x, int y)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	f5->cb1_array[x][y] = value;
}

JNIEXPORT jfloat JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_getCb1Value
(JNIEnv *env, jobject obj, jobject pntr, int x, int y)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	return f5->cb1_array[x][y];
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_setCr2Values
(JNIEnv *env, jobject obj, jobject pntr, float value, int x, int y)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	f5->cr2_array[x][y] = value;
}

JNIEXPORT jfloat JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_getCr2Value
(JNIEnv *env, jobject obj, jobject pntr, int x, int y)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	return f5->cr2_array[x][y];
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_setCb2Values
(JNIEnv *env, jobject obj, jobject pntr, float value, int x, int y)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	f5->cb2_array[x][y] = value;
}

JNIEXPORT jfloat JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_getCb2Value
(JNIEnv *env, jobject obj, jobject pntr, int x, int y)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	return f5->cb2_array[x][y];
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_setCoeffValues
(JNIEnv *env, jobject obj, jobject pntr, jintArray values, int v_len, int start)
{
	F5Coeffs* f5 = (F5Coeffs*) env->GetDirectBufferAddress(pntr);
	int* values_ = env->GetIntArrayElements(values, NULL);

	int p = 0;
	for(; p<v_len;) {
		f5->coeffs[p + start] = values_[p];
		p++;
	}

	env->ReleaseIntArrayElements(values, values_, 0);
}

JNIEXPORT jint JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_getCoeffValue
(JNIEnv *env, jobject obj, jobject pntr, int pos)
{
	F5Coeffs* f5 = (F5Coeffs*) env->GetDirectBufferAddress(pntr);
	return f5->coeffs[pos];
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_setHuffmanBufferValues
(JNIEnv *env, jobject obj, jobject pntr, jintArray values, int v_len, int start)
{
	F5HuffmanBuffer* f5 = (F5HuffmanBuffer*) env->GetDirectBufferAddress(pntr);
	int* values_ = env->GetIntArrayElements(values, NULL);

	int p = 0;
	for(; p<v_len; p++) {
		f5->buffer[p + start] = values_[p];
	}

	env->ReleaseIntArrayElements(values, values_, 0);
}

JNIEXPORT jint JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_getHuffmanBufferValue
(JNIEnv *env, jobject obj, jobject pntr, int pos)
{
	F5HuffmanBuffer* f5 = (F5HuffmanBuffer*) env->GetDirectBufferAddress(pntr);
	return f5->buffer[pos];
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_setHuffmanDecodeBufferValues
  (JNIEnv *env, jobject obj, jobject hdb_pntr, jobject hb_pntr, int v_len, int start)
{
	F5HuffmanBuffer* f5 = (F5HuffmanBuffer*) env->GetDirectBufferAddress(hb_pntr);
	F5HuffmanDecodeBuffer* f5_ = (F5HuffmanDecodeBuffer*) env->GetDirectBufferAddress(hdb_pntr);

	int p=0;
	for(; p<v_len; p++) {
		f5_->buffer[p + start] = f5->buffer[p + start];
	}
}

JNIEXPORT jint JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_getHuffmanDecodeBufferValue
  (JNIEnv *env, jobject obj, jobject pntr, int pos)
{
	F5HuffmanDecodeBuffer* f5 = (F5HuffmanDecodeBuffer*) env->GetDirectBufferAddress(pntr);
	return f5->buffer[pos];
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_setPermutationValues
  (JNIEnv *env, jobject obj, jobject pntr, jintArray values, int v_len, int start)
{
	F5Permutation* f5 = (F5Permutation*) env->GetDirectBufferAddress(pntr);
	int* values_ = env->GetIntArrayElements(values, NULL);

	int p = 0;
	for(; p<v_len; p++) {
		f5->buffer[p + start] = values_[p];
	}

	env->ReleaseIntArrayElements(values, values_, 0);
}

JNIEXPORT jint JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_getPermutationValue
  (JNIEnv *env, jobject obj, jobject pntr, int pos)
{
	F5Permutation* f5 = (F5Permutation*) env->GetDirectBufferAddress(pntr);
	return f5->buffer[pos];
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_cleanUpImage
(JNIEnv *env, jobject obj, jobject pntr)
{
	F5Image* f5 = (F5Image*) env->GetDirectBufferAddress(pntr);
	delete(f5);
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_cleanUpCoeffs
(JNIEnv *env, jobject obj, jobject pntr)
{
	F5Coeffs* f5 = (F5Coeffs*) env->GetDirectBufferAddress(pntr);
	delete(f5);
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_cleanUpHuffmanBuffer
(JNIEnv *env, jobject obj, jobject pntr)
{
	F5HuffmanBuffer* f5 = (F5HuffmanBuffer*) env->GetDirectBufferAddress(pntr);
	delete(f5);
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_cleanUpHuffmanDecodeBuffer
(JNIEnv *env, jobject obj, jobject pntr)
{
	F5HuffmanDecodeBuffer* f5 = (F5HuffmanDecodeBuffer*) env->GetDirectBufferAddress(pntr);
	delete(f5);
}

JNIEXPORT void JNICALL Java_info_guardianproject_f5android_plugins_f5_F5Buffers_cleanUpPermutation
(JNIEnv *env, jobject obj, jobject pntr)
{
	F5Permutation* f5 = (F5Permutation*) env->GetDirectBufferAddress(pntr);
	delete(f5);
}
