//
// Created by Kensuke Kosaka on 2016/10/31.
//

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>

JNIEXPORT jdouble JNICALL Java_net_trileg_motionauth_Start_mlplearn(JNIEnv *env, jobject thiz, jdouble sample) {
  double ret = sample + 1.0;
  return ret;
}

#ifdef __cplusplus
}
#endif