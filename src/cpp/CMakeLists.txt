cmake_minimum_required(VERSION 3.6)

find_library ( log-lib log )

# Generate static library
add_library ( cpufeatures STATIC ${ANDROID_NDK}/sources/android/cpufeatures/cpu-features.c )
include_directories ( ${ANDROID_NDK}/sources/android/cpufeatures/ )

set ( mp-path ../../libs/msgpack )
set ( mp-src ${mp-path}/src )
add_library ( msgpack STATIC ${mp-src}/objectc.c ${mp-src}/unpack.c ${mp-src}/version.c
  ${mp-src}/vrefbuffer.c ${mp-src}/zone.c )
include_directories ( ${mp-path}/include/ )

add_library ( mlp-lib SHARED MlpInterface.cpp MultiLayerPerceptron.cpp Neuron.cpp Normalize.cpp
  JniCppUtil.cpp StackedDenoisingAutoencoder.cpp DenoisingAutoencoder.cpp )
target_link_libraries( mlp-lib cpufeatures msgpack ${log-lib} )