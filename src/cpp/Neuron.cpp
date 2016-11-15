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
Neuron::Neuron(unsigned long inputNeuronNum, std::vector<double> weight, int iteration, std::vector<double> m, std::vector<double> nu, std::vector<double> m_hat, std::vector<double> nu_hat, double bias, int activationType) {
  this->inputNeuronNum = inputNeuronNum;
  this->activationType = activationType;
  std::random_device rnd; // 非決定的乱数生成器
  std::mt19937 mt; // メルセンヌ・ツイスタ
  mt.seed(rnd());
  std::uniform_real_distribution<double> real_rnd(0.0, 1.0);

  // 0.0以外のバイアスが渡されて入ればそれをセットし，そうでなければ乱数で初期化
  if (bias != 0.0) this->bias = bias;
  else this->bias = real_rnd(mt); // 閾値を乱数で設定

  // Adamの各パラメータについて，学習済みのものが渡されていればセットし，そうでなければ0.0で初期化
  if (iteration != 0) this->iteration = iteration;
  else this->iteration = 0;

  if (m.size() > 0) for (int i = 0; i < this->inputNeuronNum; ++i) this->m.push_back(m[i]);
  else this->m = std::vector<double>(inputNeuronNum, 0.0);

  if (nu.size() > 0) for (int i = 0; i < this->inputNeuronNum; ++i) this->nu.push_back(nu[i]);
  else this->nu = std::vector<double>(inputNeuronNum, 0.0);

  if (m_hat.size() > 0) for (int i = 0; i < this->inputNeuronNum; ++i) this->m_hat.push_back(m_hat[i]);
  else this->m_hat = std::vector<double>(inputNeuronNum, 0.0);

  if (nu_hat.size() > 0) for (int i = 0; i < this->inputNeuronNum; ++i) this->nu_hat.push_back(nu_hat[i]);
  else this->nu_hat = std::vector<double>(inputNeuronNum, 0.0);

  // 結合荷重が渡されていればそれをセットし，無ければ乱数で初期化
  if (weight.size() > 0) for (int i = 0; i < this->inputNeuronNum; ++i) this->inputWeights.push_back(weight[i]);
  else for (int i = 0; i < this->inputNeuronNum; ++i) this->inputWeights.push_back(real_rnd(mt));
}

/**
 * ニューロンの結合荷重を更新し，学習する
 * @param delta 修正量
 * @param inputValues 一つ前の層の出力データ
 */
void Neuron::learn(double delta, std::vector<double> inputValues) {
  this->delta = delta;

  // Adamを用いて，結合荷重を更新
  this->iteration += 1;
  for (int i = 0; i < inputNeuronNum; ++i) {
    this->m[i] = this->beta_one * this->m[i] + (1 - this->beta_one) * (this->delta * inputValues[i]);
    this->nu[i] = this->beta_two * this->nu[i] + (1 - this->beta_two) * pow((this->delta * inputValues[i]), 2);
    this->m_hat[i] = this->m[i] / (1 - pow(this->beta_one, this->iteration));
    this->nu_hat[i] = sqrt(this->nu[i] / (1 - pow(this->beta_two, this->iteration))) + this->epsilon;
    this->inputWeights[i] -= this->alpha * (this->m_hat[i] / this->nu_hat[i]);
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

//double Neuron::getGIndexOf(int i){
//  return this->g[i];
//}

double Neuron::getMIndexOf(int i) {
  return this->m[i];
}

double Neuron::getNuIndexOf(int i) {
  return this->nu[i];
}

double Neuron::getMHatIndexOf(int i){
  return this->m_hat[i];
}

double Neuron::getNuHatIndexOf(int i) {
  return this->nu_hat[i];
}

int Neuron::getIteration() {
  return this->iteration;
}