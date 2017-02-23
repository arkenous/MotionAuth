
#ifndef MOTIONAUTH_NEURON_H
#define MOTIONAUTH_NEURON_H

#include <vector>
#include <random>

using namespace std;

class Neuron {
 public:
  Neuron();
  Neuron(const unsigned long input_num, const vector<double> &weight, const vector<double> &m,
         const vector<double> &nu, const unsigned long iteration, const double bias,
         const int activation_type, const double dropout_rate);
  void dropout(const double random_value);
  void learn(const double delta, const vector<double> &inputValues); // 誤差逆伝播学習
  double learn_output(const vector<double> &inputValues); // 学習時のDropoutを用いた順伝播出力
  double output(const vector<double> &inputValues); // Dropoutを用いて学習したNNの順伝播出力

  unsigned long getInputNum();
  double getInputWeightIndexOf(unsigned long i);
  double getBias();
  double getDelta();
  double getMIndexOf(unsigned long i);
  double getNuIndexOf(unsigned long i);
  unsigned long getIteration();

 private:
  unsigned long input_num = 0;
  int activation_type = 0;
  vector<double> inputWeights;
  double delta = 0.0; // 修正量
  double bias = 0.0; // ニューロンのバイアス // -threshold
  double alpha = 0.005; // 学習率
  double beta_one = 0.9;
  double beta_two = 0.999;
  double epsilon = 0.00000001;
  unsigned long iteration = 0;
  vector<double> m;
  vector<double> nu;
  double lambda = 0.00001; // SGDの荷重減衰の定数．正の小さな定数にしておくことで勾配がゼロでも重みが減る
  double activation_identity(double x); // 0
  double activation_sigmoid(double x); // 1
  double activation_tanh(double x); // 2
  double activation_relu(double x); // 3

  double dropout_rate = 0.0; // どれくらいの割合で中間層ニューロンをDropoutさせるか
  double dropout_mask = 1.0; // Dropoutのマスク率，0.0で殺して1.0で生かす
};

#endif //MOTIONAUTH_NEURON_H
