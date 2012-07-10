#include "imagefuncs.h"

#define LOG_TAG "co.mwater.clientapp"
#ifdef ANDROID
#include <android/log.h>
#  define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)
#  define LOGE(...) __android_log_print(ANDROID_LOG_ERROR, LOG_TAG, __VA_ARGS__)
#else
#include <stdio.h>
#  define QUOTEME_(x) #x
#  define QUOTEME(x) QUOTEME_(x)
#  define LOGI(...) printf("I/" LOG_TAG " (" __FILE__ ":" QUOTEME(__LINE__) "): " __VA_ARGS__)
#  define LOGE(...) printf("E/" LOG_TAG "(" ")" __VA_ARGS__)
#endif

using namespace cv;

void createPreview(Mat mbgra, int& foundCircle) {
	int width = mbgra.size[1];
	int height = mbgra.size[0];

	foundCircle = 0;

	vector<Point> contour = findCircle(mbgra);
	if (contour.size() > 0) {
		// Draw contour
		vector<vector<Point> > hulls;
		hulls.push_back(contour);

		// Check circularity
		double circularity = calcCircularity(contour);

		if (circularity < 0.99) {
			drawContours(mbgra, hulls, 0, Scalar(0, 0, 255, 255), 2);
		} else
		{
			foundCircle = 1;
			drawContours(mbgra, hulls, 0, Scalar(0, 255, 0, 255), 2);
		}
	}

	// Draw cross-hairs
	line(mbgra, Point(width * 0.5, height * 0.45),
			Point(width * 0.5, height * 0.55), Scalar(0, 0, 0, 255), 2);
	line(mbgra, Point(width * 0.45, height * 0.5),
			Point(width * 0.55, height * 0.5), Scalar(0, 0, 0, 255), 2);
}

/* Calculate the circularity of a contour */
double calcCircularity(vector<Point> contour) {
	// Check circularity
	double perimeter = arcLength(contour, true);
	double area = contourArea(contour);
	double circularity = 4 * 3.14159265 * area / (perimeter * perimeter);
	return circularity;
}

/* Finds the circle in a region */
vector<Point> findCircle(Mat& mbgra) {
	int width = mbgra.size[1];
	int height = mbgra.size[0];

	// Separate the image in 3 places ( B, G and R )
	vector<Mat> rgbPlanes;
	split(mbgra, rgbPlanes);

	// Get center rectangle
	Mat green = rgbPlanes[1];
	Mat center = green(Range(height * 0.45, height * 0.55),
			Range(width * 0.45, width * 0.55));

	// Get threshold for circle
	double threshval = mean(center)[0] * 0.75;

	// Threshold
	Mat thresh;
	threshold(green, thresh, threshval, 255, THRESH_BINARY);

	Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(3, 3));
	morphologyEx(thresh, thresh, MORPH_CLOSE, kernel);

	vector<vector<Point> > contours;
	findContours(thresh, contours, CV_RETR_EXTERNAL, CHAIN_APPROX_NONE);

	// Find contour that encompasses cross-hairs and has big area
	for (int i = 0; i < contours.size(); i++) {
		Rect rect = boundingRect(contours[i]);

		if (!rect.contains(Point(width / 2, height / 2)))
			continue;

		if (rect.width < width * 0.2 || rect.height < height * 0.2)
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

		//###if (circularity < 0.85)
		//###	continue;

		return convex;
	}
	return vector<Point>(0);
}

/* Highpass of the image 
 image is a 3-channel 8-bit image
 mask3C is a 3-channel mask with 255 for yes, 0 for no.
 blursize is size of box filter applied
 returns float image scaled around 1.0
 */
Mat highpass(Mat& image, Mat& mask3C, int blursize) {
	// Create low-pass filter, only within mask
	Mat blurred;
	Mat blurredCount;
	boxFilter(image & mask3C, blurred, CV_32FC3, Size(blursize, blursize),
			Point(-1, -1), false, BORDER_CONSTANT);

	boxFilter(mask3C, blurredCount, CV_32FC3, Size(blursize, blursize),
			Point(-1, -1), false, BORDER_CONSTANT);

	Mat highpass;
	image.convertTo(highpass, CV_32FC3);

	Mat lowpass = blurred / blurredCount;

	highpass /= lowpass;

	return highpass / 255;
}

/* Removes yellow lines from the image by minimizing the edges when 
 calculating green*(1+lambda)-blue*lambda.
 */
void removeyellow(Mat& img) {
	vector<Mat> bgr;
	split(img, bgr);

	Mat g = bgr[1] - 1.0;
	Mat b = bgr[0] - 1.0;

	double lambda = mean(g.mul(b - g))[0]
			/ mean(g.mul(g) - 2 * g.mul(b) + b.mul(b))[0];

	LOGI("Remove yellow lambda = %f", lambda);
	// Do not remove big lambdas, as lines are not problem in that case
	if (lambda < 0.2)
		bgr[1] = bgr[1] * (1 + lambda) - bgr[0] * lambda;

	// Also reset blue
	bgr[0] = bgr[1];

	merge(bgr, img);
}

Mat findColonies(Mat& mbgr, int& colonies) {
	int width = mbgr.size[1];
	int height = mbgr.size[0];

	LOGI("Find colonies on %d, %d", width, height);

	vector<Point> contour = findCircle(mbgr);
	if (contour.size() == 0)
		return Mat(100, 100, CV_8UC3, Scalar(0, 0, 255));

	LOGI("Contour with %d points", contour.size());

	// Extract petri region
	Rect petriRect = boundingRect(contour);
	Mat petri = mbgr(petriRect);

	LOGI(
			"Contour rect (%d,%d,%d,%d)", petriRect.x, petriRect.y, petriRect.width, petriRect.height);

	// Create overall mask
	Mat maskBig = Mat(mbgr.size(), CV_8UC1, Scalar(0));
	fillConvexPoly(maskBig, contour, Scalar(255));

	LOGI("fillConvexPoly");

	// Create petri mask
	Mat mask = maskBig(petriRect);

	// Erode mask to remove edge effects
	Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(21, 21));
	erode(mask, mask, kernel);

	LOGI("resized mask (%d,%d)", mask.size[1], mask.size[0]);

	Mat mask3C = Mat(petri.size(), CV_8UC3, Scalar(0, 0, 0));
	mask3C.setTo(Scalar(255, 255, 255), mask);

	// Create low-pass filter, only within mask
	Mat blurred;
	Mat blurredCount;
	int blursize = (petri.rows / 12) * 2 + 1;
	boxFilter(petri & mask3C, blurred, CV_32FC3, Size(blursize, blursize),
			Point(-1, -1), false, BORDER_CONSTANT);

	LOGI("boxFilter #1");

	boxFilter(mask3C, blurredCount, CV_32FC3, Size(blursize, blursize),
			Point(-1, -1), false, BORDER_CONSTANT);

	LOGI("boxFilter #2");
	blurred = blurred / (blurredCount / 255);

	LOGI("blurred");

	// High-pass image
	Mat highpass;
	petri.convertTo(highpass, CV_32FC3);
	highpass = highpass / blurred * 200;

	LOGI("highpassed");

	// Convert back to 8-bit
	Mat highpass8;
	highpass.convertTo(highpass8, CV_8UC3);

	LOGI("8-bitted");

	// Mask outside of circle to background
	highpass8.setTo(Scalar(200, 200, 200), 255 - mask);

	LOGI("reset outside mask");

	// Remove yellow lines
	removeyellow(highpass8);

	// Remove noise
	blur(highpass8, highpass8, Size(3, 3));

	// Split into channels
	vector<Mat> rgbPlanes;
	split(highpass8, rgbPlanes);

	// Find colonies
	Mat colthresh;
	threshold(rgbPlanes[1], colthresh, 150, 200, CV_THRESH_BINARY_INV);

//	// Remove single pixels
//	kernel = getStructuringElement(MORPH_ELLIPSE, Size(3, 3));
//	erode(colthresh, colthresh, kernel);

	int mingap = petri.rows / 100;

	// Encompass entire colonies
	kernel = getStructuringElement(MORPH_ELLIPSE, Size(mingap, mingap));
	dilate(colthresh, colthresh, kernel);

	vector<vector<Point> > contours;
	findContours(colthresh, contours, CV_RETR_LIST, CV_CHAIN_APPROX_NONE);

	LOGI("Colony contours: %d", contours.size());

	colonies = 0;
	for (int i = 0; i < contours.size(); i++) {
		Rect rect = boundingRect(contours[i]);

		int neighsize = petri.rows / 50;

//		// Ignore if too large
//		if (rect.height > neighsize*2 || rect.width > neighsize*2)
//			continue;

		// Determine colony blueness (ratio of absorbed red to red+green absorbed)
		Mat colMat = highpass8(rect);
		Scalar s = sum(colMat);
		long redness = 200 * rect.width * rect.height - s[1];
		long blueness = 200 * rect.width * rect.height - s[2];
//		LOGI("%2d: redness=%d", i, redness);
//		LOGI("%2d: blueness=%d", i, blueness);
		float ratio = (float) blueness / (blueness + redness);
		LOGI("At %d,%d : %f", rect.x, rect.y, ratio);

		Scalar color;
		if (ratio < 0.23)
			color = Scalar(0, 0, 255, 255);
		else if (ratio < 0.27)
			color = Scalar(0, 255, 0, 255);
		else
			color = Scalar(255, 0, 0, 255);
		Point center = Point(rect.x + rect.width / 2, rect.y + rect.height / 2);

		// Draw rectangle
		rectangle(highpass8,
				Rect(rect.x - neighsize, rect.y - neighsize,
						rect.width + neighsize * 2,
						rect.height + neighsize * 2), color, 2);

		colonies++;
//		// Draw rectangle
//		rectangle(
//				highpass8,
//				Rect(rect.x, rect.y,
//						rect.width,
//						rect.height), Scalar(0, 0, 255, 255), 3);
	}

	return highpass8;
}
