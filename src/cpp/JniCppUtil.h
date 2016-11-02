//
// Created by Kensuke Kosaka on 2016/11/03.
//

#ifndef MOTIONAUTH_JNICPPUTIL_H
#define MOTIONAUTH_JNICPPUTIL_H

#include <vector>
#include <string>

#ifdef __cplusplus
extern "C" {
#endif

#include <jni.h>

std::vector<std::vector<double>> jobjectArrayToTwoDimenDoubleVector(JNIEnv *env, jobjectArray input);
std::vector<double> jdoubleArrayToOneDimenDoubleVector(JNIEnv *env, jdoubleArray input);
std::vector<std::string> jobjectArrayToOneDimenStringVector(JNIEnv *env, jobjectArray input);
std::string jstringToString(JNIEnv *env, jstring input);
jobjectArray twoDimenDoubleVectorToJOBjectArray(JNIEnv *env, std::vector<std::vector<double>> input);
jdoubleArray oneDimenDoubleVectorToJDoubleArray(JNIEnv *env, std::vector<double> input);
jobjectArray oneDimenStringVectorToJObjectArray(JNIEnv *env, std::vector<std::string> input);
jstring stringToJString(JNIEnv *env, std::string input);


#ifdef __cplusplus
}
#endif
#endif //MOTIONAUTH_JNICPPUTIL_H
