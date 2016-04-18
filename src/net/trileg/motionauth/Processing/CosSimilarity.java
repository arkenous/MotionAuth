package net.trileg.motionauth.Processing;

import android.util.Log;
import net.trileg.motionauth.Utility.LogUtil;

public class CosSimilarity {
  public double[] cosSimilarity(double[][][] input) {
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
    LogUtil.log(Log.INFO, "---   CosSimilarity data begin here   ---");
    for (double data : similarity) {
      LogUtil.log(Log.INFO, String.valueOf("similarity: " + data));
    }
    LogUtil.log(Log.INFO, "---   CosSimilarity data end here   ---");

    return similarity;
  }


  public double cosSimilarity(double[][] A, double[][] B) {
    double similarity = 0.0;

    for (int item = 0; item < A[0].length; item++) {
      double AB = A[0][item] * B[0][item] + A[1][item] * B[1][item] + A[2][item] * B[2][item];
      double sizeA = Math.sqrt(Math.pow(A[0][item], 2) + Math.pow(A[1][item], 2) + Math.pow(A[2][item], 2));
      double sizeB = Math.sqrt(Math.pow(B[0][item], 2) + Math.pow(B[1][item], 2) + Math.pow(B[2][item], 2));

      similarity += AB / (sizeA * sizeB);
    }

    similarity /= A[0].length;

    LogUtil.log(Log.INFO, "---   CosSimilarity data begin here ---");
    LogUtil.log(Log.INFO, String.valueOf("similarity: " + similarity));
    LogUtil.log(Log.INFO, "---   CosSimilarity data end here ---");

    return similarity;
  }
}
