package com.coup;

import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;

/**
 * Created by Sarosi on 07/02/2018.
 */
public class Matrix {

    public ArrayList<ArrayList<Double>> mat;

    public int playernum;

    public Matrix(int playernum){
        mat = new ArrayList<ArrayList<Double>>();
        this.playernum = playernum;
        for (int i = 0; i < 15; i++) {
            ArrayList<Double> inner = new ArrayList<>();
            for (int j = 0; j < 2+playernum; j++) {
                inner.add(2.0);
            }
            mat.add(inner);
        }
    }

    public void setSure(){

    }

    public ArrayList<Double> getDeck(){
      ArrayList<Double> ret = new ArrayList<>();
        for (int i = 0; i < mat.size(); i++) {
            ret.add(mat.get(i).get(0));
        }
        return ret;
    }
    public ArrayList<Double> getTable(){
        ArrayList<Double> ret = new ArrayList<>();
        for (int i = 0; i < mat.size(); i++) {
            ret.add(mat.get(i).get(1));
        }
        return ret;
    }

    public ArrayList<Double> getPlayer(int number){
        ArrayList<Double> ret = new ArrayList<>();
        for (int i = 0; i < mat.size(); i++) {
            ret.add(mat.get(i).get(2+playernum));
        }
        return ret;
    }



    public ArrayList<ArrayList<Double>> getContessa(){
        ArrayList<ArrayList<Double>> ret = new ArrayList<>();
        ret.add(mat.get(0));
        ret.add(mat.get(1));
        ret.add(mat.get(2));
        return ret;
    }
    public ArrayList<ArrayList<Double>> getCaptain(){
        ArrayList<ArrayList<Double>> ret = new ArrayList<>();
        ret.add(mat.get(3));
        ret.add(mat.get(4));
        ret.add(mat.get(5));
        return ret;
    }
    public ArrayList<ArrayList<Double>> GetAmbassador(){
        ArrayList<ArrayList<Double>> ret = new ArrayList<>();
        ret.add(mat.get(6));
        ret.add(mat.get(7));
        ret.add(mat.get(8));
        return ret;
    }
    public ArrayList<ArrayList<Double>> GetAssassin(){
        ArrayList<ArrayList<Double>> ret = new ArrayList<>();
        ret.add(mat.get(9));
        ret.add(mat.get(10));
        ret.add(mat.get(11));
        return ret;
    }
    public ArrayList<ArrayList<Double>> GatDuke(){
        ArrayList<ArrayList<Double>> ret = new ArrayList<>();
        ret.add(mat.get(12));
        ret.add(mat.get(13));
        ret.add(mat.get(14));
        return ret;
    }

    public void print(){
        for (int i = 0; i < mat.size(); i++) {
            for (int j = 0; j < mat.get(i).size(); j++) {
                System.out.print(mat.get(i).get(j) + " ");
            }
            System.out.println(" ");
        }
    }




//       for (int i = 0; i < mat.size(); i++) {
//        for (int j = 0; j < mat.get(i).size(); j++) {
//
//        }
//
//    }
}
