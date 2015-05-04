package com.ouhk.webtech.watchoutclient;

import java.util.List;

/**
 * Created by Jacky Li on 25/4/2015.
 */
class DtwData {
    private Float[] seq1_x, seq1_y, seq1_z;
    private float[] seq2_x, seq2_y, seq2_z;
    private List<List<Float>> walk_data_x, walk_data_y, walk_data_z;
    private List<List<Float>> fall_data_x, fall_data_y, fall_data_z;
    private Double final_walk_dist, final_fall_dist;
    private Double[] walk_distance, fall_distance;
    private float[] Ax, Ay, Az;
    private String DTWResult;


    public String getDTWResult() {
        return DTWResult;
    }

    public void setDTWResult(String DTWResult) {
        this.DTWResult = DTWResult;
    }


    public float[] getAx() {
        return Ax;
    }

    public void setAx(float[] ax) {
        Ax = ax;
    }

    public float[] getAy() {
        return Ay;
    }

    public void setAy(float[] ay) {
        Ay = ay;
    }

    public float[] getAz() {
        return Az;
    }

    public void setAz(float[] az) {
        Az = az;
    }


    public Double[] getWalk_distance() {
        return walk_distance;
    }

    public void setWalk_distance(Double[] walk_distance) {
        this.walk_distance = walk_distance;
    }

    public Double[] getFall_distance() {
        return fall_distance;
    }

    public void setFall_distance(Double[] fall_distance) {
        this.fall_distance = fall_distance;
    }

    public Double getFinal_walk_dist() {
        return final_walk_dist;
    }

    public void setFinal_walk_dist(Double final_walk_dist) {
        this.final_walk_dist = final_walk_dist;
    }

    public Double getFinal_fall_dist() {
        return final_fall_dist;
    }

    public void setFinal_fall_dist(Double final_fall_dist) {
        this.final_fall_dist = final_fall_dist;
    }

    public List<List<Float>> getWalk_data_x() {
        return walk_data_x;
    }

    public void setWalk_data_x(List<List<Float>> walk_data_x) {
        this.walk_data_x = walk_data_x;
    }

    public List<List<Float>> getWalk_data_y() {
        return walk_data_y;
    }

    public void setWalk_data_y(List<List<Float>> walk_data_y) {
        this.walk_data_y = walk_data_y;
    }

    public List<List<Float>> getWalk_data_z() {
        return walk_data_z;
    }

    public void setWalk_data_z(List<List<Float>> walk_data_z) {
        this.walk_data_z = walk_data_z;
    }

    public List<List<Float>> getFall_data_x() {
        return fall_data_x;
    }

    public void setFall_data_x(List<List<Float>> fall_data_x) {
        this.fall_data_x = fall_data_x;
    }

    public List<List<Float>> getFall_data_y() {
        return fall_data_y;
    }

    public void setFall_data_y(List<List<Float>> fall_data_y) {
        this.fall_data_y = fall_data_y;
    }

    public List<List<Float>> getFall_data_z() {
        return fall_data_z;
    }

    public void setFall_data_z(List<List<Float>> fall_data_z) {
        this.fall_data_z = fall_data_z;
    }

    public Float[] getSeq1_x() {
        return seq1_x;
    }

    public void setSeq1_x(Float[] seq1_x) {
        this.seq1_x = seq1_x;
    }

    public Float[] getSeq1_y() {
        return seq1_y;
    }

    public void setSeq1_y(Float[] seq1_y) {
        this.seq1_y = seq1_y;
    }

    public Float[] getSeq1_z() {
        return seq1_z;
    }

    public void setSeq1_z(Float[] seq1_z) {
        this.seq1_z = seq1_z;
    }

    public float[] getSeq2_x() {
        return seq2_x;
    }

    public void setSeq2_x(float[] seq2_x) {
        this.seq2_x = seq2_x;
    }

    public float[] getSeq2_y() {
        return seq2_y;
    }

    public void setSeq2_y(float[] seq2_y) {
        this.seq2_y = seq2_y;
    }

    public float[] getSeq2_z() {
        return seq2_z;
    }

    public void setSeq2_z(float[] seq2_z) {
        this.seq2_z = seq2_z;
    }
}
