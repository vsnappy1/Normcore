package com.pranav.normcore.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.pranav.normcore.Adapters.PostAdapter;
import com.pranav.normcore.CustomClasses.Influncer;
import com.pranav.normcore.CustomClasses.Post;
import com.pranav.normcore.R;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

public class InfluncerActivity extends AppCompatActivity {
    private static final String TAG = "YOYO";
    private static int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_CODE = 97 ;
    private Uri filePath = null;

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    PostAdapter adapter;
    ArrayList<Post> list;
    ProgressBar progressBar;
    DatabaseReference mDatabase;
    ValueEventListener valueEventListener;

    Influncer influncer;

    Button buttonSelectMedia, buttonUpload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_influncer);

        getInfluencerDetails();

        buttonUpload = findViewById(R.id.buttonUpload);
        buttonSelectMedia = findViewById(R.id.buttonSelectFile);
        progressBar = findViewById(R.id.progressBarInfluencer);

        recyclerView = findViewById(R.id.recyclerViewInfluencer);

        list = new ArrayList<>();

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PostAdapter(this, list);

        recyclerView.setAdapter(adapter);

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(filePath == null){
                    Toast.makeText(InfluncerActivity.this, "Please select a file first", Toast.LENGTH_SHORT).show();
                }
                else {
                    if(influncer != null){
                        sendImageToTheServer();
                    }
                    else {
                        Toast.makeText(InfluncerActivity.this, "wait a minute", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        buttonSelectMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(isStoragePermissionGranted()) {
                    selectMedia();
                }
            }
        });

        mDatabase = FirebaseDatabase.getInstance().getReference().child("newsFeed");

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                list.clear();
                for(DataSnapshot ds: dataSnapshot.getChildren()){
                    Map map = (Map) ds.getValue();
                    if(map != null){
                        Post post = getPost(map);
                        list.add(post);
                    }
                }
                progressBar.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabase.addValueEventListener(valueEventListener);
    }

    void getInfluencerDetails(){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Influencer").child(user.getUid());
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Log.i("jjha", "called");
                Map map = (Map) dataSnapshot.getValue();
                Log.i("jjha", map.toString());

                if(map != null){
                    influncer = getInfluencer(map);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabase.addListenerForSingleValueEvent(valueEventListener);
    }

    private Post getPost(Map map) {

        String influncername = Objects.requireNonNull(map.get("influncerName")).toString();
        String mediaUrl = Objects.requireNonNull(map.get("mediaUrl")).toString();
        boolean isImage = (boolean) Objects.requireNonNull(map.get("isImage"));
        long time = (long) Objects.requireNonNull(map.get("time"));

        return new Post(influncername,mediaUrl,time,isImage);
    }

    private Influncer getInfluencer(Map map) {

        String name = Objects.requireNonNull(map.get("name")).toString();
        String username = Objects.requireNonNull(map.get("username")).toString();
        String instagramUsername = Objects.requireNonNull(map.get("instagramUsername")).toString();
        String email = Objects.requireNonNull(map.get("email")).toString();
        String contact = Objects.requireNonNull(map.get("contact")).toString();

        return new Influncer(name,username,email,contact,instagramUsername);
    }

    void selectMedia(){
//        Intent imagePicker = new Intent(
//                Intent.ACTION_PICK,
//                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        intent.setType("image/* video/*");

        startActivityForResult(intent, RESULT_LOAD_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != data) {
            Uri selectedImage = data.getData();
            String[] filePathColumn = { MediaStore.Images.Media.DATA };

            filePath = data.getData();

            Cursor cursor = null;
            if (selectedImage != null) {
                cursor = getContentResolver().query(selectedImage,
                        filePathColumn, null, null, null);
            }
            if (cursor != null) {
                cursor.moveToFirst();
                int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
                String picturePath = cursor.getString(columnIndex);
                cursor.close();

                //imageViewProfile.setImageBitmap(BitmapFactory.decodeFile(picturePath));
            }

        }
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG,"Permission is granted");
                return true;
            } else {

                Log.v(TAG,"Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG,"Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_CODE) {
            selectMedia();
            Log.d("TAG", "GRANTED");
        }
    }

    private String getFileExtension(Uri uri){

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void sendImageToTheServer() {

        if(filePath != null)
        {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Progress...");
            progressDialog.show();


            final StorageReference ref = FirebaseStorage.getInstance().getReference("newsFeed").child(System.currentTimeMillis()+"."+getFileExtension(filePath));

            ref.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();

                            ref.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    long time = Calendar.getInstance().getTime().getTime();

                                    Post post = new Post(influncer.name, url,time, isImageFile(filePath.toString()));
                                    uploadNewsFeedToDB(post);

                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(InfluncerActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred()/taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded "+(int)progress+"%");
                        }
                    });
        }
    }

    private void uploadNewsFeedToDB(Post post) {
        long time = Calendar.getInstance().getTime().getTime();

        DatabaseReference messageRef = FirebaseDatabase.getInstance().getReference().child("newsFeed");
        messageRef.push().setValue(post, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if(databaseError == null){
                    Toast.makeText(InfluncerActivity.this, "Uploading Done", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(InfluncerActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public static boolean isImageFile(String path) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        Bitmap bitmap = BitmapFactory.decodeFile(path, options);

        if (options.outWidth != -1 && options.outHeight != -1) {
            return true;
        }
        else {
            return false;
        }
    }
}