package net.trileg.motionauth.Processing;

import static android.util.Log.DEBUG;
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
    log(DEBUG, "---   CosSimilarity data begin here   ---");
    for (double data : similarity) {
      log(DEBUG, String.valueOf("similarity: " + data));
    }
    log(DEBUG, "---   CosSimilarity data end here   ---");

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

    log(DEBUG, "---   CosSimilarity data begin here ---");
    log(DEBUG, String.valueOf("similarity: " + similarity));
    log(DEBUG, "---   CosSimilarity data end here ---");

    return similarity;
  }


  public MEASURE measure(double[] cosSimilarity) {
    log(INFO);

    MEASURE result = PERFECT;

    for (double aCosSimilarity : cosSimilarity) if (aCosSimilarity <= STRICT) result = CORRECT;
    if (result == PERFECT) return result;

    for (double aCosSimilarity : cosSimilarity) if (aCosSimilarity <= NORMAL) result = MAYBE;
    if (result == CORRECT) return result;

    for (double aCosSimilarity : cosSimilarity) if (aCosSimilarity <= LOOSE) result = INCORRECT;
    return result;
  }


  public MEASURE measure(double vector) {
    log(INFO);
    if (STRICT < vector) return PERFECT;
    else if (NORMAL < vector) return CORRECT;
    else if (LOOSE < vector) return MAYBE;
    else return INCORRECT;
  }
}
