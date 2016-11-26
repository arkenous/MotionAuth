
#ifndef MOTIONAUTH_STACKEDDENOISINGAUTOENCODER_H
#define MOTIONAUTH_STACKEDDENOISINGAUTOENCODER_H

#include <string>
#include <vector>

using namespace std;

class StackedDenoisingAutoencoder {
 public:
  StackedDenoisingAutoencoder();
  string learn(const vector<vector<double>> input, const unsigned long result_num_dimen,
               const float compression_rate);
  unsigned long getNumMiddleNeuron();

 private:
  vector<vector<double>> add_noise(vector<vector<double>> input, float rate);
  unsigned long num_middle_neurons;
};

#endif //MOTIONAUTH_STACKEDDENOISINGAUTOENCODER_H
