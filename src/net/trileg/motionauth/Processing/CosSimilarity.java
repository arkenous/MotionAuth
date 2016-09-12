package net.trileg.motionauth.Processing;

import static android.util.Log.INFO;
import static net.trileg.motionauth.Utility.Enum.*;
import static net.trileg.motionauth.Utility.Enum.MEASURE.*;
import static net.trileg.motionauth.Utility.LogUtil.log;

public class CosSimilarity {
  public double[] cosSimilarity(double[][][] input) {
    log(INFO);
    double[] similarity = new double[input.length];

    for (int time = 0; time < input.length; time++) {
      for (int item = 0; item < input[time][0].length; item++) {
        double AB = input[time][0][item] * input[(time + 1) % input.length][0][item]
            + input[time][1][item] * input[(time + 1) % input.length][1][item]
            + input[time][2][item] * input[(time + 1) % input.length][2][item];
        double sizeA = Math.sqrt(Math.pow(input[time][0][item], 2)
            + Math.pow(input[time][1][item], 2)
            + Math.pow(input[time][2][item], 2));
        double sizeB = Math.sqrt(Math.pow(input[(time + 1) % input.length][0][item], 2)
            + Math.pow(input[(time + 1) % input.length][1][item], 2)
            + Math.pow(input[(time + 1) % input.length][2][item], 2));
        similarity[time] += AB / (sizeA * sizeB);
      }

      similarity[time] /= input[time][0].length;
    }

    // 取得回数AB, AC, BCそれぞれの類似度が出るはずなので，これから判断する
    log(INFO, "---   CosSimilarity data begin here   ---");
    for (double data : similarity) {
      log(INFO, String.valueOf("similarity: " + data));
    }
    log(INFO, "---   CosSimilarity data end here   ---");

    return similarity;
  }


  public double cosSimilarity(double[][] A, double[][] B) {
    log(INFO);
    double similarity = 0.0;

    for (int item = 0; item < A[0].length; item++) {
      double AB = A[0][item] * B[0][item] + A[1][item] * B[1][item] + A[2][item] * B[2][item];
      double sizeA = Math.sqrt(Math.pow(A[0][item], 2) + Math.pow(A[1][item], 2) + Math.pow(A[2][item], 2));
      double sizeB = Math.sqrt(Math.pow(B[0][item], 2) + Math.pow(B[1][item], 2) + Math.pow(B[2][item], 2));

      similarity += AB / (sizeA * sizeB);
    }

    similarity /= A[0].length;

    log(INFO, "---   CosSimilarity data begin here ---");
    log(INFO, String.valueOf("similarity: " + similarity));
    log(INFO, "---   CosSimilarity data end here ---");

    return similarity;
  }


  public MEASURE measure(double[] cosSimilarity) {
    log(INFO);

    if (cosSimilarity[0] > STRICT && cosSimilarity[1] > STRICT && cosSimilarity[2] > STRICT) return PERFECT;
    if (cosSimilarity[0] > NORMAL && cosSimilarity[1] > NORMAL && cosSimilarity[2] > NORMAL) return CORRECT;
    if (cosSimilarity[0] > LOOSE && cosSimilarity[1] > LOOSE && cosSimilarity[2] > LOOSE) return MAYBE;
    return INCORRECT;
  }


  public MEASURE measure(double vector) {
    log(INFO);
    if (STRICT < vector) return PERFECT;
    else if (NORMAL < vector) return CORRECT;
    else if (LOOSE < vector) return MAYBE;
    else return INCORRECT;
  }
}
