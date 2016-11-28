
#ifndef MOTIONAUTH_MULTILAYERPERCEPTRON_H
#define MOTIONAUTH_MULTILAYERPERCEPTRON_H

#include <zconf.h>
#include <cpu-features.h>
#include "Neuron.h"

using namespace std;

class MultiLayerPerceptron {
 public:
  MultiLayerPerceptron(unsigned long output, unsigned long middle_layer,
                       vector<string> neuron_params,
                       int middle_layer_type, double dropout_rate);
  vector<string> learn(vector<vector<double>> x, vector<vector<double>> answer);
  vector<double> out(vector<double> input);

 private:
  static const unsigned int MAX_TRIAL = 1000000; // 学習上限回数
  constexpr static const double MAX_GAP = 0.1; // 損失関数の出力がこの値以下になれば学習をスキップする
  int num_thread = android_getCpuCount(); // Androidデバイスのプロセッサのコア数

  string mlp_params = "";
  string sda_params = "";

  // ニューロン数
  unsigned long input_neuron_num = 0;
  unsigned long middle_neuron_num = 0;
  unsigned long output_neuron_num = 0;

  unsigned long middle_layer_number = 0; // 中間層の層数

  int middle_layer_type = 0; // 中間層の活性化関数の種類指定．0: identity 1: sigmoid 2: tanh 3: ReLU

  bool successFlg = true;

  vector<vector<Neuron>> sda_neurons;
  vector<vector<double>> sda_out;

  vector<vector<Neuron>> middleNeurons; // 中間層は複数層用意する
  vector<Neuron> outputNeurons;
  vector<vector<double>> h;
  vector<double> o;

  vector<vector<double>> learned_h;
  vector<double> learned_o;

  void setupSdA(string sda_params);
  void setupMLP(string mlp_params, double dropout_rate);
  vector<double> separate_by_camma(string input);

  void middleFirstLayerForwardThread(const int begin, const int end);
  void middleLayerForwardThread(const int layer, const int begin, const int end);
  void outForwardThread(const int begin, const int end);

  void outLearnThread(const vector<double> ans, const int begin, const int end);
  void middleLastLayerLearnThread(const int begin, const int end);
  void middleMiddleLayerLearnThread(const int layer, const int begin, const int end);
  void middleFirstLayerLearnThread(const int begin, const int end);

  void sdaFirstLayerOutThread(const vector<double> in, const int begin, const int end);
  void sdaOtherLayerOutThread(const int layer, const int begin, const int end);
  void middleFirstLayerOutThread(const int begin, const int end);
  void middleLayerOutThread(const int layer, const int begin, const int end);
  void outOutThread(const int begin, const int end);

  double cross_entropy(double output, double answer);

};
#endif //MOTIONAUTH_MULTILAYERPERCEPTRON_H
