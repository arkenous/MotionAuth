//
// Created by Kensuke Kosaka on 2016/10/31.
//

#ifndef MOTIONAUTH_NEURON_H
#define MOTIONAUTH_NEURON_H

#include <vector>
#include <random>


class Neuron {
public:
  Neuron(unsigned long inputNeuronNum, std::vector<double> weight, double threshold);
  void learn(double delta, std::vector<double> inputValues);
  double output(std::vector<double> inputValues);
  double getInputWeightIndexOf(int i);
  double getThreshold();
  double getDelta();
private:
  unsigned long inputNeuronNum = 0;
  std::vector<double> inputWeights;
  double delta = 0.0; // 修正量
  double threshold = 0.0; // ニューロンの域値
  double eta = 0.3; // 学習率
  double activation_sigmoid(double x);
  double activation_relu(double x);
  double activation_tanh(double x);
};

#endif //MOTIONAUTH_NEURON_H
