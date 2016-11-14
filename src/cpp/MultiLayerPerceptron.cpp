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
 * @param middleLayerType 中間層の活性化関数の種類指定．0: identity 1: sigmoid 2: tanh 3: ReLU
 * @return
 */
MultiLayerPerceptron::MultiLayerPerceptron(unsigned short input, unsigned short middle, unsigned short output, unsigned short middleLayer, std::string neuronParams, int middleLayerType) {
  this->inputNumber = input;
  this->middleNumber = middle;
  this->outputNumber = output;
  this->middleLayerNumber = middleLayer;
  this->middleLayerType = middleLayerType;

  // ニューロンごとのパラメータを取得する
  std::vector<Neuron> neuronPerLayer;
  if (neuronParams.length() <= 0) {
    std::vector<double> emptyVector;
    // 中間層
    for (int layer = 0; layer < middleLayerNumber; ++layer) {
      for (int neuron = 0; neuron < middleNumber; ++neuron) {
        neuronPerLayer.push_back(Neuron(inputNumber, emptyVector, emptyVector, 0.0, middleLayerType));
      }
      this->middleNeurons.push_back(neuronPerLayer);
      neuronPerLayer.clear();
    }

    // 出力層
    for (int neuron = 0; neuron < output; ++neuron) {
      this->outputNeurons.push_back(Neuron(inputNumber, emptyVector, emptyVector, 0.0, 1));
    }
  } else {
    // ニューロン単位でデータを分割する
    std::vector<std::string> splitByNeuron;
    std::stringstream ssByNeuron(neuronParams);
    std::string itemPerNeuron;
    while (std::getline(ssByNeuron, itemPerNeuron, '\'')) {
      if (!itemPerNeuron.empty()) {
        splitByNeuron.push_back(itemPerNeuron);
      }
    }
    itemPerNeuron = "";
    ssByNeuron.str("");
    ssByNeuron.clear(stringstream::goodbit);

    for (int layer = 0; layer < middleLayerNumber; ++layer) {
      for (int neuron = 0; neuron < middleNumber; ++neuron) {
        // 中間層
        // 重みづけ，AdaGradのg，バイアスの三つに分割する
        std::vector<std::string> elems;
        std::stringstream ss(splitByNeuron[(layer * middleNumber) + neuron]);
        std::string item;
        while (std::getline(ss, item, '|')) {
          if (!item.empty()) elems.push_back(item);
        }
        item = "";
        ss.str("");
        ss.clear(stringstream::goodbit);

        // まずはバイアスを取り出す
        double bias = std::stod(elems.back());
        elems.pop_back();

        // 重みを取り出す
        std::vector<double> weight;
        ss = std::stringstream(elems[0]);
        while (std::getline(ss, item, ',')) {
          if (!item.empty()) weight.push_back(std::stod(item));
        }
        item = "";
        ss.str("");
        ss.clear(stringstream::goodbit);

        // AdaGradのgを取り出す
        std::vector<double> g;
        ss = std::stringstream(elems[1]);
        while (std::getline(ss, item, ',')) {
          if (!item.empty()) g.push_back(std::stod(item));
        }
        item = "";
        ss.str(""); // バッファをクリアする
        ss.clear(stringstream::goodbit); // ストリームの状態をクリアする

        neuronPerLayer.push_back(Neuron(inputNumber, weight, g, bias, middleLayerType));

        elems.clear();
      }
      this->middleNeurons.push_back(neuronPerLayer);
      neuronPerLayer.clear();
    }

    for (int neuron = 0; neuron < output; ++neuron) {
      // 出力層
      // 重みづけ，AdaGradのg，バイアスの三つに分割する
      std::vector<std::string> elems;
      std::stringstream ss(splitByNeuron[(middleLayerNumber * middleNumber) + neuron]);
      std::string item;
      while (std::getline(ss, item, '|')) {
        if (!item.empty()) elems.push_back(item);
      }
      item = "";
      ss.str("");
      ss.clear(stringstream::goodbit);

      // まずはバイアスを取り出す
      double bias = std::stod(elems.back());
      elems.pop_back();

      // 重みを取り出す
      std::vector<double> weight;
      ss = std::stringstream(elems[0]);
      while (std::getline(ss, item, ',')) {
        if (!item.empty()) weight.push_back(std::stod(item));
      }
      item = "";
      ss.str("");
      ss.clear(stringstream::goodbit);

      // AdaGradのgを取り出す
      std::vector<double> g;
      ss = std::stringstream(elems[1]);
      while (std::getline(ss, item, ',')) {
        if (!item.empty()) g.push_back(std::stod(item));
      }
      item = "";
      ss.str(""); // バッファをクリアする
      ss.clear(stringstream::goodbit); // ストリームの状態をクリアする

      this->outputNeurons.push_back(Neuron(inputNumber, weight, g, bias, 1));

      elems.clear();
    }
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
  std::vector<std::vector<double>> h = std::vector<std::vector<double>>(middleLayerNumber, std::vector<double>(middleNumber, 0.0));
  std::vector<double> o = std::vector<double>(outputNumber, 0.0);

  int succeed = 0; //  連続正解回数のカウンタを初期化
  for (int trial = 0; trial < this->MAX_TRIAL; ++trial) {
    // 使用する教師データを選択
    std::vector<double> in = x[trial % answer.size()]; // 利用する教師入力データ
    std::vector<double> ans = answer[trial % answer.size()]; // 教師出力データ

    // 出力値を推定：1層目の中間層の出力計算
    for (int neuron = 0; neuron < this->middleNumber; ++neuron) h[0][neuron] = middleNeurons[0][neuron].output(in);

    // 一つ前の中間層より得られた出力を用いて，以降の中間層を順に計算
    for (int layer = 1; layer < middleLayerNumber; ++layer) {
      for (int neuron = 0; neuron < middleNumber; ++neuron) {
        h[layer][neuron] = middleNeurons[layer][neuron].output(h[layer - 1]);
      }
    }

    // 出力値を推定：中間層の最終層の出力を用いて，出力層の出力計算
    for (int neuron = 0; neuron < outputNumber; ++neuron) o[neuron] = outputNeurons[neuron].output(h[middleLayerNumber - 1]);

    successFlg = true;

    //region 出力層を学習する
    std::vector<std::thread> threads;
    int charge = 1;
    if (outputNumber <= num_thread) charge = 1;
    else charge = outputNumber / num_thread;
    for (int i = 0; i < outputNumber; i += charge) {
      if (i != 0 && outputNumber / i == 1) threads.push_back(std::thread(&MultiLayerPerceptron::outLearnThread, this, std::ref(ans), std::ref(o), std::ref(h), i, outputNumber));
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
    if (middleLayerNumber > 1) {
      threads.clear();
      if (middleNumber <= num_thread) charge = 1;
      else charge = middleNumber / num_thread;
      for (int i = 0; i < middleNumber; i += charge) {
        if (i != 0 && middleNumber / i == 1) threads.push_back(std::thread(&MultiLayerPerceptron::middleLastLayerLearnThread, this, std::ref(h), i, middleNumber));
        else threads.push_back(std::thread(&MultiLayerPerceptron::middleLastLayerLearnThread, this, std::ref(h), i, i + charge));
      }
      for (std::thread &th : threads) th.join();
    }
    //endregion

    //region 出力層と入力層に最も近い層一つずつを除いた残りの中間層を入力層に向けて学習する
    threads.clear();
    if (middleNumber <= num_thread) charge = 1;
    else charge = middleNumber / num_thread;
    for (int i = 0; i < middleNumber; i += charge) {
      if (i != 0 && middleNumber / i == 1) threads.push_back(std::thread(&MultiLayerPerceptron::middleMiddleLayerLearnThread, this, std::ref(h), i, middleNumber));
      else threads.push_back(std::thread(&MultiLayerPerceptron::middleMiddleLayerLearnThread, this, std::ref(h), i, i + charge));
    }
    for (std::thread &th : threads) th.join();
    //endregion

    //region 中間層の最初の層を学習する
    threads.clear();
    if (middleNumber <= num_thread) charge = 1;
    else charge = middleNumber / num_thread;
    for (int i = 0; i < middleNumber; i += charge) {
      if (i != 0 && middleNumber / i == 1) threads.push_back(std::thread(&MultiLayerPerceptron::middleFirstLayerLearnThread, this, std::ref(h), std::ref(in), i, middleNumber));
      else threads.push_back(std::thread(&MultiLayerPerceptron::middleFirstLayerLearnThread, this, std::ref(h), std::ref(in), i, i + charge));
    }
    for (std::thread &th : threads) th.join();
    //endregion

    //endregion

    // 再度出力
    // 出力値を推定：中間層の出力計算
    // 1層目の中間層の出力を計算
    for (int neuron = 0; neuron < middleNumber; ++neuron) h[0][neuron] = middleNeurons[0][neuron].output(in);

    // 一つ前の中間層より得られた出力を用いて，以降の中間層を順に計算
    for (int layer = 1; layer < middleLayerNumber; ++layer) {
      for (int neuron = 0; neuron < middleNumber; ++neuron) {
        h[layer][neuron] = middleNeurons[layer][neuron].output(h[layer - 1]);
      }
    }

    // 出力値を推定：出力層の出力計算
    // 中間層の最終層の出力を用いて，出力層の出力を計算
    for (int neuron = 0; neuron < outputNumber; ++neuron) o[neuron] = outputNeurons[neuron].output(h[middleLayerNumber - 1]);
  }

  // 全ての教師データで正解を出すか，収束限度回数を超えた場合に終了

  std::string neuronParams;
  std::stringstream ss;
  for (int layer = 0; layer < middleLayerNumber; ++layer) {
    for (int neuron = 0; neuron < middleNumber; ++neuron) {
      // 重みを詰める
      for (int weightNum = 0; weightNum < inputNumber; ++weightNum) {
        ss << middleNeurons[layer][neuron].getInputWeightIndexOf(weightNum) << ',';
      }
      ss << '|';
      // AdaGradのgを詰める
      for (int gNum = 0; gNum < inputNumber; ++gNum) {
        ss << middleNeurons[layer][neuron].getGIndexOf(gNum) << ',';
      }
      ss << '|';

      // バイアスを入れ，最後に ' を入れる
      ss << middleNeurons[layer][neuron].getBias() << '\'';
    }
  }
  for (int neuron = 0; neuron < outputNumber; ++neuron) {
    // 重みを詰める
    for (int weightNum = 0; weightNum < inputNumber; ++weightNum) {
      ss << outputNeurons[neuron].getInputWeightIndexOf(weightNum) << ",";
    }
    ss << '|';
    // AdaGradのgを詰める
    for (int gNum = 0; gNum < inputNumber; ++gNum) {
      ss << outputNeurons[neuron].getGIndexOf(gNum) << ',';
    }
    ss << '|';

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
    outputNeurons[neuron].learn(delta, h[middleLayerNumber - 1]);
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
    for (int k = 0; k < outputNumber; ++k) {
      Neuron n = outputNeurons[k];
      sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
    }

    // どの活性化関数を用いるかで，deltaの計算方法が変わる
    double delta;
    if (middleLayerType == 0) delta = 1.0 * sumDelta;
    else if (middleLayerType == 1) delta = (h[middleLayerNumber - 1][neuron] * (1.0 - h[middleLayerNumber - 1][neuron])) * sumDelta;
    else if (middleLayerType == 2) delta = (1.0 - pow(h[middleLayerNumber - 1][neuron], 2)) * sumDelta;
    else {
      // ReLU
      if (h[middleLayerNumber - 1][neuron] > 0) delta = 1.0 * sumDelta;
      else delta = 0 * sumDelta;
    }

    // 学習
    middleNeurons[middleLayerNumber - 1][neuron].learn(delta, h[middleLayerNumber - 2]);
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
    for (int layer = (int)middleLayerNumber - 2; layer >= 1; --layer) {
      // 中間層ニューロンのdeltaを計算
      double sumDelta = 0.0;
      for (int k = 0; k < middleNumber; ++k) {
        Neuron n = middleNeurons[layer + 1][k];
        sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
      }

      double delta;
      if (middleLayerType == 0) delta = 1.0 * sumDelta;
      else if (middleLayerType == 1) delta = (h[layer][neuron] * (1.0 - h[layer][neuron])) * sumDelta;
      else if (middleLayerType == 2) delta = (1.0 - pow(h[layer][neuron], 2)) * sumDelta;
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

    if (middleLayerNumber > 1) {
      for (int k = 0; k < middleNumber; ++k) {
        Neuron n = middleNeurons[1][k];
        sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
      }
    } else {
      for (int k = 0; k < outputNumber; ++k) {
        Neuron n = outputNeurons[k];
        sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
      }
    }

    double delta;
    if (middleLayerType == 0) delta = 1.0 * sumDelta;
    else if (middleLayerType == 1) delta = (h[0][neuron] * (1.0 - h[0][neuron])) * sumDelta;
    else if (middleLayerType == 2) delta = (1.0 - pow(h[0][neuron], 2)) * sumDelta;
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
  std::vector<std::vector<double>> h = std::vector<std::vector<double>>(middleLayerNumber, std::vector<double>(middleNumber, 0));
  std::vector<double> o = std::vector<double>(outputNumber, 0);

  for (int neuron = 0; neuron < middleNumber; ++neuron) {
    h[0][neuron] = middleNeurons[0][neuron].output(input);
  }

  for (int layer = 1; layer < middleLayerNumber; ++layer) {
    for (int neuron = 0; neuron < middleNumber; ++neuron) {
      h[layer][neuron] = middleNeurons[layer][neuron].output(h[layer - 1]);
    }
  }

  for (int neuron = 0; neuron < outputNumber; ++neuron) {
    o[neuron] = outputNeurons[neuron].output(h[middleLayerNumber - 1]);
  }

  return o;
}