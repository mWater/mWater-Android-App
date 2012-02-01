#include <opencv/cv.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/features2d/features2d.hpp>
#include <opencv2/highgui/highgui.hpp>

double calcCircularity(std::vector<cv::Point> contour);

std::vector<cv::Point> findCircle(cv::Mat& mbgra);

cv::Mat highpass(cv::Mat& imagef, cv::Mat& mask3C, int blursize);

void removeyellow(cv::Mat& img);

cv::Mat findColonies(cv::Mat& mbgr, int& colonies);
