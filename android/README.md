Assumptions:
- We are going to install the NDK under BASE_DIR; something like: BASE_DIR=/home/amir/Android
mkdir -p ${BASE_DIR}
- JDK is already installed
- Eclipse is already installed
cd ${BASE_DIR}
tar xzvf eclipse-jee-indigo-SR1-linux-gtk.tar.gz



1. Installing the ADT Plugin for Eclipse
( Based on http://developer.android.com/sdk/eclipse-adt.html#installing )
1.1. Start Eclipse, then select Help > Install New Software....
1.2. Click Add, in the top-right corner.
1.3. In the Add Repository dialog that appears, enter "ADT Plugin" for the Name and the following URL for the Location:
    https://dl-ssl.google.com/android/eclipse/
1.4. Click OK
1.5. In the Available Software dialog, select the checkbox next to Developer Tools and click Next.
1.6. In the next window, you'll see a list of the tools to be downloaded. Click Next.
1.7. Read and accept the license agreements, then click Finish.
1.8. When the installation completes, restart Eclipse.

2. Configuring the SDK
2.1. In the "Welcome to Android Development" window, select "Install new SDK"
Note: Personally, I prefer to use a sub-directory of ${BASE_DIR} as "Target Location"
Note: In order to "Configuring the ADT Plugin": Select Window > Preferences... to open the Preferences panel then select Android from the left panel.

3. Getting the NDK
( Based on http://developer.android.com/sdk/ndk/index.html )
3.1. download the NDL from http://developer.android.com/sdk/ndk/index.html
wget -c http://dl.google.com/android/ndk/android-ndk-r7-linux-x86.tar.bz2
3.2. Extract the android-ndk-<version>.tar.gz file:
tar xjvf android-ndk-r7-linux-x86.tar.bz2


4. Getting the OpenCV package for Android development
( Based on http://opencv.itseez.com/doc/tutorials/introduction/android_binary_package/android_binary_package.html#get-the-opencv-package-for-android-development )
4.1. Go to the http://sourceforge.net/projects/opencvlibrary/files/opencv-android/ and download the latest available version. Currently it is OpenCV-2.3.1-android-bin.tar.bz2
4.2. Extract the package
tar xjvf OpenCV-2.3.1-android-bin.tar.bz2

5. Importing the android part of "Bacteria Detecto-Droid" into workspace.
5.1. File menu -> New Android project -> Create project from existing source
5.2. location is <path to the source>/AndroidBacteriaImageProcessing/android

6. Build the application from Eclipse
(Based on http://opencv.itseez.com/doc/tutorials/introduction/android_binary_package/android_binary_package_using_with_NDK.html#theory-how-to-build-android-application-having-c-native-part-from-eclipse )
6.1. Navigate to Package Explorer window and expand your project having JNI resources.
6.2. Right click on your project in Package Explorer window and select Properties.
6.3. In the Properties dialog select Builders menu and press the New... button:
http://opencv.itseez.com/_images/eclipse_builders.png
6.4. In the resulting dialog select the Program type and press OK button:
http://opencv.itseez.com/_images/eclipse_builder_types.png
6.5. In the Main tab fill the following fields:
6.5.1. Name (as you like)
6.5.2. Location - full path to ndk-build tool: Just put full path to ndk-build into this filed.
6.5.3. Working Directory - put path to your project into this field. Instead of hardcoding full path you can click Browse Workspace... button and select your project.
http://opencv.itseez.com/_images/eclipse_edit_configuration_main.png
6.6. Go to the Refresh tab and select both Refresh resources upon completion and Recursively include sub-folders.
6.6.1. Next set the Specific resources option and click Specify resources... button:
http://opencv.itseez.com/_images/eclipse_edit_configuration_refresh.png
6.7. Go to the Environment tab and add OPENCV_PACKAGE_DIR Variable with a Value equal to ${BASE_DIR}
6.8. Select libs folder under your project and click Finish:
http://opencv.itseez.com/_images/eclipse_edit_configuration_specify_resources.png
6.9. Go to the last tab Build options. Make sure that all checkboxes are set as shown on the next screen:
http://opencv.itseez.com/_images/eclipse_edit_configuration_build_options.png
6.10. Next, click the Specify resources... button.
6.11. Select jni folder of your project and click the Finish button:
http://opencv.itseez.com/_images/eclipse_edit_configuration_build_resources.png
6.12. Finally press OK in the builder configuration and project properties dialogs. If you have automatic build turned on then console showing build log should appear:
http://opencv.itseez.com/_images/eclipse_NDK_build_success.png


NOTE: If you are running an x86 system, you might get the following error:
${BASE_DIR}/android-ndk-r7/prebuilt/linux-x86/bin/awk: ${BASE_DIR}/android-ndk-r7/prebuilt/linux-x86/bin/awk: cannot execute binary file
Just link the awk from the NDL to the awk of your distro:
mv ${BASE_DIR}/android-ndk-r7/prebuilt/linux-x86/bin/awk{,.orig}
ln -s `which awk` ${BASE_DIR}/android-ndk-r7/prebuilt/linux-x86/bin/awk


