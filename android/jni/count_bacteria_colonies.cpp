#include <cv.h>
#include <highgui.h>
#include <math.h>

using namespace cv;

#define SIZE 1024.0
#define PETRI_FILM_MARGIN 10
#define PETRI_FILM_USABLE_PORTION 0.95
#define HIST_BIN_COUNT 50

// n=1     : 0.682
// n=1.644 : 0.90
// n=2     : 0.954
// n=2.575 : 0.99
// n=3     : 0.997
// n=4     : 0.999937
#define N_SIGMA 2


#define PI 3.14159265
#define DX 0.01

void checkIntegral(int maxCount, double c, int n=N_SIGMA) {
  double sigma=sqrt(c/2);
  double integral=0;
  printf("sigma:%f, dX:%f\n",sigma,DX);
  for(double i=-n*sigma; i<n*sigma; i+=DX) {
    integral += maxCount * exp( -i*i / c ) * DX;
  }
  printf("Integral should be %f\n\tGot: %f -> %2.1f%%\n", maxCount*sqrt(c*PI), integral, integral/(maxCount*sqrt(c*PI))*100);
}
double findGausianSigma(Mat hist, float maxCount, int colorIndex, Mat histImage) {
  double color = (double) colorIndex*255/HIST_BIN_COUNT;
  int hist_h = histImage.rows;
  int hist_w = histImage.cols;
  int bin_w = cvRound( (double) hist_w/HIST_BIN_COUNT );
  double y,c;
  y=(hist.at<float>(colorIndex-1)+hist.at<float>(colorIndex+1)) / 2;
  c=-1/log(y/maxCount);
  printf("Max in histogram (at %d) %.1f:%.f\n\t-> Gausian function parameters: x:(%d,%d), y:%.f, c:%.3f\n", 
                                colorIndex, color, maxCount, colorIndex-1, colorIndex+1,y,c);
  for( double x = 1; x < hist_w; x++ ) {
    line( histImage, Point( x, 10 + hist_h - hist_h*exp(-(x/bin_w-colorIndex)*(x/bin_w-colorIndex)/c)) ,
                      Point( x+1, 10 + hist_h - hist_h*exp(-((x+1)/bin_w-colorIndex)*((x+1)/bin_w-colorIndex)/c)),
                      Scalar( 255, 255, 255) );
  }
  checkIntegral(maxCount, c);
  return sqrt(c/2);
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
  //Debug
  printf("Source image resized to %d x %d\n", small.cols, small.rows);
  /// Separate the image in 3 places ( B, G and R )
  vector<Mat> rgbPlanes;
  split( small, rgbPlanes );
  /// Finding the petri-film circle
  vector<Vec3f> circles;
  int maxRad = ((small.rows<small.cols)?small.rows:small.cols) /2;
  int minRad = maxRad /4;
  //Debug
  printf("Petri-film circle rad should be between %d and %d\n", minRad, maxRad);
  double param = 250;
  do {
    HoughCircles( rgbPlanes[1], circles, CV_HOUGH_GRADIENT, 2, minRad/2, param, param*0.8, minRad, maxRad );
    //Debug
    printf("\tNo. of circles found: %d\n", circles.size());
    param *= 0.9;
  } while(circles.size()<1);
  int centerX = cvRound(circles[0][0]);
  int centerY = cvRound(circles[0][1]);
  int rad = cvRound(circles[0][2]);
  //Debug
  printf("First circle in the array: (%d, %d), %d\nLast value of HoughCircles' 'param': %f\n", centerX, centerY, rad, param/0.9);
  /*Mat debugImg(small);
  circle(debugImg, Point(centerX, centerY), rad, Scalar(0,0,255), 3, 8, 0);
  namedWindow("Small", CV_WINDOW_KEEPRATIO);
  imshow("Small", debugImg );
  waitKey(0);
  */
  
  /// Extracting petri-film circle and its RGB channels
  /// from the small image
  int newRad = rad+PETRI_FILM_MARGIN;
  int d = newRad*2;
  printf("petri-film circle region of interest: (%d, %d) - (%d, %d)\n", centerX-newRad, centerY-newRad, d, d);
  Rect petriFilmROI(centerX-newRad, centerY-newRad, d, d);
  Mat petriFilm = small(petriFilmROI);
  Mat petriFilmB= rgbPlanes[0](petriFilmROI);
  Mat petriFilmG= rgbPlanes[1](petriFilmROI);
  Mat petriFilmR= rgbPlanes[2](petriFilmROI);
  //FIXME Should I free rgbPlanes?
  split( petriFilm, rgbPlanes );
  //Debug
  namedWindow("petri-film circle region of interest", CV_WINDOW_KEEPRATIO);
  imshow("petri-film circle region of interest", petriFilm );
  
  Mat mask(d, d, CV_8UC1, Scalar(0, 0, 0));
  circle(mask, Point(newRad, newRad), cvRound(rad*PETRI_FILM_USABLE_PORTION), 255, -1);
  //Debug
  printf("Usable portion of the petrifilm: %d%%\nRadius of usable portion of the petrifilm: %d\n", 
          cvRound(PETRI_FILM_USABLE_PORTION*100), cvRound(rad*PETRI_FILM_USABLE_PORTION));
  namedWindow("mask", CV_WINDOW_KEEPRATIO);
  imshow("mask", mask );
  
  /// Compute the histograms
  int histSize = HIST_BIN_COUNT; /// Establish the number of bins
  float range[] = { 0, 255 } ; /// Set the ranges ( for R,G,B) )
  const float* histRange = { range };
  bool uniform = true; bool accumulate = false;
  Mat r_hist, g_hist, b_hist;
  calcHist( &petriFilmB, 1, 0, mask, b_hist, 1, &histSize, &histRange, uniform, accumulate );
  calcHist( &petriFilmG, 1, 0, mask, g_hist, 1, &histSize, &histRange, uniform, accumulate );
  calcHist( &petriFilmR, 1, 0, mask, r_hist, 1, &histSize, &histRange, uniform, accumulate );
  
  //Debug
  double maxBcount,maxGcount,maxRcount;
  Point maxB, maxG, maxR;
  minMaxLoc( b_hist, NULL, &maxBcount, NULL, &maxB);
  minMaxLoc( g_hist, NULL, &maxGcount, NULL, &maxG);
  minMaxLoc( r_hist, NULL, &maxRcount, NULL, &maxR);
  Scalar background = Scalar(cvRound(maxB.y * 255 / histSize ),
                             cvRound(maxG.y * 255 / histSize ),
                             cvRound(maxR.y * 255 / histSize ) );
  //Debug
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
  assert(maxB.y>1 && maxB.y<histSize && maxG.y>1 && maxG.y<histSize && maxR.y>1 && maxR.y<histSize);
  
  findGausianSigma(b_hist, maxBcount, maxB.y, histImage);
  findGausianSigma(g_hist, maxGcount, maxG.y, histImage);
  double sigma = findGausianSigma(r_hist, maxRcount, maxR.y, histImage);
  
  namedWindow("calcHist Demo", CV_WINDOW_AUTOSIZE );
  imshow("calcHist Demo", histImage );
  
  
  printf("N-Sigma = %.3f\n", N_SIGMA*sigma);
  
  
  
  
  /// Clone the petri-film circle over an 
  /// area filled with its background color
  Mat petriFilmClone(d, d, petriFilm.type(), background);
  petriFilm.copyTo(petriFilmClone, mask);

  //Debug
  namedWindow("petri-film circle clone", CV_WINDOW_KEEPRATIO);
  imshow("petri-film circle clone", petriFilmClone );
  
  /// Finding contours
  /// Separate the image in 3 places ( B, G and R )
  //FIXME Should I reuse rgbPlanes?
  vector<Mat> rgbPlanesClone;
  split( petriFilmClone, rgbPlanesClone );
  //Debug
  namedWindow("clone green channel", CV_WINDOW_KEEPRATIO);
  imshow("clone green channel", rgbPlanesClone[1] );
  namedWindow("clone red channel", CV_WINDOW_KEEPRATIO);
  imshow("clone red channel", rgbPlanesClone[2] );
  waitKey(0);

  vector<vector<Point> > contours;
  Mat blurR;
  GaussianBlur(rgbPlanesClone[2], blurR, Size(5,5), 0);
  Mat thresholdedRed;
  
  //Debug
  threshold(blurR, thresholdedRed, (maxR.y-N_SIGMA*sigma)*255/HIST_BIN_COUNT, 255, THRESH_BINARY_INV);
  printf("Thresold bin:%d, color:%f\n", maxR.y, (maxR.y-N_SIGMA*sigma)*255/HIST_BIN_COUNT);
  namedWindow("clone red thresholded", CV_WINDOW_KEEPRATIO);
  imshow("clone red thresholded", thresholdedRed );
  findContours(thresholdedRed, contours, RETR_LIST, CHAIN_APPROX_NONE);
  printf("%d contours found\n", contours.size());
  waitKey(0);
  
  //FIXME still hacky!!!
  //FIXME The adaptiveThreshold function can process the image in-place.
  adaptiveThreshold(blurR, thresholdedRed, 255, ADAPTIVE_THRESH_GAUSSIAN_C, THRESH_BINARY_INV, 
		    cvRound(4.8+2.1*log(contours.size()))*2+1, N_SIGMA*sigma*2);
  //Debug
  printf("blockSize=%d\n", cvRound(4.8+1.7*log(contours.size()))*2+1);
  imshow("clone red thresholded", thresholdedRed );
  waitKey(0);
  GaussianBlur(thresholdedRed, blurR, Size(15,15), 0);
  //Debug
  imshow("clone red thresholded", blurR );
  findContours(blurR, contours, RETR_LIST, CHAIN_APPROX_NONE);
  printf("adaptiveThreshold\n%d contours found\n", contours.size());
  waitKey(0);
  /// Draw contours
  Mat drawing = Mat::zeros( d, d, CV_8UC3 );
  for( int i = 0; i< contours.size(); i++ ) {
    Scalar color = Scalar( 128, 255, 128 );
    drawContours( drawing, contours, i, color, 2, 8 );
  }
  /// Show in a window
  namedWindow( "Contours", CV_WINDOW_AUTOSIZE );
  imshow( "Contours", drawing );
  waitKey(0);

  
  
  return 0;
}