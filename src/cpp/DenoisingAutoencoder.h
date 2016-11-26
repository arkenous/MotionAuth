
#ifndef MOTIONAUTH_DENOISINGAUTOENCODER_H
#define MOTIONAUTH_DENOISINGAUTOENCODER_H

#include <string>
#include <cpu-features.h>
#include <vector>
#include "Neuron.h"

using namespace std;

class DenoisingAutoencoder {
 public:
  DenoisingAutoencoder(unsigned long num_input, float compression_rate);
  string learn(vector<vector<double>> input, vector<vector<double>> noisy_input);
  vector<vector<double>> getMiddleOutput(vector<vector<double>> noisy_input);
  unsigned long getCurrentMiddleNeuronNum();

 private:
  static const unsigned int MAX_TRIAL = 10000; // 学習上限回数
  constexpr static const double MAX_GAP = 30.0; // 損失関数の出力がこの値以下になれば学習をスキップする
  int num_thread = android_getCpuCount(); // Androidデバイスのプロセッサのコア数

  unsigned long input_neuron_num;
  unsigned long middle_neuron_num;
  unsigned long output_neuron_num;

  int middle_layer_type = 0; // 中間層の活性化関数の種類指定：0: identity, 1: sigmoid, 2: tanh, 3: ReLU

  bool successFlg = true;

  vector<Neuron> middle_neurons;
  vector<Neuron> output_neurons;

  vector<double> h; // 中間層の出力値
  vector<double> o; // 出力層の出力値
  vector<double> learned_h;
  vector<double> learned_o;

  void middleForwardThread(const vector<double> in, const int begin, const int end);
  void outForwardThread(const int begin, const int end);
  void outLearnThread(const vector<double> in, const vector<double> ans,
                      const int begin, const int end);
  void middleLearnThread(const vector<double> in, const int begin, const int end);
  void middleOutThread(const vector<double> in, const int begin, const int end);

  double mean_squared_error(double output, double answer);
};

#endif //MOTIONAUTH_DENOISINGAUTOENCODER_H
