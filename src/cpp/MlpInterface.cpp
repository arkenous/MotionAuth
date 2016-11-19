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


JNIEXPORT jstring JNICALL Java_net_trileg_motionauth_Registration_Result_learn(JNIEnv *env, jobject thiz, jshort input, jshort middle, jshort output, jshort middleLayer, jstring neuronParams, jobjectArray x, jobjectArray answer) {
  // jstringをstringに変換する
  std::string neuronParamsString = jstringToString(env, neuronParams);

  // MultiLayerPerceptronインスタンスを用意する
  MultiLayerPerceptron mlp((unsigned short)input, (unsigned short)middle, (unsigned short)output, (unsigned short)middleLayer, neuronParamsString, 1, 0.5);

  std::vector<std::vector<double>> xVector = jobjectArrayToTwoDimenDoubleVector(env, x);
  std::vector<std::vector<double>> answerVector = jobjectArrayToTwoDimenDoubleVector(env, answer);

  // 入力データを正規化する
  for (int i = 0; i < xVector.size(); ++i) {
    xVector[i] = normalize(xVector[i]);
  }

  std::string resultString = mlp.learn(xVector, answerVector);

  while (isnan(mlp.out(xVector[0])[0])) {
    mlp = MultiLayerPerceptron((unsigned short)input, (unsigned short)middle, (unsigned short)output, (unsigned short)middleLayer, neuronParamsString, 1, 0.5);
    resultString = mlp.learn(xVector, answerVector);
  }

  jstring result = stringToJString(env, resultString);

  return result;
}

JNIEXPORT jdoubleArray JNICALL Java_net_trileg_motionauth_Registration_Result_out(JNIEnv *env, jobject thiz, jshort input, jshort middle, jshort output, jshort middleLayer, jstring neuronParams, jdoubleArray x) {
  // jstringをstringに変換する
  std::string neuronParamsString = jstringToString(env, neuronParams);

  // MultiLayerPerceptronインスタンスを用意する
  MultiLayerPerceptron mlp((unsigned short)input, (unsigned short)middle, (unsigned short) output, (unsigned short)middleLayer, neuronParamsString, 1, 0.5);

  std::vector<double> xVector = jdoubleArrayToOneDimenDoubleVector(env, x);

  // 入力データを正規化する
  xVector = normalize(xVector);

  std::vector<double> resultDoubleVector = mlp.out(xVector);

  jdoubleArray result = oneDimenDoubleVectorToJDoubleArray(env, resultDoubleVector);

  return result;
}


JNIEXPORT jdoubleArray JNICALL Java_net_trileg_motionauth_Authentication_Result_out(JNIEnv *env, jobject thiz, jshort input, jshort middle, jshort output, jshort middleLayer, jstring neuronParams, jdoubleArray x) {
  // jstringをstringに変換する
  std::string neuronParamsString = jstringToString(env, neuronParams);

  // MultiLayerPerceptronインスタンスを用意する
  MultiLayerPerceptron mlp((unsigned short)input, (unsigned short)middle, (unsigned short) output, (unsigned short)middleLayer, neuronParamsString, 1, 0.5);

  std::vector<double> xVector = jdoubleArrayToOneDimenDoubleVector(env, x);

  // 入力データを正規化する
  xVector = normalize(xVector);

  std::vector<double> resultDoubleVector = mlp.out(xVector);

  jdoubleArray result = oneDimenDoubleVectorToJDoubleArray(env, resultDoubleVector);

  return result;
}

#ifdef __cplusplus
}
#endif