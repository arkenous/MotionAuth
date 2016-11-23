//
// Created by Kensuke Kosaka on 2016/11/01.
//

#ifndef MOTIONAUTH_MULTILAYERPERCEPTRON_H
#define MOTIONAUTH_MULTILAYERPERCEPTRON_H
#include <zconf.h>
#include <cpu-features.h>

#include "Neuron.h"
class MultiLayerPerceptron {
public:
  MultiLayerPerceptron(unsigned long input, unsigned long middle, unsigned long output, unsigned long middle_layer, std::vector<std::string> neuron_params, int middle_layer_type, double dropout_rate);
  std::vector<std::string> learn(std::vector<std::vector<double>> x, std::vector<std::vector<double>> answer);
  std::vector<double> out(std::vector<double> input);

private:
  static const unsigned int MAX_TRIAL = 1000000; // 学習上限回数
  constexpr static const double MAX_GAP = 0.01; // 損失関数の出力がこの値以下になれば学習をスキップする
  int num_thread = android_getCpuCount(); // Androidデバイスのプロセッサのコア数

  std::string mlp_params = "";
  std::string sda_params = "";


  // ニューロン数
  unsigned long input_neuron_num = 0;
  unsigned long middle_neuron_num = 0;
  unsigned long output_neuron_num = 0;

  unsigned long middle_layer_number = 0; // 中間層の層数

  int middle_layer_type = 0; // 中間層の活性化関数の種類指定．0: identity 1: sigmoid 2: tanh 3: ReLU

  bool successFlg = true;

  std::vector<std::vector<Neuron>> sda_neurons;
  std::vector<std::vector<double>> sda_out;

  std::vector<std::vector<Neuron>> middleNeurons; // 中間層は複数層用意する
  std::vector<Neuron> outputNeurons;
  std::vector<std::vector<double>> h;
  std::vector<double> o;

  std::vector<std::vector<double>> learned_h;
  std::vector<double> learned_o;

  void setupSdA(std::string sda_params);
  void setupMLP(std::string mlp_params, double dropout_rate);
  std::vector<double> separate_by_camma(std::string input);

  void middleFirstLayerForwardThread(const int begin, const int end);
  void middleLayerForwardThread(const int layer, const int begin, const int end);
  void outForwardThread(const int begin, const int end);

  void outLearnThread(const std::vector<double> in, const std::vector<double> ans, const int begin, const int end);
  void middleLastLayerLearnThread(const int begin, const int end);
  void middleMiddleLayerLearnThread(const int layer, const int begin, const int end);
  void middleFirstLayerLearnThread(const int begin, const int end);
  void sdaLastLayerLearnThread(const int begin, const int end);
  void sdaMiddleLayerLearnThread(const int layer, const int begin, const int end);
  void sdaFirstLayerLearnThread(const std::vector<double> in, const int begin, const int end);

  void sdaFirstLayerOutThread(const std::vector<double> in, const int begin, const int end);
  void sdaOtherLayerOutThread(const int layer, const int begin, const int end);
  void middleFirstLayerOutThread(const int begin, const int end);
  void middleLayerOutThread(const int layer, const int begin, const int end);
  void outOutThread(const int begin, const int end);

  double cross_entropy(double output, double answer);

};
#endif //MOTIONAUTH_MULTILAYERPERCEPTRON_H
