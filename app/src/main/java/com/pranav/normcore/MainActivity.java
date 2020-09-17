package com.pranav.normcore;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.pranav.normcore.Activities.InfluncerActivity;
import com.pranav.normcore.Activities.InfluncerLogin;
import com.pranav.normcore.Activities.UserActivity;

public class MainActivity extends AppCompatActivity {

    Button userButton, influencerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userButton = findViewById(R.id.buttonUser);
        influencerButton = findViewById(R.id.buttonInfluncer);

        userButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, UserActivity.class));
            }
        });

        influencerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this, InfluncerLogin.class));
            }
        });
    }
}