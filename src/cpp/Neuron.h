//
// Created by Kensuke Kosaka on 2016/10/31.
//

#ifndef MOTIONAUTH_NEURON_H
#define MOTIONAUTH_NEURON_H

#include <vector>
#include <random>


class Neuron {
public:
  Neuron();
  Neuron(unsigned short input_num, std::vector<double> weight, int iteration, std::vector<double> m, std::vector<double> nu, std::vector<double> m_hat, std::vector<double> nu_hat, double bias, int activation_type, double dropout_ratio);
  void dropout(double random_value);
  void learn(double delta, std::vector<double> inputValues); // 誤差逆伝播学習
  double learn_output(std::vector<double> inputValues); // 学習時のDropoutを用いた順伝播出力
  double output(std::vector<double> inputValues); // Dropoutを用いて学習したNNの順伝播出力
  double getInputWeightIndexOf(int i);
  double getBias();
  double getDelta();
  double getMIndexOf(int i);
  double getNuIndexOf(int i);
  double getMHatIndexOf(int i);
  double getNuHatIndexOf(int i);
  int getIteration();

private:
  unsigned short input_num = 0;
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

  double dropout_ratio; // どれくらいの割合で中間層ニューロンをDropoutさせるか
  double dropout_mask; // Dropoutのマスク率，0.0で殺して1.0で生かす
};

#endif //MOTIONAUTH_NEURON_H
