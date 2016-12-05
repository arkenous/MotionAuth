
#ifndef MOTIONAUTH_MULTILAYERPERCEPTRON_H
#define MOTIONAUTH_MULTILAYERPERCEPTRON_H

#include <zconf.h>
#include <cpu-features.h>
#include "Neuron.h"

using namespace std;

class MultiLayerPerceptron {
 public:
  MultiLayerPerceptron(const unsigned long input, const unsigned long middle,
                       const unsigned long output, const unsigned long middle_layer,
                       const vector<string> &neuron_params,
                       const int middle_layer_type, const double dropout_rate);
  vector<string> learn(const vector<vector<double>> &x,
                       const vector<vector<double>> &answer);
  vector<double> out(const vector<double> &input);

 private:
  static const unsigned int MAX_TRIAL = 1000; // 学習上限回数
  constexpr static const double MAX_GAP = 0.1; // 損失関数の出力がこの値以下になれば学習をスキップする
  int num_thread = android_getCpuCount(); // Androidデバイスのプロセッサのコア数

  string mlp_params = "";

  // ニューロン数
  unsigned long input_neuron_num = 0;
  unsigned long middle_neuron_num = 0;
  unsigned long output_neuron_num = 0;

  unsigned long middle_layer_number = 0; // 中間層の層数

  int middle_layer_type = 0; // 中間層の活性化関数の種類指定．0: identity 1: sigmoid 2: tanh 3: ReLU

  bool successFlg = true;

  vector<vector<Neuron>> middleNeurons; // 中間層は複数層用意する
  vector<Neuron> outputNeurons;
  vector<vector<double>> h;
  vector<double> o;

  vector<vector<double>> learned_h;
  vector<double> learned_o;

  void setupMLP(const string &mlp_params, const double dropout_rate);
  vector<double> separate_by_camma(const string &input);

  void middleFirstLayerForwardThread(const vector<double> &in, const int begin, const int end);
  void middleLayerForwardThread(const int layer, const int begin, const int end);
  void outForwardThread(const int begin, const int end);

  void outLearnThread(const vector<double> &ans, const int begin, const int end);
  void middleLastLayerLearnThread(const int begin, const int end);
  void middleMiddleLayerLearnThread(const int layer, const int begin, const int end);
  void middleFirstLayerLearnThread(const vector<double> &in, const int begin, const int end);

  void middleFirstLayerOutThread(const vector<double> &in, const int begin, const int end);
  void middleLayerOutThread(const int layer, const int begin, const int end);
  void outOutThread(const int begin, const int end);

  double cross_entropy(double output, double answer);
};
#endif //MOTIONAUTH_MULTILAYERPERCEPTRON_H
