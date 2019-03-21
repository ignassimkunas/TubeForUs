package com.example.myapplication;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;
import android.widget.Toolbar;
import android.widget.VideoView;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {

    static ArrayList<Videos> videoList;
    private VideoList adapter;
    private FirebaseAuth firebaseAuth;
    private ListView listView;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private String title = "";
    static StorageReference videoRef;
    Uri downloadUri = null;
    private final int READ_REQUEST_CODE = 1;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == READ_REQUEST_CODE && resultCode == RESULT_OK){
            try{
                AlertDialog.Builder alert = new AlertDialog.Builder(this);

                alert.setTitle("Add title: ");
                alert.setMessage("Add a title for the video you're uploading");
                final Uri receivedData = data.getData();
                final EditText input = new EditText(this);
                alert.setView(input);
                alert.setPositiveButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        title = input.getText().toString();
                        uploadVideo(receivedData, getPath(getApplicationContext(), receivedData));

                    }
                });
                alert.show();
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    public static String getPath(Context context, Uri uri ) {
        String result = null;
        String[] proj = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver( ).query( uri, proj, null, null, null );
        if(cursor != null){
            if ( cursor.moveToFirst( ) ) {
                int column_index = cursor.getColumnIndexOrThrow( proj[0] );
                result = cursor.getString( column_index );
            }
            cursor.close( );
        }
        if(result == null) {
            result = "Not found";
        }
        return result;
    }

    public void uploadVideo(Uri videoUri, final String localPath){

        Calendar calendar = Calendar.getInstance();
        final String date  = calendar.getTime().toString().split(" G")[0];
        final String email = firebaseAuth.getCurrentUser().getEmail();
        final String videoPath = "/video/" + date + email + title + ".mp4";
        videoRef = storageReference.child(videoPath);
        if (videoUri != null) {
            UploadTask uploadTask = videoRef.putFile(videoUri);
            uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()){
                            throw Objects.requireNonNull(task.getException());
                    }
                    return videoRef.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()){
                        Toast.makeText(MainActivity.this, "Upload complete", Toast.LENGTH_SHORT).show();
                        downloadUri = task.getResult();
                        if (downloadUri != null){
                            Videos newVideo = new Videos(email, title, date, downloadUri.toString(), localPath);
                            databaseReference.child(Integer.toString(videoList.size())).setValue(newVideo);
                            adapter.notifyDataSetChanged();
                        }
                        else {
                            Toast.makeText(MainActivity.this, "No download URL", Toast.LENGTH_SHORT).show();
                        }

                    }
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Toast.makeText(MainActivity.this, "Upload failed", Toast.LENGTH_SHORT).show();
                }
            });
        }
        else {
            Toast.makeText(this, "No video found", Toast.LENGTH_SHORT).show();
        }
    }

    public void selectVideo () {
        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_REQUEST_CODE);
        }
        else {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Video.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, READ_REQUEST_CODE);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);

        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);

        if (item.getItemId() == R.id.upload){
            selectVideo();

        }
        if (item.getItemId() == R.id.logout){

            firebaseAuth.signOut();
            startActivity(intent);
        }
        return true;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.upload_menu, menu);
        return true;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        firebaseAuth = FirebaseAuth.getInstance();
        Toast.makeText(this, firebaseAuth.getCurrentUser().getEmail(), Toast.LENGTH_SHORT).show();

        if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_REQUEST_CODE);
        }
        if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        storageReference = storage.getReference();
        listView = findViewById(R.id.listView);
        videoList = new ArrayList<>();

        adapter = new VideoList(MainActivity.this, videoList);

        databaseReference = FirebaseDatabase.getInstance("https://tracking-b095b-c67b0.firebaseio.com").getReference();

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                videoList.clear();
                for (DataSnapshot videosSnapshot: dataSnapshot.getChildren()){
                    Videos videos = videosSnapshot.getValue(Videos.class);
                    videoList.add(videos);
                }
                listView.setAdapter(adapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

        final Intent intent = new Intent(this, VideoActivity.class);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                intent.putExtra("position", position);
                startActivity(intent);
            }
        });



    }
}
