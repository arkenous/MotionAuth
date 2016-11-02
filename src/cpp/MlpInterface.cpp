//
// Created by Kensuke Kosaka on 2016/10/31.
//

#include <vector>
#include <stddef.h>
#include "MultiLayerPerceptron.h"
#include "Normalize.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>
#include "JniCppUtil.h"


JNIEXPORT jobjectArray JNICALL Java_net_trileg_motionauth_Start_test(JNIEnv *env, jobject thiz, jobjectArray input) {

  std::vector<std::vector<double>> inputData = jobjectArrayToTwoDimenDoubleVector(env, input);

  for (int i = 0; i < inputData.size(); ++i) {
    for (int j = 0; j < inputData[i].size(); ++j) {
      inputData[i][j] += 1.0;
    }
  }

  jobjectArray array2D = twoDimenDoubleVectorToJOBjectArray(env, inputData);

  return array2D;
}

JNIEXPORT jint JNICALL Java_net_trileg_motionauth_Start_getCpuNum(JNIEnv *env, jobject thiz) {
  int num_cpu_core = android_getCpuCount();
  jint result = num_cpu_core;
  return result;
}

JNIEXPORT jstring JNICALL Java_net_trileg_motionauth_Start_learn(JNIEnv *env, jobject thiz, jshort input, jshort middle, jshort output, jshort middleLayer, jstring weightAndThreshold, jobjectArray x, jobjectArray answer) {
  // jstringをstringに変換する
  std::string weightAndThresholdString = jstringToString(env, weightAndThreshold);

  // MultiLayerPerceptronインスタンスを用意する
  MultiLayerPerceptron mlp((unsigned short)input, (unsigned short)middle, (unsigned short)output, (unsigned short)middleLayer, weightAndThresholdString);

  std::vector<std::vector<double>> xVector = jobjectArrayToTwoDimenDoubleVector(env, x);
  std::vector<std::vector<double>> answerVector = jobjectArrayToTwoDimenDoubleVector(env, answer);

  // 入力データを正規化する
  for (int i = 0; i < xVector.size(); ++i) {
    xVector[i] = normalize(xVector[i]);
  }

  std::string resultString = mlp.learn(xVector, answerVector);

  jstring result = stringToJString(env, resultString);

  return result;
}

#ifdef __cplusplus
}
#endif