package com.pranav.normcore.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pranav.normcore.Adapters.PostAdapter;
import com.pranav.normcore.CustomClasses.Post;
import com.pranav.normcore.R;

import java.util.ArrayList;
import java.util.Map;
import java.util.Objects;

public class UserActivity extends AppCompatActivity {


    RecyclerView recyclerView;
    LinearLayoutManager layoutManager;
    PostAdapter adapter;
    ArrayList<Post> list;
    ProgressBar progressBar;
    DatabaseReference mDatabase;
    ValueEventListener valueEventListener;
    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        progressBar = findViewById(R.id.progressBarUser);
        recyclerView = findViewById(R.id.recyclerViewUser);
        textView = findViewById(R.id.textViewNoDataUser);

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

            }
        });

        recyclerView.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("newsFeed");

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                list.clear();
                for (DataSnapshot ds : dataSnapshot.getChildren()) {
                    Map map = (Map) ds.getValue();
                    if (map != null) {
                        Post post = getPost(map);
                        list.add(post);
                    }
                }

                if(list.size() == 0){
                    textView.setVisibility(View.VISIBLE);
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


    private Post getPost(Map map) {

        String influncername = Objects.requireNonNull(map.get("influncerName")).toString();
        String mediaUrl = Objects.requireNonNull(map.get("mediaUrl")).toString();
        boolean isImage = (boolean) Objects.requireNonNull(map.get("isImage"));
        long time = (long) Objects.requireNonNull(map.get("time"));

        return new Post(influncername, mediaUrl, time, isImage);
    }
}