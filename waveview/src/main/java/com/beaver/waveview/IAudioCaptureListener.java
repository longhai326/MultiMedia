package com.beaver.waveview;

public interface IAudioCaptureListener {

    void onCaptureFrame(byte[] buffer, int readSize);

}
