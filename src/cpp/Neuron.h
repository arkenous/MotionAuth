//
// Created by Kensuke Kosaka on 2016/10/31.
//

#ifndef MOTIONAUTH_NEURON_H
#define MOTIONAUTH_NEURON_H

#include <vector>
#include <random>


class Neuron {
public:
  Neuron(unsigned long inputNeuronNum, std::vector<double> weight, std::vector<double> g, double bias, int activationType);
  void learn(double delta, std::vector<double> inputValues);
  double output(std::vector<double> inputValues);
  double getInputWeightIndexOf(int i);
  double getBias();
  double getDelta();
  double getGIndexOf(int i);
private:

  unsigned long inputNeuronNum = 0;
  int activationType = 0;
  std::vector<double> inputWeights;
  double delta = 0.0; // 修正量
  double bias = 0.0; // ニューロンのバイアス // -threshold
  double alpha = 0.3; // 学習率
  std::vector<double> g; // 学習率用AdaGrad，過去の勾配の二乗和を覚えておく
  double rambda = 0.00001; // SGDの荷重減衰の定数．正の小さな定数にしておくことで勾配がゼロでも重みが減る
  double activation_identity(double x); // 0
  double activation_sigmoid(double x); // 1
  double activation_tanh(double x); // 2
  double activation_relu(double x); // 3
};

#endif //MOTIONAUTH_NEURON_H
