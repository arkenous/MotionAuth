
#ifndef MOTIONAUTH_NEURON_H
#define MOTIONAUTH_NEURON_H

#include <vector>
#include <random>

using namespace std;

class Neuron {
 public:
  Neuron();
  Neuron(const unsigned long input_num, const vector<double> &weight, const vector<double> &m,
         const vector<double> &nu, const vector<double> &m_hat, const vector<double> &nu_hat,
         const int iteration, const double bias, const int activation_type,
         const double dropout_rate);
  void dropout(const double random_value);
  void learn(const double delta, const vector<double> &inputValues); // 誤差逆伝播学習
  double learn_output(const vector<double> &inputValues); // 学習時のDropoutを用いた順伝播出力
  double output(const vector<double> &inputValues); // Dropoutを用いて学習したNNの順伝播出力
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
  vector<double> inputWeights;
  double delta = 0.0; // 修正量
  double bias = 0.0; // ニューロンのバイアス // -threshold
  double alpha = 0.01; // 学習率
  double beta_one = 0.9;
  double beta_two = 0.999;
  double epsilon = 0.00000001;
  int iteration = 0;
  vector<double> m;
  vector<double> nu;
  vector<double> m_hat;
  vector<double> nu_hat;
  double lambda = 0.00001; // SGDの荷重減衰の定数．正の小さな定数にしておくことで勾配がゼロでも重みが減る
  double activation_identity(double x); // 0
  double activation_sigmoid(double x); // 1
  double activation_tanh(double x); // 2
  double activation_relu(double x); // 3

  double dropout_rate; // どれくらいの割合で中間層ニューロンをDropoutさせるか
  double dropout_mask; // Dropoutのマスク率，0.0で殺して1.0で生かす
};

#endif //MOTIONAUTH_NEURON_H
