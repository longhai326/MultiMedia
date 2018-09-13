package com.lh.mediatest;

import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.lh.mediatest.util.PermissionUtils;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity implements PermissionUtils.PermissionGrant {

    private static final String TAG = "LH/MainActivity";
    private SoundPool mSoundPool;
    private Map<String, Integer> mPoolMap;
    private boolean poolLoadCompleted;
    private int mPlayingId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        PermissionUtils.requestMultiPermissions(this, this);

        mPoolMap = new HashMap<String, Integer>();
        // 实例化SoundPool，大小为3
        mSoundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        // 装载音频进音频池，并且把ID记录在Map中
        mPoolMap.put("audio1", mSoundPool.load(this, R.raw.audio1, 1));
        mPoolMap.put("audio2", mSoundPool.load(this, R.raw.audio2, 1));

        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {

            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                if (sampleId == mPoolMap.size()) {
                    Toast.makeText(MainActivity.this, "加载声音池完成!",
                            Toast.LENGTH_SHORT).show();
                    poolLoadCompleted = true;
                }
            }
        });

    }

    @Override
    protected void onDestroy() {
        if (mSoundPool != null) {
            mSoundPool.release();
            mSoundPool = null;
        }
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPermissionGranted(int requestCode) {
        if (requestCode == PermissionUtils.CODE_MULTI_PERMISSION) {
            Log.d(TAG, "onPermissionGranted");
        }
    }

    public void onRecording(View view) {
        startActivity(new Intent(this, RecordingActivity.class));
    }

    public void onPlaying1(View view) {
        if (!poolLoadCompleted)
            return;
        mSoundPool.stop(mPlayingId);
        mPlayingId = mSoundPool.play(mPoolMap.get("audio1"), 1.0f, 1.0f, 1, 0,
                1.0f);
    }

    public void onPlaying2(View view) {
        if (!poolLoadCompleted)
            return;
        mSoundPool.stop(mPlayingId);
        mPlayingId = mSoundPool.play(mPoolMap.get("audio2"), 1.0f, 1.0f, 0, 0,
                1.0f);
    }

    public void onJNICall(View view) {
        startActivity(new Intent(this, FFmpegActivity.class));
    }

}
