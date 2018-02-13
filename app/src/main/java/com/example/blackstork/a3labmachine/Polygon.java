package com.example.blackstork.a3labmachine;


import android.annotation.TargetApi;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.Build;

public class Polygon {

    static final int pos1 = -50, pos2 = 50;
    Points pt[];
    private Points allPt[]; //Используются для высчитывания нормали к вершине
    Points normals[];
    final int p = 200, c2 = 960, c1 = 540, d = 400;
    int posX, posY, posZ;
    private String mode;
    private Color color0;
    double sinTheta, sinPhi, cosTheta, cosPhi;
    ZBuffer zBuffer;

    Polygon(Points p[], Points points[], ZBuffer zBuffer) {
        allPt = points;
        this.zBuffer = zBuffer;
        pt = new Points[4];
        normals = new Points[4];
        color0 = new Color();
        for(int i = 0; i < 4; i++)
            pt[i] = p[i];
        posX = p[0].getX();
        posY = p[0].getY();
        posZ = p[0].getZ();

        mode = "";
        if(posZ == pt[2].getZ())
            mode = "Bottom";
        if(posX == pt[2].getX())
            mode = "Left";
        if(posY == pt[2].getY())
            mode = "Up";
    }

    float getDistance(Points p1, Points p2) {
        return (float) Math.sqrt( Math.pow(p1.realX - p2.realX, 2) + Math.pow(p1.realY - p2.realY, 2) + Math.pow(p1.realZ - p2.realZ, 2));
    }

    float getLenght(Points p) {
        return (float) Math.sqrt(p.realX * p.realX + p.realY * p.realY + p.realZ * p.realZ);
    }

    Points cross(Points p1, Points p2) {
        Points crossed = new Points();
        crossed.changeRealPos( p1.realY * p2.realZ - p1.realZ * p2.realY, p1.realZ * p2.realX - p1.realX * p2.realZ, p1.realX * p2.realY - p1.realY * p2.realX );
        return crossed;
    }

    Points getNormal(int i) {
        Points p = new Points();
        Points q = new Points();
        Points norm = new Points();

        p.changeRealPos(pt[(i+1)%4].realX - pt[i%4].realX, pt[(i+1)%4].realY - pt[i%4].realY, pt[(i+1)%4].realZ - pt[i%4].realZ);
        q.changeRealPos(pt[(i-1)%4].realX - pt[i%4].realX, pt[(i-1)%4].realY - pt[i%4].realY, pt[(i-1)%4].realZ - pt[i%4].realZ);

        Points crossed = cross(p, q);
        float len = getLenght(crossed);

        norm.changeRealPos(crossed.realX / len + pt[i%4].realX, crossed.realY / len + pt[i%4].realY, crossed.realZ / len + pt[i%4].realZ);

        return norm;
    }

    double dot(Points p1, Points p2) {
        return p1.realX * p2.realX + p1.realY * p2.realY + p1.realZ * p2.realZ;
    }

    Points norm(Points p1) {
        float len = getLenght(p1);
        Points p = new Points();
        p.changeRealPos(p1.realX / len, p1.realY / len, p1.realZ / len);
        return p;
    }

    Points normalize(Points p1, Points p2) {
        Points p = new Points();
        p.changeRealPos( p1.realX - p2.realX, p1.realY - p2.realY, p1.realZ - p2.realZ);
        p = norm(p);
        //p.changeRealPos( p.realX + p2.realX, p.realY + p2.realY, p.realZ + p2.realZ);
        return p;
    }

    Points getVertexNormal(int i) {
        Points point = new Points();
        Points p = new Points();
        Points q = new Points();
        Points r = new Points();

        p.changeRealPos(pt[(i+1)%4].realX - pt[i%4].realX, pt[(i+1)%4].realY - pt[i%4].realY, pt[(i+1)%4].realZ - pt[i%4].realZ);
        q.changeRealPos(pt[(i-1)%4].realX - pt[i%4].realX, pt[(i-1)%4].realY - pt[i%4].realY, pt[(i-1)%4].realZ - pt[i%4].realZ);
        r.changeRealPos(allPt[i%4].realX - pt[i%4].realX, allPt[i%4].realY - pt[i%4].realY, allPt[i%4].realZ - pt[i%4].realZ);

        Points crossedQP, crossedRP, crossedRQ;
        if( (mode == "Bottom" && pt[0].z == 50) || (mode == "Left" && pt[0].x == 50) || (mode == "Up" && pt[0].y == -50)) {
            crossedQP = cross(p, q);
            crossedRP = cross(r, p);
            crossedRQ = cross(q, r);
        }
        else {
            crossedQP = cross(q, p);
            crossedRP = cross(p, r);
            crossedRQ = cross(r, q);
        }

        float len1 = getLenght(crossedQP);
        float len2 = getLenght(crossedRP);;
        float len3 = getLenght(crossedRQ);

        point.changeRealPos(
                (crossedQP.realX / len1 + crossedRP.realX / len2 + crossedRQ.realX / len3),
                (crossedQP.realY / len1 + crossedRP.realY / len2 + crossedRQ.realY / len3),
                (crossedQP.realZ / len1 + crossedRP.realZ / len2 + crossedRQ.realZ / len3)
        );

        point = norm(point);
        //float len = getLenght(point);
        //point.changeRealPos(point.realX / len + pt[i%4].realX, point.realY / len + pt[i%4].realY, point.realZ / len + pt[i%4].realZ);
        return point;
    }

    @TargetApi(Build.VERSION_CODES.O)
    private float []interpolate(float y, float x) {
        float u, w, t;
        if(mode == "Bottom") {
            u = Math.abs(1.0f * (pt[0].y - y) / (pt[0].y - pt[1].y));
            w = Math.abs(1.0f * (pt[2].y - y) / (pt[2].y - pt[3].y));
            t = Math.abs((pt[0].x - x) / (pt[0].x - pt[2].x));
        }
        else if(mode == "Left") {
            u = Math.abs(1.0f * (pt[0].z - x) / (pt[0].z - pt[1].z));
            w = Math.abs(1.0f * (pt[2].z - x) / (pt[2].z - pt[3].z));
            t = Math.abs((pt[0].z - y) / (pt[0].z - pt[2].z));
        }
        else {
            u = Math.abs(1.0f * (pt[0].z - x) / (pt[0].z - pt[1].z));
            w = Math.abs(1.0f * (pt[2].z - x) / (pt[2].z - pt[3].z));
            t = Math.abs((pt[0].x - y) / (pt[0].x - pt[2].x));
        }

        float I1[] = new float[] {pt[0].I.R * (1 - u) + pt[1].I.R * u, pt[0].I.G * (1 - u) + pt[1].I.G * u, pt[0].I.B * (1 - u) + pt[1].I.B * u};
        float I2[] = new float[] {pt[2].I.R * (1 - w) + pt[3].I.R * w, pt[2].I.G * (1 - w) + pt[3].I.G * w, pt[2].I.B * (1 - w) + pt[3].I.B * w};

        return new float[] {I1[0] * (1 - t) + I2[0] * t, I1[1] * (1 - t) + I2[1] * t, I1[2] * (1 - t) + I2[2] * t};

    }

    @TargetApi(Build.VERSION_CODES.O)
    private Points interpolateNormals(float y, float x, Points norm[]) {
        float u, w, t;
        if(mode == "Bottom") {
            u = Math.abs(1.0f * (pt[0].y - y) / (pt[0].y - pt[1].y));
            w = Math.abs(1.0f * (pt[2].y - y) / (pt[2].y - pt[3].y));
            t = Math.abs((pt[0].x - x) / (pt[0].x - pt[2].x));
        }
        else if(mode == "Left") {
            u = Math.abs(1.0f * (pt[0].z - x) / (pt[0].z - pt[1].z));
            w = Math.abs(1.0f * (pt[2].z - x) / (pt[2].z - pt[3].z));
            t = Math.abs((pt[0].z - y) / (pt[0].z - pt[2].z));
        }
        else {
            u = Math.abs(1.0f * (pt[0].z - x) / (pt[0].z - pt[1].z));
            w = Math.abs(1.0f * (pt[2].z - x) / (pt[2].z - pt[3].z));
            t = Math.abs((pt[0].x - y) / (pt[0].x - pt[2].x));
        }

        double N1[] = new double[] {norm[0].realX * (1 - u) + norm[1].realX * u, norm[0].realY * (1 - u) + norm[1].realY * u, norm[0].realZ * (1 - u) + norm[1].realZ * u};
        double N2[] = new double[] {norm[2].realX * (1 - w) + norm[3].realX * w, norm[2].realY * (1 - w) + norm[3].realY * w, norm[2].realZ * (1 - w) + norm[3].realZ * w};

        return new Points(N1[0] * (1 - t) + N2[0] * t, N1[1] * (1 - t) + N2[1] * t, N1[2] * (1 - t) + N2[2] * t);
    }

    void draw(int method, float angleXY, float angleXZ, Points lights[], Paint paint, Canvas canvas) {
        switch (method) {
            case 0: drawSimple(angleXY, angleXZ, lights, paint, canvas);
                break;
            case 1: drawGuro(angleXY, angleXZ, lights, paint, canvas);
                break;
            case 2: drawPhong(angleXY, angleXZ, lights, paint, canvas);
                break;

        }

    }

    @TargetApi(Build.VERSION_CODES.O)
    void drawSimple(float angleXY, float angleXZ, Points lights[], Paint paint, Canvas canvas) {
        sinTheta = Math.sin(Math.toRadians(angleXY));
        sinPhi = Math.sin(Math.toRadians(angleXZ));
        cosTheta = Math.cos(Math.toRadians(angleXY));
        cosPhi = Math.cos(Math.toRadians(angleXZ));

        for(int i = 0; i < 4; i++)
            normals[(i + 1)%4] = getVertexNormal(i + 1);

        float X, Y, Z;
        float buf[] = new float[3];
        float colors[] = new float[3];
        for (int i = 0; i < 4; i++) {
            buf[0] = 0.1f; buf[1] = 0.1f; buf[2] = 0.1f;
            for (Points light: lights) {
                if (light != null) {
                    float distance = getDistance(new Points(0, 0, 200), light);
                    Points lightVector = normalize(light, new Points(0, 0, 200));
                    double cosTheta2 = dot(lightVector, normals[i]);
                    double diffuse = (100 * cosTheta2 + 0.5) / (distance + 1);

                    color0 = Color.valueOf(light.getColor());
                    buf[0] += color0.red() * diffuse;
                    buf[1] += color0.green() * diffuse;
                    buf[2] += color0.blue() * diffuse;
                }
            }
            colors[0] += buf[0];
            colors[1] += buf[1];
            colors[2] += buf[2];
        }
        for(int i = 0; i < 3; i++) {
            if (colors[i] > 4)
                colors[i] = 4.f;
            else if (colors[i] < 0)
                colors[i] = 0.f;
        }


        switch (mode) {
            case "Bottom":
                float aX, aY;
                for (float i = pos1; i < pos2; i += 0.25) {
                    for (float j = pos1; j < pos2; j += 0.25) {
                        X = (float) (-1 * sinTheta * j + cosTheta * i);
                        Y = (float) (-1 * cosPhi * cosTheta * j - cosPhi * sinTheta * i + sinPhi * posZ);
                        Z = (float) (-1 * sinPhi * cosTheta * j - sinPhi * sinTheta * i - cosPhi * posZ + p);

                        aX = d * X / Z + c1;
                        aY = d * Y / Z + c2;
                        if (zBuffer.setIf(aX, aY, Z)) {
                            int color = Color.rgb(colors[0] / 4.f, colors[1] / 4.f, colors[2] / 4.f);
                            paint.setColor(color);
                            canvas.drawPoint(aX, aY, paint);
                        }
                    }
                }
                break;
            case "Left":
                for (float i = pos1; i < pos2; i += 0.25) {
                    for (float j = pos1; j < pos2; j += 0.25) {
                        X = (float) (-1 * sinTheta * posX + cosTheta * i);
                        Y = (float) (-1 * cosPhi * cosTheta * posX - cosPhi * sinTheta * i + sinPhi * j);
                        Z = (float) (-1 * sinPhi * cosTheta * posX - sinPhi * sinTheta * i - cosPhi * j + p);

                        aX = d * X / Z + c1;
                        aY = d * Y / Z + c2;
                        if (zBuffer.setIf(aX, aY, Z)) {
                            int color = Color.rgb(colors[0] / 4.f, colors[1] / 4.f, colors[2] / 4.f);
                            paint.setColor(color);
                            canvas.drawPoint(aX, aY, paint);
                        }
                    }
                }
                break;
            case "Up":
                for (float i = pos1; i < pos2; i += 0.25) {
                    for (float j = pos1; j < pos2; j += 0.25) {
                        X = (float) (-1 * sinTheta * i + cosTheta * posY);
                        Y = (float) (-1 * cosPhi * cosTheta * i - cosPhi * sinTheta * posY + sinPhi * j);
                        Z = (float) (-1 * sinPhi * cosTheta * i - sinPhi * sinTheta * posY - cosPhi * j + p);

                        aX = d * X / Z + c1;
                        aY = d * Y / Z + c2;
                        if (zBuffer.setIf(aX, aY, Z)) {
                            int color = Color.rgb(colors[0] / 4.f, colors[1] / 4.f, colors[2] / 4.f);
                            paint.setColor(color);
                            canvas.drawPoint(aX, aY, paint);
                        }
                    }
                }
                break;
        }

    }

    @TargetApi(Build.VERSION_CODES.O)
    void drawGuro(float angleXY, float angleXZ, Points lights[], Paint paint, Canvas canvas) {
        sinTheta = Math.sin(Math.toRadians(angleXY));
        sinPhi = Math.sin(Math.toRadians(angleXZ));
        cosTheta = Math.cos(Math.toRadians(angleXY));
        cosPhi = Math.cos(Math.toRadians(angleXZ));

        for(int i = 0; i < 4; i++)
            normals[(i + 1)%4] = getVertexNormal(i + 1);

        float X, Y, Z;

        for (int i = 0; i < 4; i++) {
            pt[i].I.R = 0.1f; pt[i].I.G = 0.1f; pt[i].I.B = 0.1f;
            for (Points light: lights) {
                if (light != null) {
                    float distance = getDistance(new Points(0, 0, 200), light);
                    Points lightVector = normalize(light, new Points(0, 0, 200));

                    double cosTheta2 = dot(lightVector, normals[i]) / getLenght(lightVector) / getLenght(normals[i]);
                    double diffuse = (100 * cosTheta2 + 0.5) / (distance + 1);

                    color0 = Color.valueOf(light.getColor());
                    pt[i].I.R += color0.red() * diffuse;
                    pt[i].I.G += color0.green() * diffuse;
                    pt[i].I.B += color0.blue() * diffuse;
                }
                if(pt[i].I.R > 1) pt[i].I.R = 1;
                if(pt[i].I.G > 1) pt[i].I.G = 1;
                if(pt[i].I.B > 1) pt[i].I.B = 1;
            }
        }

        for(int i = 0; i < 3; i++) {
            if(pt[i].I.R > 1) pt[i].I.R = 1;
            if(pt[i].I.G > 1) pt[i].I.G = 1;
            if(pt[i].I.B > 1) pt[i].I.B = 1;
            if(pt[i].I.R < 0) pt[i].I.R = 0.0f;
            if(pt[i].I.G < 0) pt[i].I.G = 0.0f;
            if(pt[i].I.B < 0) pt[i].I.B = 0.0f;
        }

        Points pts = new Points();
        float I[];
        switch (mode) {
            case "Bottom":
                float aX, aY;
                for (float i = pos1; i < pos2; i += 0.25f) {
                    for (float j = pos1; j < pos2; j += 0.25f) {
                        X = (float) (-1 * sinTheta * j + cosTheta * i);
                        Y = (float) (-1 * cosPhi * cosTheta * j - cosPhi * sinTheta * i + sinPhi * posZ);
                        Z = (float) (-1 * sinPhi * cosTheta * j - sinPhi * sinTheta * i - cosPhi * posZ + p);

                        aX = d * X / Z + c1;
                        aY = d * Y / Z + c2;

                        if (zBuffer.setIf(aX, aY, Z)) {
                            pts.changeRealPos(X, Y, Z);
                            I = interpolate(i, j);
                            if(I[0] > 1) I[0] = 1;
                            if(I[1] > 1) I[1] = 1;
                            if(I[2] > 1) I[2] = 1;
                            if(I[0] < 0) I[0] = 0.0f;
                            if(I[1] < 0) I[1] = 0.0f;
                            if(I[2] < 0) I[2] = 0.0f;
                            int color = Color.rgb(I[0], I[1], I[2]);
                            paint.setColor(color);
                            canvas.drawPoint(aX, aY, paint);
                        }
                    }
                }
                break;
            case "Left":
                for (float i = pos1; i < pos2; i += 0.25f) {
                    for (float j = pos1; j < pos2; j += 0.25f) {
                        X = (float) (-1 * sinTheta * posX + cosTheta * i);
                        Y = (float) (-1 * cosPhi * cosTheta * posX - cosPhi * sinTheta * i + sinPhi * j);
                        Z = (float) (-1 * sinPhi * cosTheta * posX - sinPhi * sinTheta * i - cosPhi * j + p);

                        aX = d * X / Z + c1;
                        aY = d * Y / Z + c2;

                        if (zBuffer.setIf(aX, aY, Z)) {
                            pts.changeRealPos(X, Y, Z);
                            I = interpolate(i, j);
                            if(I[0] > 1) I[0] = 1;
                            if(I[1] > 1) I[1] = 1;
                            if(I[2] > 1) I[2] = 1;
                            if(I[0] < 0) I[0] = 0.0f;
                            if(I[1] < 0) I[1] = 0.0f;
                            if(I[2] < 0) I[2] = 0.0f;
                            int color = Color.rgb(I[0], I[1], I[2]);
                            paint.setColor(color);
                            canvas.drawPoint(aX, aY, paint);
                        }
                    }
                }
                break;
            case "Up":
                for (float i = pos1; i < pos2; i += 0.25f) {
                    for (float j = pos1; j < pos2; j += 0.25f) {
                        X = (float) (-1 * sinTheta * i + cosTheta * posY);
                        Y = (float) (-1 * cosPhi * cosTheta * i - cosPhi * sinTheta * posY + sinPhi * j);
                        Z = (float) (-1 * sinPhi * cosTheta * i - sinPhi * sinTheta * posY - cosPhi * j + p);

                        aX = d * X / Z + c1;
                        aY = d * Y / Z + c2;

                        if (zBuffer.setIf(aX, aY, Z)) {
                            pts.changeRealPos(X, Y, Z);
                            I = interpolate(i, j);
                            if(I[0] > 1) I[0] = 1;
                            if(I[1] > 1) I[1] = 1;
                            if(I[2] > 1) I[2] = 1;
                            if(I[0] < 0) I[0] = 0.0f;
                            if(I[1] < 0) I[1] = 0.0f;
                            if(I[2] < 0) I[2] = 0.0f;
                            int color = Color.rgb(I[0], I[1], I[2]);
                            paint.setColor(color);
                            canvas.drawPoint(aX, aY, paint);
                        }
                    }
                }
                break;
        }

    }

    @TargetApi(Build.VERSION_CODES.O)
    void drawPhong(float angleXY, float angleXZ, Points lights[], Paint paint, Canvas canvas) {
        sinTheta = Math.sin(Math.toRadians(angleXY));
        sinPhi = Math.sin(Math.toRadians(angleXZ));
        cosTheta = Math.cos(Math.toRadians(angleXY));
        cosPhi = Math.cos(Math.toRadians(angleXZ));

        for(int i = 0; i < 4; i++)
            normals[(i + 1)%4] = getVertexNormal(i + 1);

        paint.setColor(Color.rgb(0.1f, 0.1f, 0.1f));
        float X, Y, Z;

        Points pts = new Points();
        Points Normal;
        double distance, cosTheta2, diffuse;
        switch (mode) {
            case "Bottom":
                float aX, aY;
                for (float i = pos1; i < pos2; i += 0.25f) {
                    for (float j = pos1; j < pos2; j += 0.25f) {
                        X = (float) (-1 * sinTheta * j + cosTheta * i);
                        Y = (float) (-1 * cosPhi * cosTheta * j - cosPhi * sinTheta * i + sinPhi * posZ);
                        Z = (float) (-1 * sinPhi * cosTheta * j - sinPhi * sinTheta * i - cosPhi * posZ + p);

                        aX = d * X / Z + c1;
                        aY = d * Y / Z + c2;
                        if (zBuffer.setIf(aX, aY, Z)) {
                            pts.I.R = 0.1f; pts.I.G = 0.1f; pts.I.B = 0.1f;
                            pts.changeRealPos(X, Y, Z);
                            Normal = interpolateNormals(i, j, normals);

                            for (Points light: lights) {
                                if (light != null) {
                                    distance = getDistance(new Points(0, 0, 200), light);
                                    Points lightVector = normalize(light, new Points(0, 0, 200));
                                    cosTheta2 = dot(lightVector, Normal) / getLenght(lightVector) / getLenght(Normal);
                                    diffuse = (100 * cosTheta2 + 0.5) / (distance + 1);

                                    color0 = Color.valueOf(light.getColor());
                                    pts.I.R += color0.red() * diffuse;
                                    pts.I.G += color0.green() * diffuse;
                                    pts.I.B += color0.blue() * diffuse;
                                }
                            }
                            if(pts.I.R > 1) pts.I.R = 1;
                            if(pts.I.G > 1) pts.I.G = 1;
                            if(pts.I.B > 1) pts.I.B = 1;
                            if(pts.I.R < 0) pts.I.R = 0.0f;
                            if(pts.I.G < 0) pts.I.G = 0.0f;
                            if(pts.I.B < 0) pts.I.B = 0.0f;
                            int color = Color.rgb(pts.I.R, pts.I.G, pts.I.B);
                            paint.setColor(color);
                            canvas.drawPoint(aX, aY, paint);
                        }
                    }
                }
                break;
            case "Left":
                for (float i = pos1; i < pos2; i += 0.25f) {
                    for (float j = pos1; j < pos2; j += 0.25f) {
                        X = (float) (-1 * sinTheta * posX + cosTheta * i);
                        Y = (float) (-1 * cosPhi * cosTheta * posX - cosPhi * sinTheta * i + sinPhi * j);
                        Z = (float) (-1 * sinPhi * cosTheta * posX - sinPhi * sinTheta * i - cosPhi * j + p);

                        aX = d * X / Z + c1;
                        aY = d * Y / Z + c2;

                        if (zBuffer.setIf(aX, aY, Z)) {
                            pts.I.R = 0.1f; pts.I.G = 0.1f; pts.I.B = 0.1f;
                            pts.changeRealPos(X, Y, Z);
                            Normal = interpolateNormals(i, j, normals);

                            for (Points light: lights) {
                                if (light != null) {
                                    distance = getDistance(new Points(0, 0, 200), light);
                                    Points lightVector = normalize(light, new Points(0, 0, 200));
                                    cosTheta2 = dot(lightVector, Normal) / getLenght(lightVector) / getLenght(Normal);
                                    diffuse = (100 * cosTheta2 + 0.5) / (distance + 1);

                                    color0 = Color.valueOf(light.getColor());
                                    pts.I.R += color0.red() * diffuse;
                                    pts.I.G += color0.green() * diffuse;
                                    pts.I.B += color0.blue() * diffuse;
                                }
                            }
                            if(pts.I.R > 1) pts.I.R = 1;
                            if(pts.I.G > 1) pts.I.G = 1;
                            if(pts.I.B > 1) pts.I.B = 1;
                            if(pts.I.R < 0) pts.I.R = 0.0f;
                            if(pts.I.G < 0) pts.I.G = 0.0f;
                            if(pts.I.B < 0) pts.I.B = 0.0f;
                            int color = Color.rgb(pts.I.R, pts.I.G, pts.I.B);
                            paint.setColor(color);
                            canvas.drawPoint(aX, aY, paint);
                        }
                    }
                }
                break;
            case "Up":
                for (float i = pos1; i < pos2; i += 0.25f) {
                    for (float j = pos1; j < pos2; j += 0.25f) {
                        X = (float) (-1 * sinTheta * i + cosTheta * posY);
                        Y = (float) (-1 * cosPhi * cosTheta * i - cosPhi * sinTheta * posY + sinPhi * j);
                        Z = (float) (-1 * sinPhi * cosTheta * i - sinPhi * sinTheta * posY - cosPhi * j + p);

                        aX = d * X / Z + c1;
                        aY = d * Y / Z + c2;

                        if (zBuffer.setIf(aX, aY, Z)) {
                            pts.I.R = 0.1f; pts.I.G = 0.1f; pts.I.B = 0.1f;
                            pts.changeRealPos(X, Y, Z);
                            Normal = interpolateNormals(i, j, normals);

                            for (Points light: lights) {
                                if (light != null) {
                                    distance = getDistance(new Points(0, 0, 200), light);
                                    Points lightVector = normalize(light, new Points(0, 0, 200));
                                    cosTheta2 = dot(lightVector, Normal) / getLenght(lightVector) / getLenght(Normal);
                                    diffuse = (100 * cosTheta2 + 0.5) / (distance + 1);

                                    color0 = Color.valueOf(light.getColor());
                                    pts.I.R += color0.red() * diffuse;
                                    pts.I.G += color0.green() * diffuse;
                                    pts.I.B += color0.blue() * diffuse;
                                }
                            }
                            if(pts.I.R > 1) pts.I.R = 1;
                            if(pts.I.G > 1) pts.I.G = 1;
                            if(pts.I.B > 1) pts.I.B = 1;
                            if(pts.I.R < 0) pts.I.R = 0.0f;
                            if(pts.I.G < 0) pts.I.G = 0.0f;
                            if(pts.I.B < 0) pts.I.B = 0.0f;
                            int color = Color.rgb(pts.I.R, pts.I.G, pts.I.B);
                            paint.setColor(color);
                            canvas.drawPoint(aX, aY, paint);
                        }
                    }
                }
                break;
        }


    }

}
