cmake_minimum_required(VERSION 3.6)

find_library ( log-lib log )

# Generate static library
add_library ( cpufeatures STATIC ${ANDROID_NDK}/sources/android/cpufeatures/cpu-features.c )
include_directories ( ${ANDROID_NDK}/sources/android/cpufeatures/ )

add_library ( mlp-lib SHARED MlpInterface.cpp Neuron.cpp Normalize.cpp
  JniCppUtil.cpp StackedDenoisingAutoencoder.cpp DenoisingAutoencoder.cpp AddNoise.cpp)
target_link_libraries( mlp-lib cpufeatures ${log-lib} )