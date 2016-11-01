cmake_minimum_required(VERSION 3.6)

add_library ( mlp-lib SHARED MlpInterface.cpp MultiLayerPerceptron.cpp Neuron.cpp)
target_include_directories(mlp-lib PRIVATE ${ANDROID_NDK}/sources/android/cpufeatures)