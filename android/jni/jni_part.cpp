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

void FindFeatures(jint width, jint height, Mat& mbgra)
{
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
}

void FindCircle(jint width, jint height, Mat& mbgra) {
	// Separate the image in 3 places ( B, G and R )
	vector<Mat> rgbPlanes;
	split(mbgra, rgbPlanes);

	// Get center rectangle
	Mat green = rgbPlanes[1];
	Mat center = green(Range(width * 0.4, width * 0.6),
			Range(width * 0.4, width * 0.6));

	// Get threshold for circle
	double threshval=mean(center)[0]*0.75;

	// Threshold
	Mat thresh;
	threshold(green, thresh, threshval, 255, THRESH_BINARY);

	// Close
	Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(3,3));
	morphologyEx(thresh, thresh, MORPH_CLOSE, kernel);

	vector<vector<Point> > contours;
    findContours(thresh, contours, CV_RETR_EXTERNAL, CHAIN_APPROX_NONE);

    // Find contour that encompasses crosshairs and has big area
    for (int i=0; i<contours.size(); i++)
    {
        Rect rect=boundingRect(contours[i]);

        if (!rect.contains(Point(width/2, height/2)))
        	continue;

        if (rect.width<width*0.25 || rect.height<height*0.25)
        	continue;

        // Check that center is inside
        if (pointPolygonTest(contours[i], Point(width/2, height/2), false)<=0)
        	continue;

        // Convexify contour
        vector<Point> convex;
        convexHull(contours[i], convex);

        vector<vector<Point> > hulls;
        hulls.push_back(convex);

        // Check circularity
        double perimeter=arcLength(convex, true);
        double area=contourArea(convex);
        double circularity = 4*3.14159*area/(perimeter*perimeter);

//        // Not quite as neat and simple:
//        ostringstream temp;
//        temp << circularity;
//        putText(mbgra, temp.str(), Point(width/2,height/2), FONT_HERSHEY_SIMPLEX, 0.3, Scalar(0,255,0,255));

        if (circularity<0.995)
            drawContours(mbgra, hulls, 0, Scalar(0, 0, 255,255), 1);
        else
            drawContours(mbgra, hulls, 0, Scalar(0,255,0,255), 1);

        //polylines(mbgra, convex, true, Scalar(0,255,0,255), 2);
        break;
    }

//	// Draw in light green
//	Mat areamat;
//	mbgra.copyTo(areamat);
//	areamat.setTo(Scalar(0,255,0,255), thresh);
//	areamat = areamat * 0.5 + mbgra * 0.5;
//	areamat.copyTo(mbgra, thresh);

	// Draw crosshairs
	line(mbgra, Point(width*0.5, height*0.4), Point(width*0.5, height*0.6),
			Scalar(0,0,0,255), 2);
	line(mbgra, Point(width*0.4, height*0.5), Point(width*0.6, height*0.5),
			Scalar(0,0,0,255), 2);
//
//	elem=cv2.getStructuringElement(cv2.MORPH_ELLIPSE, (10,10))
//	#cv2.dilate(petri, e
//	petri2 = cv2.morphologyEx(petri, cv2.MORPH_CLOSE, elem, iterations=2)
//	cv2.imwrite('petri2.png', petri2)
//
//	cont=cv2.findContours(petri2.astype(numpy.uint8), cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_NONE)[0]
//
}



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

    //FindFeatures(width, height, mbgra);
    FindCircle(width, height, mbgra);

    env->ReleaseIntArrayElements(bgra, _bgra, 0);
    env->ReleaseByteArrayElements(yuv, _yuv, 0);
}

}



