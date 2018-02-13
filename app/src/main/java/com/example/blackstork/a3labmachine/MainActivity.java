package com.example.blackstork.a3labmachine;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import java.lang.Math;
import android.graphics.Canvas;

import android.app.Activity;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class MainActivity extends Activity {
    private Paint paint;
    public int touchX, touchY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new DrawView(this));
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        touchX = (int)event.getX();
        touchY = (int)event.getY();
        return false;
    }

    class DrawView extends SurfaceView implements SurfaceHolder.Callback {

        private DrawThread drawThread;

        public DrawView(Context context) {
            super(context);

            paint = new Paint();
            paint.setColor(Color.BLACK);
            paint.setStrokeWidth(2);
            paint.setTextSize(10);
            getHolder().addCallback(this);

        }

        @Override
        public void surfaceChanged(SurfaceHolder holder, int format, int width,
                                   int height) {

        }

        @Override
        public void surfaceCreated(SurfaceHolder holder) {
            drawThread = new DrawThread(getHolder());
            drawThread.setRunning(true);
            drawThread.start();
        }

        @Override
        public void surfaceDestroyed(SurfaceHolder holder) {
            boolean retry = true;
            drawThread.setRunning(false);
            while (retry) {
                try {
                    drawThread.join();
                    retry = false;
                } catch (InterruptedException e) {
                }
            }
        }

        class DrawThread extends Thread {

            private boolean running = false;
            private SurfaceHolder surfaceHolder;
            private Cube cube;

            public DrawThread(SurfaceHolder surfaceHolder) {
                this.surfaceHolder = surfaceHolder;
            }

            public void setRunning(boolean running) {
                this.running = running;
            }

            @Override
            public void run() {
                Canvas canvas;
                cube = new Cube();
                while (running) {
                    canvas = null;
                    try {
                        canvas = surfaceHolder.lockCanvas(null);
                        if (canvas == null)
                            continue;

                        cube.update(canvas);
                        canvas = cube.draw();

                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
            }
        }

    }

    class Cube {
        Points points[];
        Points lightPoints[];
        Polygon polygons[];
        ZBuffer zbuf;
        int angleXY = 0, angleXZ = 0;
        float p = 200, d = 400;
        double[] X, Y, Z;
        int c1, c2;
        int method; //0 - обычная. 1 - Гурро. 2 - Фонг
        int rotX[], rotY[], rotZ[];
        Bitmap buttons[];
        Canvas canvas;

        Cube() {
            method = 0;
            zbuf = new ZBuffer(1920, 1080);
            rotX = new int[]{0, 0, 0}; rotY = new int[]{0, 0, 0}; rotZ = new int[]{0, 0, 0};
            int pos1 = -50, pos2 = 50;
            points = new Points[8];
            for(int i = 0; i < 8; i++) {
                points[i] = new Points();
            }
            points[0].init(pos1, pos1, pos2);
            points[1].init(pos1, pos2, pos2);
            points[2].init(pos2, pos2, pos2);
            points[3].init(pos2, pos1, pos2);
            points[4].init(pos1, pos1, pos1);
            points[5].init(pos1, pos2, pos1);
            points[6].init(pos2, pos2, pos1);
            points[7].init(pos2, pos1, pos1);

            lightPoints = new Points[3];
            for(int i = 0; i < 3; i++) {
                lightPoints[i] = new Points();
            }
            lightPoints[0].init( 0, 0, -120 );
            lightPoints[1].init( 0, 0, -20 );
            lightPoints[2].init( 0, 0, -20 );
            lightPoints[0].changeColor(Color.RED);
            lightPoints[1].changeColor(Color.GREEN);
            lightPoints[2].changeColor(Color.BLUE);

            polygons = new Polygon[6];
            Points buf[][];
            buf = new Points[][] {
                    {points[0], points[1], points[2], points[3]},
                    {points[4], points[5], points[6], points[7]},
                    {points[7], points[3], points[2], points[6]},
                    {points[4], points[0], points[1], points[5]},
                    {points[5], points[1], points[2], points[6]},
                    {points[4], points[0], points[3], points[7]},
            };
            polygons[0] = new Polygon(buf[0], buf[1], zbuf);//Bottom
            polygons[1] = new Polygon(buf[1], buf[0], zbuf);
            polygons[2] = new Polygon(buf[2], buf[3], zbuf);//Left
            polygons[3] = new Polygon(buf[3], buf[2], zbuf);
            polygons[4] = new Polygon(buf[4], buf[5], zbuf);//Up
            polygons[5] = new Polygon(buf[5], buf[4], zbuf);


            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            c2 = displayMetrics.heightPixels / 2;
            c1 = displayMetrics.widthPixels / 2;

            buttons = new Bitmap[3];
            buttons[0] = BitmapFactory.decodeResource(getResources(), R.drawable.simple);
            buttons[1] = BitmapFactory.decodeResource(getResources(), R.drawable.guro);
            buttons[2] = BitmapFactory.decodeResource(getResources(), R.drawable.phong);

        }

        void update(Canvas canvas) {
            this.canvas = canvas;

            canvas.drawARGB(255, 0, 0, 0);

            if(touchY > 1750 && (this.angleXY != touchX % 360 || angleXZ != touchY % 360)) {
                for (int i = 0; i < 3; i++)
                    if (touchX < (i + 1) * 370) {
                        method = i;
                        break;
                    }
            }
            else{
                this.angleXY = touchX % 360;
                this.angleXZ = touchY % 360;
            }

            for(int i = 0; i < 3; i++) {
                if(i != method) canvas.drawBitmap(buttons[i], i * 370, 1750, paint);
            }


        }

        boolean connected(Points light, Polygon polygon) {
            Points v1 = polygon.pt[0]; Points v2 = polygon.pt[1]; Points v3 = polygon.pt[2];
            double opr=v1.realX*v2.realY*v3.realZ + v1.realY*v2.realZ*v3.realX + v1.realZ*v2.realX*v3.realY - (v1.realZ*v2.realY*v3.realX + v1.realY*v2.realX*v3.realZ + v1.realX*v2.realZ*v3.realY);
            int d = 1;
            if (opr == 0) {
                opr = 1;
                d = 0;
            }

            double A= ((-1)*(v2.realY)*(v3.realZ)+(v1.realY)*(v2.realZ)*(-1)+(v1.realZ)*(-1)*(v3.realY)-
                    ((v1.realZ)*(v2.realY)*(-1)+(v1.realY)*(-1)*(v3.realZ)+(-1)*(v2.realZ)*(v3.realY)))/opr;
            double B=((v1.realX)*(-1)*(v3.realZ)+(-1)*(v2.realZ)*(v3.realX)+(v1.realZ)*(v2.realX)*(-1)-
                    ((v1.realZ)*(-1)*(v3.realX)+(-1)*(v2.realX)*(v3.realZ)+(v1.realX)*(v2.realZ)*(-1)))/opr;
            double C=((v1.realX)*(v2.realY)*(-1)+(v1.realY)*(-1)*(v3.realX)+(-1)*(v2.realX)*(v3.realY)-
                    ((-1)*(v2.realY)*(v3.realX)+(v1.realY)*(v2.realX)*(-1)+(v1.realX)*(-1)*(v3.realY)))/opr;

            //double A = (polygon.pt[0].realY - polygon.pt[1].realY) * (polygon.pt[2].realZ - polygon.pt[1].realZ) - (polygon.pt[2].realY - polygon.pt[1].realY) * (polygon.pt[0].realZ - polygon.pt[1].realZ) / opr;
            //double B = (polygon.pt[0].realX - polygon.pt[1].realX) * (polygon.pt[2].realZ - polygon.pt[1].realZ) - (polygon.pt[2].realX - polygon.pt[1].realX) * (polygon.pt[0].realZ - polygon.pt[1].realZ) / opr;
            //double C = (polygon.pt[0].realX - polygon.pt[1].realX) * (polygon.pt[2].realY - polygon.pt[1].realY) - (polygon.pt[2].realX - polygon.pt[1].realX) * (polygon.pt[0].realY - polygon.pt[1].realY) / opr;
            if ((light.realX * A + light.realY * B + light.realZ * C + d) > 0)
                return true;
            return false;
        }


        Canvas draw() {
            double sinTheta = Math.sin(Math.toRadians(angleXY));
            double sinPhi = Math.sin(Math.toRadians(angleXZ));
            double cosTheta = Math.cos(Math.toRadians(angleXY));
            double cosPhi = Math.cos(Math.toRadians(angleXZ));
            zbuf.update();

            float posX, posY;

            for(int i = 0; i < 8; i++) {
                points[i].realX = -1 * sinTheta * points[i].getX() + cosTheta * points[i].getY();
                points[i].realY = -1 * cosPhi * cosTheta * points[i].getX() - cosPhi * sinTheta * points[i].getY() + sinPhi * points[i].getZ();
                points[i].realZ = -1 * sinPhi * cosTheta * points[i].getX() - sinPhi * sinTheta * points[i].getY() - cosPhi * points[i].getZ() + p;
            }

            lightPoints[0].changeRealPos(lightPoints[0].x, lightPoints[0].y + 60 * Math.cos(Math.toRadians(rotX[0])), lightPoints[0].z + p);
            lightPoints[1].changeRealPos(lightPoints[1].x + 80 * Math.cos(Math.toRadians(-1 * rotX[1])), lightPoints[1].y, lightPoints[1].z + 100 * Math.sin(Math.toRadians(-1 * rotY[1])) + p);
            lightPoints[2].changeRealPos(lightPoints[2].x - 80 * Math.cos(Math.toRadians(rotX[2])), lightPoints[2].y + 60 * Math.cos(Math.toRadians(rotY[2])), lightPoints[2].z + 100 * Math.sin(Math.toRadians(-1 * rotZ[2])) + p);

            paint.setStrokeWidth(1.0f);

            for(int i = 0; i < 6; i++) {
                /*int k = 0;
                Points light[] = new Points[3];
                for(int j = 0; j < 3; j++) {
                    if (connected(lightPoints[j], polygons[i])) {
                        light[k] = lightPoints[j];
                        k += 1;
                    }
                }*/
                polygons[i].draw(method, angleXY, angleXZ, lightPoints, paint, canvas);
            }

            paint.setStrokeWidth(10.0f);
            for (int i = 0; i < 3; i++) {
                posX = (float) (d * lightPoints[i].realX / lightPoints[i].realZ + c1);
                posY = (float) (d * lightPoints[i].realY / lightPoints[i].realZ + c2);
                if (zbuf.setIf(posX, posY, lightPoints[i].realZ)) {
                    paint.setColor(lightPoints[i].getColor());
                    canvas.drawPoint(posX, posY, paint);
                }
            }

            rotX[0] += 3;
            rotX[1] += 3; rotY[1] += 3;
            rotX[2] += 3; rotY[2] += 3; rotZ[2] += 3;
            paint.setStrokeWidth(0.1f);

            return canvas;
        }
    }

}