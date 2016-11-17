//
// Created by Kensuke Kosaka on 2016/11/01.
//

#include "MultiLayerPerceptron.h"

#include <thread>
#include <sstream>
using namespace std;

/**
 * MultiLayerPerceptronのコンストラクタ
 * @param input 入力層のニューロン数
 * @param middle 中間層のニューロン数
 * @param output 出力層のニューロン数
 * @param middleLayer 中間層の層数
 * @param neuronParams 全ニューロンの重みづけ，AdaGradのg，バイアスデータ
 * @param middle_layer_type 中間層の活性化関数の種類指定．0: identity 1: sigmoid 2: tanh 3: ReLU
 * @return MLPインスタンス
 */
MultiLayerPerceptron::MultiLayerPerceptron(unsigned short input, unsigned short middle, unsigned short output, unsigned short middle_layer, std::string neuron_params, int middle_layer_type) {
  this->input_neuron_num = input;
  this->middle_neuron_num = middle;
  this->output_neuron_num = output;
  this->middle_layer_number = middle_layer;
  this->middle_layer_type = middle_layer_type;

  // ニューロンごとのパラメータを取得する
  std::vector<Neuron> neuronPerLayer;
  if (neuron_params.length() <= 0) {
    std::vector<double> emptyVector;
    // 中間層
    for (int layer = 0; layer < middle_layer_number; ++layer) {
      if (layer == 0) {
        for (int neuron = 0; neuron < middle_neuron_num; ++neuron) {
          // 中間層の最初の層については，入力層のニューロン数がニューロンへの入力数になる
          neuronPerLayer.push_back(
              Neuron(input_neuron_num, emptyVector, 0, emptyVector, emptyVector, emptyVector, emptyVector, 0.0, middle_layer_type));
        }
      } else {
        for (int neuron = 0; neuron < middle_neuron_num; ++neuron) {
          // それ以降の層については，中間層の各層のニューロン数がニューロンへの入力数になる
          neuronPerLayer.push_back(
              Neuron(middle_neuron_num, emptyVector, 0, emptyVector, emptyVector, emptyVector, emptyVector, 0.0, middle_layer_type));
        }
      }
      this->middleNeurons.push_back(neuronPerLayer);
      neuronPerLayer.clear();
    }

    // 出力層
    for (int neuron = 0; neuron < output; ++neuron) {
      this->outputNeurons.push_back(Neuron(middle_neuron_num, emptyVector, 0, emptyVector, emptyVector, emptyVector, emptyVector, 0.0, 1));
    }
  } else {
    // ニューロン単位でデータを分割する
    std::vector<std::string> splitByNeuron;
    std::stringstream ssByNeuron(neuron_params);
    std::string itemPerNeuron;
    while (std::getline(ssByNeuron, itemPerNeuron, '\'')) {
      if (!itemPerNeuron.empty()) {
        splitByNeuron.push_back(itemPerNeuron);
      }
    }
    itemPerNeuron = "";
    ssByNeuron.str("");
    ssByNeuron.clear(stringstream::goodbit);

    for (int layer = 0; layer < middle_layer_number; ++layer) {
      if (layer == 0) {
        // 中間層一層目については，入力層のニューロン数がニューロンへの入力数となる
        neuronPerLayer = setup_layer_by_params(splitByNeuron, layer * middle_neuron_num, middle_neuron_num, input_neuron_num, middle_layer_type);
      } else {
        // それ以降の層については，中間層の各層のニューロン数がニューロンへの入力数となる
        neuronPerLayer = setup_layer_by_params(splitByNeuron, layer * middle_neuron_num, middle_neuron_num, middle_neuron_num, middle_layer_type);
      }
      this->middleNeurons.push_back(neuronPerLayer);
      neuronPerLayer.clear();
    }

    neuronPerLayer = setup_layer_by_params(splitByNeuron, middle_layer_number * middle_neuron_num, output_neuron_num, middle_neuron_num, 1);
    this->outputNeurons = neuronPerLayer;

    splitByNeuron.clear();
    ssByNeuron.str("");
    ssByNeuron.clear(stringstream::goodbit);
    itemPerNeuron = "";
  }
}

/**
 * 教師入力データと教師出力データを元にニューラルネットワークを学習する
 * @param x 二次元の教師入力データ，データセット * データ
 * @param answer 教師入力データに対応した二次元の教師出力データ，データセット * データ
 * @return ニューロンごとの学習後の重み付けが入ったdouble型二次元ベクトル
 */
std::string MultiLayerPerceptron::learn(std::vector<std::vector<double>> x, std::vector<std::vector<double>> answer) {
  std::vector<std::vector<double>> h = std::vector<std::vector<double>>(middle_layer_number, std::vector<double>(middle_neuron_num, 0.0));
  std::vector<double> o = std::vector<double>(output_neuron_num, 0.0);

  int succeed = 0; //  連続正解回数のカウンタを初期化
  for (int trial = 0; trial < this->MAX_TRIAL; ++trial) {
    // 使用する教師データを選択
    std::vector<double> in = x[trial % answer.size()]; // 利用する教師入力データ
    std::vector<double> ans = answer[trial % answer.size()]; // 教師出力データ

    // 出力値を推定：1層目の中間層の出力計算
    for (int neuron = 0; neuron < this->middle_neuron_num; ++neuron) h[0][neuron] = middleNeurons[0][neuron].output(in);

    // 一つ前の中間層より得られた出力を用いて，以降の中間層を順に計算
    for (int layer = 1; layer < middle_layer_number; ++layer) {
      for (int neuron = 0; neuron < middle_neuron_num; ++neuron) {
        h[layer][neuron] = middleNeurons[layer][neuron].output(h[layer - 1]);
      }
    }

    // 出力値を推定：中間層の最終層の出力を用いて，出力層の出力計算
    for (int neuron = 0; neuron < output_neuron_num; ++neuron) o[neuron] = outputNeurons[neuron].output(h[middle_layer_number - 1]);

    successFlg = true;

    //region 出力層を学習する
    std::vector<std::thread> threads;
    int charge = 1;
    if (output_neuron_num <= num_thread) charge = 1;
    else charge = output_neuron_num / num_thread;
    for (int i = 0; i < output_neuron_num; i += charge) {
      if (i != 0 && output_neuron_num / i == 1) threads.push_back(std::thread(&MultiLayerPerceptron::outLearnThread, this, std::ref(ans), std::ref(o), std::ref(h), i, output_neuron_num));
      else threads.push_back(std::thread(&MultiLayerPerceptron::outLearnThread, this, std::ref(ans), std::ref(o), std::ref(h), i, i + charge));
    }
    for (std::thread &th : threads) th.join();
    //endregion

    // 連続成功回数による終了判定
    if (successFlg) {
      succeed++;
      if (succeed >= x.size()) break;
      else continue;
    } else succeed = 0;

    //region 中間層の更新．末尾層から先頭層に向けて更新する

    //region 中間層の層数が2以上の場合のみ，中間層の最終層の学習をする
    if (middle_layer_number > 1) {
      threads.clear();
      if (middle_neuron_num <= num_thread) charge = 1;
      else charge = middle_neuron_num / num_thread;
      for (int i = 0; i < middle_neuron_num; i += charge) {
        if (i != 0 && middle_neuron_num / i == 1) threads.push_back(std::thread(&MultiLayerPerceptron::middleLastLayerLearnThread, this, std::ref(h), i, middle_neuron_num));
        else threads.push_back(std::thread(&MultiLayerPerceptron::middleLastLayerLearnThread, this, std::ref(h), i, i + charge));
      }
      for (std::thread &th : threads) th.join();
    }
    //endregion

    //region 出力層と入力層に最も近い層一つずつを除いた残りの中間層を入力層に向けて学習する
    threads.clear();
    if (middle_neuron_num <= num_thread) charge = 1;
    else charge = middle_neuron_num / num_thread;
    for (int i = 0; i < middle_neuron_num; i += charge) {
      if (i != 0 && middle_neuron_num / i == 1) threads.push_back(std::thread(&MultiLayerPerceptron::middleMiddleLayerLearnThread, this, std::ref(h), i, middle_neuron_num));
      else threads.push_back(std::thread(&MultiLayerPerceptron::middleMiddleLayerLearnThread, this, std::ref(h), i, i + charge));
    }
    for (std::thread &th : threads) th.join();
    //endregion

    //region 中間層の最初の層を学習する
    threads.clear();
    if (middle_neuron_num <= num_thread) charge = 1;
    else charge = middle_neuron_num / num_thread;
    for (int i = 0; i < middle_neuron_num; i += charge) {
      if (i != 0 && middle_neuron_num / i == 1) threads.push_back(std::thread(&MultiLayerPerceptron::middleFirstLayerLearnThread, this, std::ref(h), std::ref(in), i, middle_neuron_num));
      else threads.push_back(std::thread(&MultiLayerPerceptron::middleFirstLayerLearnThread, this, std::ref(h), std::ref(in), i, i + charge));
    }
    for (std::thread &th : threads) th.join();
    //endregion

    //endregion

    // 再度出力
    // 出力値を推定：中間層の出力計算
    // 1層目の中間層の出力を計算
    for (int neuron = 0; neuron < middle_neuron_num; ++neuron) h[0][neuron] = middleNeurons[0][neuron].output(in);

    // 一つ前の中間層より得られた出力を用いて，以降の中間層を順に計算
    for (int layer = 1; layer < middle_layer_number; ++layer) {
      for (int neuron = 0; neuron < middle_neuron_num; ++neuron) {
        h[layer][neuron] = middleNeurons[layer][neuron].output(h[layer - 1]);
      }
    }

    // 出力値を推定：出力層の出力計算
    // 中間層の最終層の出力を用いて，出力層の出力を計算
    for (int neuron = 0; neuron < output_neuron_num; ++neuron) o[neuron] = outputNeurons[neuron].output(h[middle_layer_number - 1]);
  }

  // 全ての教師データで正解を出すか，収束限度回数を超えた場合に終了

  std::string neuronParams;
  std::stringstream ss;
  for (int layer = 0; layer < middle_layer_number; ++layer) {
    for (int neuron = 0; neuron < middle_neuron_num; ++neuron) {
      // 重みを詰める
      for (int weightNum = 0; weightNum < input_neuron_num; ++weightNum) {
        ss << middleNeurons[layer][neuron].getInputWeightIndexOf(weightNum) << ',';
      }
      ss << '|';

      // Adamのmを詰める
      for (int mNum = 0; mNum < input_neuron_num; ++mNum) {
        ss << middleNeurons[layer][neuron].getMIndexOf(mNum) << ',';
      }
      ss << '|';

      // Adamのnuを詰める
      for (int nuNum = 0; nuNum < input_neuron_num; ++nuNum) {
        ss << middleNeurons[layer][neuron].getNuIndexOf(nuNum) << ',';
      }
      ss << '|';

      // Adamのm_hatを詰める
      for (int mHatNum = 0; mHatNum < input_neuron_num; ++mHatNum) {
        ss << middleNeurons[layer][neuron].getMHatIndexOf(mHatNum) << ',';
      }
      ss << '|';

      // Adamのnu_hatを詰める
      for (int nuHatNum = 0; nuHatNum < input_neuron_num; ++nuHatNum) {
        ss << middleNeurons[layer][neuron].getNuHatIndexOf(nuHatNum) << ',';
      }
      ss << '|';

      // Adamのiterationを詰める
      ss << middleNeurons[layer][neuron].getIteration() << '|';

      // バイアスを入れ，最後に ' を入れる
      ss << middleNeurons[layer][neuron].getBias() << '\'';
    }
  }
  for (int neuron = 0; neuron < output_neuron_num; ++neuron) {
    // 重みを詰める
    for (int weightNum = 0; weightNum < input_neuron_num; ++weightNum) {
      ss << outputNeurons[neuron].getInputWeightIndexOf(weightNum) << ",";
    }
    ss << '|';

    //Adamのmを詰める
    for (int mNum = 0; mNum < input_neuron_num; ++mNum) {
      ss << outputNeurons[neuron].getMIndexOf(mNum) << ',';
    }
    ss << '|';

    // Adamのnuを詰める
    for (int nuNum = 0; nuNum < input_neuron_num; ++nuNum) {
      ss << outputNeurons[neuron].getNuIndexOf(nuNum) << ',';
    }
    ss << '|';

    // Adamのm_hatを詰める
    for (int mHatNum = 0; mHatNum < input_neuron_num; ++mHatNum) {
      ss << outputNeurons[neuron].getMHatIndexOf(mHatNum) << ',';
    }
    ss << '|';

    // Adamのnu_hatを詰める
    for (int nuHatNum = 0; nuHatNum < input_neuron_num; ++nuHatNum) {
      ss << outputNeurons[neuron].getNuHatIndexOf(nuHatNum) << ',';
    }
    ss << '|';

    // Adamのiterationを詰める
    ss << outputNeurons[neuron].getIteration() << '|';

    // バイアスを入れ，最後に ' を入れる
    ss << outputNeurons[neuron].getBias() << '\'';
  }

  neuronParams = ss.str();
  // 末尾の ' を削除する
  neuronParams.pop_back();
  ss.str(""); // バッファをクリアする
  ss.clear(stringstream::goodbit); // ストリームの状態をクリアする

  return neuronParams;
}

/**
 * 受け取ったパラメータ情報を分割し，層ごとのニューロンを構築していく
 * @param params パラメータ情報
 * @param previous_neurons_num 設定している層以前の入力層を除く全てのニューロン個数
 * @param layer_neuron_num 設定している層のニューロンする
 * @param input_number ニューロンの入力数
 * @param activation_type ニューロンの活性化関数
 * @return 構築が完了したニューロン層
 */
std::vector<Neuron> MultiLayerPerceptron::setup_layer_by_params(std::vector<std::string> params, int previous_neurons_num, int layer_neuron_num, unsigned long input_number, int activation_type) {
  std::vector<Neuron> neuronPerLayer;
  std::stringstream ss;
  std::string item;
  std::vector<std::string> elems;

  for (int neuron = 0; neuron < layer_neuron_num; ++neuron) {
    // 重みづけ，Adamのm，nu，m_hat，nu_hat，iteration，バイアスの七つに分割する
    ss = std::stringstream(params[previous_neurons_num + neuron]);

    while (std::getline(ss, item, '|')) {
      if (!item.empty()) elems.push_back(item);
    }
    item = "";
    ss.str("");
    ss.clear(stringstream::goodbit);

    // まずはバイアスを取り出す
    double bias = std::stod(elems.back());
    elems.pop_back();

    // iterationを取り出す
    int iteration = std::stoi(elems.back());
    elems.pop_back();

    // 重みを取り出す
    std::vector<double> weight = separate_by_camma(elems[0]);
    // Adamのmを取り出す
    std::vector<double> m = separate_by_camma(elems[1]);
    // Adamのnuを取り出す
    std::vector<double> nu = separate_by_camma(elems[2]);
    // Adamのm_hatを取り出す
    std::vector<double> m_hat = separate_by_camma(elems[3]);
    // Adamのnu_hatを取り出す
    std::vector<double> nu_hat = separate_by_camma(elems[4]);

    neuronPerLayer.push_back(Neuron(input_number, weight, iteration, m, nu, m_hat, nu_hat, bias, middle_layer_type));

    elems.clear();
  }

  return neuronPerLayer;
}

std::vector<double> MultiLayerPerceptron::separate_by_camma(std::string input) {
  std::vector<double> result;
  std::stringstream ss = std::stringstream(input);
  std::string item;
  while (std::getline(ss, item, ',')) {
    if (!item.empty()) result.push_back(std::stod(item));
  }
  item = "";
  ss.str("");
  ss.clear(stringstream::goodbit);

  return result;
}

/**
 * 出力層の学習，スレッドを用いて並列学習するため，学習するニューロンの開始点と終了点も必要
 * 誤差関数には交差エントロピーを，活性化関数にシグモイド関数を用いるため，deltaは 出力 - 教師出力 で得られる
 * @param ans 教師出力データ
 * @param o 出力層の出力データ
 * @param h 中間層の出力データ
 * @param begin 学習するニューロンセットの開始点
 * @param end 学習するニューロンセットの終了点
 */
void MultiLayerPerceptron::outLearnThread(const std::vector<double> ans, const std::vector<double> o,
                                          const std::vector<std::vector<double>> h, const int begin, const int end){
  for (int neuron = begin; neuron < end; ++neuron) {
    // 出力層ニューロンのdeltaの計算
    double delta = o[neuron] - ans[neuron];

    // 教師データとの誤差が十分小さい場合は学習しない．そうでなければ正解フラグをfalseに
    if (std::abs(ans[neuron] - o[neuron]) < MAX_GAP) continue;
    else successFlg = false;

    // 出力層の学習
    outputNeurons[neuron].learn(delta, h[middle_layer_number - 1]);
  }
}

/**
 * 中間層の最終層の学習．中間層の層数が2以上の場合のみこれを使う．
 * 活性化関数に何を使うかで，deltaの計算式が変わる
 * @param h 中間層の出力データ
 * @param begin 学習するニューロンセットの開始点
 * @param end 学習するニューロンセットの終了点
 */
void MultiLayerPerceptron::middleLastLayerLearnThread(const std::vector<std::vector<double>> h, const int begin,
                                                      const int end){
  for (int neuron = begin; neuron < end; ++neuron) {
    // 中間層ニューロンのdeltaを計算
    double sumDelta = 0.0;
    for (int k = 0; k < output_neuron_num; ++k) {
      Neuron n = outputNeurons[k];
      sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
    }

    // どの活性化関数を用いるかで，deltaの計算方法が変わる
    double delta;
    if (middle_layer_type == 0) delta = 1.0 * sumDelta;
    else if (middle_layer_type == 1) delta = (h[middle_layer_number - 1][neuron] * (1.0 - h[middle_layer_number - 1][neuron])) * sumDelta;
    else if (middle_layer_type == 2) delta = (1.0 - pow(h[middle_layer_number - 1][neuron], 2)) * sumDelta;
    else {
      // ReLU
      if (h[middle_layer_number - 1][neuron] > 0) delta = 1.0 * sumDelta;
      else delta = 0 * sumDelta;
    }

    // 学習
    middleNeurons[middle_layer_number - 1][neuron].learn(delta, h[middle_layer_number - 2]);
  }
}

/**
 * 出力層と入力層に最も近い層一つずつを除いた残りの中間層を入力層に向けて学習する．中間層が3層以上の場合にこれを使う．
 * @param h 中間層の出力データ
 * @param begin 学習するニューロンセットの開始点
 * @param end 学習するニューロンセットの終了点
 */
void MultiLayerPerceptron::middleMiddleLayerLearnThread(const std::vector<std::vector<double>> h, const int begin,
                                                        const int end) {
  for (int neuron = begin; neuron < end; ++neuron) {
    for (int layer = (int)middle_layer_number - 2; layer >= 1; --layer) {
      // 中間層ニューロンのdeltaを計算
      double sumDelta = 0.0;
      for (int k = 0; k < middle_neuron_num; ++k) {
        Neuron n = middleNeurons[layer + 1][k];
        sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
      }

      double delta;
      if (middle_layer_type == 0) delta = 1.0 * sumDelta;
      else if (middle_layer_type == 1) delta = (h[layer][neuron] * (1.0 - h[layer][neuron])) * sumDelta;
      else if (middle_layer_type == 2) delta = (1.0 - pow(h[layer][neuron], 2)) * sumDelta;
      else {
        // ReLU
        if (h[layer][neuron] > 0) delta = 1.0 * sumDelta;
        else delta = 0 * sumDelta;
      }

      // 学習
      middleNeurons[layer][neuron].learn(delta, h[layer - 1]);
    }
  }
}

/**
 * 中間層の最初の層を学習する
 * @param h 中間層の出力データ
 * @param in 教師入力データ
 * @param begin 学習するニューロンセットの開始点
 * @param end 学習するニューロンセットの終了点
 */
void MultiLayerPerceptron::middleFirstLayerLearnThread(const std::vector<std::vector<double>> h,
                                                       const std::vector<double> in, const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron) {
    // 中間層ニューロンのdeltaを計算
    double sumDelta = 0.0;

    if (middle_layer_number > 1) {
      for (int k = 0; k < middle_neuron_num; ++k) {
        Neuron n = middleNeurons[1][k];
        sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
      }
    } else {
      for (int k = 0; k < output_neuron_num; ++k) {
        Neuron n = outputNeurons[k];
        sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
      }
    }

    double delta;
    if (middle_layer_type == 0) delta = 1.0 * sumDelta;
    else if (middle_layer_type == 1) delta = (h[0][neuron] * (1.0 - h[0][neuron])) * sumDelta;
    else if (middle_layer_type == 2) delta = (1.0 - pow(h[0][neuron], 2)) * sumDelta;
    else {
      // ReLU
      if (h[0][neuron] > 0) delta = 1.0 * sumDelta;
      else delta = 0 * sumDelta;
    }

    // 学習
    middleNeurons[0][neuron].learn(delta, in);
  }
}

/**
 * 与えられたデータをニューラルネットワークに入力し，出力をコンソールに書き出す
 * @param input ニューラルネットワークに入力するデータ
 * @return ニューラルネットワークの計算結果
 */
std::vector<double> MultiLayerPerceptron::out(std::vector<double> input){
  std::vector<std::vector<double>> h = std::vector<std::vector<double>>(middle_layer_number, std::vector<double>(middle_neuron_num, 0));
  std::vector<double> o = std::vector<double>(output_neuron_num, 0);

  for (int neuron = 0; neuron < middle_neuron_num; ++neuron) {
    h[0][neuron] = middleNeurons[0][neuron].output(input);
  }

  for (int layer = 1; layer < middle_layer_number; ++layer) {
    for (int neuron = 0; neuron < middle_neuron_num; ++neuron) {
      h[layer][neuron] = middleNeurons[layer][neuron].output(h[layer - 1]);
    }
  }

  for (int neuron = 0; neuron < output_neuron_num; ++neuron) {
    o[neuron] = outputNeurons[neuron].output(h[middle_layer_number - 1]);
  }

  return o;
}