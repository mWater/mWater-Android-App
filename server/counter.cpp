#include <cv.h>
#include <highgui.h>
#include <math.h>

using namespace cv;

#define SIZE 1024.0
#define PETRI_FILM_MARGIN 10
#define PETRI_FILM_USABLE_PORTION 0.95
#define HIST_BIN_COUNT 50
// Min and Max acceptable contour sizes
// Min is in pixels
#define MIN_COLONY_AREA 2
// Max is the portion of (rad^2) of petri-film circle
// which is area/Pi
#define MAX_COLONY_AREA 50

// In the histogram, how far from the bucket with highest
// number of pixels, is considered to be background:
// A gausian curve is fitted (badly!) and the distance is
// product of N_SIGMA by sigma
// Portion of the integral of the gausian funcion for some
// N_SIGMA values:
// n=1     : 0.682
// n=1.644 : 0.90
// n=2     : 0.954
// n=2.575 : 0.99
// n=3     : 0.997
// n=4     : 0.999937
#define N_SIGMA 1


#ifdef DEBUG
double findGausianSigma(Mat hist, float maxCount, int colorIndex, Mat histImage) {
#else
double findGausianSigma(Mat hist, float maxCount, int colorIndex) {
#endif
  double y,c;
  y=(hist.at<float>(colorIndex-1)+hist.at<float>(colorIndex+1)) / 2;
  c=-1/log(y/maxCount);

#ifdef DEBUG
  double color = (double) colorIndex*255/HIST_BIN_COUNT;
  int hist_h = histImage.rows;
  int hist_w = histImage.cols;
  double bin_w = (double) hist_w/HIST_BIN_COUNT;
  printf("Max in histogram (at %d) is %.1f (%.f pixels)\n\t-> Gausian function parameters: x:(%d,%d), y:%.f, c:%.3f\n", 
                                colorIndex, color, maxCount, colorIndex-1, colorIndex+1, y, c);
  for( double x = 1; x < hist_w; x++ ) {
    line( histImage, Point( x, 10 + hist_h - hist_h*exp(-(x/bin_w-colorIndex)*(x/bin_w-colorIndex)/c)) ,
                      Point( x+1, 10 + hist_h - hist_h*exp(-((x+1)/bin_w-colorIndex)*((x+1)/bin_w-colorIndex)/c)),
                      Scalar( 255, 255, 255) );
  }
  int colorThreshold = (colorIndex-N_SIGMA*sqrt(c/2))*bin_w;
  line( histImage, Point( colorThreshold, 0), Point( colorThreshold, hist_h), Scalar( 0, 255, 255) );
  colorThreshold = (colorIndex+N_SIGMA*sqrt(c/2))*bin_w;
  line( histImage, Point( colorThreshold, 0), Point( colorThreshold, hist_h), Scalar( 0, 255, 255) );
#endif
  
  return sqrt(c/2)*255/HIST_BIN_COUNT;
}

int main( int argc, char** argv )
{
  Mat img;

  /// Load image
  if( argc != 2 || !(img=imread(argv[1], 1)).data || img.channels()!=3 ) return -1;
  
  /// Resize (downsize) the image
  double scale = SIZE / ((img.rows>img.cols)?img.rows:img.cols);
  Mat small;
  resize(img, small, small.size(), scale, scale, INTER_AREA);
#ifdef DEBUG
  printf("Source image resized to %d x %d\n", small.cols, small.rows);
#endif
  /// Separate the image in 3 places ( B, G and R )
  vector<Mat> rgbPlanes;
  split( small, rgbPlanes );
  /// Finding the petri-film circle
  vector<Vec3f> circles;
  int maxRad = ((small.rows<small.cols)?small.rows:small.cols) /2;
  int minRad = maxRad /4;
#ifdef DEBUG
  printf("Petri-film circle rad should be between %d and %d\n", minRad, maxRad);
#endif
  double param = 250;
  do {
    HoughCircles( rgbPlanes[1], circles, CV_HOUGH_GRADIENT, 2, minRad/2, param, param*0.8, minRad, maxRad );
#ifdef DEBUG
    printf("\tNo. of circles found: %d\n", circles.size());
#endif
    param *= 0.9;
  } while(circles.size()<1);
  int centerX = cvRound(circles[0][0]);
  int centerY = cvRound(circles[0][1]);
  int rad = cvRound(circles[0][2]);
#ifdef DEBUG
  printf("First circle in the array: (%d, %d), %d\nLast value of HoughCircles' 'param': %f\n", 
          centerX, centerY, rad, param/0.9);
#endif

  /// Extracting petri-film circle and its RGB channels
  /// from the small image
  int newRad = rad+PETRI_FILM_MARGIN;
  int d = newRad*2;
#ifdef DEBUG
  printf("petri-film circle region of interest: (%d, %d) - (%d, %d)\n", centerX-newRad, centerY-newRad, d, d);
#endif
  Rect petriFilmROI(centerX-newRad, centerY-newRad, d, d);
  Mat petriFilm = small(petriFilmROI);
#ifdef DEBUG
  Mat petriFilmB= rgbPlanes[0](petriFilmROI);
  Mat petriFilmG= rgbPlanes[1](petriFilmROI);
#endif
  Mat petriFilmR= rgbPlanes[2](petriFilmROI);
  //FIXME Should I free rgbPlanes?
  split( petriFilm, rgbPlanes );
#ifdef DEBUG
  namedWindow("petri-film circle region of interest", CV_WINDOW_KEEPRATIO);
  imshow("petri-film circle region of interest", petriFilm );
#endif  
  Mat mask(d, d, CV_8UC1, Scalar(0, 0, 0));
  circle(mask, Point(newRad, newRad), cvRound(rad*PETRI_FILM_USABLE_PORTION), 255, -1);
#ifdef DEBUG
  printf("Usable portion of the petrifilm: %d%%\nRadius of usable portion of the petrifilm: %d\n", 
          cvRound(PETRI_FILM_USABLE_PORTION*100), cvRound(rad*PETRI_FILM_USABLE_PORTION));
  namedWindow("mask", CV_WINDOW_KEEPRATIO);
  imshow("mask", mask );
#endif
  /// Compute the histograms
  int histSize = HIST_BIN_COUNT; /// Establish the number of bins
  float range[] = { 0, 255 } ; /// Set the ranges ( for R,G,B )
  const float* histRange = { range };
  bool uniform = true; bool accumulate = false;
  Mat r_hist, g_hist, b_hist;
#ifdef DEBUG
  calcHist( &petriFilmB, 1, 0, mask, b_hist, 1, &histSize, &histRange, uniform, accumulate );
  calcHist( &petriFilmG, 1, 0, mask, g_hist, 1, &histSize, &histRange, uniform, accumulate );
#endif
  calcHist( &petriFilmR, 1, 0, mask, r_hist, 1, &histSize, &histRange, uniform, accumulate );

  double maxBcount,maxGcount,maxRcount;
  Point maxB, maxG, maxR;
  minMaxLoc( b_hist, NULL, &maxBcount, NULL, &maxB);
  minMaxLoc( g_hist, NULL, &maxGcount, NULL, &maxG);
  minMaxLoc( r_hist, NULL, &maxRcount, NULL, &maxR);
  Scalar background = Scalar(cvRound(maxB.y * 255 / histSize ),
                             cvRound(maxG.y * 255 / histSize ),
                             cvRound(maxR.y * 255 / histSize ) );
#ifdef DEBUG
  printf("Background color of petri-film circle: %d, %d, %d\n", 
                             cvRound(maxR.y * 255 / histSize ),
                             cvRound(maxG.y * 255 / histSize ),
                             cvRound(maxB.y * 255 / histSize ) );
  // Draw the histograms for R, G and B
  int hist_w = 600; int hist_h = 600;
  int bin_w = cvRound( (double) hist_w/histSize );
  Mat histImage( hist_h+20, hist_w, CV_8UC3, Scalar( 0,0,0) );
  for( int i = 1; i < histSize; i++ ) {
     line( histImage, Point( bin_w*(i-1), 10 + hist_h - cvRound(r_hist.at<float>(i-1)*hist_h/maxRcount) ) ,
                      Point( bin_w*(i), 10 + hist_h - cvRound(r_hist.at<float>(i)*hist_h/maxRcount) ),
                      Scalar( 0, 0, 255), 2, 8, 0  );
     line( histImage, Point( bin_w*(i-1), 10 + hist_h - cvRound(g_hist.at<float>(i-1)*hist_h/maxGcount) ) ,
                      Point( bin_w*(i), 10 + hist_h - cvRound(g_hist.at<float>(i)*hist_h/maxGcount) ),
                      Scalar( 0, 255, 0), 2, 8, 0  );
     line( histImage, Point( bin_w*(i-1), 10 + hist_h - cvRound(b_hist.at<float>(i-1)*hist_h/maxBcount) ) ,
                      Point( bin_w*(i), 10 + hist_h - cvRound(b_hist.at<float>(i)*hist_h/maxBcount) ),
                      Scalar( 255, 0, 0), 2, 8, 0  );
  }
  //FIXME beginning and the end of the *_hist arrays are out of bound
  assert(maxB.y>1 && maxB.y<histSize && maxG.y>1 && maxG.y<histSize);
#endif
  assert(maxR.y>1 && maxR.y<histSize);

#ifdef DEBUG
  //findGausianSigma(b_hist, maxBcount, maxB.y, histImage);
  //findGausianSigma(g_hist, maxGcount, maxG.y, histImage);
  double sigma = findGausianSigma(r_hist, maxRcount, maxR.y, histImage);
#else
  double sigma = findGausianSigma(r_hist, maxRcount, maxR.y);
#endif
  
#ifdef DEBUG
  namedWindow("calcHist Demo", CV_WINDOW_AUTOSIZE );
  imshow("calcHist Demo", histImage );
  
  printf("N-Sigma = %.3f\n", N_SIGMA*sigma);
#endif
  
  /// Clone the petri-film circle over an 
  /// area filled with its background color
  Mat petriFilmClone(d, d, petriFilm.type(), background);
  petriFilm.copyTo(petriFilmClone, mask);

#ifdef DEBUG
  namedWindow("petri-film circle clone", CV_WINDOW_KEEPRATIO);
  imshow("petri-film circle clone", petriFilmClone );
#endif

  /// Finding contours
  /// Separate the image in 3 places ( B, G and R )
  //FIXME Should I reuse rgbPlanes?
  vector<Mat> rgbPlanesClone;
  split( petriFilmClone, rgbPlanesClone );
#ifdef DEBUG
  //namedWindow("clone green channel", CV_WINDOW_KEEPRATIO);
  //imshow("clone green channel", rgbPlanesClone[1] );
  namedWindow("clone red channel", CV_WINDOW_KEEPRATIO);
  imshow("clone red channel", rgbPlanesClone[2] );
#endif
  
  vector<vector<Point> > contours;
  Mat blurR;
  GaussianBlur(rgbPlanesClone[2], blurR, Size(5,5), 0);
  Mat thresholdedRed;
  
  /// A simple thresholding for colony count estimation and
  /// estimation of adaptiveThreshold's blockSize
  int colorThreshold = maxR.y*255/HIST_BIN_COUNT-N_SIGMA*sigma;
  threshold(blurR, thresholdedRed, colorThreshold, colorThreshold, THRESH_BINARY_INV );
#ifdef DEBUG
  printf("Thresold bin:%d, color:%d\n", maxR.y, colorThreshold);
  namedWindow("clone red thresholded", CV_WINDOW_KEEPRATIO);
  imshow("clone red thresholded", thresholdedRed );
#endif
  findContours(thresholdedRed, contours, RETR_LIST, CHAIN_APPROX_NONE);
  int unacceptableContours=0;
  double maxArea = rad*rad/MAX_COLONY_AREA;
  for( int i = 0; i< contours.size(); i++ ) {
    double area = contourArea(contours[i]);
    if( area<MIN_COLONY_AREA || area>maxArea ) unacceptableContours++;
  }
  int blockSize = 1 + 2 * cvRound( rad / sqrt(contours.size()-unacceptableContours) );
  //FIXME Max blockSize should not be hardcoded here
  if (blockSize > rad/2.5) blockSize = 1+2*cvRound(rad/5);
#ifdef DEBUG
  printf("%d - %d = %d contours found -> block size: %d\n", 
          contours.size(), unacceptableContours, contours.size()-unacceptableContours, blockSize);
#endif
  
  /*Could it be useful?
  Mat equalizedRed;
  equalizeHist(thresholdedRed, equalizedRed);
  namedWindow("clone red equalized", CV_WINDOW_KEEPRATIO);
  imshow("clone red equalized", equalizedRed );
  waitKey(0);
  */
  
  
  /// Now using adaptiveThreshold
  /// In order to remove (or at least reduce) bubbles
  //colorThreshold = maxR.y*255/HIST_BIN_COUNT;
  threshold(blurR, thresholdedRed, colorThreshold, colorThreshold, THRESH_TRUNC);
  adaptiveThreshold(blurR, thresholdedRed, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV, 
		    blockSize, N_SIGMA*sigma);
#ifdef DEBUG
  namedWindow("clone red adaptive thresholded", CV_WINDOW_KEEPRATIO);
  imshow("clone red adaptive thresholded", thresholdedRed );
#endif
  //GaussianBlur(thresholdedRed, blurR, Size(5,5), 0);
  findContours(thresholdedRed, contours, RETR_LIST, CHAIN_APPROX_NONE);

  printf("adaptiveThreshold\n%d contours found\n", contours.size());

  
#ifdef DEBUG
  /// Draw contours
  Mat drawing = Mat::zeros( d, d, CV_8UC3 );
  for( int i = 0; i< contours.size(); i++ ) {
    double area = contourArea(contours[i]);
    Scalar color = ( area<MIN_COLONY_AREA || area>maxArea )? Scalar( 64, 64, 128 ) : Scalar( 128, 255, 128 );
    drawContours( drawing, contours, i, color, 2, 8 );
  }
  /// Show in a window
  namedWindow( "Contours", CV_WINDOW_AUTOSIZE );
  imshow( "Contours", drawing );
  waitKey(0);
#endif

  
  
  return 0;
}