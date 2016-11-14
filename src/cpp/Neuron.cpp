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
Neuron::Neuron(unsigned long inputNeuronNum, std::vector<double> weight, std::vector<double> g, double bias, int activationType) {
  this->inputNeuronNum = inputNeuronNum;
  this->activationType = activationType;
  std::random_device rnd; // 非決定的乱数生成器
  std::mt19937 mt; // メルセンヌ・ツイスタ
  mt.seed(rnd());
  std::uniform_real_distribution<double> real_rnd(0.0, 1.0);

  // 0.0以外のバイアスが渡されて入ればそれをセットし，そうでなければ乱数で初期化
  if (bias != 0.0) this->bias = bias;
  else this->bias = real_rnd(mt); // 閾値を乱数で設定

  // AdaGrad用のgが渡されていればそれをセットし，そうでなければ0.0で初期化する
  if (g.size() > 0) for (int i = 0; i < g.size(); ++i) this->g.push_back(g[i]);
  else this->g = std::vector<double>(inputNeuronNum, 0.0);

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

  // AdaGradによる学習率で，結合荷重を更新
  for (int i = 0; i < this->inputNeuronNum; ++i) {
    this->g[i] += pow(this->delta * inputValues[i], 2);

    this->inputWeights[i] -= (this->alpha / (sqrt(this->g[i]) + this->epsilon)) * (this->delta * inputValues[i]);
  }

  // SGDでバイアスを更新
  this->bias -= (this->alpha * this->delta) - (this->alpha * this->lambda * this->bias);
}

/**
 * ニューロンの出力を得る
 * @param inputValues ニューロンの入力データ
 * @return ニューロンの出力
 */
double Neuron::output(std::vector<double> inputValues){
  double sum = this->bias;
  for (int i = 0; i < this->inputNeuronNum; ++i) {
    sum += inputValues[i] * this->inputWeights[i];
  }

  // 得られた重み付き和を活性化関数に入れて出力を得る
  if (activationType == 0) return activation_identity(sum);
  else if (activationType == 1) return activation_sigmoid(sum);
  else if (activationType == 2) return activation_tanh(sum);
  else return activation_relu(sum);
}

/**
 * 活性化関数：恒等写像
 * @param x 入力
 * @return 計算結果
 */
double Neuron::activation_identity(double x) {
  return x;
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
 * 活性化関数 : tanh
 * @param x 入力
 * @return 計算結果
 */
double Neuron::activation_tanh(double x) {
  return tanh(x);
}

/**
 * 活性化関数 : ランプ関数（ReLU）
 * @param x 入力
 * @return 計算結果
 */
double Neuron::activation_relu(double x) {
  return std::max(0.0, x);
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
double Neuron::getBias() {
  return this->bias;
}

/**
 * 現在の修正量を返す
 * @return 修正量
 */
double Neuron::getDelta() {
  return this->delta;
}

double Neuron::getGIndexOf(int i){
  return this->g[i];
}