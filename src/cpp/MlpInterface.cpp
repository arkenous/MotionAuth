//
// Created by Kensuke Kosaka on 2016/10/31.
//

#include <vector>

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>
#include <stddef.h>

/**
 * double型二次元配列が入ったjobjectArrayをdouble型二次元vectorに変換する
 */
std::vector<std::vector<double>> jobjArrayToVector(JNIEnv *env, jobjectArray input) {
  int len1 = env->GetArrayLength(input); // 配列の長さ取得
  jdoubleArray dim = (jdoubleArray)env->GetObjectArrayElement(input, 0); // 配列0番目のオブジェクトをjdoubleArrayにキャストして取得
  int len2 = env->GetArrayLength(dim); // 配列の長さを得る

  std::vector<std::vector<double>> output;
  for (int i = 0; i < len1; ++i) {
    std::vector<double> tmp;
    jdoubleArray oneDim = (jdoubleArray)env->GetObjectArrayElement(input, i); // 配列i番目のオブジェクトをjdoubleArrayにキャストして取得
    jdouble *element = env->GetDoubleArrayElements(oneDim, 0); // jdoubleArray要素を取得する
    for (int j = 0; j < len2; ++j) {
      tmp.push_back(element[j]);
    }
    output.push_back(tmp);
    tmp.clear();
    env->ReleaseDoubleArrayElements(oneDim, element, 0);
  }

  return output;
}

JNIEXPORT jobjectArray JNICALL Java_net_trileg_motionauth_Start_mlplearn(JNIEnv *env, jobject thiz, jobjectArray input) {

  std::vector<std::vector<double>> inputData = jobjArrayToVector(env, input);

  for (int i = 0; i < inputData.size(); ++i) {
    for (int j = 0; j < inputData[i].size(); ++j) {
      inputData[i][j] += 1.0;
    }
  }

  double result[inputData.size()][inputData[0].size()];
  for (int i = 0; i < inputData.size(); ++i) {
    for (int j = 0; j < inputData[i].size(); ++j) {
      result[i][j] = inputData[i][j];
    }
  }

  int len1 = sizeof(result) / sizeof(result[0]);
  int len2 = sizeof(result[0]) / sizeof(result[0][0]);

  jclass doubleArray1DClass = env->FindClass("[D");

  jobjectArray array2D = env->NewObjectArray(len1, doubleArray1DClass, NULL); // 二次元配列オブジェクトの作成
  for (jint i = 0; i < len1; ++i) {
    jdoubleArray array1D = env->NewDoubleArray(len2); // 一次元配列オブジェクトの作成
    env->SetDoubleArrayRegion(array1D, 0, len2, result[i]); // 一次元配列オブジェクトに配列をセット
    env->SetObjectArrayElement(array2D, i, array1D);
  }

  return array2D;
}

#ifdef __cplusplus
}
#endif