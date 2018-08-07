package com.beaver.waveview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.lang.ref.WeakReference;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;

public class WaveformView extends SurfaceView implements SurfaceHolder.Callback, IAudioCaptureListener {

    private static final String TAG = "LH/WaveformView";
    private static final int MSG_DRAW_WAVE = 100;
    private SurfaceHolder mHolder;
    private int offset;
    private HandlerThread mWaveThread;
    private WaveHandler mHandler;

    private ArrayList<Short> drawBuffer = new ArrayList<>();
    private static int RATE_X = 100;//控制多少帧取一帧
    private static float RATE_Y = 1; //Y轴缩小的比例 默认为1
    private static int DRAW_TIME = 5;//两次绘图间隔的时间  2.5ms~60ms
    private float INTERVAL = 0.2f;//每0.2像素绘制一次数据
    private long c_time;
    private int mSampleRate;
    private int mChannelNum;
    private int mBitPerSample;

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public void setSampleRate(int sampleRate, int channel, int bitPerSample) {
        this.mSampleRate = sampleRate;
        this.mChannelNum = channel;
        this.mBitPerSample = bitPerSample;
    }

    public int getOffset() {
        return offset;
    }

    public WaveformView(Context context) {
        this(context, null);
    }

    public WaveformView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaveformView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.mHolder = getHolder();
        mHolder.addCallback(this);
    }

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {
        mHolder = surfaceHolder;
        mWaveThread = new HandlerThread("wave_thread");
        mWaveThread.start();
        mHandler = new WaveHandler(mWaveThread.getLooper(), this);
        Message message = mHandler.obtainMessage();
        message.arg1 = MSG_DRAW_WAVE;
        mHandler.sendMessage(message);

    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        mHandler.removeCallbacksAndMessages(null);
        mWaveThread.quit();
        mHolder.removeCallback(this);
    }

    public void resumeCanvas() {
        drawBuffer.clear();
        Message message = mHandler.obtainMessage();
        message.arg1 = MSG_DRAW_WAVE;
        mHandler.sendMessage(message);
    }

    public void stopDrawing() {
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    public void onCaptureFrame(byte[] buffer, int readSize) {
        if (buffer == null || buffer.length == 0 || !mWaveThread.isAlive()) {
            return;
        }
        DecimalFormat decimalFormat = new DecimalFormat(".00");
        String timeStr = decimalFormat.format(readSize / (mSampleRate * mBitPerSample * mChannelNum / 8.0f));
        float timeOfBuffer = Float.parseFloat(timeStr);// 当前缓冲区总时间

        short[] shortsBuffer = new short[buffer.length / 2];
        ByteBuffer.wrap(buffer).order(ByteOrder.LITTLE_ENDIAN).asShortBuffer().get(shortsBuffer);

        synchronized (drawBuffer) {
            for (int i = 0; i < shortsBuffer.length; i += RATE_X) {
                drawBuffer.add(shortsBuffer[i]);
            }
        }

        long time = System.currentTimeMillis();
        if (time - c_time >= DRAW_TIME) {
            ArrayList<Short> buf;
            synchronized (drawBuffer) {
                while (drawBuffer.size() > getWidth() / INTERVAL) {
                    drawBuffer.remove(0);// 超出画布绘制区域时，将前面绘制数据删除
                }
                buf = (ArrayList<Short>) drawBuffer.clone();
            }
            Message message = mHandler.obtainMessage();
            message.arg1 = MSG_DRAW_WAVE;
            message.obj = buf;
            mHandler.sendMessage(message);

            c_time = new Date().getTime();
        }
    }

    private static class WaveHandler extends Handler {

        private WeakReference<WaveformView> weakReference;
        private Paint paintLine;
        private Paint centerLine;
        private Paint circlePaint;
        private Paint mPaint;

        WaveHandler(Looper looper, WaveformView waveformView) {
            super(looper);
            weakReference = new WeakReference<>(waveformView);
            paintLine = new Paint();
            paintLine.setStrokeWidth(1.5f);
            paintLine.setColor(Color.rgb(221, 221, 221));

            centerLine = new Paint();
            centerLine.setStrokeWidth(1.5f);
            centerLine.setColor(Color.rgb(39, 199, 175));// 画笔为color
            centerLine.setAntiAlias(true);
            centerLine.setFilterBitmap(true);
            centerLine.setStyle(Paint.Style.FILL);

            circlePaint = new Paint();
            circlePaint.setStrokeWidth(3);
            circlePaint.setColor(Color.rgb(246, 131, 126));//设置上圆的颜色
            circlePaint.setAntiAlias(true);

            mPaint = new Paint();
            mPaint.setColor(Color.rgb(39, 199, 175));// 画笔为color
            mPaint.setStrokeWidth(1);// 设置画笔粗细
            mPaint.setAntiAlias(true);
            mPaint.setFilterBitmap(true);
            mPaint.setStyle(Paint.Style.FILL);
        }

        @Override
        public void handleMessage(Message msg) {
            WaveformView waveformView = weakReference.get();
            if (waveformView == null) {
                return;
            }
            if (msg.arg1 == MSG_DRAW_WAVE) {
                ArrayList<Short> buffer = (ArrayList<Short>) msg.obj;
                int surfaceWidth = waveformView.getWidth();
                int surfaceHeight = waveformView.getHeight();
                int offset = waveformView.offset;
                Canvas canvas = waveformView.mHolder.lockCanvas(new Rect(0, 0, surfaceWidth, surfaceHeight));
                if (canvas == null) {
                    return;
                }
                canvas.drawARGB(255, 239, 239, 239);
                canvas.drawLine(0, offset / 2, surfaceWidth, offset / 2, paintLine);//最上面的那根线
                canvas.drawLine(0, surfaceHeight - offset / 2 - 1, surfaceWidth, surfaceHeight - offset / 2 - 1, paintLine);//最下面的那根线
                int height = surfaceHeight - offset;
                canvas.drawLine(0, height * 0.5f + offset / 2, surfaceWidth, height * 0.5f + offset / 2, centerLine);//中心线

                // 绘制位置线
                float startX = 0;
                if (buffer != null) {
                    waveformView.INTERVAL = (float) (surfaceWidth / (44100 / RATE_X * 20.00));
                    RATE_Y = (65535 / 4 / (surfaceHeight - offset));
                    for (int i = 0; i < buffer.size(); i++) {
                        byte bus[] = getBytes(buffer.get(i));
                        buffer.set(i, (short) ((0x0000 | bus[1]) << 8 | bus[0]));//高低位交换
                    }

                    startX = buffer.size() * waveformView.INTERVAL;
                    if (surfaceWidth - startX <= 0) {
                        startX = surfaceWidth;
                    }
                    float y;
                    for (int i = 0; i < buffer.size(); i++) {
                        y = buffer.get(i) / RATE_Y + (surfaceHeight / 2);// 调节缩小比例，调节基准线
                        float x = (i) * waveformView.INTERVAL;
                        if (surfaceWidth - (i - 1) * waveformView.INTERVAL <= 0) {
                            x = surfaceWidth;
                        }
                        //画线的方式很多，你可以根据自己要求去画。这里只是为了简单
                        float y1 = surfaceHeight - y;
                        if (y < offset / 2) {
                            y = offset / 2;
                        }
                        if (y > surfaceHeight - offset / 2 - 1) {
                            y = surfaceHeight - offset / 2 - 1;
                        }
                        if (y1 < offset / 2) {
                            y1 = offset / 2;
                        }
                        if (y1 > (surfaceHeight - offset / 2 - 1)) {
                            y1 = (surfaceHeight - offset / 2 - 1);
                        }
                        canvas.drawLine(x, y, x, y1, mPaint);//中间出波形
                    }
                }
                canvas.drawCircle(startX, offset / 4, offset / 4, circlePaint);// 上面小圆
                canvas.drawCircle(startX, surfaceHeight - offset / 4, offset / 4, circlePaint);// 下面小圆
                canvas.drawLine(startX, 0, startX, surfaceHeight, circlePaint);//垂直的线

                waveformView.mHolder.unlockCanvasAndPost(canvas);// 解锁画布，提交画好的图像
            }

        }

        private byte[] getBytes(short s) {
            byte[] buf = new byte[2];
            for (int i = 0; i < buf.length; i++) {
                buf[i] = (byte) (s & 0x00ff);
                s >>= 8;
            }
            return buf;
        }
    }

}
