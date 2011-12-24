#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

using namespace std;
using namespace cv;


#define PETRI_FILM_MARGIN 10
#define PETRI_FILM_USABLE_PORTION 0.95
#define HIST_BIN_COUNT 50


extern "C" {
JNIEXPORT void JNICALL Java_ca_ilanguage_rhok_imageupload_ui_Sample3View_FindFeatures(JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv, jintArray bgra)
{
    jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
    jint*  _bgra = env->GetIntArrayElements(bgra, 0);

    Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
    Mat mbgra(height, width, CV_8UC4, (unsigned char *)_bgra);
    Mat mgray(height, width, CV_8UC1, (unsigned char *)_yuv);

    //Please make attention about BGRA byte order
    //ARGB stored in java as int array becomes BGRA at native level
    cvtColor(myuv, mbgra, CV_YUV420sp2BGR, 4);
    
    /// Separate the image in 3 places ( B, G and R )
    vector<Mat> rgbPlanes;
    split( mbgra, rgbPlanes );
    /// Finding the petri-film circle
    vector<Vec3f> circles;
    int maxRad = ((mbgra.rows<mbgra.cols)?mbgra.rows:mbgra.cols) /2;
    int minRad = maxRad /4;
    
    double param = 250;
    do {
      HoughCircles( rgbPlanes[1], circles, CV_HOUGH_GRADIENT, 2, minRad/2, param, param*0.8, minRad, maxRad );
      param *= 0.9;
    } while(circles.size()<1);
    int centerX = cvRound(circles[0][0]);
    int centerY = cvRound(circles[0][1]);
    int rad = cvRound(circles[0][2]);
    
    circle(mbgra, Point(centerX, centerY), rad, Scalar(0,255,255,255));
    
    env->ReleaseIntArrayElements(bgra, _bgra, 0);
    env->ReleaseByteArrayElements(yuv, _yuv, 0);
}

}
