cmake_minimum_required(VERSION 3.6)

include_directories(${ANDROID_NDK}/sources/android/cpufeatures)
add_library ( mlp-lib SHARED MlpInterface.cpp MultiLayerPerceptron.cpp Neuron.cpp ${ANDROID_NDK}/sources/android/cpufeatures/cpu-features.c)