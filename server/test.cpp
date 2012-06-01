#include <opencv/cv.h>
#include <opencv/highgui.h>
#include <math.h>
#include <functions.hpp>

using namespace cv;

#define SIZE 1024.0

//FIXME not needed
// n=1     : 0.682
// n=1.644 : 0.90
// n=2     : 0.954
// n=2.575 : 0.99
// n=3     : 0.997
// n=4     : 0.999937
#define N_SIGMA 2

#ifdef DEBUG
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
#endif

double findGausianSigma(Mat hist, float maxCount, int colorIndex, Mat histImage) {
  double color = (double) colorIndex*255/HIST_BIN_COUNT;
  int hist_h = histImage.rows - 20;
  int hist_w = histImage.cols;
  int bin_w = cvRound( (double) hist_w/HIST_BIN_COUNT );
  double y,c;
  y=(hist.at<float>(colorIndex-1)+hist.at<float>(colorIndex+1)) / 2;
  c=-1/log(y/maxCount);
#ifdef DEBUG
  printf("Max in histogram (at %d) %.1f:%.f\n\t-> Gausian function parameters: x:(%d,%d), y:%.f, c:%.3f\n",
                                colorIndex, color, maxCount, colorIndex-1, colorIndex+1,y,c);
#endif
  for( double x = 1; x < hist_w; x++ ) {
    line( histImage, Point( x, 10 + hist_h - hist_h*exp(-(x/bin_w-colorIndex)*(x/bin_w-colorIndex)/c)) ,
                      Point( x+1, 10 + hist_h - hist_h*exp(-((x+1)/bin_w-colorIndex)*((x+1)/bin_w-colorIndex)/c)),
                      Scalar( 255, 255, 255) );
  }
#ifdef DEBUG
  checkIntegral(maxCount, c);
#endif
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
#ifdef DEBUG
    printf("Source image resized to %d x %d\n", small.cols, small.rows);
#endif

    /// Separate the image in 3 places ( B, G and R )
    vector<Mat> rgbPlanes;
    split( small, rgbPlanes );

    /// Finding the petri-film circle
    // TODO keep time
    std::vector<cv::Point> circle;
    findCircle(rgbPlanes[1], circle);
    if(circle.size() <= 0) {
        printf("Could not find the petri-film circle!\nPlease make sure that 20 percent of center of the image is entirely on the perti-film.\n");
        exit(1);
    }
#ifdef DEBUG
    printf("Petri-film contour: %d points\n", circle.size());
    Mat debugImg1 = small.clone();
    std::vector< std::vector<cv::Point> > debugContours;
    debugContours.push_back(circle);
    drawContours(debugImg1, debugContours, 0, Scalar(0,255,0), 3, 8);
    namedWindow("Found petri-film circle", CV_WINDOW_KEEPRATIO);
    imshow("Found petri-film circle", debugImg1 );
    //waitKey(0);
#endif


    /// Get the petri-film region and circle mask
    Mat petri;
    Mat mask;
    getMask(small, circle, petri, mask);
    vector<Mat> maskPlanes;
    split( mask, maskPlanes );

    /// Separate the image in 3 places ( B, G and R )
    vector<Mat> petriPlanes;
    split( petri, petriPlanes );

#ifdef DEBUG
    /// Histo-Quad
    printf("\nHisto-Quad\n");
#endif

    // TODO Check Background function (instead of mean)
    /// Calculate 4 background colors
    Rect tmpRect;
    Mat tmpROI;
    Mat tmpMask;
    //cv::Scalar tmpMean;
    Scalar tmpBG[4];

    /// Top region
    tmpRect.x = 0;
    tmpRect.y = 0;
    tmpRect.width = petri.cols;
    tmpRect.height = petri.rows / 2;
    tmpROI = Mat(petri, tmpRect);
    tmpMask = Mat(maskPlanes[0], tmpRect);
    tmpBG[0] = mean( tmpROI, tmpMask );

    /// Buttom region
    //tmpRect.x = 0;
    tmpRect.y = petri.rows / 2;
    //tmpRect.width = petri.cols;
    //tmpRect.height = petri.rows / 2;
    tmpROI = Mat(petri, tmpRect);
    tmpMask = Mat(maskPlanes[0], tmpRect);
    tmpBG[1] = mean( tmpROI, tmpMask );

    /// Left region
    //tmpRect.x = 0;
    tmpRect.y = 0;
    tmpRect.width = petri.cols / 2;
    tmpRect.height = petri.rows;
    tmpROI = Mat(petri, tmpRect);
    tmpMask = Mat(maskPlanes[0], tmpRect);
    tmpBG[2] = mean( tmpROI, tmpMask );

    /// Right region
    tmpRect.x = petri.cols / 2;
    //tmpRect.y = 0;
    //tmpRect.width = petri.cols / 2;
    //tmpRect.height = petri.rows;
    tmpROI = Mat(petri, tmpRect);
    tmpMask = Mat(maskPlanes[0], tmpRect);
    tmpBG[3] = mean( tmpROI, tmpMask );

#ifdef DEBUG
    printf("Mean colors of regions (4 sides) of the petri film ( B , G , R):\nBefore Histo-Quad:\n");
    printf("Top: %.1f, %.1f, %.1f\n",    tmpBG[0][0], tmpBG[0][1], tmpBG[0][2]);
    printf("Buttom: %.1f, %.1f, %.1f\n", tmpBG[1][0], tmpBG[1][1], tmpBG[1][2]);
    printf("Left: %.1f, %.1f, %.1f\n",   tmpBG[2][0], tmpBG[2][1], tmpBG[2][2]);
    printf("Right: %.1f, %.1f, %.1f\n",  tmpBG[3][0], tmpBG[3][1], tmpBG[3][2]);

    /*
    Mat debugImg3_b4 = petriPlanes[0].clone();
    namedWindow("B befor Histo-Quad", CV_WINDOW_KEEPRATIO);
    imshow("B befor Histo-Quad", debugImg3_b4 );
    */
#endif

    double m[2][3];
    double l = sqrt(petri.cols*petri.cols + petri.rows*petri.rows)*PI/8;
    double colorConst[2][3];
    // TODO use the max instead of avgBG ?
    double avgBG[3];
    avgBG[0] = (tmpBG[0][0] + tmpBG[1][0] + tmpBG[2][0] + tmpBG[3][0] ) /4;
    avgBG[1] = (tmpBG[0][1] + tmpBG[1][1] + tmpBG[2][1] + tmpBG[3][1] ) /4;
    avgBG[2] = (tmpBG[0][2] + tmpBG[1][2] + tmpBG[2][2] + tmpBG[3][2] ) /4;
    /// Top Left
    m[0][0] = ( tmpBG[0][0] - tmpBG[2][0] ) / l;
    colorConst[0][0] = tmpBG[2][0];
    m[0][1] = ( tmpBG[0][1] - tmpBG[2][1] ) / l;
    colorConst[0][1] = tmpBG[2][1];
    m[0][2] = ( tmpBG[0][2] - tmpBG[2][2] ) / l;
    colorConst[0][2] = tmpBG[2][2];
    /// Buttom Right
    m[1][0] = ( tmpBG[3][0] - tmpBG[1][0] ) / l;
    colorConst[1][0] = tmpBG[1][0];
    m[1][1] = ( tmpBG[3][1] - tmpBG[1][1] ) / l;
    colorConst[1][1] = tmpBG[1][1];
    m[1][2] = ( tmpBG[3][2] - tmpBG[1][2] ) / l;
    colorConst[1][2] = tmpBG[1][2];
    double row0 = petri.rows * (0.5 - PI/8);
    double col0 = petri.cols * (0.5 - PI/8);

    double intColors[2][3];
    for( int row=0; row<petri.rows; row++) {
        uchar* rowStartB = petriPlanes[0].ptr<uchar>(row);
        uchar* rowStartG = petriPlanes[1].ptr<uchar>(row);
        uchar* rowStartR = petriPlanes[2].ptr<uchar>(row);
        double y = row - row0;
        y /= sqrt(2);
        for( int col=0; col<petri.cols; col += 1) {
            double x = col - col0;
            x /= sqrt(2);
            double x2 = x - y + l/2;
            double y2 = x + y - l/2;
            /// Top Left
            intColors[0][0] = m[0][0] * x2 + colorConst[0][0];
            intColors[0][1] = m[0][1] * x2 + colorConst[0][1];
            intColors[0][2] = m[0][2] * x2 + colorConst[0][2];
            /// Buttom Right
            intColors[1][0] = m[1][0] * x2 + colorConst[1][0];
            intColors[1][1] = m[1][1] * x2 + colorConst[1][1];
            intColors[1][2] = m[1][2] * x2 + colorConst[1][2];
            //*(rowStart + col) = intColors[0];
            *(rowStartB + col) += avgBG[0] - ( (intColors[1][0] - intColors[0][0]) / l * y2 + intColors[0][0] );
            *(rowStartG + col) += avgBG[1] - ( (intColors[1][1] - intColors[0][1]) / l * y2 + intColors[0][1] );
            *(rowStartR + col) += avgBG[2] - ( (intColors[1][2] - intColors[0][2]) / l * y2 + intColors[0][2] );
        }
    }

    // FIXME I think we do not need "petri" Mat from here, but anyway......
    merge(petriPlanes, petri);

#ifdef DEBUG
    /// Re-Calculate 4 background colors
    /// Top region
    tmpRect.x = 0;
    tmpRect.y = 0;
    tmpRect.width = petri.cols;
    tmpRect.height = petri.rows / 2;
    tmpROI = Mat(petri, tmpRect);
    tmpMask = Mat(maskPlanes[0], tmpRect);
    tmpBG[0] = mean( tmpROI, tmpMask );

    /// Buttom region
    //tmpRect.x = 0;
    tmpRect.y = petri.rows / 2;
    //tmpRect.width = petri.cols;
    //tmpRect.height = petri.rows / 2;
    tmpROI = Mat(petri, tmpRect);
    tmpMask = Mat(maskPlanes[0], tmpRect);
    tmpBG[1] = mean( tmpROI, tmpMask );

    /// Left region
    //tmpRect.x = 0;
    tmpRect.y = 0;
    tmpRect.width = petri.cols / 2;
    tmpRect.height = petri.rows;
    tmpROI = Mat(petri, tmpRect);
    tmpMask = Mat(maskPlanes[0], tmpRect);
    tmpBG[2] = mean( tmpROI, tmpMask );

    /// Right region
    tmpRect.x = petri.cols / 2;
    //tmpRect.y = 0;
    //tmpRect.width = petri.cols / 2;
    //tmpRect.height = petri.rows;
    tmpROI = Mat(petri, tmpRect);
    tmpMask = Mat(maskPlanes[0], tmpRect);
    tmpBG[3] = mean( tmpROI, tmpMask );

    printf("After Histo-Quad:\n");
    printf("Top: %.1f, %.1f, %.1f\n",    tmpBG[0][0], tmpBG[0][1], tmpBG[0][2]);
    printf("Buttom: %.1f, %.1f, %.1f\n", tmpBG[1][0], tmpBG[1][1], tmpBG[1][2]);
    printf("Left: %.1f, %.1f, %.1f\n",   tmpBG[2][0], tmpBG[2][1], tmpBG[2][2]);
    printf("Right: %.1f, %.1f, %.1f\n",  tmpBG[3][0], tmpBG[3][1], tmpBG[3][2]);
    printf("Histo-Quad ends here\n\n");
    /*
    Mat debugImg3_after = petriPlanes[0].clone();
    namedWindow("B after Histo-Quad", CV_WINDOW_KEEPRATIO);
    imshow("B after Histo-Quad", debugImg3_after );
    */
#endif
    /// Histo-Quad ends here

#ifdef DEBUG
    //namedWindow("Mask", CV_WINDOW_KEEPRATIO);
    //imshow("Mask", mask );
    Mat debugImg2 = petri.clone();
    namedWindow("RIO", CV_WINDOW_KEEPRATIO);
    imshow("RIO", debugImg2 );

    Mat debugImg3_0 = petriPlanes[0].clone();
    namedWindow("B", CV_WINDOW_KEEPRATIO);
    imshow("B", debugImg3_0 );

    Mat debugImg3_1 = petriPlanes[1].clone();
    namedWindow("G", CV_WINDOW_KEEPRATIO);
    imshow("G", debugImg3_1 );

    Mat debugImg3_2 = petriPlanes[2].clone();
    namedWindow("R", CV_WINDOW_KEEPRATIO);
    imshow("R", debugImg3_2 );
    //waitKey(0);
#endif


    /// Compute histograms
    vector<Mat> histogram;
    Scalar BGcolor = computeHistograms(petriPlanes, maskPlanes[0], histogram);

#ifdef DEBUG
    printf("Background color (peaks in histogram) RGB: %.0f,%.0f,%.0f\n",
           BGcolor[2] *255/HIST_BIN_COUNT, BGcolor[1] *255/HIST_BIN_COUNT, BGcolor[0] *255/HIST_BIN_COUNT);
#endif

    /// Find a point on yellow lines
    float yellowB, yellowG = 0; //Initialize as the first color in the histogram, but it should not be as dark as the first color and will check it later
    for( int i = 2; i < BGcolor[0]; i++ ) { //Is not in the same HIST_BIN as BG; it is even darker
        if(  histogram[0].at<float>(i) > histogram[0].at<float>(yellowB)
                && histogram[0].at<float>(i-1) < histogram[0].at<float>(i)
                && histogram[0].at<float>(i) > histogram[0].at<float>(i+1) ) {
            yellowB = i;
#ifdef DEBUG
            printf("Local max B in histogram: %.1f (at bin %d)\n", i*255./HIST_BIN_COUNT, i);
#endif
        }
    }

#ifdef DEBUG
    printf("\tYellow line B bin in histogram: %d\n", yellowB);
#endif

    if( yellowB < 2 || yellowB > HIST_BIN_COUNT-2 ) {//it should not be neither as dark as the first color nor as bright as the last
        printf("Could not find yellow lines!\nPlease make sure that the picture is neither over nor under exposed.\n");
        exit(2);
    }
    yellowB *= 255./HIST_BIN_COUNT;
#ifdef DEBUG
            printf("\tYellow line B: %.1f\n", yellowB);
#endif


    {
        // Color difference between B at a point and the B at yellow lines
        float colorDist = fabs( yellowB - petriPlanes[0].at<uchar>(petri.cols/2, petri.rows/2)); //Initialize at center
        float tmpBdist;
        /// Searching for yellow lines to find G at a point on lines
        //Going to search from 3 pixels from center to 80% of radious on the center line in 2 directions
        //TODO it can be done in 2 separete threads
        uchar* centerPixelB = petriPlanes[0].ptr<uchar>(petri.rows/2) + (petri.cols/2);
        uchar* centerPixelG = petriPlanes[1].ptr<uchar>(petri.rows/2) + (petri.cols/2);
        for(int i=3; i< (petri.cols+petri.rows)/5; i++) {
            tmpBdist = fabs( yellowB - *(centerPixelB + i) );
            if(tmpBdist < colorDist) {
                colorDist = tmpBdist;
                yellowG = *(centerPixelG + i);
            }

            tmpBdist = fabs( yellowB - *(centerPixelB - i) );
            if(tmpBdist < colorDist) {
                colorDist = tmpBdist;
                yellowG = *(centerPixelG - i);
            }
        }
    }
    //FIXME add some assertions
#ifdef DEBUG
    printf("Yellow lines: G=%.1f, B=%.1f\n", yellowG, yellowB);
#endif


    cv::Scalar tmpMean = mean( petri, maskPlanes[0] );
#ifdef DEBUG
    printf("Mean color RGB: %.2f,%.2f,%.2f\n", tmpMean[2], tmpMean[1], tmpMean[0]);
#endif

    // Merging G and B planes to remove yellow lines
    double tmpDeltaG = yellowG - tmpMean[1];
    double lambda = tmpDeltaG / (yellowB - tmpMean[0] - tmpDeltaG);
#ifdef DEBUG
    //tmpDeltaG = yellowG - BGcolor[1];
    //printf("By histogram: %.3f\n", tmpDeltaG / (yellowB - BGcolor[0] - tmpDeltaG));
    //tmpDeltaG = yellowG - tmpMean[1];
    //printf("By Mean: %.3f\n", tmpDeltaG / (yellowG - tmpMean[0] - tmpDeltaG));

    printf("Lambda: %.3f\n", lambda);
#endif

    Mat merge_G, merge_B, merged;
    petriPlanes[0].convertTo(merge_B, CV_64FC1);
    petriPlanes[1].convertTo(merge_G, CV_64FC1);

    if (0.01<lambda && lambda<=0.5) {
        merged = merge_G * (1+lambda) - merge_B * lambda;
#ifdef DEBUG
        Mat debugImg4 ; //= merged.clone();
        namedWindow("Merged", CV_WINDOW_KEEPRATIO);
        merged.convertTo(debugImg4, CV_8UC1);
        imshow("Merged", debugImg4 );
#endif
    } else {
        printf("lambda: %.2f => No yellow line removal\n", lambda);
    }

    /// Create low-pass filter, only within mask
    Mat blurred;
    Mat blurredCount;
    int blursize = (merged.rows / 12) * 2 + 1;
    Mat mergedMask;
    maskPlanes[0].convertTo(mergedMask, CV_64FC1);

    boxFilter(merged & mergedMask, blurred, CV_64FC1, Size(blursize, blursize),
              Point(-1, -1), false, BORDER_CONSTANT);
    boxFilter(mergedMask, blurredCount, CV_64FC1, Size(blursize, blursize),
              Point(-1, -1), false, BORDER_CONSTANT);
    blurred /= blurredCount / 255;

    /// High-pass image
    Mat highpass;
    merged.convertTo(highpass, CV_64FC1);
    highpass = highpass / blurred * 255;
    // Mask outside of circle to background (which has become Scalar(255,255,255))
    highpass.setTo(Scalar(255,255,255), 255 - maskPlanes[0]);


#ifdef DEBUG
    Mat debugImg5;
    highpass.convertTo(debugImg5, CV_8UC1);
    namedWindow("Low-pass / high-pass filtered petri", CV_WINDOW_KEEPRATIO);
    imshow("Low-pass / high-pass filtered petri", debugImg5 );
#endif

    // Convert back to 8-bit
    //Mat highpass8;
    //highpass.convertTo(highpass8, CV_8UC3);





    waitKey(0);

    return 0;
}
