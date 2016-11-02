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
 * @param double型二次元の重み付け&閾値データ，（ニューロン * 重み付け）
 * @return
 */
MultiLayerPerceptron::MultiLayerPerceptron(unsigned short input, unsigned short middle, unsigned short output, unsigned short middleLayer, std::string weightAndThreshold) {
  this->inputNumber = input;
  this->middleNumber = middle;
  this->outputNumber = output;
  this->middleLayerNumber = middleLayer;

  std::vector<Neuron> neuronPerLayer;

  if (weightAndThreshold.length() <= 0) {
    std::vector<double> emptyWeightAndThreshold;
    for (int layer = 0; layer < middleLayerNumber; ++layer) {
      for (int neuron = 0; neuron < middleNumber; ++neuron) {
        neuronPerLayer.push_back(Neuron(inputNumber, emptyWeightAndThreshold, 0.0));
      }
      this->middleNeurons.push_back(neuronPerLayer);
      neuronPerLayer.clear();
    }

    for (int neuron = 0; neuron < output; ++neuron) {
      this->outputNeurons.push_back(Neuron(inputNumber, emptyWeightAndThreshold, 0.0));
    }
  } else {
    std::vector<std::string> splitByNeuron;
    std::stringstream ssByNeuron(weightAndThreshold);
    std::string itemPerNeuron;
    while (std::getline(ssByNeuron, itemPerNeuron, '\'')) {
      if (!itemPerNeuron.empty()) {
        splitByNeuron.push_back(itemPerNeuron);
      }
    }
    ssByNeuron.str("");
    ssByNeuron.clear(stringstream::goodbit);

    for (int layer = 0; layer < middleLayerNumber; ++layer) {
      for (int neuron = 0; neuron < middleNumber; ++neuron) {
        // ニューロンごとの重み付けデータと閾値データを取り出す
        std::vector<double> dataPerNeuron;
        std::stringstream ss(splitByNeuron[(layer * middleNumber) + neuron]);
        std::string item;
        while (std::getline(ss, item, ',')) {
          if (!item.empty()) {
            dataPerNeuron.push_back(std::stod(item));
          }
        }
        // まずは閾値を取り出す
        double threshold = dataPerNeuron.back();
        dataPerNeuron.pop_back();
        neuronPerLayer.push_back(Neuron(inputNumber, dataPerNeuron, threshold));

        dataPerNeuron.clear();
        ss.str(""); // バッファをクリアする
        ss.clear(stringstream::goodbit); // ストリームの状態をクリアする
      }
      this->middleNeurons.push_back(neuronPerLayer);
      neuronPerLayer.clear();
    }

    for (int neuron = 0; neuron < output; ++neuron) {
      // ニューロンごとの重み付けデータと閾値データを取り出す
      std::vector<double> dataPerNeuron;
      std::stringstream ss(splitByNeuron[(middleLayerNumber * middleNumber) + neuron]);
      std::string item;
      while (std::getline(ss, item, ',')) {
        if (!item.empty()) {
          dataPerNeuron.push_back(std::stod(item));
        }
      }

      // まずは閾値を取り出す
      double threshold = dataPerNeuron.back();
      dataPerNeuron.pop_back();
      this->outputNeurons.push_back(Neuron(inputNumber, dataPerNeuron, threshold));

      dataPerNeuron.clear();
      ss.str(""); // バッファをクリアする
      ss.clear(stringstream::goodbit); // ストリームの状態をクリアする
    }
  }
}

/**
 * 教師入力データと教師出力データを元にニューラルネットワークを学習する
 * @param x 二次元の教師入力データ，データセット * データ
 * @param answer 教師入力データに対応した二次元の教師出力データ，データセット * データ
 * @return ニューロンごとの学習後の重み付けが入ったdouble型二次元ベクトル
 */
std::string MultiLayerPerceptron::learn(std::vector<std::vector<double>> x, std::vector<std::vector<double>> answer) {
  std::vector<std::vector<double>> h = std::vector<std::vector<double>>(middleLayerNumber, std::vector<double>(middleNumber, 0));
  std::vector<double> o = std::vector<double>(outputNumber, 0);

  int succeed = 0; //  連続正解回数のカウンタを初期化
  for (int trial = 0; trial < this->MAX_TRIAL; ++trial) {
    // 使用する教師データを選択
    std::vector<double> in = x[trial % answer.size()]; // 利用する教師入力データ
    std::vector<double> ans = answer[trial % answer.size()]; // 教師出力データ

    // 出力値を推定：1層目の中間層の出力計算
    for (int neuron = 0; neuron < this->middleNumber; ++neuron) {
      h[0][neuron] = middleNeurons[0][neuron].output(in);
//            std::cout << "h[0][" << neuron << "]: " << h[0][neuron] << std::endl;
    }

    // 一つ前の中間層より得られた出力を用いて，以降の中間層を順に計算
    for (int layer = 1; layer < middleLayerNumber; ++layer) {
      for (int neuron = 0; neuron < middleNumber; ++neuron) {
        h[layer][neuron] = middleNeurons[layer][neuron].output(h[layer - 1]);
//                std::cout << "h[" << layer << "][" << neuron << "]: " << h[layer][neuron] << std::endl;
      }
    }

    // 出力値を推定：中間層の最終層の出力を用いて，出力層の出力計算
    for (int neuron = 0; neuron < outputNumber; ++neuron) {
      o[neuron] = outputNeurons[neuron].output(h[middleLayerNumber - 1]);
//            std::cout << "before o[" << neuron << "]: " << o[neuron] << std::endl;
    }


//        // 教師入力データを出力
//        std::cout << "[input] ";
//        for (int i = 0; i < in.size(); ++i) {
//          std::cout << in[i] << " ";
//        }
//        std::cout << std::endl;
//
//        // 教師出力データを出力
//        std::cout << "[answer] " << ans << std::endl;
//
//        // 出力層から得られたデータを出力
//        std::cout << "[output] " << o[0] << std::endl;
//
//        // 中間層から得られたデータを出力
//        for (int layer = 0; layer < middleLayerNumber; ++layer) {
//          std::cout << "[middle " << layer << "] ";
//          for (int neuron = 0; neuron < middleNumber; ++neuron) {
//            std::cout << h[layer][neuron] << " ";
//          }
//          std::cout << std::endl;
//        }


    successFlg = true;

    //region 出力層を学習する
    std::vector<std::thread> threads;
    int charge = 1;
    if (outputNumber <= num_thread) charge = 1;
    else charge = outputNumber / num_thread;
    for (int i = 0; i < outputNumber; i += charge) {
      if (i != 0 && outputNumber / i == 1) {
//                std::cout << "i: " << i << "   outputNumber: " << outputNumber << std::endl;
        threads.push_back(std::thread(&MultiLayerPerceptron::outLearnThread, this, std::ref(ans), std::ref(o), std::ref(h), i, outputNumber));
      } else {
//                std::cout << "i: " << i << "   i + charge: " << i + charge << std::endl;
        threads.push_back(std::thread(&MultiLayerPerceptron::outLearnThread, this, std::ref(ans), std::ref(o), std::ref(h), i, i + charge));
      }
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
        if (i != 0 && middleNumber / i == 1) {
          threads.push_back(std::thread(&MultiLayerPerceptron::middleLastLayerLearnThread, this, std::ref(h), i, middleNumber));
        } else {
          threads.push_back(std::thread(&MultiLayerPerceptron::middleLastLayerLearnThread, this, std::ref(h), i, i + charge));
        }
      }
      for (std::thread &th : threads) th.join();
    }
    //endregion

    //region 出力層と入力層に最も近い層一つずつを除いた残りの中間層を入力層に向けて学習する
    threads.clear();
    if (middleNumber <= num_thread) charge = 1;
    else charge = middleNumber / num_thread;
    for (int i = 0; i < middleNumber; i += charge) {
      if (i != 0 && middleNumber / i == 1) {
        threads.push_back(std::thread(&MultiLayerPerceptron::middleMiddleLayerLearnThread, this, std::ref(h), i, middleNumber));
      } else {
        threads.push_back(std::thread(&MultiLayerPerceptron::middleMiddleLayerLearnThread, this, std::ref(h), i, i + charge));
      }
    }
    for (std::thread &th : threads) th.join();
    //endregion

    //region 中間層の最初の層を学習する
    threads.clear();
    if (middleNumber <= num_thread) charge = 1;
    else charge = middleNumber / num_thread;
    for (int i = 0; i < middleNumber; i += charge) {
      if (i != 0 && middleNumber / i == 1) {
        threads.push_back(std::thread(&MultiLayerPerceptron::middleFirstLayerLearnThread, this, std::ref(h), std::ref(in), i, middleNumber));
      } else {
        threads.push_back(std::thread(&MultiLayerPerceptron::middleFirstLayerLearnThread, this, std::ref(h), std::ref(in), i, i + charge));
      }
    }
    for (std::thread &th : threads) th.join();
    //endregion

    //endregion

    // 再度出力
    // 出力値を推定：中間層の出力計算
    // 1層目の中間層の出力を計算
    for (int neuron = 0; neuron < middleNumber; ++neuron) {
      h[0][neuron] = middleNeurons[0][neuron].output(in);
//            std::cout << "h[0][" << neuron << "]: " << h[0][neuron] << std::endl;
    }

    // 一つ前の中間層より得られた出力を用いて，以降の中間層を順に計算
    for (int layer = 1; layer < middleLayerNumber; ++layer) {
      for (int neuron = 0; neuron < middleNumber; ++neuron) {
        h[layer][neuron] = middleNeurons[layer][neuron].output(h[layer - 1]);
//                std::cout << "h[" << layer << "][" << neuron << "]: " << h[layer][neuron] << std::endl;
      }
    }

    // 出力値を推定：出力層の出力計算
    // 中間層の最終層の出力を用いて，出力層の出力を計算
    for (int neuron = 0; neuron < outputNumber; ++neuron) {
      o[neuron] = outputNeurons[neuron].output(h[middleLayerNumber - 1]);
//            std::cout << "after o[" << neuron << "]: " << o[neuron] << std::endl;
    }


//        std::cout << "[input] ";
//        for (int i = 0; i < in.size(); ++i) {
//          std::cout << in[i] << " ";
//        }
//        std::cout << std::endl;
//
//        std::cout << "[output] " << o[0] << std::endl;
//
//        for (int layer = 0; layer < middleLayerNumber; ++layer) {
//          std::cout << "[middle " << layer << "] ";
//          for (int i = 0; i < h[layer].size(); ++i) {
//            std::cout << h[layer][i] << " ";
//          }
//          std::cout << std::endl;
//        }
  }

  // 全ての教師データで正解を出すか，収束限度回数を超えた場合に終了
//    std::cout << "[finish] " << this->toString() << std::endl;

  std::string resultWeightAndThreshold;
  std::stringstream ss;
  for (int layer = 0; layer < middleLayerNumber; ++layer) {
    for (int neuron = 0; neuron < middleNumber; ++neuron) {
      for (int weightNum = 0; weightNum < inputNumber; ++weightNum) {
        ss << middleNeurons[layer][neuron].getInputWeightIndexOf(weightNum) << ",";
      }
      // 各ニューロンの閾値を入れ，最後に ' を入れる
      ss << middleNeurons[layer][neuron].getThreshold() << "'";
    }
  }
  for (int neuron = 0; neuron < outputNumber; ++neuron) {
    for (int weightNum = 0; weightNum < inputNumber; ++weightNum) {
      ss << outputNeurons[neuron].getInputWeightIndexOf(weightNum) << ",";
    }
    // 各ニューロンの閾値を入れ，最後に ' を入れる
    ss << outputNeurons[neuron].getThreshold() << "'";
  }

  resultWeightAndThreshold = ss.str();
  // 末尾の ' を削除する
  resultWeightAndThreshold.pop_back();
  ss.str(""); // バッファをクリアする
  ss.clear(stringstream::goodbit); // ストリームの状態をクリアする

  return resultWeightAndThreshold;
}

/**
 * 出力層の学習，スレッドを用いて並列学習するため，学習するニューロンの開始点と終了点も必要
 * @param ans 教師出力データ
 * @param o 出力層の出力データ
 * @param h 中間層の出力データ
 * @param begin 学習するニューロンセットの開始点
 * @param end 学習するニューロンセットの終了点
 */
void MultiLayerPerceptron::outLearnThread(const std::vector<double> ans, const std::vector<double> o,
                                          const std::vector<std::vector<double>> h, const int begin, const int end){
  for (int neuron = begin; neuron < end; ++neuron) {
    // 出力層ニューロンの修正量deltaを計算
    double delta = (ans[neuron] - o[neuron]) * o[neuron] * (1.0 - o[neuron]);

//        std::cout << "delta: " << delta << "= (" << ans[neuron] << " - " << o[neuron] << ") * " << o[neuron] << " * (1.0 - " << o[neuron] << ")" << std::endl;
    if (std::abs(ans[neuron] - o[neuron]) < MAX_GAP) continue;
    else successFlg = false;

    // 出力層の学習
//        std::cout << "[learn] before o: " << outputNeurons[neuron].toString() << std::endl;
    outputNeurons[neuron].learn(delta, h[middleLayerNumber - 1]);
//        std::cout << "[learn] after o: " << outputNeurons[neuron].toString() << std::endl;
  }
}

/**
 * 中間層の最終層の学習．中間層の層数が2以上の場合のみこれを使う．
 * @param h 中間層の出力データ
 * @param begin 学習するニューロンセットの開始点
 * @param end 学習するニューロンセットの終了点
 */
void MultiLayerPerceptron::middleLastLayerLearnThread(const std::vector<std::vector<double>> h, const int begin,
                                                      const int end){
  for (int neuron = begin; neuron < end; ++neuron) {
    // 中間層ニューロンの修正量deltaを計算
    double sumDelta = 0.0;
    for (int k = 0; k < outputNumber; ++k) {
      Neuron n = outputNeurons[k];
      sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
    }
    double delta = h[middleLayerNumber - 1][neuron] * (1.0 - h[middleLayerNumber - 1][neuron]) * sumDelta;

    // 学習
//        std::cout << "[learn] before m: " << middleNeurons[middleLayerNumber - 1][neuron].toString() << std::endl;
    middleNeurons[middleLayerNumber - 1][neuron].learn(delta, h[middleLayerNumber - 2]);
//        std::cout << "[learn] after m: " << middleNeurons[middleLayerNumber - 1][neuron].toString() << std::endl;
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
      // 中間層ニューロンの修正量deltaを計算
      double sumDelta = 0.0;
      for (int k = 0; k < middleNumber; ++k) {
        Neuron n = middleNeurons[layer + 1][k];
        sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
      }
      double delta = h[layer][neuron] * (1.0 - h[layer][neuron]) * sumDelta;

      // 学習
//                std::cout << "[learn] before m: " << middleNeurons[layer][neuron].toString() << std::endl;
      middleNeurons[layer][neuron].learn(delta, h[layer - 1]);
//                std::cout << "[learn] after m: " << middleNeurons[layer][neuron].toString() << std::endl;
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
    // 中間層ニューロンの修正量deltaを計算
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
    double delta = h[0][neuron] * (1.0 - h[0][neuron]) * sumDelta;

    // 学習
//            std::cout << "[learn] before m: " << middleNeurons[0][neuron].toString() << std::endl;
    middleNeurons[0][neuron].learn(delta, in);
//            std::cout << "[learn] after m: " << middleNeurons[0][neuron].toString() << std::endl;
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