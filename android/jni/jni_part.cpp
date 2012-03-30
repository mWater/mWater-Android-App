#include <stdarg.h>

#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include <android/log.h>
#include <android/bitmap.h>

#include "imagefuncs.h"

#define APPNAME "ca.ilanguage.rhok.imageupload"

#define OUTPUT_PIPELINE 1

using namespace cv;
using namespace std;

#define PETRI_FILM_MARGIN 10
#define PETRI_FILM_USABLE_PORTION 0.95
#define HIST_BIN_COUNT 50

//void debugimage(const char* name, Mat& image)
//{
//	// put imwrite here to see debugging output of pipeline
//}


Mat process(Mat input, int& colonies) {
	return findColonies(input, colonies);
}

extern "C" {
JNIEXPORT void JNICALL Java_ca_ilanguage_rhok_imageupload_ui_PetrifilmCameraView_Process(
		JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv,
		jobject bitmap) {
	// Get input and output arrays
	jbyte* _yuv = env->GetByteArrayElements(yuv, 0);

	void* pixels;
	if (AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0) {
		Mat myuv(height + height / 2, width, CV_8UC1, (unsigned char *) _yuv);
		Mat mbgra(height, width, CV_8UC4, (unsigned char *) pixels);

		// Please pay attention to BGRA byte order
		// ARGB stored in java as int array becomes BGRA at native level
		cvtColor(myuv, mbgra, CV_YUV420sp2BGR, 4);

		createPreview(mbgra);

		cvtColor(mbgra, mbgra, CV_BGRA2RGBA);

		AndroidBitmap_unlockPixels(env, bitmap);
	}

	env->ReleaseByteArrayElements(yuv, _yuv, 0);
}

JNIEXPORT void JNICALL Java_ca_ilanguage_rhok_imageupload_PetrifilmImageProcessor_process(
		JNIEnv* env, jobject thiz, jbyteArray jpeg, jobject results) {
	jbyte* _jpeg = env->GetByteArrayElements(jpeg, 0);

	// Open jpeg
	Mat jpegdata = Mat(Size(1, env->GetArrayLength(jpeg)), CV_8UC1, _jpeg);
	Mat input = imdecode(jpegdata, 1);

	// Process image
	int colonies = 0;
	Mat processed = process(input, colonies);

	// Encode jpeg
	vector<uchar> encoded;
	imencode(".jpg", processed, encoded);

	jclass resultsClass = env->GetObjectClass(results);

	jfieldID coloniesField = env->GetFieldID(resultsClass, "colonies", "I");
	env->SetIntField(results, coloniesField, colonies);

	jfieldID jpegField = env->GetFieldID(resultsClass, "jpeg", "[B");
	jbyteArray jpegarr = env->NewByteArray(encoded.size());
	env->SetByteArrayRegion(jpegarr, 0, encoded.size(),
			(const signed char*) (&encoded[0]));
	env->SetObjectField(results, jpegField, jpegarr);
	env->DeleteLocalRef(jpegarr);
}

}

