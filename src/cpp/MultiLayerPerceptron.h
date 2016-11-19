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
  MultiLayerPerceptron(unsigned short input, unsigned short middle, unsigned short output, unsigned short middle_layer, std::string neuron_params, int middle_layer_type, double dropout_ratio);
  std::string learn(std::vector<std::vector<double>> x, std::vector<std::vector<double>> answer);
  std::vector<double> out(std::vector<double> input);
private:
  static const unsigned int MAX_TRIAL = 5000; // 学習上限回数
  constexpr static const double MAX_GAP = 0.1; // 許容する誤差の域値
  int num_thread = android_getCpuCount(); // Androidデバイスのプロセッサのコア数


  // ニューロン数
  unsigned short input_neuron_num = 0;
  unsigned short middle_neuron_num = 0;
  unsigned short output_neuron_num = 0;

  unsigned short middle_layer_number = 0; // 中間層の層数

  int middle_layer_type = 0; // 中間層の活性化関数の種類指定．0: identity 1: sigmoid 2: tanh 3: ReLU

  bool successFlg = true;

  std::vector<std::vector<Neuron>> middleNeurons; // 中間層は複数層用意する
  std::vector<Neuron> outputNeurons;
  std::vector<std::vector<double>> h;
  std::vector<double> o;

  void middleFirstLayerForwardThread(const std::vector<double> in, const int begin, const int end);
  void middleLayerForwardThread(const int layer, const int begin, const int end);
  void outForwardThread(const int begin, const int end);
  void outLearnThread(const std::vector<double> in, const std::vector<double> ans, const std::vector<double> o,
                      const std::vector<std::vector<double>> h, const int begin, const int end);
  void middleLastLayerLearnThread(const std::vector<std::vector<double>> h, const int begin, const int end);
  void middleMiddleLayerLearnThread(const std::vector<std::vector<double>> h, const int layer, const int begin, const int end);
  void middleFirstLayerLearnThread(const std::vector<std::vector<double>> h, const std::vector<double> in, const int begin, const int end);
  std::vector<double> separate_by_camma(std::string input);
  std::vector<Neuron> setup_layer_by_params(std::vector<std::string> params, int previous_neurons_num, int layer_neuron_num, unsigned long input_number, int activation_type, double dropout_ratio);
};
#endif //MOTIONAUTH_MULTILAYERPERCEPTRON_H
