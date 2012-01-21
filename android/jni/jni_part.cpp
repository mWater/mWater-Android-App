#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <vector>

#include <android/log.h>

#define APPNAME "ca.ilanguage.rhok.imageupload"

#define OUTPUT_PIPELINE 1

using namespace std;
using namespace cv;

#define PETRI_FILM_MARGIN 10
#define PETRI_FILM_USABLE_PORTION 0.95
#define HIST_BIN_COUNT 50

void debugimage(const char* name, Mat& image)
{
	// put imwrite here to see debugging output of pipeline
}

double calcCircularity(vector<Point> contour) {
	// Check circularity
	double perimeter = arcLength(contour, true);
	double area = contourArea(contour);
	double circularity = 4 * 3.14159265 * area / (perimeter * perimeter);
	return circularity;
}

vector<Point> findCircle(Mat& mbgra) {
	int width = mbgra.size[1];
	int height = mbgra.size[0];

	// Separate the image in 3 places ( B, G and R )
	vector<Mat> rgbPlanes;
	split(mbgra, rgbPlanes);

	// Get center rectangle
	Mat green = rgbPlanes[1];
	Mat center = green(Range(height * 0.4, height * 0.6),
			Range(width * 0.4, width * 0.6));

	// Get threshold for circle
	double threshval = mean(center)[0] * 0.75;

	// Threshold
	Mat thresh;
	threshold(green, thresh, threshval, 255, THRESH_BINARY);

	// Close
	debugimage("thresh.png", thresh); //###
	debugimage("green.png", green); //###
	debugimage("center.png", center); //###

	Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(3, 3));
	morphologyEx(thresh, thresh, MORPH_CLOSE, kernel);

	vector<vector<Point> > contours;
	findContours(thresh, contours, CV_RETR_EXTERNAL, CHAIN_APPROX_NONE);

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "%d contours", contours.size());

	// Find contour that encompasses cross-hairs and has big area
	for (int i = 0; i < contours.size(); i++) {
		Rect rect = boundingRect(contours[i]);

		if (!rect.contains(Point(width / 2, height / 2)))
			continue;

		if (rect.width < width * 0.25 || rect.height < height * 0.25)
			continue;

		// Check that center is inside
		if (pointPolygonTest(contours[i], Point(width / 2, height / 2), false)
				<= 0)
			continue;

		// Make contour convex
		vector<Point> convex;
		convexHull(contours[i], convex);

		// Check circularity
		double circularity = calcCircularity(convex);

		__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Circularity %f", circularity);

		if (circularity < 0.9)
			continue;

		return convex;
	}
	return vector<Point>(0);
}

void createPreview(Mat mbgra) {
	int width = mbgra.size[1];
	int height = mbgra.size[0];

	vector<Point> contour = findCircle(mbgra);
	if (contour.size() == 0)
		return;

	// Draw contour
	vector<vector<Point> > hulls;
	hulls.push_back(contour);

	// Check circularity
	double circularity = calcCircularity(contour);

	if (circularity < 0.995)
		drawContours(mbgra, hulls, 0, Scalar(0, 0, 255, 255), 2);
	else
		drawContours(mbgra, hulls, 0, Scalar(0, 255, 0, 255), 2);

	// Draw cross-hairs
	line(mbgra, Point(width * 0.5, height * 0.4),
			Point(width * 0.5, height * 0.6), Scalar(0, 0, 0, 255), 2);
	line(mbgra, Point(width * 0.4, height * 0.5),
			Point(width * 0.6, height * 0.5), Scalar(0, 0, 0, 255), 2);

}

Mat findColonies(Mat& mbgr, int& colonies) {
	int width = mbgr.size[1];
	int height = mbgr.size[0];

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Find colonies on %d, %d",
			width, height);

	vector<Point> contour = findCircle(mbgr);
	if (contour.size() == 0)
		return Mat(100, 100, CV_8UC3, Scalar(0, 0, 255));

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "Contour with %d points",
			contour.size());

	// Extract petri region
	Rect petriRect = boundingRect(contour);
	Mat petri = mbgr(petriRect);

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
			"Contour rect (%d,%d,%d,%d)", petriRect.x, petriRect.y,
			petriRect.width, petriRect.height);

	// Create overall mask
	Mat maskBig = Mat(mbgr.size(), CV_8UC1, Scalar(0));
	fillConvexPoly(maskBig, contour, Scalar(255));

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "fillConvexPoly");

	// Create petri mask
	Mat mask = maskBig(petriRect);

	// Erode mask to remove edge effects
	Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(21, 21));
	erode(mask, mask, kernel);

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "resized mask (%d,%d)", mask.size[1], mask.size[0]);

	Mat mask3C = Mat(petri.size(), CV_8UC3, Scalar(0, 0, 0));
	mask3C.setTo(Scalar(255, 255, 255), mask);

	debugimage("mask3C.png", mask3C); //###

	// Create low-pass filter, only within mask
	Mat blurred;
	Mat blurredCount;
	int blursize = (petri.rows / 12) * 2 + 1;
	boxFilter(petri & mask3C, blurred, CV_32FC3, Size(blursize, blursize),
			Point(-1, -1), false, BORDER_CONSTANT);

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "boxFilter #1");

	boxFilter(mask3C, blurredCount, CV_32FC3, Size(blursize, blursize),
			Point(-1, -1), false, BORDER_CONSTANT);

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "boxFilter #2");
	blurred = blurred / (blurredCount / 255);

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "blurred");

	// High-pass image
	Mat highpass;
	petri.convertTo(highpass, CV_32FC3);
	highpass = highpass / blurred * 200;

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "highpassed");

	// Convert back to 8-bit
	Mat highpass8;
	highpass.convertTo(highpass8, CV_8UC3);

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "8-bitted");

	debugimage("highpass8premask.png", highpass8); //###

	// Mask outside of circle to background
	highpass8.setTo(Scalar(200, 200, 200), 255 - mask);

	debugimage("highpass8postmask.png", highpass8); //###

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME, "reset outside mask");

	// Remove noise
	blur(highpass8, highpass8, Size(3,3));

	debugimage("highpass8postblur.png", highpass8); //###

	// Split into channels
	vector<Mat> rgbPlanes;
	split(highpass8, rgbPlanes);

	// Find colonies
	Mat colthresh;
	threshold(rgbPlanes[1], colthresh, 150, 200, CV_THRESH_BINARY_INV);

	debugimage("colthresh.png", colthresh); //###

//	// Remove single pixels
//	kernel = getStructuringElement(MORPH_ELLIPSE, Size(3, 3));
//	erode(colthresh, colthresh, kernel);

	int mingap = petri.rows / 100;

	// Encompass entire colonies
	kernel = getStructuringElement(MORPH_ELLIPSE, Size(mingap, mingap));
	dilate(colthresh, colthresh, kernel);

	debugimage("colthreshdilated.png", colthresh); //###

	vector<vector<Point> > contours;
	findContours(colthresh, contours, CV_RETR_LIST, CV_CHAIN_APPROX_NONE);

	__android_log_print(ANDROID_LOG_VERBOSE, APPNAME,
			"Colony contours: %d", contours.size());

	colonies = 0;
	for (int i = 0; i < contours.size(); i++) {
		Rect rect = boundingRect(contours[i]);

		int neighsize = petri.rows / 50;

//		// Ignore if too large
//		if (rect.height > neighsize*2 || rect.width > neighsize*2)
//			continue;

		Point center = Point(rect.x + rect.width / 2, rect.y + rect.height / 2);

		// Draw rectangle
		rectangle(
				highpass8,
				Rect(rect.x - neighsize, rect.y - neighsize,
						rect.width + neighsize * 2,
						rect.height + neighsize * 2), Scalar(0, 0, 255, 255), 2);

		colonies++;
//		// Draw rectangle
//		rectangle(
//				highpass8,
//				Rect(rect.x, rect.y,
//						rect.width,
//						rect.height), Scalar(0, 0, 255, 255), 3);
	}

	debugimage("highpass8.png", highpass8); //###

	return highpass8;
}

Mat process(Mat input, int& colonies) {
	return findColonies(input, colonies);
}

extern "C" {
JNIEXPORT void JNICALL Java_ca_ilanguage_rhok_imageupload_ui_PetrifilmSnapPreviewView_Process(
		JNIEnv* env, jobject thiz, jint width, jint height, jbyteArray yuv,
		jintArray bgra) {
	// Get input and output arrays
	jbyte* _yuv = env->GetByteArrayElements(yuv, 0);
	jint* _bgra = env->GetIntArrayElements(bgra, 0);

	Mat myuv(height + height / 2, width, CV_8UC1, (unsigned char *) _yuv);
	Mat mbgra(height, width, CV_8UC4, (unsigned char *) _bgra);

	// Please pay attention to BGRA byte order
	// ARGB stored in java as int array becomes BGRA at native level
	cvtColor(myuv, mbgra, CV_YUV420sp2BGR, 4);

	createPreview(mbgra);

	env->ReleaseIntArrayElements(bgra, _bgra, 0);
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

