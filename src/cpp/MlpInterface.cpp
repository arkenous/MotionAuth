//
// Created by Kensuke Kosaka on 2016/10/31.
//

#include <vector>
#include <stddef.h>
#include "MultiLayerPerceptron.h"
#include "Normalize.h"
#include "StackedDenoisingAutoencoder.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>
#include "JniCppUtil.h"


JNIEXPORT jobjectArray JNICALL Java_net_trileg_motionauth_Registration_Result_learn(JNIEnv *env, jobject thiz, jlong middleLayer, jstring neuronParams, jobjectArray x, jobjectArray answer) {
  // jstringをstringに変換する
  std::string neuronParamsString = jstringToString(env, neuronParams); // 空文字

  std::vector<std::vector<double>> xVector = jobjectArrayToTwoDimenDoubleVector(env, x);
  std::vector<std::vector<double>> answerVector = jobjectArrayToTwoDimenDoubleVector(env, answer);

  // 入力データを正規化する
  for (int i = 0; i < xVector.size(); ++i) {
    xVector[i] = normalize(xVector[i]);
  }

  // Pre training SdA
  StackedDenoisingAutoencoder stackedDenoisingAutoencoder;
  std::string sda_params = stackedDenoisingAutoencoder.learn(xVector, xVector[0].size(), 0.5);

  std::vector<std::string> neuronParamsVector(2);
  neuronParamsVector[0] = sda_params;
  neuronParamsVector[1] = neuronParamsString;
  unsigned long mlp_input_size = stackedDenoisingAutoencoder.getNumMiddleNeuron();

  // MultiLayerPerceptronインスタンスを用意する
  MultiLayerPerceptron mlp(mlp_input_size, mlp_input_size, answerVector[0].size(), (unsigned long)middleLayer, neuronParamsVector, 1, 0.0);

  std::vector<std::string> resultString = mlp.learn(xVector, answerVector);

  while (isnan(mlp.out(xVector[0])[0])) {
    mlp = MultiLayerPerceptron(mlp_input_size, mlp_input_size, answerVector[0].size(), (unsigned long)middleLayer, neuronParamsVector, 1, 0.0);
    resultString = mlp.learn(xVector, answerVector);
  }

  jobjectArray result = oneDimenStringVectorToJObjectArray(env, resultString);

  return result;
}

JNIEXPORT jdoubleArray JNICALL Java_net_trileg_motionauth_Registration_Result_out(JNIEnv *env, jobject thiz, jlong middleLayer, jobjectArray neuronParams, jdoubleArray x) {
  // jstringをstringに変換する
  std::vector<std::string> neuronParamsVector = jobjectArrayToOneDimenStringVector(env, neuronParams);
//  std::string neuronParamsString = jstringToString(env, neuronParams);

  std::vector<double> xVector = jdoubleArrayToOneDimenDoubleVector(env, x);

  // 入力データを正規化する
  xVector = normalize(xVector);

  // MultiLayerPerceptronインスタンスを用意する
  MultiLayerPerceptron mlp(xVector.size(), xVector.size(), 1, (unsigned long)middleLayer, neuronParamsVector, 1, 0.0);

  std::vector<double> resultDoubleVector = mlp.out(xVector);

  jdoubleArray result = oneDimenDoubleVectorToJDoubleArray(env, resultDoubleVector);

  return result;
}


JNIEXPORT jdoubleArray JNICALL Java_net_trileg_motionauth_Authentication_Result_out(JNIEnv *env, jobject thiz, jlong middleLayer, jobjectArray neuronParams, jdoubleArray x) {
  std::vector<std::string> neuronParamsVector = jobjectArrayToOneDimenStringVector(env, neuronParams);
  // jstringをstringに変換する
//  std::string neuronParamsString = jstringToString(env, neuronParams);

  std::vector<double> xVector = jdoubleArrayToOneDimenDoubleVector(env, x);

  // 入力データを正規化する
  xVector = normalize(xVector);

  // MultiLayerPerceptronインスタンスを用意する
  MultiLayerPerceptron mlp(xVector.size(), xVector.size(), 1, (unsigned long)middleLayer, neuronParamsVector, 1, 0.0);

  std::vector<double> resultDoubleVector = mlp.out(xVector);

  jdoubleArray result = oneDimenDoubleVectorToJDoubleArray(env, resultDoubleVector);

  return result;
}

#ifdef __cplusplus
}
#endif