#include <math.h>
#include "Neuron.h"

using namespace std;

/**
 * vectorのサイズ確保のためだけに用いるNeuronのデフォルトコンストラクタ
 * @return Neuronのインスタンス
 */
Neuron::Neuron() { }

/**
 * Neuronのコンストラクタ
 * @param input_num 入力ニューロン数（入力データ数）
 * @param weight 結合荷重の重み付けデータ
 * @param dropout_rate Dropout率
 * @return Neuronのインスタンス
 */
Neuron::Neuron(const unsigned long input_num, const vector<double> &weight,
               const vector<double> &m, const vector<double> &nu,
               const unsigned long iteration, const double bias, const int activation_type,
               const double dropout_rate) {
  this->input_num = input_num;
  this->activation_type = activation_type;
  random_device rnd; // 非決定的乱数生成器
  mt19937 mt; // メルセンヌ・ツイスタ
  mt.seed(rnd());
  uniform_real_distribution<double> real_rnd(0.0, 1.0);

  // 0.0以外のバイアスが渡されて入ればそれをセットし，そうでなければ乱数で初期化
  if (bias != 0.0) this->bias = bias;
  else this->bias = real_rnd(mt); // 閾値を乱数で設定

  // Adamの各パラメータについて，学習済みのものが渡されていればセットし，そうでなければ0.0で初期化
  if (iteration != 0) this->iteration = iteration;
  else this->iteration = 0;

  if (m.size() > 0) this->m = vector<double>(m);
  else this->m = vector<double>(input_num, 0.0);

  if (nu.size() > 0) this->nu = vector<double>(nu);
  else this->nu = vector<double>(input_num, 0.0);

  // 結合荷重が渡されていればそれをセットし，無ければ乱数で初期化
  if (weight.size() > 0) this->inputWeights = vector<double>(weight);
  else {
    this->inputWeights.resize(input_num);
    for (int i = 0; i < this->input_num; ++i) this->inputWeights[i] = real_rnd(mt);
  }

  this->dropout_rate = dropout_rate;
}

/**
 * 受け取った0.0以上1.0未満の乱数値からdropout_maskを設定する
 * @param random_value 0.0以上1.0未満の乱数値
 */
void Neuron::dropout(const double random_value) {
  if (random_value < dropout_rate) this->dropout_mask = 0.0;
  else this->dropout_mask = 1.0;
}

/**
 * dropout_maskが1.0であれば，Adamを用いてニューロンの結合荷重を学習し，SGDでバイアスを更新する
 * @param delta 修正量
 * @param inputValues 一つ前の層の出力データ
 */
void Neuron::learn(const double delta, const vector<double> &inputValues) {
  this->delta = delta;
  double gradient;

  if (this->dropout_mask == 1.0) {
    // Adamを用いて，結合荷重を更新
    this->iteration += 1;
    for (int i = 0; i < input_num; ++i) {
      gradient = this->delta * inputValues[i];
      this->m[i] = this->beta_one * this->m[i]
          + (1 - this->beta_one) * gradient;
      this->nu[i] = this->beta_two * this->nu[i]
          + (1 - this->beta_two) * pow(gradient, 2);
      this->inputWeights[i] -= this->alpha *
          ((this->m[i] / (1 - pow(this->beta_one, this->iteration))) /
              (sqrt(this->nu[i] / (1 - pow(this->beta_two, this->iteration))) + this->epsilon));
    }

    // SGDでバイアスを更新
    this->bias -= (this->alpha * this->delta) - (this->alpha * this->lambda * this->bias);
  }
}

/**
 * ニューロンの出力を得て，それにdropout_maskを掛ける
 * @param inputValues ニューロンの入力データ
 * @return ニューロンの出力
 */
double Neuron::learn_output(const vector<double> &inputValues) {
  double sum = this->bias;
  for (int i = 0; i < this->input_num; ++i) sum += inputValues[i] * this->inputWeights[i];

  double activated;
  if (activation_type == 0) activated = activation_identity(sum);
  else if (activation_type == 1) activated = activation_sigmoid(sum);
  else if (activation_type == 2) activated = activation_tanh(sum);
  else activated = activation_relu(sum);

  return activated * this->dropout_mask;
}

/**
 * ニューロンの出力を得る．バイアスや重み付けにdropout_ratioを掛けて処理する
 * @param inputValues ニューロンの入力データ
 * @return ニューロンの出力
 */
double Neuron::output(const vector<double> &inputValues) {
  double sum = this->bias * (1.0 - this->dropout_rate);
  for (int i = 0; i < this->input_num; ++i)
    sum += inputValues[i] * (this->inputWeights[i] * (1.0 - this->dropout_rate));

  double activated;
  // 得られた重み付き和を活性化関数に入れて出力を得る
  if (activation_type == 0) activated = activation_identity(sum);
  else if (activation_type == 1) activated = activation_sigmoid(sum);
  else if (activation_type == 2) activated = activation_tanh(sum);
  else activated = activation_relu(sum);

  return activated;
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
double Neuron::activation_sigmoid(double x) {
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
  return max(0.0, x);
}

unsigned long Neuron::getInputNum(){
  return this->input_num;
}

/**
 * このニューロンの指定された入力インデックスの結合荷重を返す
 * @param i 入力インデックス
 * @return 結合荷重
 */
double Neuron::getInputWeightIndexOf(unsigned long i) {
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

double Neuron::getMIndexOf(unsigned long i) {
  return this->m[i];
}

double Neuron::getNuIndexOf(unsigned long i) {
  return this->nu[i];
}

unsigned long Neuron::getIteration() {
  return this->iteration;
}
