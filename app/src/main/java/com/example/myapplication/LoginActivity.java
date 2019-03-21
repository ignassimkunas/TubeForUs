package com.example.myapplication;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private FirebaseAuth firebaseAuth;

    public void login(View view) {

        firebaseAuth = FirebaseAuth.getInstance();

        EditText emailEdit = findViewById(R.id.email);
        EditText passwordEdit = findViewById(R.id.password);
        final String email = emailEdit.getText().toString();
        final String password = passwordEdit.getText().toString();

        final Intent intent = new Intent(this, MainActivity.class);

        if (!email.isEmpty() && !password.isEmpty()) {

            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(LoginActivity.this, "Login succesful, welcome" + email, Toast.LENGTH_SHORT).show();
                                startActivity(intent);
                            }
                            else {

                                Toast.makeText(LoginActivity.this, "Authentification failed", Toast.LENGTH_SHORT).show();
                            }
                        }});
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        Intent intent = new Intent(this, MainActivity.class);

        if (user != null) {

            startActivity(intent);
        }

    }
}
