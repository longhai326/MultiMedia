package com.lh.mediatest.widget;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.lh.mediatest.R;

public class CustomSurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private static final String TAG = "LH/CustomSurfaceView";
    private SurfaceHolder mHolder;
    private Canvas mCanvas;
    private boolean mIsDrawing;
    private Bitmap mBitmap;
    private WorkThread mWorkThread;

    public CustomSurfaceView(Context context) {
        this(context, null);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public CustomSurfaceView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mBitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.right_top_bg);
        mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        mIsDrawing = true;
        mWorkThread = new WorkThread();
        mWorkThread.start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mIsDrawing = false;
        try {
            mWorkThread.interrupt();
            mWorkThread.join(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

    }

    private class WorkThread extends Thread {

        @Override
        public void run() {
            while (mIsDrawing) {
                try {
                    mCanvas = mHolder.lockCanvas();
                    int width = getWidth();
                    int height = getHeight();
                    if (mCanvas == null || mBitmap == null) {
                        return;
                    }
                    mCanvas.drawBitmap(mBitmap, (width - mBitmap.getWidth()) / 2, (height - mBitmap.getHeight()) / 2, null);

                    Thread.sleep(10);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } finally {
                    if (mCanvas != null)
                        mHolder.unlockCanvasAndPost(mCanvas);
                }
            }
        }

    }
}
