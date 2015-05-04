package com.ouhk.webtech.watchoutclient;

import android.content.res.AssetManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

class DtwUtility {
    private int[][] warpingPath;
    private int n;
    private int m;
    private int K;
    private double warpingDistance;

    public DtwUtility() {
    }

    private static List<String> load_file_name(AssetManager assetMgr, final String sample_axis) throws IOException {
        List<String> matches = new ArrayList<>();
        for (String name : assetMgr.list("MotionTemplate")) {
            if (name.startsWith(sample_axis) && name.endsWith(".txt")) {
                matches.add(name);
            }
        }
        return matches;
    }

    public void prepare_variables(DtwData dtwData) {

        n = dtwData.getSeq1_x().length;
        m = dtwData.getSeq2_x().length;
        K = 1;

        warpingPath = new int[n + m][2];    // max(n, m) <= K < n + m
        warpingDistance = 0.0;
    }

    public void compute(DtwData dtwData) {
        double accumulatedDistance;

        double[][] d = new double[n][m];    // local distances
        double[][] D = new double[n][m];    // global distances

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                d[i][j] = Math.pow(
                        distanceBetween(dtwData.getSeq1_x()[i], dtwData.getSeq2_x()[j])
                                + distanceBetween(dtwData.getSeq1_y()[i], dtwData.getSeq2_y()[j])
                                + distanceBetween(dtwData.getSeq1_z()[i], dtwData.getSeq2_z()[j]), .5);
            }
        }

        D[0][0] = d[0][0];

        for (int i = 1; i < n; i++) {
            D[i][0] = d[i][0] + D[i - 1][0];
        }

        for (int j = 1; j < m; j++) {
            D[0][j] = d[0][j] + D[0][j - 1];
        }

        for (int i = 1; i < n; i++) {
            for (int j = 1; j < m; j++) {
                accumulatedDistance = Math.min(Math.min(D[i - 1][j], D[i - 1][j - 1]), D[i][j - 1]);
                accumulatedDistance += d[i][j];
                D[i][j] = accumulatedDistance;
            }
        }
        accumulatedDistance = D[n - 1][m - 1];

        int i = n - 1;
        int j = m - 1;
        int minIndex;

        warpingPath[K - 1][0] = i;
        warpingPath[K - 1][1] = j;

        while ((i + j) != 0) {
            if (i == 0) {
                j -= 1;
            } else if (j == 0) {
                i -= 1;
            } else {    // i != 0 && j != 0
                double[] array = {D[i - 1][j], D[i][j - 1], D[i - 1][j - 1]};
                minIndex = this.getIndexOfMinimum(array);

                if (minIndex == 0) {
                    i -= 1;
                } else if (minIndex == 1) {
                    j -= 1;
                } else if (minIndex == 2) {
                    i -= 1;
                    j -= 1;
                }
            } // end else
            K++;
            warpingPath[K - 1][0] = i;
            warpingPath[K - 1][1] = j;
        } // end while
        warpingDistance = accumulatedDistance / K;

        this.reversePath(warpingPath);
    }

    private void reversePath(int[][] path) {
        int[][] newPath = new int[K][2];
        for (int i = 0; i < K; i++) {
            System.arraycopy(path[K - i - 1], 0, newPath[i], 0, 2);
        }
        warpingPath = newPath;
    }

    public double getDistance() {
        return warpingDistance;
    }

    private double distanceBetween(double p1, double p2) {
        return (p1 - p2) * (p1 - p2);
    }

    private int getIndexOfMinimum(double[] array) {
        int index = 0;
        double val = array[0];

        for (int i = 1; i < array.length; i++) {
            if (array[i] < val) {
                val = array[i];
                index = i;
            }
        }
        return index;
    }

    public String toString() {
        String retVal = "Warping Distance: " + warpingDistance + "\n";
        retVal += "Warping Path: {";
        for (int i = 0; i < K; i++) {
            retVal += "(" + warpingPath[i][0] + ", " + warpingPath[i][1] + ")";
            retVal += (i == K - 1) ? "}" : ", ";

        }
        return retVal;
    }

    public String evaluate_result(Double walk_dist, Double fall_dist) {
        return (walk_dist < fall_dist) ? "Not Fall" : "Fall";
    }

    private List<Float> read_file(AssetManager assetMgr, String filename) {
        List<Float> result = new ArrayList();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(assetMgr.open("MotionTemplate/" + filename)));
            String strLine;
            while ((strLine = br.readLine()) != null) {
                result.add(Float.valueOf(strLine));
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("<read_file>Error: " + e.toString());
        }
        return result;
    }

    public double mean_distance(Double[] distance_arr) {
        Double total_distance = 0.0;
        for (Double each_dist : distance_arr) {
            total_distance += each_dist;
        }
        return (1.0d * total_distance / distance_arr.length);
    }

    public List<List<Float>> scan_data(AssetManager assetMgr, String sample_axis) {

        List<List<Float>> all_sample_data = new ArrayList();

        try {
            List<String> fileList = load_file_name(assetMgr, sample_axis);
            for (String eachfile : fileList) {
                all_sample_data.add(read_file(assetMgr, eachfile));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return all_sample_data;
    }

}
