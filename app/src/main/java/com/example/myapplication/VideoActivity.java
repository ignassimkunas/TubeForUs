package com.example.myapplication;

import android.Manifest;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import android.widget.VideoView;

import java.io.File;
import java.io.IOException;

public class VideoActivity extends AppCompatActivity {

    private DownloadManager downloadManager;
    private final int WRITE_REQUEST_CODE = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video);

        Intent intent = getIntent();
        VideoView videoView = findViewById(R.id.videoView);
        final int position = intent.getIntExtra("position", 0);
        final String urlVideo = MainActivity.videoList.get(position).videoPath;
        Button downloadButton = findViewById(R.id.download);
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = Uri.parse(urlVideo);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, MainActivity.videoList.get(position).title+".mp4");
                Long reference = downloadManager.enqueue(request);
            }
        });
        String mediaPath = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + MainActivity.videoList.get(position).title + ".mp4";
        Uri uri = Uri.parse(mediaPath);
        String localPath = MainActivity.videoList.get(position).localPath;
        Uri localVideo = Uri.parse(localPath);

        if (new File(localPath).exists()){
            videoView.setVideoURI(localVideo);
            videoView.start();
            videoView.requestFocus();
        }
        else {
            videoView.setVideoURI(uri);
            videoView.start();
            videoView.requestFocus();
        }
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                return true;
            }
        });
    }
}
