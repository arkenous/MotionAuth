//
// Created by Kensuke Kosaka on 2016/10/31.
//

#ifndef MOTIONAUTH_NEURON_H
#define MOTIONAUTH_NEURON_H

#include <vector>
#include <random>


class Neuron {
public:
  Neuron(unsigned long input_num, std::vector<double> weight, int iteration, std::vector<double> m, std::vector<double> nu, std::vector<double> m_hat, std::vector<double> nu_hat, double bias, int activation_type);
  void learn(double delta, std::vector<double> inputValues); // 誤差逆伝播学習
  double output(std::vector<double> inputValues); // 学習済みNNの順伝播出力
  double getInputWeightIndexOf(int i);
  double getBias();
  double getDelta();
  double getMIndexOf(int i);
  double getNuIndexOf(int i);
  double getMHatIndexOf(int i);
  double getNuHatIndexOf(int i);
  int getIteration();

private:
  unsigned long input_num = 0;
  int activation_type = 0;
  std::vector<double> inputWeights;
  double delta = 0.0; // 修正量
  double bias = 0.0; // ニューロンのバイアス // -threshold
  double alpha = 0.001; // 学習率
  double beta_one = 0.9;
  double beta_two = 0.999;
  double epsilon = 0.00000001;
  int iteration = 0;
  std::vector<double> m;
  std::vector<double> nu;
  std::vector<double> m_hat;
  std::vector<double> nu_hat;
  double lambda = 0.00001; // SGDの荷重減衰の定数．正の小さな定数にしておくことで勾配がゼロでも重みが減る
  double activation_identity(double x); // 0
  double activation_sigmoid(double x); // 1
  double activation_tanh(double x); // 2
  double activation_relu(double x); // 3
};

#endif //MOTIONAUTH_NEURON_H
