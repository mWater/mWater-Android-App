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
	// Get input and output arrays
    jbyte* _yuv  = env->GetByteArrayElements(yuv, 0);
    jint*  _bgra = env->GetIntArrayElements(bgra, 0);

    Mat myuv(height + height/2, width, CV_8UC1, (unsigned char *)_yuv);
    Mat mbgra(height, width, CV_8UC4, (unsigned char *)_bgra);

    // Please pay attention to BGRA byte order
    // ARGB stored in java as int array becomes BGRA at native level
    cvtColor(myuv, mbgra, CV_YUV420sp2BGR, 4);
    
    // Separate the image in 3 places ( B, G and R )
    vector<Mat> rgbPlanes;
    split(mbgra, rgbPlanes);

    // Find petri-film circle
    vector<Vec3f> circles;
    int maxRad = ((mbgra.rows<mbgra.cols)?mbgra.rows:mbgra.cols) /2;
    int minRad = maxRad/2;

    // Create scratch 8-bit mat
    Mat scratch(height, width, CV_8UC1);

	blur(rgbPlanes[1], scratch, Size(3,3));
	HoughCircles(scratch, circles, CV_HOUGH_GRADIENT, 1, minRad/2, 100, 50, minRad, maxRad );

	if (circles.size()>0)
	{
		int centerX = cvRound(circles[0][0]);
	    int centerY = cvRound(circles[0][1]);
	    int rad = cvRound(circles[0][2]);

	    // HACK: Reduce radius because of HoughCircles imprecision!
	    rad*=0.95;

	    // Draw red circle
	    circle(mbgra, Point(centerX, centerY), rad, Scalar(0,0,255,255));

	    if (centerX+rad<width && centerX-rad>0 && centerY+rad<height && centerY-rad>0)
	    {
			// Extract squared circle
			Mat square = mbgra(Range(centerY-rad, centerY+rad), Range(centerX-rad, centerX+rad));

			// Create mask
			Mat mask = Mat(rad*2, rad*2, CV_8UC1, Scalar(0));
			circle(mask, Point(rad, rad), rad, Scalar(255), -1);

			Mat mask4C = Mat(rad*2, rad*2, CV_8UC4, Scalar(0,0,0,255));
			circle(mask4C, Point(rad, rad), rad, Scalar(255,255,255,255), -1);

//			// Copy square to signed 32
//			Mat squareCopy;
//			square.convertTo(squareCopy, CV_32SC4);
//
//			// Mask circle
//			squareCopy |= mask;

			// Create low-pass filter, only within mask
			Mat blurred;
			Mat blurredCount;
			int blursize=square.rows/8;
			boxFilter(square & mask4C, blurred, CV_32FC4, Size(blursize,blursize), Point(-1,-1), false, BORDER_CONSTANT);
			boxFilter(mask4C, blurredCount, CV_32FC4, Size(blursize,blursize), Point(-1,-1), false, BORDER_CONSTANT);
			blurred = blurred / (blurredCount/255);

			// High-pass image
			Mat highpass;
			square.convertTo(highpass, CV_32FC4);
			highpass = highpass / blurred * 200;

			// Convert back to 8-bit, resetting alpha
			Mat highpass8;
			highpass.convertTo(highpass8, CV_8UC4);
			highpass8 |= Mat(rad*2, rad*2, CV_8UC4, Scalar(0,0,0,255));

			// Mask outside of circle to background
			highpass8.setTo(Scalar(200,200,200,255), 255-mask);

			// Split into channels
		    split(highpass8, rgbPlanes);

		    // Find colonies
		    Mat colthresh;
		    threshold(rgbPlanes[1], colthresh, 160, 200, CV_THRESH_BINARY_INV); // Should be 150 or so

		    vector<vector<Point> > contours;
		    //vector<Vec4i> hierarchy;
		    findContours(colthresh, contours, CV_RETR_LIST, CHAIN_APPROX_NONE);
		    for (int i=0; i<contours.size(); i++)
		    {
		        Rect rect=boundingRect(contours[i]);

		        int neighsize=square.rows/50;
		        // Ignore if too large
		        if (rect.height>neighsize || rect.width>neighsize)
		        	continue;

		        Point center = Point(rect.x+rect.width/2, rect.y+rect.height/2);

		        // Draw rectangle
			    rectangle(highpass8,
			    		Rect(rect.x-neighsize, rect.y-neighsize, rect.width+neighsize*2, rect.height+neighsize*2),
			    		Scalar(0,0,255,255));

//		        // Draw green arrow
//		        line(highpass8, center, Point(center.x+30, center.y+30), Scalar(0,255,0,255));
//		        line(highpass8, center, Point(center.x+10, center.y), Scalar(0,255,0,255));
//		        line(highpass8, center, Point(center.x, center.y+10), Scalar(0,255,0,255));
		    }

			highpass8.copyTo(square, mask);
	    }
	}


//    double param = 100;
//    do {
//    	blur(rgbPlanes[1], scratch, Size(3,3));
//    	HoughCircles(scratch, circles, CV_HOUGH_GRADIENT, 1, minRad/2, param, param*0.5, minRad, maxRad );
//    	param *= 0.9;
//    } while(circles.size()<1);


/*    // Create mat for edges
    Mat edges(height, width, CV_8UC1);
    Canny(rgbPlanes[1], edges, 5, 20);

    mbgra.setTo(Scalar(255,0,0,255), edges);
*/
    env->ReleaseIntArrayElements(bgra, _bgra, 0);
    env->ReleaseByteArrayElements(yuv, _yuv, 0);
}

}
