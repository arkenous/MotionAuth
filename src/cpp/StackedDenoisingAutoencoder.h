//
// Created by Kensuke Kosaka on 2016/11/23.
//

#ifndef MOTIONAUTH_STACKEDDENOISINGAUTOENCODER_H
#define MOTIONAUTH_STACKEDDENOISINGAUTOENCODER_H

#include <string>
#include <vector>

class StackedDenoisingAutoencoder {
public:
  StackedDenoisingAutoencoder();
  std::string learn(const std::vector<std::vector<double>> input, const unsigned long result_num_dimen,
                    const float compression_rate);
  unsigned long getNumMiddleNeuron();

private:
  std::vector<std::vector<double>> add_noise(std::vector<std::vector<double>> input, float rate);
  unsigned long num_middle_neurons;
};

#endif //MOTIONAUTH_STACKEDDENOISINGAUTOENCODER_H
