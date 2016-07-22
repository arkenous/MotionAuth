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


  public MEASURE measure(double[] linearDistance, double[] angle) {
    log(INFO);
    double[] combined = new double[linearDistance.length + angle.length];
    int combinedCount = 0;

    for (double value : linearDistance) {
      combined[combinedCount] = value;
      combinedCount++;
    }
    for (double value : angle) {
      combined[combinedCount] = value;
      combinedCount++;
    }

    double average = 0.0;
    for (double value : combined) {
      average += value;
    }
    average /= combined.length;

    if (average > LOOSE) {
      if (average > NORMAL) {
        if (average > STRICT) {
          return PERFECT;
        }
        return CORRECT;
      }
      return MAYBE;
    }
    return INCORRECT;
  }


  public MEASURE measure(double linearDistance, double angle) {
    log(INFO);
    if (STRICT < linearDistance && STRICT < angle) return PERFECT;
    else if (NORMAL < linearDistance && NORMAL < angle) return CORRECT;
    else if (LOOSE < linearDistance && LOOSE < angle) return MAYBE;
    else return INCORRECT;
  }
}
