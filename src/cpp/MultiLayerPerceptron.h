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
  MultiLayerPerceptron(unsigned short input, unsigned short middle, unsigned short output, unsigned short middleLayer, std::string weightAndThreshold);
  std::string learn(std::vector<std::vector<double>> x, std::vector<std::vector<double>> answer);
  std::vector<double> out(std::vector<double> input);
private:
  static const unsigned int MAX_TRIAL = 10000000; // 学習上限回数
  constexpr static const double MAX_GAP = 0.001; // 許容する誤差の域値
//  int num_thread = (int)sysconf(_SC_NPROCESSORS_ONLN); // プロセッサのコア数
  int num_thread = android_getCpuCount(); // Androidデバイスのプロセッサのコア数


  // ニューロン数
  unsigned short inputNumber = 0;
  unsigned short middleNumber = 0;
  unsigned short outputNumber = 0;

  unsigned short middleLayerNumber = 0; // 中間層の層数

  bool successFlg = true;

  std::vector<std::vector<Neuron>> middleNeurons; // 中間層は複数層用意する
  std::vector<Neuron> outputNeurons;


  void outLearnThread(const std::vector<double> ans, const std::vector<double> o,
                      const std::vector<std::vector<double>> h, const int begin, const int end);
  void middleLastLayerLearnThread(const std::vector<std::vector<double>> h, const int begin, const int end);
  void middleMiddleLayerLearnThread(const std::vector<std::vector<double>> h, const int begin, const int end);
  void middleFirstLayerLearnThread(const std::vector<std::vector<double>> h, const std::vector<double> in, const int begin, const int end);
};
#endif //MOTIONAUTH_MULTILAYERPERCEPTRON_H
