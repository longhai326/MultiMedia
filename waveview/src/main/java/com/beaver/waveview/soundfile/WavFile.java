package com.beaver.waveview.soundfile;

import android.util.Log;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import static android.content.ContentValues.TAG;

public class WavFile {
    public static final int WAV_FILE_HEADER_SIZE = 44;
    public static final int WAV_CHUNKSIZE_EXCLUDE_DATA = 36;

    public static final int WAV_CHUNKSIZE_OFFSET = 4;
    public static final int WAV_SUB_CHUNKSIZE1_OFFSET = 16;
    public static final int WAV_SUB_CHUNKSIZE2_OFFSET = 40;

    // 顶层信息块
    public String mChunkID = "RIFF";
    public int mChunkSize = 0;
    public String mFormat = "WAVE";

    // fmt信息块
    public String mSubChunk1ID = "fmt ";
    public int mSubChunk1Size = 16;
    public short mAudioFormat = 1;
    public short mNumChannel = 1;
    public int mSampleRate = 8000;
    public int mByteRate = 0;
    public short mBlockAlign = 0;
    public short mBitsPerSample = 8;

    // data信息块
    public String mSubChunk2ID = "data";
    public int mSubChunk2Size = 0;

    public WavFile() {
    }

    public WavFile(int sampleRateInHz, int channels, int bitsPerSample) {
        mSampleRate = sampleRateInHz;
        mNumChannel = (short) channels;
        mBitsPerSample = (short) bitsPerSample;
        mByteRate = mSampleRate * mNumChannel * mBitsPerSample / 8;
        mBlockAlign = (short) (mNumChannel * mBitsPerSample / 8);
    }

    public static boolean writeWavHeader(int sampleRateInHz, int channels, int bitsPerSample, DataOutputStream dataOutputStream) {
        if (dataOutputStream == null) {
            return false;
        }
        WavFile header = new WavFile(sampleRateInHz, channels, bitsPerSample);
        try {
            dataOutputStream.writeBytes(header.mChunkID);
            dataOutputStream.write(intToByteArray(header.mChunkSize), 0, 4);
            dataOutputStream.writeBytes(header.mFormat);
            dataOutputStream.writeBytes(header.mSubChunk1ID);
            dataOutputStream.write(intToByteArray(header.mSubChunk1Size), 0, 4);
            dataOutputStream.write(shortToByteArray(header.mAudioFormat), 0, 2);
            dataOutputStream.write(shortToByteArray(header.mNumChannel), 0, 2);
            dataOutputStream.write(intToByteArray(header.mSampleRate), 0, 4);
            dataOutputStream.write(intToByteArray(header.mByteRate), 0, 4);
            dataOutputStream.write(shortToByteArray(header.mBlockAlign), 0, 2);
            dataOutputStream.write(shortToByteArray(header.mBitsPerSample), 0, 2);
            dataOutputStream.writeBytes(header.mSubChunk2ID);
            dataOutputStream.write(intToByteArray(header.mSubChunk2Size), 0, 4);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static WavFile readWavHeader(DataInputStream dataInputStream) {
        if (dataInputStream == null) {
            return null;
        }
        WavFile header = new WavFile();
        byte[] intValue = new byte[4];
        byte[] shortValue = new byte[2];
        try {
            header.mChunkID = "" + (char) dataInputStream.readByte() + (char) dataInputStream.readByte() + (char) dataInputStream.readByte() + (char) dataInputStream.readByte();
            Log.d(TAG, "Read file chunkID:" + header.mChunkID);

            dataInputStream.read(intValue);
            header.mChunkSize = byteArrayToInt(intValue);
            Log.d(TAG, "Read file chunkSize:" + header.mChunkSize);

            header.mFormat = "" + (char) dataInputStream.readByte() + (char) dataInputStream.readByte() + (char) dataInputStream.readByte() + (char) dataInputStream.readByte();
            Log.d(TAG, "Read file format:" + header.mFormat);

            header.mSubChunk1ID = "" + (char) dataInputStream.readByte() + (char) dataInputStream.readByte() + (char) dataInputStream.readByte() + (char) dataInputStream.readByte();
            Log.d(TAG, "Read fmt chunkID:" + header.mSubChunk1ID);

            dataInputStream.read(intValue);
            header.mSubChunk1Size = byteArrayToInt(intValue);
            Log.d(TAG, "Read fmt chunkSize:" + header.mSubChunk1Size);

            dataInputStream.read(shortValue);
            header.mAudioFormat = byteArrayToShort(shortValue);
            Log.d(TAG, "Read audioFormat:" + header.mAudioFormat);

            dataInputStream.read(shortValue);
            header.mNumChannel = byteArrayToShort(shortValue);
            Log.d(TAG, "Read channel number:" + header.mNumChannel);

            dataInputStream.read(intValue);
            header.mSampleRate = byteArrayToInt(intValue);
            Log.d(TAG, "Read samplerate:" + header.mSampleRate);

            dataInputStream.read(intValue);
            header.mByteRate = byteArrayToInt(intValue);
            Log.d(TAG, "Read byterate:" + header.mByteRate);

            dataInputStream.read(shortValue);
            header.mBlockAlign = byteArrayToShort(shortValue);
            Log.d(TAG, "Read blockalign:" + header.mBlockAlign);

            dataInputStream.read(shortValue);
            header.mBitsPerSample = byteArrayToShort(shortValue);
            Log.d(TAG, "Read bitspersample:" + header.mBitsPerSample);

            header.mSubChunk2ID = "" + (char) dataInputStream.readByte() + (char) dataInputStream.readByte() + (char) dataInputStream.readByte() + (char) dataInputStream.readByte();
            Log.d(TAG, "Read data chunkID:" + header.mSubChunk2ID);

            dataInputStream.read(intValue);
            header.mSubChunk2Size = byteArrayToInt(intValue);
            Log.d(TAG, "Read data chunkSize:" + header.mSubChunk2Size);

            Log.d(TAG, "Read wav file success !");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
        return header;
    }

    private static byte[] intToByteArray(int data) {
        return ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN).putInt(data).array();
    }

    private static byte[] shortToByteArray(short data) {
        return ByteBuffer.allocate(2).order(ByteOrder.LITTLE_ENDIAN).putShort(data).array();
    }

    private static short byteArrayToShort(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getShort();
    }

    private static int byteArrayToInt(byte[] b) {
        return ByteBuffer.wrap(b).order(ByteOrder.LITTLE_ENDIAN).getInt();
    }

}
