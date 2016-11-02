//
// Created by Kensuke Kosaka on 2016/10/31.
//

#include "Neuron.h"


/**
 * Neuronのコンストラクタ
 * @param inputNeuronNum 入力ニューロン数（入力データ数）
 * @param weight 結合荷重の重み付けデータ
 * @return
 */
Neuron::Neuron(unsigned long inputNeuronNum, std::vector<double> weight, double threshold) {
  this->inputNeuronNum = inputNeuronNum;
  std::random_device rnd; // 非決定的乱数生成器
  std::mt19937 mt; // メルセンヌ・ツイスタ
  mt.seed(rnd());
  std::uniform_real_distribution<double> real_rnd(0.0, 1.0);

  // 0.0以外の閾値が渡されて入ればそれをセットし，そうでなければ乱数で初期化
  if (threshold != 0.0) this->threshold = threshold;
  else this->threshold = real_rnd(mt); // 閾値を乱数で設定

  // 結合荷重が渡されていればそれをセットし，無ければ乱数で初期化
  if (weight.size() > 0) for (int i = 0; i < weight.size(); ++i) this->inputWeights.push_back(weight[i]);
  else for (int i = 0; i < this->inputNeuronNum; ++i) this->inputWeights.push_back(real_rnd(mt));
}

/**
 * ニューロンの結合荷重を更新し，学習する
 * @param delta 修正量
 * @param inputValues 一つ前の層の出力データ
 */
void Neuron::learn(double delta, std::vector<double> inputValues) {
  this->delta = delta;

  // 結合荷重の更新
  for (int i = 0; i < this->inputNeuronNum; ++i) {
    this->inputWeights[i] += this->eta * this->delta * inputValues[i];
  }
}

/**
 * ニューロンの出力を得る
 * @param inputValues ニューロンの入力データ
 * @return ニューロンの出力
 */
double Neuron::output(std::vector<double> inputValues){
  double sum = -this->threshold;
  for (int i = 0; i < this->inputNeuronNum; ++i) {
    sum += inputValues[i] * this->inputWeights[i];
  }

//    std::cout << "sum: " << sum <<std::endl;

  // 活性化関数を適用し，出力値を得る
//    std::cout << "activation(sum): " << activation_tanh(sum) << std::endl;

  return activation_tanh(sum);
}

/**
 * 活性化関数 : シグモイド関数
 * @param x 入力
 * @return 計算結果
 */
double Neuron::activation_sigmoid(double x){
  return 1.0 / (1.0 + pow(M_E, -x));
}

/**
 * 活性化関数 : ランプ関数（ReLU）
 * @param x 入力
 * @return 計算結果
 */
double Neuron::activation_relu(double x) {
  return std::max(x, 0.0);
}

/**
 * 活性化関数 : tanh
 * @param x 入力
 * @return 計算結果
 */
double Neuron::activation_tanh(double x) {
  return tanh(x);
}

/**
 * このニューロンの指定された入力インデックスの結合荷重を返す
 * @param i 入力インデックス
 * @return 結合荷重
 */
double Neuron::getInputWeightIndexOf(int i){
  return this->inputWeights[i];
}

/**
 * このニューロンの閾値を返す
 */
double Neuron::getThreshold() {
  return this->threshold;
}

/**
 * 現在の修正量を返す
 * @return 修正量
 */
double Neuron::getDelta() {
  return this->delta;
}
