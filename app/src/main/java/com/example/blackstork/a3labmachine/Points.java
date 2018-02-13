package com.example.blackstork.a3labmachine;

import android.graphics.Color;


public class Points {
    int x, y, z;
    int color;
    double realX, realY, realZ;
    Lights I;

    Points() {
        I = new Lights();
        x = 0; y = 0; z = 0;
        realX = 0; realZ = 0; realY = 0;

        color = Color.GRAY;
    }

    Points(double X, double Y, double Z) {
        I = new Lights();
        realX = X; realY = Y; realZ = Z;
        x = (int) X; y = (int) Y; z = (int) Z;

        color = Color.GRAY;
    }

    void init(int x, int y, int z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    void changeRealPos(double x, double y, double z) {
        this.realX = x;
        this.realY = y;
        this.realZ = z;
    }

    void changeColor(int color)
    {
        this.color = color;
    }

    int getX(){
        return x;
    }

    int getY(){
        return y;
    }

    int getZ(){
        return z;
    }

    int getColor()
    {
        return color;
    }

}