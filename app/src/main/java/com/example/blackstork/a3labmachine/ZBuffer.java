package com.example.blackstork.a3labmachine;

public class ZBuffer {
    int depth[][];
    int h = 0, w = 0, min = 1000;

    ZBuffer(int h, int w){
        this.h = h;
        this.w = w;
        depth = new int[h][w];
        for(int i = 0; i < h; i++){
            for(int j = 0; j < w; j++) {
                depth[i][j] = min;
            }
        }
    }

    void update() {
        for(int i = 0; i < h; i++)
            for(int j = 0; j < w; j++) {
                depth[i][j] = min;
            }
    }

    boolean setIf(double x, double y, double value)
    {
        return setIf((int) x, (int) y, (int) value);
    }

    boolean setIf(float x, float y, float value)
    {
        return setIf((int) x, (int) y, (int) value);
    }

    boolean setIf(int x, int y, int value)
    {
        if(x >= 1080 || x <= 0 || y <= 0 || y >= 1920)
            return false;
        if (value < depth[y][x]) {
            depth[y][x] = value;
            return true;
        }
        return false;
    }

}
