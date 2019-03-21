package com.example.myapplication;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.example.myapplication.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.OnProgressListener;

import org.w3c.dom.Text;

import java.io.File;
import java.util.List;

public class VideoList extends ArrayAdapter<Videos> {

    private Activity context;
    private List<Videos> videoList;

    /*
    * TODO:
    * Pakeist xml, kad tiesiog parsisiųst galėtum.
    *
    * */

    public VideoList(Activity context, List<Videos> videoList) {

        super(context, R.layout.list_layout, videoList);
        this.context = context;
        this.videoList = videoList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = context.getLayoutInflater();

        View listViewItem = inflater.inflate(R.layout.list_layout, null, true);

        TextView user = listViewItem.findViewById(R.id.user);
        TextView title = listViewItem.findViewById(R.id.title);
        TextView date = listViewItem.findViewById(R.id.date);

        user.setText(videoList.get(position).user);
        title.setText(videoList.get(position).title);
        date.setText(videoList.get(position).date);

        return listViewItem;
    }
}
