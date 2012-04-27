#include <opencv/cv.h>
#include <math.h>
#include <functions.hpp>
#ifdef DEBUG
#include <opencv/cv.h>
#include <opencv/highgui.h>
#endif

using namespace cv;

/// Finding the petri-film circle
// Calculate the circularity of a contour
double calcCircularity(vector<Point> contour) {
  // Check circularity
  double perimeter = arcLength(contour, true);
  double area = contourArea(contour);
  double circularity = 4 * PI * area / (perimeter * perimeter);
  return circularity;
}

void findCircle(Mat& greenPlane, vector<Point>& circle) {  
  int width = greenPlane.cols;
  int height = greenPlane.rows;
  
  Mat center = greenPlane( Range(height * 0.4, height * 0.6), 
                           Range(width * 0.4, width * 0.6));
  
  // Get threshold for circle
  double threshval = mean(center)[0] * 0.75;
  
  // Threshold
  Mat thresh;
  threshold(greenPlane, thresh, threshval, 255, THRESH_BINARY);
  
  Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(3, 3));
  morphologyEx(thresh, thresh, MORPH_CLOSE, kernel);
  
  vector<vector<Point> > contours;
  findContours(thresh, contours, CV_RETR_EXTERNAL, CHAIN_APPROX_NONE);
  
  // Find contour that encompasses cross-hairs and has big area
#ifdef DEBUG
  printf("Total contours found (when searching for the petri-film circle): %d\n", contours.size());
#endif
  for (int i = 0; i < contours.size(); i++) {
    Rect rect = boundingRect(contours[i]);
    
    if (!rect.contains(Point(width / 2, height / 2)))
      continue;
    
    if (rect.width < width * 0.25 || rect.height < height * 0.25)
      continue;
    
    // Check that center is inside
      if (pointPolygonTest(contours[i], Point(width / 2, height / 2), false) <= 0)
        continue;
      
      // Make contour convex
        vector<Point> convex;
        convexHull(contours[i], convex);

        // Check circularity
        double circularity = calcCircularity(convex);
        
        if (circularity < 0.9)
          continue;
        
        circle = convex;
#ifdef DEBUG
  printf("Circularity of contour considered as petri-film: %.2f\n", circularity);
#endif
        return;
  }
}



/// Get the petri-film region and circle mask
void getMask(Mat& src, vector<Point>& circle, Mat& petri, Mat& mask) {
    // Extract petri region
    Rect petriRect = boundingRect(circle);
    petriRect.x -= PETRI_MARGIN;
    petriRect.y -= PETRI_MARGIN;
    petriRect.width += 2 * PETRI_MARGIN;
    petriRect.height += 2 * PETRI_MARGIN;
#ifdef DEBUG
  printf("Contour rect of petri (%d,%d,%d,%d)\n", petriRect.x, petriRect.y,
         petriRect.width, petriRect.height);
#endif

    // Create petri mask
    Mat bigMask = Mat(src.size(), CV_8UC3, Scalar(0));
    fillConvexPoly(bigMask, circle, Scalar(255,255,255));
#ifdef DEBUG
    namedWindow("Big Mask", CV_WINDOW_KEEPRATIO);
    imshow("Big Mask", bigMask );
#endif

    //mask = bigMask(petriRect).clone();
    // Erode mask to remove edge effects
    Mat kernel = getStructuringElement(MORPH_ELLIPSE, Size(PETRI_EROSION_KERNEL_SIZE, PETRI_EROSION_KERNEL_SIZE));
    erode(bigMask(petriRect), mask, kernel);

    petri = Mat(src, petriRect);
}



/// Compute histograms and return color at the peak
Scalar computeHistograms(vector<Mat> rgbPlanes, Mat mask, vector<Mat>& hist) {
    /// Compute the histograms
    int histSize = HIST_BIN_COUNT; /// Establish the number of bins
    float range[] = { 0, 255 } ; /// Set the ranges ( for R,G,B) )
    const float* histRange = { range };
    bool uniform = true; bool accumulate = false;
    hist.resize(3);
    calcHist( &rgbPlanes[0], 1, 0, mask, hist[0], 1, &histSize, &histRange, uniform, accumulate );
    calcHist( &rgbPlanes[1], 1, 0, mask, hist[1], 1, &histSize, &histRange, uniform, accumulate );
    calcHist( &rgbPlanes[2], 1, 0, mask, hist[2], 1, &histSize, &histRange, uniform, accumulate );

    Point maxB, maxG, maxR;

#ifdef DEBUG
    double maxBcount,maxGcount,maxRcount;
    minMaxLoc( hist[0], NULL, &maxBcount, NULL, &maxB);
    minMaxLoc( hist[1], NULL, &maxGcount, NULL, &maxG);
    minMaxLoc( hist[2], NULL, &maxRcount, NULL, &maxR);
    printf("Background color (RGB): %d, %d, %d\n",
                               cvRound(maxR.y * 255 / histSize ),
                               cvRound(maxG.y * 255 / histSize ),
                               cvRound(maxB.y * 255 / histSize ) );
    // Draw the histograms for R, G and B
    int hist_w = 600; int hist_h = 600;
    int bin_w = cvRound( (double) hist_w/histSize );
    Mat histImage( hist_h+20, hist_w, CV_8UC3, Scalar(0,0,0) );
    float Bbg = -1, By = -1;
    for( int i = 1; i < histSize; i++ ) {
       line( histImage, Point( bin_w*(i-1), 10 + hist_h - cvRound(hist[2].at<float>(i-1)*hist_h/maxRcount) ) ,
                        Point( bin_w*(i), 10 + hist_h - cvRound(hist[2].at<float>(i)*hist_h/maxRcount) ),
                        Scalar( 0, 0, 255), 2, 8, 0  );
       line( histImage, Point( bin_w*(i-1), 10 + hist_h - cvRound(hist[1].at<float>(i-1)*hist_h/maxGcount) ) ,
                        Point( bin_w*(i), 10 + hist_h - cvRound(hist[1].at<float>(i)*hist_h/maxGcount) ),
                        Scalar( 0, 255, 0), 2, 8, 0  );
       line( histImage, Point( bin_w*(i-1), 10 + hist_h - cvRound(hist[0].at<float>(i-1)*hist_h/maxBcount) ) ,
                        Point( bin_w*(i), 10 + hist_h - cvRound(hist[0].at<float>(i)*hist_h/maxBcount) ),
                        Scalar( 255, 0, 0), 2, 8, 0  );
    }
    namedWindow("calcHist Demo", CV_WINDOW_AUTOSIZE );
    imshow("calcHist Demo", histImage );

#else
    minMaxLoc( hist[0], NULL, NULL, NULL, &maxB);
    minMaxLoc( hist[1], NULL, NULL, NULL, &maxG);
    minMaxLoc( hist[2], NULL, NULL, NULL, &maxR);
#endif
    //FIXME Maybe we should round it to int
    /* return Scalar(cvRound(maxB.y * 255 / histSize ),
                  cvRound(maxG.y * 255 / histSize ),
                  cvRound(maxR.y * 255 / histSize ) );
                  */
    return Scalar(maxB.y, maxG.y, maxR.y );
}
