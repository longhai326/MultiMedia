package com.beaver.waveview;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioTrack;
import android.os.Environment;
import android.util.Log;

import com.beaver.waveview.soundfile.WavFile;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class AudioTrackPlayer {

    private static final String TAG = "LH/AudioTrackPlayer";
    private final String OUTPUT_DIR;
    private static final int DEFAULT_STREAM_TYPE = AudioManager.STREAM_MUSIC;
    private int DEFAULT_SAMPLE_RATE = 44100;
    private int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_OUT_MONO;
    private int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioTrack mAudioTrack;
    private int mMinBufferSize = 0;
    private IAudioCaptureListener iAudioCaptureListener;

    private Thread mPlayerThread;
    private boolean mIsPlayStarted = false;
    private volatile boolean mIsLoopExit = false;

    private DataInputStream mDataInputStream;

    public static int TYPE_PCM = 1;
    public static int TYPE_WAV = 2;
    private int mCurrentAudioType;

    public AudioTrackPlayer() {
        OUTPUT_DIR = Environment.getExternalStorageDirectory() + "/MediaTest";
    }

    public void setAudioCaptureListener(IAudioCaptureListener audioCaptureListener) {
        this.iAudioCaptureListener = audioCaptureListener;
    }

    public void startPlayer(String fileName, int audioType) {
        if (mIsPlayStarted) {
            Log.e(TAG, "Player already started !");
            return;
        }
        mCurrentAudioType = audioType;
        String filePath = OUTPUT_DIR + "/" + fileName;
        File playFile = new File(filePath);
        if (!playFile.exists()) {
            Log.e(TAG, "Source file not found !");
            return;
        }
        try {
            mDataInputStream = new DataInputStream(new FileInputStream(filePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (mCurrentAudioType == TYPE_WAV) {
            WavFile wavFile = WavFile.readWavHeader(mDataInputStream);
            if (wavFile == null) {
                Log.e(TAG, "Wav file header read fail !");
                return;
            }
            DEFAULT_SAMPLE_RATE = wavFile.mSampleRate;
            DEFAULT_AUDIO_FORMAT = wavFile.mBitsPerSample;
        }
        mMinBufferSize = AudioTrack.getMinBufferSize(DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
        if (mMinBufferSize == AudioTrack.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid parameter !");
            return;
        }
        Log.d(TAG, "getMinBufferSize = " + mMinBufferSize + " bytes !");
        mAudioTrack = new AudioTrack(
                DEFAULT_STREAM_TYPE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT, mMinBufferSize, AudioTrack.MODE_STREAM);
        if (mAudioTrack.getState() == AudioTrack.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioTrack initialize fail !");
            return;
        }
        mIsLoopExit = false;
        mPlayerThread = new Thread(new AudioPlayerRunnable());
        mPlayerThread.start();
        mIsPlayStarted = true;
        Log.d(TAG, "Start audio player success !");
    }

    public void stopPlayer() {
        if (!mIsPlayStarted) {
            return;
        }
        mIsLoopExit = true;
        try {
            mPlayerThread.interrupt();
            mPlayerThread.join(1000);
        } catch (InterruptedException e) {
        }
        if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
            mAudioTrack.stop();
        }
        mAudioTrack.release();
        mIsPlayStarted = false;
        if (mDataInputStream != null) {
            try {
                mDataInputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mDataInputStream = null;
        }
        Log.d(TAG, "Stop audio player success !");
    }

    public boolean isPlaying() {
        return mIsPlayStarted;
    }

    private class AudioPlayerRunnable implements Runnable {

        @Override
        public void run() {
            byte[] audioData = new byte[mMinBufferSize];
            mAudioTrack.play();
            try {
                while (!mIsLoopExit && mDataInputStream.read(audioData, 0, audioData.length) > 0) {
                    int ret = mAudioTrack.write(audioData, 0, audioData.length);
                    if (ret != audioData.length) {
                        Log.e(TAG, "Could not write all the samples to the audio device !");
                    }
                    if (iAudioCaptureListener != null) {
                        iAudioCaptureListener.onCaptureFrame(audioData, ret);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                stopPlayer();
            }
        }
    }

}
