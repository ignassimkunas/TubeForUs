package com.example.myapplication;

import android.net.Uri;

public class Videos {

    String user;
    String title;
    String date;
    String videoPath;
    String localPath;

    public Videos() {

    }
    public Videos(String user, String title, String date, String videoPath, String localPath) {
        this.user = user;
        this.title = title;
        this.date = date;
        this.videoPath = videoPath;
        this.localPath = localPath;
    }
}
