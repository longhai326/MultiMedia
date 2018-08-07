package com.beaver.waveview;

import android.media.AudioFormat;
import android.media.AudioRecord;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;

import com.beaver.waveview.soundfile.WavFile;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class AudioCapture {

    private static final String TAG = "LH/AudioCapture";
    private final String OUTPUT_DIR;
    private int DEFAULT_AUDIO_SOURCE = MediaRecorder.AudioSource.MIC;
    private int DEFAULT_SAMPLE_RATE = 44100;
    private int DEFAULT_CHANNEL_CONFIG = AudioFormat.CHANNEL_IN_MONO;
    private int DEFAULT_AUDIO_FORMAT = AudioFormat.ENCODING_PCM_16BIT;

    private AudioRecord mAudioRecord;
    private int mMinBufferSize = 0;
    private IAudioCaptureListener iAudioCaptureListener;

    private Thread mCaptureThread;
    private boolean mIsCaptureStarted = false;
    private volatile boolean mIsLoopExit = false;

    private int mWavDataSize = 0;
    private DataOutputStream mDataOutputStream;
    private String mFilePath;

    public static int TYPE_PCM = 1;
    public static int TYPE_WAV = 2;
    private int mCurrentRecordingType;

    public AudioCapture() {
        OUTPUT_DIR = Environment.getExternalStorageDirectory() + "/MediaTest";
        File outputDir = new File(OUTPUT_DIR);
        if (!outputDir.exists()) {
            outputDir.mkdir();
        }
    }

    public int getSampleRate() {
        return DEFAULT_SAMPLE_RATE;
    }

    public void setAudioCaptureListener(IAudioCaptureListener audioCaptureListener) {
        this.iAudioCaptureListener = audioCaptureListener;
    }

    public void startCapture(String fileName, int recordType) {
        if (mIsCaptureStarted) {
            Log.e(TAG, "It is already started record.");
            return;
        }
        mMinBufferSize = AudioRecord.getMinBufferSize(DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT);
        if (mMinBufferSize == AudioRecord.ERROR_BAD_VALUE) {
            Log.e(TAG, "Invalid Parameter !");
            return;
        }
        Log.d(TAG, "MinBufferSize : " + mMinBufferSize);
        mAudioRecord = new AudioRecord(
                DEFAULT_AUDIO_SOURCE, DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT, mMinBufferSize);
        if (mAudioRecord.getState() == AudioRecord.STATE_UNINITIALIZED) {
            Log.e(TAG, "AudioRecord initialize fail !");
            return;
        }
        mCurrentRecordingType = recordType;

        mFilePath = OUTPUT_DIR + "/" + fileName;
        File captureFile = new File(mFilePath);
        if (captureFile.exists()) {
            captureFile.delete();
        }
        try {
            mDataOutputStream = new DataOutputStream(new FileOutputStream(mFilePath));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return;
        }

        // WAV文件先写入头信息
        if (mCurrentRecordingType == TYPE_WAV) {
            mWavDataSize = 0;
            if (!writeWavHeader(DEFAULT_SAMPLE_RATE, DEFAULT_CHANNEL_CONFIG, DEFAULT_AUDIO_FORMAT)) {
                return;
            }
        }

        mAudioRecord.startRecording();
        mIsLoopExit = false;
        mCaptureThread = new Thread(new AudioCaptureRunnable());
        mCaptureThread.start();
        mIsCaptureStarted = true;
        Log.d(TAG, "Start audio capture success !");
    }

    public void stopCapture() {
        if (!mIsCaptureStarted) {
            return;
        }
        mIsLoopExit = true;
        try {
            mCaptureThread.interrupt();
            mCaptureThread.join(1000);
        } catch (InterruptedException e) {
        }
        if (mDataOutputStream != null) {
            if (mCurrentRecordingType == TYPE_WAV) {
                if (!writeDataSize()) {
                    Log.e(TAG, "WAV录音文件Data信息块写入失败!");
                    File captureFile = new File(mFilePath);
                    if (captureFile.exists()) {
                        captureFile.delete();
                    }
                }
            }
            try {
                mDataOutputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            mDataOutputStream = null;
        }
        if (mAudioRecord.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
            mAudioRecord.stop();
        }
        mAudioRecord.release();
        mIsCaptureStarted = false;
        Log.d(TAG, "Stop audio capture success !");

    }

    public boolean isRecording() {
        return mIsCaptureStarted;
    }

    private class AudioCaptureRunnable implements Runnable {

        @Override
        public void run() {
            while (!mIsLoopExit) {
                byte[] buffer = new byte[mMinBufferSize];
                int ret = mAudioRecord.read(buffer, 0, buffer.length);
                if (iAudioCaptureListener != null) {
                    iAudioCaptureListener.onCaptureFrame(buffer, ret);
                }
                if (ret == AudioRecord.ERROR_INVALID_OPERATION) {
                    Log.e(TAG, "Error ERROR_INVALID_OPERATION");
                } else if (ret == AudioRecord.ERROR_BAD_VALUE) {
                    Log.e(TAG, "Error ERROR_BAD_VALUE");
                } else {
                    try {
                        mDataOutputStream.write(buffer, 0, buffer.length);
                        if (mCurrentRecordingType == TYPE_WAV) {
                            mWavDataSize += buffer.length;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private boolean writeWavHeader(int sampleRateInHz, int channels, int bitsPerSample) {
        return WavFile.writeWavHeader(sampleRateInHz, channels, bitsPerSample, mDataOutputStream);
    }

    private boolean writeDataSize() {
        if (mDataOutputStream == null) {
            return false;
        }
        try {
            RandomAccessFile wavFile = new RandomAccessFile(mFilePath, "rw");
            wavFile.seek(WavFile.WAV_CHUNKSIZE_OFFSET);
            wavFile.write(intToByteArray((mWavDataSize + WavFile.WAV_CHUNKSIZE_EXCLUDE_DATA)), 0, 4);
            wavFile.seek(WavFile.WAV_SUB_CHUNKSIZE2_OFFSET);
            wavFile.write(intToByteArray((mWavDataSize)), 0, 4);
            wavFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private static byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    /**
     * 录音时增大音量
     */
    private void recordAudioIncreaseVolumn() {
        int minRecBufBytes = AudioRecord.getMinBufferSize(DEFAULT_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        AudioRecord audioRecord = new AudioRecord(DEFAULT_AUDIO_SOURCE, DEFAULT_SAMPLE_RATE,
                AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT, minRecBufBytes);
        // Setup the recording buffer, size, and pointer (in this case quadruple buffering)
        int recBufferByteSize = minRecBufBytes * 2;
        byte[] recBuffer = new byte[recBufferByteSize];
        int frameByteSize = minRecBufBytes / 2;
        int sampleBytes = frameByteSize;
        int recBufferBytePtr = 0;
        audioRecord.startRecording();
        // Do the following in the loop you prefer, e.g.
        while (true) {
            int reallySampledBytes = audioRecord.read(recBuffer, recBufferBytePtr, sampleBytes);
            int i = 0;
            while (i < reallySampledBytes) {
                float sample = (float) (recBuffer[recBufferBytePtr + i] & 0xFF
                        | recBuffer[recBufferBytePtr + i + 1] << 8);
                // THIS is the point were the work is done:
                // Increase level by about 6dB:
                sample *= 2;
                // Or increase level by 20dB:
                // sample *= 10;
                // Or if you prefer any dB value, then calculate the gain factor outside the loop
                // float gainFactor = (float)Math.pow( 10., dB / 20. );    // dB to gain factor
                // sample *= gainFactor;

                // Avoid 16-bit-integer overflow when writing back the manipulated data:
                if (sample >= 32767f) {
                    recBuffer[recBufferBytePtr + i] = (byte) 0xFF;
                    recBuffer[recBufferBytePtr + i + 1] = 0x7F;
                } else if (sample <= -32768f) {
                    recBuffer[recBufferBytePtr + i] = 0x00;
                    recBuffer[recBufferBytePtr + i + 1] = (byte) 0x80;
                } else {
                    int s = (int) (0.5f + sample);  // Here, dithering would be more appropriate
                    recBuffer[recBufferBytePtr + i] = (byte) (s & 0xFF);
                    recBuffer[recBufferBytePtr + i + 1] = (byte) (s >> 8 & 0xFF);
                }
                i += 2;
            }

            // Do other stuff like saving the part of buffer to a file
            // if ( reallySampledBytes > 0 ) { ... save recBuffer+recBufferBytePtr, length: reallySampledBytes

            // Then move the recording pointer to the next position in the recording buffer
            recBufferBytePtr += reallySampledBytes;

            // Wrap around at the end of the recording buffer, e.g. like so:
            if (recBufferBytePtr >= recBufferByteSize) {
                recBufferBytePtr = 0;
                sampleBytes = frameByteSize;
            } else {
                sampleBytes = recBufferByteSize - recBufferBytePtr;
                if (sampleBytes > frameByteSize)
                    sampleBytes = frameByteSize;
            }
        }
    }

}
