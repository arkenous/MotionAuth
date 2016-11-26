
#include <random>
#include <sstream>
#include <iostream>
#include <android/log.h>
#include "StackedDenoisingAutoencoder.h"
#include "DenoisingAutoencoder.h"

using namespace std;

StackedDenoisingAutoencoder::StackedDenoisingAutoencoder() { }

string StackedDenoisingAutoencoder::learn(const vector<vector<double>> input,
                                          const unsigned long result_num_dimen,
                                          const float compression_rate) {
  stringstream ss;

  vector<vector<double>> answer(input);
  vector<vector<double>> noisy_input = add_noise(input, 0.1);
  DenoisingAutoencoder denoisingAutoencoder(noisy_input[0].size(), compression_rate);
  ss << denoisingAutoencoder.learn(answer, noisy_input) << ':';
  num_middle_neurons = denoisingAutoencoder.getCurrentMiddleNeuronNum();

  __android_log_print(ANDROID_LOG_INFO, "SdA", "num_middle_neurons: %lu", num_middle_neurons);

  while (num_middle_neurons > result_num_dimen) {
    answer = vector<vector<double>>(noisy_input);
    noisy_input = add_noise(denoisingAutoencoder.getMiddleOutput(noisy_input), 0.1);
    denoisingAutoencoder = DenoisingAutoencoder(noisy_input[0].size(), compression_rate);
    ss << denoisingAutoencoder.learn(answer, noisy_input) << ':';
    num_middle_neurons = denoisingAutoencoder.getCurrentMiddleNeuronNum();
    __android_log_print(ANDROID_LOG_INFO, "SdA", "num_middle_neurons: %lu", num_middle_neurons);
  }

  // 末尾の余計な : を削除する
  string result = ss.str();
  result.pop_back();
  ss.str("");
  ss.clear(stringstream::goodbit);

  num_middle_neurons = denoisingAutoencoder.getCurrentMiddleNeuronNum();

  return result;
}

/**
 * データごとに0.0以上1.0未満の乱数を生成し，rate未満であればそのデータを0.0にする
 * @param input ノイズをのせるデータ
 * @param rate ノイズをのせる確率
 * @return ノイズをのせたデータ
 */
vector<vector<double>> StackedDenoisingAutoencoder::add_noise(vector<vector<double>> input,
                                                              float rate) {
  random_device rnd;
  mt19937 mt;
  mt.seed(rnd());
  uniform_real_distribution<double> real_rnd(0.0, 1.0);

  for (int i = 0; i < input.size(); ++i)
    for (int j = 0; j < input[i].size(); ++j)
      if (real_rnd(mt) < rate) input[i][j] = 0.0;

  return input;
}

unsigned long StackedDenoisingAutoencoder::getNumMiddleNeuron() {
  return num_middle_neurons;
}
