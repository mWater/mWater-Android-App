//#include <stdarg.h>

#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include <android/log.h>
//#include <android/bitmap.h>

#include "imagefuncs.h"

#define APPNAME "co.mwater.clientapp"

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


Mat process(Mat input, int& ecoli, int& tc, int& other) {
    return findColonies(input, ecoli, tc, other, 0);
}

extern "C" {
JNIEXPORT void JNICALL Java_co_mwater_clientapp_ui_petrifilm_PetrifilmCameraView_Process(
        JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv,
        jintArray processedRGBA,  jobject results) {

    jsize yuvLength = env->GetArrayLength(yuv);
    jsize processedLength = env->GetArrayLength(processedRGBA);

    // AR
    jboolean isCopy;

    // Get input and output arrays
    jbyte* pyuv = env->GetByteArrayElements(yuv, &isCopy);
    jint* pprocessedRGBA = env->GetIntArrayElements(processedRGBA, &isCopy);

    //if (AndroidBitmap_lockPixels(env, bitmap, &pixels) >= 0) {
    Mat myuv(height + height / 2, width, CV_8UC1, (unsigned char *) pyuv);
    Mat mbgra(height, width, CV_8UC4, (unsigned char *) pprocessedRGBA);

    // Please pay attention to BGRA byte order
    // ARGB stored in java as int array becomes BGRA at native level
    cvtColor(myuv, mbgra, CV_YUV420sp2BGR, 4);

    // don't need pyuv anymore, release the pin
    env->ReleaseByteArrayElements(yuv, pyuv, 0);

    int foundCircle;
    createPreview(mbgra, foundCircle);

    // Set results
    jclass resultsClass = env->GetObjectClass(results);
    jfieldID foundCircleField = env->GetFieldID(resultsClass, "foundCircle", "Z");
    env->SetBooleanField(results, foundCircleField, foundCircle ? JNI_TRUE : JNI_FALSE);

    //cvtColor(mbgra, mbgra, CV_BGRA2RGBA); // AR - not required anymore

    //	AndroidBitmap_unlockPixels(env, bitmap);
    //}
    env->ReleaseIntArrayElements(processedRGBA, pprocessedRGBA, 0);

}

JNIEXPORT void JNICALL Java_co_mwater_clientapp_petrifilmanalysis_PetrifilmImageProcessor_process(
        JNIEnv* env, jobject thiz, jbyteArray jpeg, jobject results) {
    jbyte* _jpeg = env->GetByteArrayElements(jpeg, 0);

    // Open jpeg
    Mat jpegdata = Mat(Size(1, env->GetArrayLength(jpeg)), CV_8UC1, _jpeg);
    Mat input = imdecode(jpegdata, 1);

    // Process image
    int ecoli = 0, tc = 0, other = 0;
    Mat processed = process(input, ecoli, tc, other);

    // Encode jpeg
    vector<uchar> encoded;
    imencode(".jpg", processed, encoded);

    jclass resultsClass = env->GetObjectClass(results);

    jfieldID ecoliField = env->GetFieldID(resultsClass, "ecoli", "I");
    env->SetIntField(results, ecoliField, ecoli);

    jfieldID tcField = env->GetFieldID(resultsClass, "tc", "I");
    env->SetIntField(results, tcField, tc);

    jfieldID otherField = env->GetFieldID(resultsClass, "other", "I");
    env->SetIntField(results, otherField, other);

    jfieldID jpegField = env->GetFieldID(resultsClass, "jpeg", "[B");
    jbyteArray jpegarr = env->NewByteArray(encoded.size());
    env->SetByteArrayRegion(jpegarr, 0, encoded.size(),
            (const signed char*) (&encoded[0]));
    env->SetObjectField(results, jpegField, jpegarr);
    env->DeleteLocalRef(jpegarr);
}

}

