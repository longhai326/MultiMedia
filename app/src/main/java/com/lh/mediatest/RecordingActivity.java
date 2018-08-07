package com.lh.mediatest;

import android.graphics.PixelFormat;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.RadioGroup;

import com.beaver.waveview.AudioCapture;
import com.beaver.waveview.AudioTrackPlayer;
import com.beaver.waveview.WaveformView;

public class RecordingActivity extends AppCompatActivity {

    private static final String TAG = "LH/RecordingActivity";
    private AudioCapture mAudioCapture;
    private AudioTrackPlayer mAudioPlayer;
    private String fileName = "audioRecorder.pcm";
    private int recordType = AudioCapture.TYPE_PCM;
    private Button recordingBtn, playingBtn;
    private WaveformView recordingWav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recording);

        recordingBtn = findViewById(R.id.recordingBtn);
        playingBtn = findViewById(R.id.playingBtn);
        RadioGroup radio_group = findViewById(R.id.radio_group);
        recordingWav = findViewById(R.id.recordingWav);
        recordingWav.setOffset(42);
        //解决surfaceView黑色闪动效果
        recordingWav.setZOrderOnTop(true);
        recordingWav.getHolder().setFormat(PixelFormat.TRANSLUCENT);

        radio_group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                switch (checkedId) {
                    case R.id.radio_pcm:
                        recordType = AudioCapture.TYPE_PCM;
                        fileName = "audioRecorder.pcm";
                        break;
                    case R.id.radio_wav:
                        recordType = AudioCapture.TYPE_WAV;
                        fileName = "audioRecorder.wav";
                        break;
                }
            }
        });
        mAudioCapture = new AudioCapture();
        mAudioPlayer = new AudioTrackPlayer();
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mAudioCapture.isRecording()) {
            mAudioCapture.stopCapture();
        }
        if (mAudioPlayer.isPlaying()) {
            mAudioPlayer.stopPlayer();
        }
    }

    public void onRecording(View view) {
        if (mAudioCapture.isRecording()) {
            mAudioCapture.stopCapture();
            recordingWav.stopDrawing();
            mAudioCapture.setAudioCaptureListener(null);
            recordingBtn.setText("Start Record");
        } else {
            recordingWav.resumeCanvas();
            recordingWav.setSampleRate(mAudioCapture.getSampleRate(), 1, 16);
            mAudioCapture.setAudioCaptureListener(recordingWav);
            mAudioCapture.startCapture(fileName, recordType);
            recordingBtn.setText("Stop Record");
        }
    }

    public void onPlaying(View view) {
        if (mAudioPlayer.isPlaying()) {
            mAudioPlayer.stopPlayer();
            recordingWav.stopDrawing();
            mAudioPlayer.setAudioCaptureListener(null);
            playingBtn.setText("Start Play");
        } else {
            recordingWav.resumeCanvas();
            recordingWav.setSampleRate(mAudioCapture.getSampleRate(), 1, 16);
            mAudioPlayer.setAudioCaptureListener(recordingWav);
            mAudioPlayer.startPlayer(fileName, recordType);
            playingBtn.setText("Stop Play");
        }
    }

}
