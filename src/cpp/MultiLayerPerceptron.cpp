//
// Created by Kensuke Kosaka on 2016/10/31.
//

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>
#include <stddef.h>

JNIEXPORT jobjectArray JNICALL Java_net_trileg_motionauth_Start_mlplearn(JNIEnv *env, jobject thiz, jobjectArray input) {
  int len1 = env->GetArrayLength(input); // 配列の長さ取得
  jdoubleArray dim = (jdoubleArray)env->GetObjectArrayElement(input, 0); // 配列0番目のオブジェクトをjdoubleArrayにキャストして取得
  int len2 = env->GetArrayLength(dim); // 配列の長さを得る


  double **localArray;
  // allocate localArray using len1
  localArray = new double*[len1];
  for (int i = 0; i < len1; ++i) {
    jdoubleArray oneDim = (jdoubleArray)env->GetObjectArrayElement(input, i); // 配列i番目のオブジェクトをjdoubleArrayにキャストして取得
    jdouble *element = env->GetDoubleArrayElements(oneDim, 0); // jdoubleArray要素を取得する
    // allocate localArray[i] using len2
    localArray[i] = new double [len2];
    for (int j = 0; j < len2; ++j) {
      localArray[i][j] = element[j];
    }
    env->ReleaseDoubleArrayElements(oneDim, element, 0);
  }

  for (int i = 0; i < len1; ++i) {
    for (int j = 0; j < len2; ++j) {
      localArray[i][j] += 1.0;
    }
  }


  jclass doubleArray1DClass = env->FindClass("[D");

  jobjectArray array2D = env->NewObjectArray(len1, doubleArray1DClass, NULL); // 二次元配列オブジェクトの作成
  for (jint i = 0; i < len1; ++i) {
    jdoubleArray array1D = env->NewDoubleArray(len2); // 一次元配列オブジェクトの作成
    env->SetDoubleArrayRegion(array1D, 0, len2, localArray[i]); // 一次元配列オブジェクトに配列をセット
    env->SetObjectArrayElement(array2D, i, array1D);
  }


  return array2D;
}

#ifdef __cplusplus
}
#endif