package com.example.theledger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // finding id's of the text group and sign up button....
        AppCompatEditText name = findViewById(R.id.nametext);
        AppCompatEditText email = findViewById(R.id.emailtxt);
        AppCompatEditText phone = findViewById(R.id.phonetxt);
        AppCompatEditText pin = findViewById(R.id.pintxt);
        AppCompatButton signup = findViewById(R.id.signupbtn);
        ProgressBar progress = findViewById(R.id.customProgress);
        ShapeableImageView backBtn = findViewById(R.id.backbtn);

        // Back button functionality
        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backTochoose = new Intent(getApplicationContext(), chooseLoginSignup.class);
                startActivity(backTochoose);
            }
        });

        // connecting to the database....
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // for authentication....
        FirebaseAuth auth = FirebaseAuth.getInstance();

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);

                String Name = name.getText().toString().trim();
                String Email = email.getText().toString().trim();
                String Phone = phone.getText().toString().trim();
                String Pin = pin.getText().toString().trim();

                // Check for empty fields
                if (Name.isEmpty() || Email.isEmpty() || Phone.isEmpty() || Pin.isEmpty()) {
                    Toast.makeText(signup.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                    return;
                }

                // Firebase requires password with at least 6 characters
                if (Pin.length() < 6) {
                    Toast.makeText(signup.this, "PIN must be at least 6 digits", Toast.LENGTH_SHORT).show();
                    progress.setVisibility(View.GONE);
                    return;
                }

                // Create user with email and password
                auth.createUserWithEmailAndPassword(Email, Pin)
                        .addOnSuccessListener(authResult -> {
                            // storing the data in the hashmap....
                            Map<String, Object> user = new HashMap<>();
                            user.put("Name", Name);
                            user.put("Email", Email);
                            user.put("Phone", Phone);
                            user.put("Pin", Pin);

                            // this will get the uid of the user who just signed up.....
                            String uid = authResult.getUser().getUid();
                            // will store the data in the database ......
                            db.collection("users").document(uid)
                                    .set(user)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(getApplicationContext(), "User Added Successfully", Toast.LENGTH_SHORT).show();
                                        // resetting the Entry field after the registration....
                                        name.setText("");
                                        email.setText("");
                                        phone.setText("");
                                        pin.setText("");
                                        progress.setVisibility(View.GONE);
                                        Intent backtoChoose = new Intent(signup.this, chooseLoginSignup.class);
                                        startActivity(backtoChoose);
                                        finish();
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getApplicationContext(), "Try Again: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        progress.setVisibility(View.GONE);
                                    });
                        })
                        .addOnFailureListener(e -> {
                            // Handle Firebase Auth failure (weak password, invalid email, etc.)
                            Toast.makeText(signup.this, "Sign up failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                            progress.setVisibility(View.GONE);
                        });
            }
        });
    }
}
