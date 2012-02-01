LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OPENCV_PACKAGE_DIR should be set in your Builder, and point to something like this on your system:
# /Users/gina/opencv/OpenCV-2.3.1-android-bin

ifeq ("$(wildcard $(OPENCV_PACKAGE_DIR))","")
	#try to load OpenCV.mk from default install location
	include $(TOOLCHAIN_PREBUILT_ROOT)/user/share/OpenCV/OpenCV.mk
else
	OPENCV_MK_PATH:=$(OPENCV_PACKAGE_DIR)/OpenCV-2.3.1/share/OpenCV/OpenCV.mk
	include $(OPENCV_MK_PATH)
endif

LOCAL_MODULE    := native_sample
LOCAL_SRC_FILES := jni_part.cpp imagefuncs.cpp
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)
