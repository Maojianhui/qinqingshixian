package com.app.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.app.view.FullScreenVideoView;
import com.app.R;

import butterknife.Bind;
import butterknife.ButterKnife;

/**
 * Created by acer on 2016/11/15.
 */

public class VideoLook extends Activity {
    @Bind(R.id.play)
    FullScreenVideoView play;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.videolook);
        ButterKnife.bind(this);
        Intent i=getIntent();
        String path=i.getStringExtra("Path");
        play.setVideoPath(path);
        play.start();
    }
}
