package com.example.myapplication;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;


public class TestSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    static class DrawThread extends Thread {
        private boolean runFlag = false;
        private final SurfaceHolder surfaceHolder;
        private long prevTime;
        int x = -1;
        int y = -1;
        int rad = 0;

        public DrawThread(SurfaceHolder surfaceHolder) {
            this.surfaceHolder = surfaceHolder;
            prevTime = System.currentTimeMillis();
        }

        public void setPos(int x, int y) {
            this.x = x;
            this.y = y;
            this.rad = 0;
            prevTime = System.currentTimeMillis()-1000;
        }

        public void setRunning(boolean run) {
            runFlag = run;
        }

        @Override
        public void run() {
            Canvas canvas;
            while (runFlag) {
                long now = System.currentTimeMillis();
                long elapsedTime = now - prevTime;
                if (elapsedTime >= 1000) {
                    prevTime = now;
                    if (x != -1 && y != -1) {
                        rad += 5;
                    }
                    canvas = null;
                    try {
                        canvas = surfaceHolder.lockCanvas();
                        canvas.drawColor(Color.BLUE);
                        Paint cPaint = new Paint();
                        cPaint.setColor(Color.YELLOW);
                        canvas.drawCircle(x, y, rad, cPaint);

                    } finally {
                        if (canvas != null) {
                            surfaceHolder.unlockCanvasAndPost(canvas);
                        }
                    }
                }
                try {
                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        }

    }

    private DrawThread drawThread;

    public TestSurfaceView(Context context) {
        super(context);
        getHolder().addCallback(this);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        int x = (int) event.getX();
        int y = (int) event.getY();
        drawThread.setPos(x, y);

        return true;
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        Canvas canvas = null;
        try {
            canvas = surfaceHolder.lockCanvas();
            canvas.drawColor(Color.BLUE);
        } finally {
            if (canvas != null) {
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
        drawThread = new DrawThread(getHolder());
        drawThread.setRunning(true);
        drawThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
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
}
