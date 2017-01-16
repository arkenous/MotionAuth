
#include <vector>
#include <android/log.h>
#include "MlpInterface.h"
#include "Normalize.h"
#include "StackedDenoisingAutoencoder.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>
#include "JniCppUtil.h"

using namespace std;

JNIEXPORT jdouble JNICALL Java_net_trileg_motionauth_Authentication_Result_out
    (JNIEnv *env, jobject thiz, jobjectArray neuronParams, jdoubleArray x) {
  //TODO SdA仕様に変える

  vector<string> neuronParamsVector = jobjectArrayToOneDimenStringVector(env, neuronParams);
  vector<double> xVector = jdoubleArrayToOneDimenDoubleVector(env, x);

  __android_log_print(ANDROID_LOG_DEBUG, "MlpInterface", "converted");

  // 入力データを正規化する
  normalize(&xVector);

  __android_log_print(ANDROID_LOG_DEBUG, "MlpInterface", "normalized");

  StackedDenoisingAutoencoder stackedDenoisingAutoencoder;
  stackedDenoisingAutoencoder.setup(neuronParamsVector,dropout_rate);

  __android_log_print(ANDROID_LOG_DEBUG, "MlpInterface", "finished setup SdA");

  double result = stackedDenoisingAutoencoder.out(xVector);

  __android_log_print(ANDROID_LOG_DEBUG, "MlpInterface", "SdA out: %f", result);

  jdouble resultJdouble = result;

  __android_log_print(ANDROID_LOG_DEBUG, "MlpInterface", "result converted");

  return resultJdouble;
}


#ifdef __cplusplus
}
#endif
