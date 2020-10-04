package com.pranav.normcore;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.FirebaseDatabase;

public class FeedbackActivity extends AppCompatActivity {

    EditText editTextFeedback;
    Button buttonSubmit;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_feedback);

        editTextFeedback = findViewById(R.id.editTextFeedback);
        buttonSubmit = findViewById(R.id.buttonFeedbackSubmit);
        progressBar = findViewById(R.id.progressBarFeedback);


        buttonSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String feedback = editTextFeedback.getText().toString().trim();

                if(feedback.length() > 10){
                    progressBar.setVisibility(View.VISIBLE);
                    sendFeedback(feedback);
                }else {
                    Toast.makeText(FeedbackActivity.this, "Please write at least 10 character to submit feedback", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }

    private void sendFeedback(String feedback) {
        FirebaseDatabase.getInstance().getReference()
                .child("feedback")
                .push()
                .setValue(feedback)
        .addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(FeedbackActivity.this, "Feedback submitted", Toast.LENGTH_SHORT).show();
                onBackPressed();
            }
        });
    }
}