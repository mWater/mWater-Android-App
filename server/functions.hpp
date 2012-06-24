//using namespace BacteriaDetectoDroid;

#define PI 3.14159265
#define PETRI_MARGIN 3
#define PETRI_EROSION_KERNEL_SIZE 10
#define HIST_BIN_COUNT 50
#define HIGHPASS_BOXFILTER_SIZE_DIVIDER 24

/// Finding the petri-film circle
// Calculate the circularity of a contour
double calcCircularity(std::vector<cv::Point> contour);
void findCircle(cv::Mat& greenPlane, std::vector<cv::Point>& circle);


/// Get the petri-film region and circle mask
void getMask(cv::Mat& src, std::vector<cv::Point>& circle, cv::Mat& petri, cv::Mat& mask);


/// Compute histograms and return color at the peak
cv::Scalar computeHistograms(std::vector<cv::Mat> rgbPlanes, cv::Mat mask, std::vector<cv::Mat>& hist);

/// Histo-Quad
void histoquad(cv::Mat& petri, std::vector<cv::Mat> petriPlanes, std::vector<cv::Mat> maskPlanes);

/// High-pass
void getHighpass(cv::Mat merged, cv::Mat mask, cv::Mat& highpass);


