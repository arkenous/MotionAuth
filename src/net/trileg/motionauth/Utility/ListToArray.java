package net.trileg.motionauth.Utility;

import java.util.ArrayList;
import java.util.Arrays;

public class ListToArray {
  public float[][] listTo2DArray(ArrayList<ArrayList<Float>> list) {
    float[][] array = new float[list.size()][];

    for (int i = 0; i < list.size(); i++) {
      float[] tmp = new float[list.get(i).size()];
      for (int j = 0; j < list.get(i).size(); j++) {
        tmp[j] = list.get(i).get(j);
      }
      array[i] = Arrays.copyOf(tmp, tmp.length);
    }

    return array;
  }


  public float[][][] listTo3DArray(ArrayList<ArrayList<ArrayList<Float>>> list) {
    float[][][] array = new float[list.size()][list.get(0).size()][];

    for (int i = 0; i < list.size(); i++) {
      for (int j = 0; j < list.get(i).size(); j++) {
        float[] tmp = new float[list.get(i).get(j).size()];
        for (int k = 0; k < list.get(i).get(j).size(); k++) {
          tmp[k] = list.get(i).get(j).get(k);
        }
        array[i][j] = Arrays.copyOf(tmp, tmp.length);
      }
    }

    return array;
  }
}
