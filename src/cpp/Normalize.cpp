
#include <cmath>
#include "Normalize.h"

using namespace std;

void normalize(vector<double> *input) {
  // 一つのセットにおける平均値を求める
  double avg = 0;
  double sum = 0;
  for (int data = 0; data < (*input).size(); ++data) sum += (*input)[data];
  avg = sum / (*input).size();
  // 偏差の二乗の総和を求める
  sum = 0;
  for (int data = 0; data < (*input).size(); ++data) sum += pow(((*input)[data] - avg), 2);
  // 分散を求める
  double dispersion = sum / (*input).size();

  // 標準偏差を求める
  double standard_deviation = sqrt(dispersion);

  // 正規化し，得たデータで上書きする
  for (int data = 0; data < (*input).size(); ++data)
    (*input)[data] = ((*input)[data] - avg) / standard_deviation;

//  return *input;
}
