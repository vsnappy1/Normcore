package com.pranav.normcore.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.FileUtils;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Map;
import java.util.Objects;

public class InfluncerActivity extends AppCompatActivity {
    public static final String MY_PREFS_NAME = "TheScoutApp";

    private static final String TAG = "YOYO";
    private static int RESULT_LOAD_IMAGE = 1;
    private static final int REQUEST_CODE = 97;
    private Uri filePath = null;

    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    PostAdapter adapter;
    ArrayList<Post> list;
    ProgressBar progressBar;
    TextView textView;


    Influncer influncer;
    String influencerId = "";
    FirebaseUser user;

    Button buttonSelectMedia, buttonUpload, buttonLogout;
    ArrayList<String> myPostIds, myPostIdsInfluencer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_influncer);

        myPostIds = new ArrayList<>();
        myPostIdsInfluencer = new ArrayList<>();

        getInfluencerDetails();

        buttonUpload = findViewById(R.id.buttonUpload);
        buttonSelectMedia = findViewById(R.id.buttonSelectFile);
        buttonLogout = findViewById(R.id.buttonLogout);
        progressBar = findViewById(R.id.progressBarInfluencer);
        textView = findViewById(R.id.textViewNoDataInfluencer);

        recyclerView = findViewById(R.id.recyclerViewInfluencer);

        list = new ArrayList<>();

        recyclerView.setHasFixedSize(true);

        // use a linear layout manager
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        adapter = new PostAdapter(this, list);
        adapter.setOnItemClickListener(new PostAdapter.ClickListener() {
            @Override
            public void onItemClick(int position, View v) {

            }

            @Override
            public void onLongClick(int position, View v) {
                //Toast.makeText(InfluncerActivity.this, myPostIdsInfluencer.get(position), Toast.LENGTH_SHORT).show();
                showDialogBox(position);
            }
        });

        recyclerView.setAdapter(adapter);

        buttonUpload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Toast.makeText(InfluncerActivity.this, String.valueOf(checkIsImage(InfluncerActivity.this,filePath)), Toast.LENGTH_SHORT).show();
                if (filePath == null) {
                    Toast.makeText(InfluncerActivity.this, "Please select a file first", Toast.LENGTH_SHORT).show();
                } else {
                    if (influncer != null) {
                        sendImageToTheServer();
                    } else {
                        Toast.makeText(InfluncerActivity.this, "wait a minute", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        buttonSelectMedia.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (isStoragePermissionGranted()) {
                    selectMedia();
                }
            }
        });

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                logOut();
            }
        });

    }

    void showDialogBox(final int position) {
        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        deletePost(position);
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Delete");
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }

    void getMyPostIds(String userId) {

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference()
                .child("Influencer")
                .child(userId)
                .child("myPost");

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                myPostIds.clear();
                myPostIdsInfluencer.clear();
                list.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    myPostIds.add(ds.getValue().toString());
                    myPostIdsInfluencer.add(ds.getKey());
                }

                if (myPostIds.size() == 0) {
                    textView.setVisibility(View.VISIBLE);
                    progressBar.setVisibility(View.GONE);

                }
                fetchMyPosts(myPostIds);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        mDatabase.addValueEventListener(valueEventListener);

    }

    void deletePost(int position) {
        //Delete post id from blogger account
        FirebaseDatabase.getInstance().getReference()
                .child("Influencer")
                .child(user.getUid())
                .child("myPost")
                .child(myPostIdsInfluencer.get(position))
                .removeValue();

        // Delete the post
        FirebaseDatabase.getInstance().getReference()
                .child("newsFeed")
                .child(myPostIds.get(position))
                .removeValue();

        //Get the reference of media and delete it
        FirebaseStorage.getInstance().getReferenceFromUrl(list.get(position)
                .mediaUrl)
                .delete()
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                Toast.makeText(InfluncerActivity.this, "Deleted", Toast.LENGTH_SHORT).show();
            }
        });
    }

    void fetchMyPosts(ArrayList<String> ids) {

        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                Map map = (Map) dataSnapshot.getValue();
                if (map != null) {
                    Post post = getPost(map);
                    list.add(post);
                }
                progressBar.setVisibility(View.GONE);
                textView.setVisibility(View.GONE);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        };

        for (String id : ids) {
            DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("newsFeed").child(id);
            mDatabase.addValueEventListener(valueEventListener);
        }
    }

    void getInfluencerDetails() {
        user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            influencerId = user.getUid();
        }
        getMyPostIds(influencerId);

        DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference().child("Influencer").child(influencerId);
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Map map = (Map) dataSnapshot.getValue();

                if (map != null) {
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

        return new Post(influncername, mediaUrl, time, isImage);
    }

    private Influncer getInfluencer(Map map) {

        String name = Objects.requireNonNull(map.get("name")).toString();
        String username = Objects.requireNonNull(map.get("username")).toString();
        String instagramUsername = Objects.requireNonNull(map.get("instagramUsername")).toString();
        String email = Objects.requireNonNull(map.get("email")).toString();
        String contact = Objects.requireNonNull(map.get("contact")).toString();

        return new Influncer(name, username, email, contact, instagramUsername);
    }

    void selectMedia() {
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
            String[] filePathColumn = {MediaStore.Images.Media.DATA};

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

    public boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    == PackageManager.PERMISSION_GRANTED) {
                Log.v(TAG, "Permission is granted");
                return true;
            } else {

                Log.v(TAG, "Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
                return false;
            }
        } else { //permission is automatically granted on sdk<23 upon installation
            Log.v(TAG, "Permission is granted");
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

    private String getFileExtension(Uri uri) {

        ContentResolver contentResolver = getContentResolver();
        MimeTypeMap mimeTypeMap = MimeTypeMap.getSingleton();
        return mimeTypeMap.getExtensionFromMimeType(contentResolver.getType(uri));

    }

    private void sendImageToTheServer() {

        if (filePath != null) {
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Progress...");
            progressDialog.show();


            final StorageReference ref = FirebaseStorage.getInstance().getReference("newsFeed").child(System.currentTimeMillis() + "." + getFileExtension(filePath));

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

                                    Post post = new Post(influncer.username, url, time, checkIsImage(InfluncerActivity.this, filePath));
                                    uploadNewsFeedToDB(post);
                                    filePath = null;


                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(InfluncerActivity.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot
                                    .getTotalByteCount());
                            progressDialog.setMessage("Uploaded " + (int) progress + "%");
                        }
                    });
        }
    }

    void addPostIdInInfluencer(String id) {
        FirebaseDatabase.getInstance().getReference()
                .child("Influencer")
                .child(influencerId)
                .child("myPost")
                .push()
                .setValue(id);

    }

    private void uploadNewsFeedToDB(Post post) {

        DatabaseReference newsFeed = FirebaseDatabase.getInstance().getReference().child("newsFeed");
        final String id = newsFeed.push().getKey();
        newsFeed.child(id).setValue(post, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError == null) {
                    Toast.makeText(InfluncerActivity.this, "Uploading Done", Toast.LENGTH_SHORT).show();
                    addPostIdInInfluencer(id);
                } else {
                    Toast.makeText(InfluncerActivity.this, "Some error occurred", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public static boolean checkIsImage(Context context, Uri uri) {
        ContentResolver contentResolver = context.getContentResolver();
        String type = contentResolver.getType(uri);
        if (type != null) {
            return type.startsWith("image/");
        } else {
            // try to decode as image (bounds only)
            InputStream inputStream = null;
            try {
                inputStream = contentResolver.openInputStream(uri);
                if (inputStream != null) {
                    BitmapFactory.Options options = new BitmapFactory.Options();
                    options.inJustDecodeBounds = true;
                    BitmapFactory.decodeStream(inputStream, null, options);
                    return options.outWidth > 0 && options.outHeight > 0;
                }
            } catch (IOException e) {
                // ignore
            } finally {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    FileUtils.closeQuietly(inputStream);
                }
            }
        }
        // default outcome if image not confirmed
        return false;
    }


    void logOut(){
        //Remove the saved email and password also make the googleSignIn false cause we have signed out
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.remove("email");
        editor.remove("password");
        editor.putBoolean("googleSignIn",false);
        editor.apply();
        finish();
    }
    @Override
    public void onBackPressed() {


        DialogInterface.OnClickListener dialogClickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case DialogInterface.BUTTON_POSITIVE:
                        //Yes button clicked
                        finish();
                        break;

                    case DialogInterface.BUTTON_NEGATIVE:
                        //No button clicked
                        break;
                }
            }
        };

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Exit");
        builder.setMessage("Are you sure?").setPositiveButton("Yes", dialogClickListener)
                .setNegativeButton("No", dialogClickListener).show();
    }
}