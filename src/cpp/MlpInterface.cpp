
#include <vector>
#include "MultiLayerPerceptron.h"
#include "Normalize.h"
#include "StackedDenoisingAutoencoder.h"

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>
#include "JniCppUtil.h"

using namespace std;

JNIEXPORT jobjectArray JNICALL Java_net_trileg_motionauth_Registration_Result_learn
    (JNIEnv *env, jobject thiz, jlong middleLayer, jstring neuronParams,
     jobjectArray x, jobjectArray answer) {
  // jstringをstringに変換する
  string neuronParamsString = jstringToString(env, neuronParams); // 空文字
  vector<vector<double>> xVector = jobjectArrayToTwoDimenDoubleVector(env, x);
  vector<vector<double>> answerVector = jobjectArrayToTwoDimenDoubleVector(env, answer);

  // 入力データを正規化する
  for (unsigned long i = 0, n = xVector.size(); i < n; ++i) normalize(&xVector[i]);

  vector<string> neuronParamsVector(1);
  neuronParamsVector[0] = neuronParamsString;

  // MultiLayerPerceptronインスタンスを用意する
  MultiLayerPerceptron mlp(xVector[0].size(), xVector[0].size(), answerVector[0].size(),
                           (unsigned long) middleLayer, neuronParamsVector, 1, 0.0);

  vector<string> resultString = mlp.learn(xVector, answerVector);

  while (isnan(mlp.out(xVector[0])[0])) {
    mlp = MultiLayerPerceptron(xVector[0].size(), xVector[0].size(), answerVector[0].size(),
                                   (unsigned long) middleLayer, neuronParamsVector, 1, 0.0);
    resultString = mlp.learn(xVector, answerVector);
  }

  jobjectArray result = oneDimenStringVectorToJObjectArray(env, resultString);

  return result;
}

JNIEXPORT jdoubleArray JNICALL Java_net_trileg_motionauth_Registration_Result_out
    (JNIEnv *env, jobject thiz, jlong middleLayer, jobjectArray neuronParams, jdoubleArray x) {
  // jstringをstringに変換する
  vector<string> neuronParamsVector = jobjectArrayToOneDimenStringVector(env, neuronParams);
  vector<double> xVector = jdoubleArrayToOneDimenDoubleVector(env, x);

  // 入力データを正規化する
  normalize(&xVector);

  // MultiLayerPerceptronインスタンスを用意する
  MultiLayerPerceptron mlp(xVector.size(), xVector.size(), 1,
                           (unsigned long) middleLayer, neuronParamsVector, 1, 0.0);

  vector<double> resultDoubleVector = mlp.out(xVector);

  jdoubleArray result = oneDimenDoubleVectorToJDoubleArray(env, resultDoubleVector);

  return result;
}


JNIEXPORT jdoubleArray JNICALL Java_net_trileg_motionauth_Authentication_Result_out
    (JNIEnv *env, jobject thiz, jlong middleLayer, jobjectArray neuronParams, jdoubleArray x) {
  vector<string> neuronParamsVector = jobjectArrayToOneDimenStringVector(env, neuronParams);
  vector<double> xVector = jdoubleArrayToOneDimenDoubleVector(env, x);

  // 入力データを正規化する
  normalize(&xVector);

  // MultiLayerPerceptronインスタンスを用意する
  MultiLayerPerceptron mlp(xVector.size(), xVector.size(), 1,
                           (unsigned long) middleLayer, neuronParamsVector, 1, 0.0);

  vector<double> resultDoubleVector = mlp.out(xVector);

  jdoubleArray result = oneDimenDoubleVectorToJDoubleArray(env, resultDoubleVector);

  return result;
}

JNIEXPORT jobjectArray JNICALL Java_net_trileg_motionauth_Authentication_Result_learn
    (JNIEnv *env, jobject thiz, jlong middleLayer, jobjectArray neuronParams,
     jobjectArray x, jobjectArray answer) {
  // jstringをstringに変換する
  vector<string> neuronParamsVector = jobjectArrayToOneDimenStringVector(env, neuronParams);
  vector<vector<double>> xVector = jobjectArrayToTwoDimenDoubleVector(env, x);
  vector<vector<double>> answerVector = jobjectArrayToTwoDimenDoubleVector(env, answer);

  // 入力データを正規化する
  for (unsigned long i = 0, n = xVector.size(); i < n; ++i) normalize(&xVector[i]);

  // MultiLayerPerceptronインスタンスを用意する
  MultiLayerPerceptron mlp(xVector[0].size(), xVector[0].size(), answerVector[0].size(),
                           (unsigned long) middleLayer, neuronParamsVector, 1, 0.0);

  vector<string> resultString = mlp.learn(xVector, answerVector);

  while (isnan(mlp.out(xVector[0])[0])) {
    mlp = MultiLayerPerceptron(xVector[0].size(), xVector[0].size(), answerVector[0].size(),
                               (unsigned long) middleLayer, neuronParamsVector, 1, 0.0);
    resultString = mlp.learn(xVector, answerVector);
  }

  jobjectArray result = oneDimenStringVectorToJObjectArray(env, resultString);

  return result;
}

#ifdef __cplusplus
}
#endif
