package com.lh.mediatest;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

public class FFmpegActivity extends AppCompatActivity {

    private static final String TAG = "LH/FFmpegActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ffmpeg);

        TextView tv = findViewById(R.id.sample_text);
        tv.setText(stringFromJNI());

        TextView ffmpeg_info = findViewById(R.id.ffmpeg_info);
        StringBuffer sb = new StringBuffer("FFMPEG_INFO:");
        sb.append("\nConfiguration: ").append(getConfigurationInfo());
//        sb.append("\nUrlProtocol: ").append(getUrlProtocolInfo());
//        sb.append("\nAvformat: ").append(getAvformatInfo());
//        sb.append("\nAvcodec: ").append(getAvcodecInfo());
//        sb.append("\nAvfilter: ").append(getAvfilterInfo());
        ffmpeg_info.setText(sb.toString());
    }

    /**
     * native methods
     */
    static {
        System.loadLibrary("native-lib");
    }

    public native String stringFromJNI();

    public native String getConfigurationInfo();
//
//    public native String getUrlProtocolInfo();
//
//    public native String getAvformatInfo();
//
//    public native String getAvcodecInfo();
//
//    public native String getAvfilterInfo();
}
