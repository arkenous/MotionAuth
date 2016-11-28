
#include "MultiLayerPerceptron.h"

#include <thread>
#include <sstream>
#include <android/log.h>

using namespace std;

/**
 * MultiLayerPerceptronのコンストラクタ
 * @param output 出力層のニューロン数
 * @param middleLayer 中間層の層数
 * @param neuron_params ニューロンのパラメータ
 * @param middle_layer_type 中間層の活性化関数の種類指定．0: identity 1: sigmoid 2: tanh 3: ReLU
 * @param dropout_rate ドロップアウト率
 * @return MLPインスタンス
 */
MultiLayerPerceptron::MultiLayerPerceptron(unsigned long output, unsigned long middle_layer,
                                           vector<string> neuron_params, int middle_layer_type,
                                           double dropout_rate) {
  sda_params = neuron_params[0];
  mlp_params = neuron_params[1];

  setupSdA(sda_params);

  // SdA末尾レイヤの出力数がMLPの入力数となる
  this->input_neuron_num = sda_neurons[sda_neurons.size() - 1].size();
  this->middle_neuron_num = sda_neurons[sda_neurons.size() - 1].size();
  this->output_neuron_num = output;
  this->middle_layer_number = middle_layer;
  this->middle_layer_type = middle_layer_type;

  setupMLP(mlp_params, dropout_rate);
}

void MultiLayerPerceptron::setupSdA(string sda_params) {
  stringstream ss(sda_params);
  string item;
  vector<string> elems_per_dA;
  vector<string> elems_per_neuron;
  vector<string> elems_per_param;

  // : でdA単位で分割する（= dA層のレイヤ数）
  while (getline(ss, item, ':')) if (!item.empty()) elems_per_dA.push_back(item);
  sda_neurons.resize(elems_per_dA.size());
  sda_out.resize(elems_per_dA.size());
  item = "";
  ss.str("");
  ss.clear(stringstream::goodbit);

  for (int dA = 0; dA < elems_per_dA.size(); ++dA) {
    // ' でニューロン単位で分割する
    ss = stringstream(elems_per_dA[dA]);
    while (std::getline(ss, item, '\'')) if (!item.empty()) elems_per_neuron.push_back(item);
    sda_neurons[dA].resize(elems_per_neuron.size());
    sda_out[dA].resize(elems_per_neuron.size());
    item = "";
    ss.str("");
    ss.clear(stringstream::goodbit);

    for (int neuron = 0; neuron < elems_per_neuron.size(); ++neuron) {
      // パラメータごとに分割する
      ss = stringstream(elems_per_neuron[neuron]);
      while (getline(ss, item, '|')) if (!item.empty()) elems_per_param.push_back(item);
      item = "";
      ss.str("");
      ss.clear(stringstream::goodbit);

      double bias = stod(elems_per_param.back());
      elems_per_param.pop_back();

      int iteration = stoi(elems_per_param.back());
      elems_per_param.pop_back();

      vector<double> weight = separate_by_camma(elems_per_param[0]);
      vector<double> m = separate_by_camma(elems_per_param[1]);
      vector<double> nu = separate_by_camma(elems_per_param[2]);
      vector<double> m_hat = separate_by_camma(elems_per_param[3]);
      vector<double> nu_hat = separate_by_camma(elems_per_param[4]);

      sda_neurons[dA][neuron] = Neuron(weight.size(), weight, m, nu, m_hat, nu_hat,
                                       iteration, bias, 1, 0.0);

      elems_per_param.clear();
    }
    elems_per_neuron.clear();
  }
  elems_per_dA.clear();
}

void MultiLayerPerceptron::setupMLP(string mlp_params, double dropout_rate) {
  if (mlp_params.length() <= 0) {
    middleNeurons.resize(middle_layer_number);
    h.resize(middle_layer_number);
    learned_h.resize(middle_layer_number);

    vector<Neuron> neuron_per_layer(middle_neuron_num);
    vector<double> emptyVector;
    //region 中間層を構築する
    for (int layer = 0; layer < middle_layer_number; ++layer) {
      if (layer == 0)
        for (int neuron = 0; neuron < middle_neuron_num; ++neuron)
          // 中間層の最初の層については，入力層のニューロン数がニューロンへの入力数になる
          neuron_per_layer[neuron] = Neuron(input_neuron_num, emptyVector, emptyVector,
                                            emptyVector, emptyVector, emptyVector,
                                            0, 0.0, middle_layer_type, dropout_rate);
      else
        for (int neuron = 0; neuron < middle_neuron_num; ++neuron)
          // それ以降の層については，中間層の各層のニューロン数がニューロンへの入力数になる
          neuron_per_layer[neuron] = Neuron(middle_neuron_num, emptyVector, emptyVector,
                                            emptyVector, emptyVector, emptyVector,
                                            0, 0.0, middle_layer_type, dropout_rate);
      middleNeurons[layer] = vector<Neuron>(neuron_per_layer);
      h[layer].resize(middle_neuron_num);
      learned_h[layer].resize(middle_neuron_num);
    }
    //endregion

    //region 出力層を構築する
    outputNeurons.resize(output_neuron_num);
    o.resize(output_neuron_num);
    learned_o.resize(output_neuron_num);
    for (int neuron = 0; neuron < output_neuron_num; ++neuron)
      this->outputNeurons[neuron] = Neuron(middle_neuron_num, emptyVector, emptyVector,
                                           emptyVector, emptyVector, emptyVector,
                                           0, 0.0, 1, dropout_rate);
    //endregion
  } else {
    //region 中間層を構築する
    stringstream ss = stringstream(mlp_params);
    string item;
    vector<string> elems_per_layer;
    vector<string> elems_per_neuron;
    vector<string> elems_per_param;

    // : でレイヤ単位で分割する
    while (getline(ss, item, ':')) if (!item.empty()) elems_per_layer.push_back(item);

    // 最終層は出力層のため，これを取り出す
    string out_layer_params = elems_per_layer.back();
    elems_per_layer.pop_back();

    middleNeurons.resize(elems_per_layer.size());
    h.resize(elems_per_layer.size());
    learned_h.resize(elems_per_layer.size());

    item = "";
    ss.str("");
    ss.clear(stringstream::goodbit);

    for (int layer = 0; layer < elems_per_layer.size(); ++layer) {
      // ' でニューロン単位で分割する
      ss = stringstream(elems_per_layer[layer]);
      while (std::getline(ss, item, '\'')) if (!item.empty()) elems_per_neuron.push_back(item);
      middleNeurons[layer].resize(elems_per_neuron.size());
      h[layer].resize(elems_per_neuron.size());
      learned_h[layer].resize(elems_per_neuron.size());

      item = "";
      ss.str("");
      ss.clear(stringstream::goodbit);

      for (int neuron = 0; neuron < elems_per_neuron.size(); ++neuron) {
        // パラメータごとに分割する
        ss = stringstream(elems_per_neuron[neuron]);
        while (getline(ss, item, '|')) if (!item.empty()) elems_per_param.push_back(item);
        item = "";
        ss.str("");
        ss.clear(stringstream::goodbit);

        double bias = stod(elems_per_param.back());
        elems_per_param.pop_back();

        int iteration = stoi(elems_per_param.back());
        elems_per_param.pop_back();

        vector<double> weight = separate_by_camma(elems_per_param[0]);
        vector<double> m = separate_by_camma(elems_per_param[1]);
        vector<double> nu = separate_by_camma(elems_per_param[2]);
        vector<double> m_hat = separate_by_camma(elems_per_param[3]);
        vector<double> nu_hat = separate_by_camma(elems_per_param[4]);

        middleNeurons[layer][neuron] = Neuron(weight.size(), weight, m, nu, m_hat, nu_hat,
                                              iteration, bias, middle_layer_type, dropout_rate);

        elems_per_param.clear();
      }
      elems_per_neuron.clear();
    }
    elems_per_layer.clear();
    //endregion

    //region 出力層を構築する
    ss = stringstream(out_layer_params);

    // ' でニューロン単位で分割する
    while (std::getline(ss, item, '\'')) if (!item.empty()) elems_per_neuron.push_back(item);
    outputNeurons.resize(elems_per_neuron.size());
    o.resize(elems_per_neuron.size());
    learned_o.resize(elems_per_neuron.size());

    item = "";
    ss.str("");
    ss.clear(stringstream::goodbit);

    for (int neuron = 0; neuron < elems_per_neuron.size(); ++neuron) {
      // パラメータごとに分割する
      ss = stringstream(elems_per_neuron[neuron]);
      while (getline(ss, item, '|')) if (!item.empty()) elems_per_param.push_back(item);
      item = "";
      ss.str("");
      ss.clear(stringstream::goodbit);

      double bias = stod(elems_per_param.back());
      elems_per_param.pop_back();

      int iteration = stoi(elems_per_param.back());
      elems_per_param.pop_back();

      vector<double> weight = separate_by_camma(elems_per_param[0]);
      vector<double> m = separate_by_camma(elems_per_param[1]);
      vector<double> nu = separate_by_camma(elems_per_param[2]);
      vector<double> m_hat = separate_by_camma(elems_per_param[3]);
      vector<double> nu_hat = separate_by_camma(elems_per_param[4]);

      outputNeurons[neuron] = Neuron(weight.size(), weight, m, nu, m_hat, nu_hat,
                                     iteration, bias, 1, dropout_rate);

      elems_per_param.clear();
    }
    elems_per_neuron.clear();
    //endregion
  }
}

/**
 * 教師入力データと教師出力データを元にニューラルネットワークを学習する
 * @param x 二次元の教師入力データ，データセット * データ
 * @param answer 教師入力データに対応した二次元の教師出力データ，データセット * データ
 * @return SdAとMLPのニューロンパラメータ
 */
vector<string> MultiLayerPerceptron::learn(vector<vector<double>> x,
                                           vector<vector<double>> answer) {
  int succeed = 0; //  連続正解回数のカウンタを初期化
  int loop_count = 0;

  random_device rnd; // 非決定的乱数生成器
  mt19937 mt; // メルセンヌ・ツイスタ
  mt.seed(rnd());
  uniform_real_distribution<double> real_rnd(0.0, 1.0); // 0.0以上1.0未満の範囲で値を生成する

  for (int trial = 0; trial < this->MAX_TRIAL; ++trial) {
    //region Dropoutの設定
    for (int layer = 0; layer < middle_layer_number; ++layer)
      for (int neuron = 0; neuron < middle_neuron_num; ++neuron)
        middleNeurons[layer][neuron].dropout(real_rnd(mt));
    for (int neuron = 0; neuron < output_neuron_num; ++neuron)
      outputNeurons[neuron].dropout(1.0); // 出力層ニューロンはDropoutさせない
    //endregion

    // 使用する教師データを選択
    vector<double> in = x[trial % answer.size()]; // 利用する教師入力データ
    vector<double> ans = answer[trial % answer.size()]; // 教師出力データ

    //region Feed Forward

    // SdA First Layer
    vector<thread> threads(num_thread);
    int charge = 1;
    threads.clear();
    if (sda_neurons[0].size() <= num_thread) charge = 1;
    else charge = sda_neurons[0].size() / num_thread;
    for (int i = 0; i < sda_neurons[0].size(); i += charge)
      if (i != 0 && sda_neurons[0].size() / i == 1)
        threads.push_back(thread(&MultiLayerPerceptron::sdaFirstLayerOutThread, this,
                                      ref(in), i, sda_neurons[0].size()));
      else
        threads.push_back(thread(&MultiLayerPerceptron::sdaFirstLayerOutThread, this,
                                      ref(in), i, i + charge));
    for (thread &th : threads) th.join();

    // SdA Other Layer
    for (int layer = 1; layer <= (int) sda_neurons.size() - 1; ++layer) {
      threads.clear();
      if (sda_neurons[layer].size() <= num_thread) charge = 1;
      else charge = sda_neurons[layer].size() / num_thread;
      for (int i = 0; i < sda_neurons[layer].size(); i += charge)
        if (i != 0 && sda_neurons[layer].size() / i == 1)
          threads.push_back(thread(&MultiLayerPerceptron::sdaOtherLayerOutThread, this,
                                        layer, i, sda_neurons[layer].size()));
        else
          threads.push_back(thread(&MultiLayerPerceptron::sdaOtherLayerOutThread, this,
                                        layer, i, i + charge));
      for (thread &th : threads) th.join();
    }

    // 1層目の中間層の出力計算
    threads.clear();
    if (middle_neuron_num <= num_thread) charge = 1;
    else charge = middle_neuron_num / num_thread;
    for (int i = 0; i < middle_neuron_num; i += charge)
      if (i != 0 && middle_neuron_num / i == 1)
        threads.push_back(thread(&MultiLayerPerceptron::middleFirstLayerForwardThread, this,
                                      i, middle_neuron_num));
      else
        threads.push_back(thread(&MultiLayerPerceptron::middleFirstLayerForwardThread, this,
                                      i, i + charge));
    for (thread &th : threads) th.join();

    // 一つ前の中間層より得られた出力を用いて，以降の中間層を順に計算
    if (middle_neuron_num <= num_thread) charge = 1;
    else charge = middle_neuron_num / num_thread;
    for (int layer = 1; layer <= (int) middle_layer_number - 1; ++layer) {
      threads.clear();
      for (int i = 0; i < middle_neuron_num; i += charge)
        if (i != 0 && middle_neuron_num / i == 1)
          threads.push_back(thread(&MultiLayerPerceptron::middleLayerForwardThread, this,
                                        layer, i, middle_neuron_num));
        else
          threads.push_back(thread(&MultiLayerPerceptron::middleLayerForwardThread, this,
                                        layer, i, i + charge));
      for (thread &th : threads) th.join();
    }

    // 出力値を推定：中間層の最終層の出力を用いて，出力層の出力計算
    threads.clear();
    if (output_neuron_num <= num_thread) charge = 1;
    else charge = output_neuron_num / num_thread;
    for (int i = 0; i < output_neuron_num; i += charge)
      if (i != 0 && output_neuron_num / i == 1)
        threads.push_back(thread(&MultiLayerPerceptron::outForwardThread, this,
                                      i, output_neuron_num));
      else
        threads.push_back(thread(&MultiLayerPerceptron::outForwardThread, this,
                                      i, i + charge));
    for (thread &th : threads) th.join();

    //endregion

    successFlg = true;

    //region Back Propagation (learn phase)

    //region 出力層を学習する
    threads.clear();
    if (output_neuron_num <= num_thread) charge = 1;
    else charge = output_neuron_num / num_thread;
    for (int i = 0; i < output_neuron_num; i += charge)
      if (i != 0 && output_neuron_num / i == 1)
        threads.push_back(thread(&MultiLayerPerceptron::outLearnThread, this,
                                      ref(ans), i, output_neuron_num));
      else
        threads.push_back(thread(&MultiLayerPerceptron::outLearnThread, this,
                                      ref(ans), i, i + charge));
    for (thread &th : threads) th.join();
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
      for (int i = 0; i < middle_neuron_num; i += charge)
        if (i != 0 && middle_neuron_num / i == 1)
          threads.push_back(thread(&MultiLayerPerceptron::middleLastLayerLearnThread, this,
                                        i, middle_neuron_num));
        else
          threads.push_back(thread(&MultiLayerPerceptron::middleLastLayerLearnThread, this,
                                        i, i + charge));
      for (thread &th : threads) th.join();
    }
    //endregion

    //region 出力層と入力層に最も近い層一つずつを除いた残りの中間層を入力層に向けて学習する
    if (middle_neuron_num <= num_thread) charge = 1;
    else charge = middle_neuron_num / num_thread;
    for (int layer = (int) middle_layer_number - 2; layer >= 1; --layer) {
      threads.clear();
      for (int i = 0; i < middle_neuron_num; i += charge)
        if (i != 0 && middle_neuron_num / i == 1)
          threads.push_back(thread(&MultiLayerPerceptron::middleMiddleLayerLearnThread, this,
                                        layer, i, middle_neuron_num));
        else
          threads.push_back(thread(&MultiLayerPerceptron::middleMiddleLayerLearnThread, this,
                                        layer, i, i + charge));
      for (thread &th : threads) th.join();
    }
    //endregion

    //region 中間層の最初の層を学習する
    threads.clear();
    if (middle_neuron_num <= num_thread) charge = 1;
    else charge = middle_neuron_num / num_thread;
    for (int i = 0; i < middle_neuron_num; i += charge)
      if (i != 0 && middle_neuron_num / i == 1)
        threads.push_back(thread(&MultiLayerPerceptron::middleFirstLayerLearnThread, this,
                                      i, middle_neuron_num));
      else
        threads.push_back(thread(&MultiLayerPerceptron::middleFirstLayerLearnThread, this,
                                      i, i + charge));
    for (thread &th : threads) th.join();
    //endregion

    //endregion

    //endregion

    loop_count++; // 学習回数をカウント
  }

  // 全ての教師データで正解を出すか，収束限度回数を超えた場合に終了
  vector<string> neuron_params(3);

  // まずは学習上限回数内に学習できたかを詰める（成功: 1，失敗: 0）
  if (loop_count == MAX_TRIAL) neuron_params[0] = to_string(0);
  else neuron_params[0] = to_string(1);

  //次にSdAのパラメータを詰める
  neuron_params[1] = sda_params;

  //region MLP中間層ニューロンのパラメータを詰める
  string mlp_params = "";
  for (int layer = 0; layer < middle_layer_number; ++layer) {
    for (int neuron = 0; neuron < middle_neuron_num; ++neuron) {
      // 重みを詰める
      for (int weightNum = 0; weightNum < input_neuron_num; ++weightNum)
        mlp_params += to_string(middleNeurons[layer][neuron].getInputWeightIndexOf(weightNum))
            + ',';
      mlp_params.pop_back();
      mlp_params += '|';

      // Adamのmを詰める
      for (int mNum = 0; mNum < input_neuron_num; ++mNum)
        mlp_params += to_string(middleNeurons[layer][neuron].getMIndexOf(mNum)) + ',';
      mlp_params.pop_back();
      mlp_params += '|';

      // Adamのnuを詰める
      for (int nuNum = 0; nuNum < input_neuron_num; ++nuNum)
        mlp_params += to_string(middleNeurons[layer][neuron].getNuIndexOf(nuNum)) + ',';
      mlp_params.pop_back();
      mlp_params += '|';

      // Adamのm_hatを詰める
      for (int mHatNum = 0; mHatNum < input_neuron_num; ++mHatNum)
        mlp_params += to_string(middleNeurons[layer][neuron].getMHatIndexOf(mHatNum)) + ',';
      mlp_params.pop_back();
      mlp_params += '|';

      // Adamのnu_hatを詰める
      for (int nuHatNum = 0; nuHatNum < input_neuron_num; ++nuHatNum)
        mlp_params += to_string(middleNeurons[layer][neuron].getNuHatIndexOf(nuHatNum)) + ',';
      mlp_params.pop_back();
      mlp_params += '|';

      // Adamのiterationを詰める
      mlp_params += to_string(middleNeurons[layer][neuron].getIteration()) + '|';

      // バイアスを入れ，最後に ' を入れる
      mlp_params += to_string(middleNeurons[layer][neuron].getBias()) + '\'';
    }

    // 末尾の余計な ' を削除する
    mlp_params.pop_back();

    // レイヤごとの終わりに ':' を入れる
    mlp_params += ':';
  }
  //endregion

  //region MLP出力層ニューロンのパラメータを詰める
  for (int neuron = 0; neuron < output_neuron_num; ++neuron) {
    // 重みを詰める
    for (int weightNum = 0; weightNum < input_neuron_num; ++weightNum)
      mlp_params += to_string(outputNeurons[neuron].getInputWeightIndexOf(weightNum)) + ',';
    mlp_params.pop_back();
    mlp_params += '|';

    //Adamのmを詰める
    for (int mNum = 0; mNum < input_neuron_num; ++mNum)
      mlp_params += to_string(outputNeurons[neuron].getMIndexOf(mNum)) + ',';
    mlp_params.pop_back();
    mlp_params += '|';

    // Adamのnuを詰める
    for (int nuNum = 0; nuNum < input_neuron_num; ++nuNum)
      mlp_params += to_string(outputNeurons[neuron].getNuIndexOf(nuNum)) + ',';
    mlp_params.pop_back();
    mlp_params += '|';

    // Adamのm_hatを詰める
    for (int mHatNum = 0; mHatNum < input_neuron_num; ++mHatNum)
      mlp_params += to_string(outputNeurons[neuron].getMHatIndexOf(mHatNum)) + ',';
    mlp_params.pop_back();
    mlp_params += '|';

    // Adamのnu_hatを詰める
    for (int nuHatNum = 0; nuHatNum < input_neuron_num; ++nuHatNum)
      mlp_params += to_string(outputNeurons[neuron].getNuHatIndexOf(nuHatNum)) + ',';
    mlp_params.pop_back();
    mlp_params += '|';

    // Adamのiterationを詰める
    mlp_params += to_string(outputNeurons[neuron].getIteration()) + '|';

    // バイアスを入れ，最後に ' を入れる
    mlp_params += to_string(outputNeurons[neuron].getBias()) + '\'';
  }
  //endregion

  // 末尾の余計な ' を削除する
  mlp_params.pop_back();

  neuron_params[2] = mlp_params;
  return neuron_params;
}

vector<double> MultiLayerPerceptron::separate_by_camma(string input) {
  vector<double> result;
  stringstream ss = stringstream(input);
  string item;
  while (getline(ss, item, ',')) if (!item.empty()) result.push_back(stod(item));
  item = "";
  ss.str("");
  ss.clear(stringstream::goodbit);

  return result;
}

void MultiLayerPerceptron::sdaFirstLayerOutThread(const vector<double> in,
                                                  const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron)
    sda_out[0][neuron] = sda_neurons[0][neuron].output(in);
}

void MultiLayerPerceptron::sdaOtherLayerOutThread(const int layer,
                                                  const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron)
    sda_out[layer][neuron] = sda_neurons[layer][neuron].output(sda_out[layer - 1]);
}

void MultiLayerPerceptron::middleFirstLayerForwardThread(const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron)
    // SdAの最終層の出力を入れる
    h[0][neuron] = middleNeurons[0][neuron].learn_output(sda_out[sda_out.size() - 1]);
}

void MultiLayerPerceptron::middleLayerForwardThread(const int layer,
                                                    const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron)
    h[layer][neuron] = middleNeurons[layer][neuron].learn_output(h[layer - 1]);
}

void MultiLayerPerceptron::outForwardThread(const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron)
    o[neuron] = outputNeurons[neuron].learn_output(h[middle_layer_number - 1]);
}

void MultiLayerPerceptron::outLearnThread(const vector<double> ans,
                                          const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron) {
    // 出力層ニューロンのdeltaの計算
    double delta = o[neuron] - ans[neuron];

    __android_log_print(ANDROID_LOG_VERBOSE, "MLP", "ce: %f", cross_entropy(o[neuron], ans[neuron]));

    // 損失関数の出力が十分小さい場合は学習しない．そうでなければ正解フラグをfalseに
    if (cross_entropy(o[neuron], ans[neuron]) < MAX_GAP) continue;
    else successFlg = false;

    // 出力層の学習
    outputNeurons[neuron].learn(delta, h[middle_layer_number - 1]);
  }
}

void MultiLayerPerceptron::middleLastLayerLearnThread(const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron) {
    // 中間層ニューロンのdeltaを計算
    double sumDelta = 0.0;
    for (int k = 0; k < output_neuron_num; ++k) {
      Neuron n = outputNeurons[k];
      sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
    }

    // どの活性化関数を用いるかで，deltaの計算方法が変わる
    double delta;
    if (middle_layer_type == 0)
      delta = 1.0 * sumDelta;
    else if (middle_layer_type == 1)
      delta = (h[middle_layer_number - 1][neuron]
          * (1.0 - h[middle_layer_number - 1][neuron])) * sumDelta;
    else if (middle_layer_type == 2)
      delta = (1.0 - pow(h[middle_layer_number - 1][neuron], 2)) * sumDelta;
    else {
      // ReLU
      if (h[middle_layer_number - 1][neuron] > 0) delta = 1.0 * sumDelta;
      else delta = 0 * sumDelta;
    }

    // 学習
    middleNeurons[middle_layer_number - 1][neuron].learn(delta, h[middle_layer_number - 2]);
  }
}

void MultiLayerPerceptron::middleMiddleLayerLearnThread(const int layer,
                                                        const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron) {
    // 中間層ニューロンのdeltaを計算
    double sumDelta = 0.0;
    for (int k = 0; k < middle_neuron_num; ++k) {
      Neuron n = middleNeurons[layer + 1][k];
      sumDelta += n.getInputWeightIndexOf(neuron) * n.getDelta();
    }

    double delta;
    if (middle_layer_type == 0)
      delta = 1.0 * sumDelta;
    else if (middle_layer_type == 1)
      delta = (h[layer][neuron] * (1.0 - h[layer][neuron])) * sumDelta;
    else if (middle_layer_type == 2)
      delta = (1.0 - pow(h[layer][neuron], 2)) * sumDelta;
    else {
      // ReLU
      if (h[layer][neuron] > 0) delta = 1.0 * sumDelta;
      else delta = 0 * sumDelta;
    }

    // 学習
    middleNeurons[layer][neuron].learn(delta, h[layer - 1]);
  }
}

void MultiLayerPerceptron::middleFirstLayerLearnThread(const int begin, const int end) {
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
    if (middle_layer_type == 0)
      delta = 1.0 * sumDelta;
    else if (middle_layer_type == 1)
      delta = (h[0][neuron] * (1.0 - h[0][neuron])) * sumDelta;
    else if (middle_layer_type == 2)
      delta = (1.0 - pow(h[0][neuron], 2)) * sumDelta;
    else {
      // ReLU
      if (h[0][neuron] > 0) delta = 1.0 * sumDelta;
      else delta = 0 * sumDelta;
    }

    // 学習
    middleNeurons[0][neuron].learn(delta, sda_out[sda_out.size() - 1]);
  }
}

/**
 * 与えられたデータをSdA -> MLPと処理した出力を返す
 * @param input ニューラルネットワークに入力するデータ
 * @return SdA -> MLPの出力データ
 */
vector<double> MultiLayerPerceptron::out(vector<double> input) {
  // SdA First Layer
  vector<thread> threads(num_thread);
  int charge = 1;
  threads.clear();
  if (sda_neurons[0].size() <= num_thread) charge = 1;
  else charge = sda_neurons[0].size() / num_thread;
  for (int i = 0; i < sda_neurons[0].size(); i += charge)
    if (i != 0 && sda_neurons[0].size() / i == 1)
      threads.push_back(thread(&MultiLayerPerceptron::sdaFirstLayerOutThread, this,
                                    ref(input), i, sda_neurons[0].size()));
    else
      threads.push_back(thread(&MultiLayerPerceptron::sdaFirstLayerOutThread, this,
                                    ref(input), i, i + charge));
  for (thread &th : threads) th.join();

  // SdA Other Layer
  for (int layer = 1; layer <= (int) sda_neurons.size() - 1; ++layer) {
    threads.clear();
    if (sda_neurons[layer].size() <= num_thread) charge = 1;
    else charge = sda_neurons[layer].size() / num_thread;
    for (int i = 0; i < sda_neurons[layer].size(); i += charge)
      if (i != 0 && sda_neurons[layer].size() / i == 1)
        threads.push_back(thread(&MultiLayerPerceptron::sdaOtherLayerOutThread, this,
                                      layer, i, sda_neurons[layer].size()));
      else
        threads.push_back(thread(&MultiLayerPerceptron::sdaOtherLayerOutThread, this,
                                      layer, i, i + charge));
    for (thread &th : threads) th.join();
  }

  // MLP
  threads.clear();
  if (middle_neuron_num <= num_thread) charge = 1;
  else charge = middle_neuron_num / num_thread;
  for (int i = 0; i < middle_neuron_num; i += charge)
    if (i != 0 && middle_neuron_num / i == 1)
      threads.push_back(thread(&MultiLayerPerceptron::middleFirstLayerOutThread, this,
                                    i, middle_neuron_num));
    else
      threads.push_back(thread(&MultiLayerPerceptron::middleFirstLayerOutThread, this,
                                    i, i + charge));
  for (thread &th : threads) th.join();

  if (middle_neuron_num <= num_thread) charge = 1;
  else charge = middle_neuron_num / num_thread;
  for (int layer = 1; layer <= (int) middle_layer_number - 1; ++layer) {
    threads.clear();
    for (int i = 0; i < middle_neuron_num; i += charge)
      if (i != 0 && middle_neuron_num / i == 1)
        threads.push_back(thread(&MultiLayerPerceptron::middleLayerOutThread, this,
                                      layer, i, middle_neuron_num));
      else
        threads.push_back(thread(&MultiLayerPerceptron::middleLayerOutThread, this,
                                      layer, i, i + charge));
    for (thread &th : threads) th.join();
  }

  threads.clear();
  if (output_neuron_num <= num_thread) charge = 1;
  else charge = output_neuron_num / num_thread;
  for (int i = 0; i < output_neuron_num; i += charge)
    if (i != 0 && output_neuron_num / i == 1)
      threads.push_back(thread(&MultiLayerPerceptron::outOutThread, this,
                                    i, output_neuron_num));
    else
      threads.push_back(thread(&MultiLayerPerceptron::outOutThread, this,
                                    i, i + charge));
  for (thread &th : threads) th.join();

  return learned_o;
}

void MultiLayerPerceptron::middleFirstLayerOutThread(const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron)
    learned_h[0][neuron] = middleNeurons[0][neuron].output(sda_out[sda_out.size() - 1]);
}

void MultiLayerPerceptron::middleLayerOutThread(const int layer, const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron)
    learned_h[layer][neuron] = middleNeurons[layer][neuron].output(learned_h[layer - 1]);
}

void MultiLayerPerceptron::outOutThread(const int begin, const int end) {
  for (int neuron = begin; neuron < end; ++neuron)
    learned_o[neuron] = outputNeurons[neuron].output(learned_h[middle_layer_number - 1]);
}

double MultiLayerPerceptron::cross_entropy(double output, double answer) {
  return -answer * log(output) - (1.0 - answer) * log(1.0 - output);
}
