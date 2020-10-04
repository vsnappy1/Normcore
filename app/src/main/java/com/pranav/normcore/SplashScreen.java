package com.pranav.normcore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;
import android.widget.VideoView;

import com.pranav.normcore.Activities.UserActivity;

public class SplashScreen extends AppCompatActivity {

    VideoView mVideoView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);

        mVideoView = findViewById(R.id.videoView);


    }

    private Uri getMedia() {
        return Uri.parse("android.resource://" + getPackageName() + "/" + R.raw.back_video);
    }

    private void initializePlayer() {
        Uri videoUri = getMedia();
        mVideoView.setVideoURI(videoUri);
        mVideoView.start();

        mVideoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                finish();
                startActivity(new Intent(SplashScreen.this, MainActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        initializePlayer();
    }
}