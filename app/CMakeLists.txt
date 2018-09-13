# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.4.1)

# Creates and names a library, sets it as either STATIC
# or SHARED, and provides the relative paths to its source code.
# You can define multiple libraries, and CMake builds them for you.
# Gradle automatically packages shared libraries with your APK.

# 将静态库的头文件添加进来
include_directories(${CMAKE_SOURCE_DIR}/src/main/cpp/include/)

add_library(x264 STATIC IMPORTED)
add_library(avutil SHARED IMPORTED)
add_library(postproc SHARED IMPORTED)
add_library(swresample SHARED IMPORTED)
add_library(swscale SHARED IMPORTED)
add_library(avcodec SHARED IMPORTED)
add_library(avformat SHARED IMPORTED)
add_library(avdevice SHARED IMPORTED)
add_library(avfilter SHARED IMPORTED)
add_library(native-lib SHARED src/main/cpp/native-lib.cpp)

set_target_properties(x264
                      PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libx264.a)
set_target_properties(avutil
                      PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libavutil.so)
set_target_properties(postproc
                      PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libpostproc.so)
set_target_properties(swresample
                      PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libswresample.so)
set_target_properties(swscale
                      PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libswscale.so)
set_target_properties(avcodec
                      PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libavcodec.so)
set_target_properties(avformat
                      PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libavformat.so)
set_target_properties(avdevice
                      PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libavdevice.so)
set_target_properties(avfilter
                      PROPERTIES IMPORTED_LOCATION
                      ${CMAKE_SOURCE_DIR}/libs/${ANDROID_ABI}/libavfilter.so)

# Searches for a specified prebuilt library and stores the path as a
# variable. Because CMake includes system libraries in the search path by
# default, you only need to specify the name of the public NDK library
# you want to add. CMake verifies that the library exists before
# completing its build.

find_library( # Sets the name of the path variable.
              log-lib

              # Specifies the name of the NDK library that
              # you want CMake to locate.
              log )

# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
                       native-lib
                       x264
                       avutil
                       postproc
                       swresample
                       swscale
                       avcodec
                       avformat
                       avdevice
                       avfilter

                       # Links the target library to the log library
                       # included in the NDK.
                       ${log-lib} )

# 静态库顺序不能随意更改
# LOCAL_CFLAGS += -I$(FFMPEG)/include
# LOCAL_LDLIBS += -l$(FFMPEG)/lib/libavformat.a
# LOCAL_LDLIBS += -l$(FFMPEG)/lib/libavcodec.a
# LOCAL_LDLIBS += -l$(FFMPEG)/lib/libavdevice.a
# LOCAL_LDLIBS += -l$(FFMPEG)/lib/libswresample.a
# LOCAL_LDLIBS += -l$(FFMPEG)/lib/libavdevice.a
# LOCAL_LDLIBS += -l$(FFMPEG)/lib/libswscale.a
# LOCAL_LDLIBS += -l$(FFMPEG)/lib/libpostproc.a
# LOCAL_LDLIBS += -l$(FFMPEG)/lib/libavutil.a
# fdk_aac
# LOCAL_LDLIBS += -l/opt/ndk/lib/libfdk-aac.a
# LOCAL_LDLIBS += -l/opt/ndk/lib/libmp3lame.a
# LOCAL_LDLIBS += -l/opt/ndk/lib/libx264.a
