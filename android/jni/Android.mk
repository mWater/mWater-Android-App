LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

#OPENCV_MK_PATH should be set in your Builder, and point to something like this on your system:
#/home/clayton/install/OpenCV-2.4.3.2-android-sdk/sdk/native/jni/OpenCV.mk

OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on
include $(OPENCV_MK_PATH)

LOCAL_MODULE    := native_sample
LOCAL_SRC_FILES := jni_part.cpp imagefuncs.cpp
# Changed this to get rid of jnigraphics
# LOCAL_LDLIBS += -llog -ldl -ljnigraphics
LOCAL_LDLIBS += -llog -ldl

include $(BUILD_SHARED_LIBRARY)
