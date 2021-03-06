package com.pranav.normcore.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.pranav.normcore.CustomClasses.Influncer;
import com.pranav.normcore.MainActivity;
import com.pranav.normcore.R;

import static com.pranav.normcore.Activities.InfluncerActivity.MY_PREFS_NAME;

public class InfluncerLogin extends AppCompatActivity {
    private static final String TAG = "TAKATAK";

    EditText editTextEmail, editTextPassword;
    TextView textViewSignUp;
    Button buttonLogin;
    ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_influncer_login);

        editTextEmail = findViewById(R.id.editTextLoginEmail);
        editTextPassword = findViewById(R.id.editTextLoginPassword);
        textViewSignUp = findViewById(R.id.textViewSignupButton);
        buttonLogin = findViewById(R.id.buttonLogin);
        progressBar = findViewById(R.id.progressBarLogin);

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = editTextEmail.getText().toString().trim();
                String password = editTextPassword.getText().toString().trim();

                userLogin(email, password);
            }
        });

        textViewSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(InfluncerLogin.this, InfluncerSignup.class));
            }
        });

        SharedPreferences prefs = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE);
        if(prefs.contains("email")) {
            //Check weather shared preference contains userName, is it contains then login
            String email = prefs.getString("email", "");
            String password = prefs.getString("password", "");

            this.editTextEmail.setText(email);
            this.editTextPassword.setText(password);

            if (email != null ) {
                userLogin(email,password);
            }

        }

    }

    void rememberLogin(){
        // If user sign in successfully remember the email and password
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        SharedPreferences.Editor editor = getSharedPreferences(MY_PREFS_NAME, MODE_PRIVATE).edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.apply();
    }

    void userLogin(String email, String password){
        progressBar.setVisibility(View.VISIBLE);

        final FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        progressBar.setVisibility(View.GONE);
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "signInWithEmail:success");
                            rememberLogin();
                            finish();
                            startActivity(new Intent(InfluncerLogin.this, InfluncerActivity.class));


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "signInWithEmail:failure", task.getException());
                            Toast.makeText(InfluncerLogin.this, "Invalid email or password",
                                    Toast.LENGTH_SHORT).show();
                            // ...
                        }
                        // ...
                    }
                });
    }
}