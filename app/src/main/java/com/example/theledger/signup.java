package com.example.theledger;

import android.content.Intent;
import android.os.Bundle;
import android.text.style.TabStopSpan;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {

    // UI components
    private TextInputLayout nameLayout, emailLayout, phoneLayout, passwordLayout;
    private TextInputEditText name, email, phone, pin;
    private AppCompatButton signupBtn;
    private ProgressBar progress;
    private ShapeableImageView backBtn;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // --- Initialize Views ---
        nameLayout = findViewById(R.id.nameLayout);
        emailLayout = findViewById(R.id.emailLayout);
        phoneLayout = findViewById(R.id.phoneLayout);
        passwordLayout = findViewById(R.id.passwordLayout);

        name = findViewById(R.id.nametext);
        email = findViewById(R.id.emailtxt);
        phone = findViewById(R.id.phonetxt);
        pin = findViewById(R.id.pintxt);
        signupBtn = findViewById(R.id.signupbtn);
        progress = findViewById(R.id.customProgress);
        backBtn = findViewById(R.id.backbtn);

        // --- Firebase Instances ---
        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        // --- Back Button ---
        backBtn.setOnClickListener(v -> {
            Toast.makeText(this,"Retreating, huh? Not ready for commitment?",Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, chooseLoginSignup.class));
            finish();
        });

        // --- Signup Button ---
        signupBtn.setOnClickListener(v -> handleSignup());
    }

    private void handleSignup() {
        progress.setVisibility(View.VISIBLE);
        signupBtn.setEnabled(false);

        // --- Get Inputs ---
        String Name = name.getText() != null ? name.getText().toString().trim() : "";
        String Email = email.getText() != null ? email.getText().toString().trim()
                .replaceAll("\\s+", "")
                .replaceAll("[\\u200B-\\u200D\\uFEFF]", "")
                .toLowerCase() : "";
        String Phone = phone.getText() != null ? phone.getText().toString().trim() : "";
        String Pin = pin.getText() != null ? pin.getText().toString().trim() : "";

        // --- Input Validation ---
        if (Name.isEmpty() || Email.isEmpty() || Phone.isEmpty() || Pin.isEmpty()) {
            showError("Leaving blanks? Planning to sign up telepathically?");
            return;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(Email).matches()) {
            showError("That’s not even an email… unless Gmail suddenly lowered its standards.");
            return;
        }

        // Password validation (letters + numbers, 6+ chars)
        if (!Pin.matches("^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@#$%!]{6,}$")) {
            showError("That password’s so weak it probably skips leg day.");
            return;
        }

        if (Phone.length() < 10) {
            showError("Ten digits. It’s not rocket science, Einstein.");
            return;
        }

        // --- Create Firebase User ---
        auth.createUserWithEmailAndPassword(Email, Pin)
                .addOnSuccessListener(authResult -> {
                    FirebaseUser currentUser = authResult.getUser();
                    if (currentUser == null) {
                        showError("Congrats, you just broke logic. Try again");
                        return;
                    }

                    String uid = currentUser.getUid();
                    Toast.makeText(this, "Finally, you exist. Humanity has been waiting.", Toast.LENGTH_SHORT).show();

                    // --- Save to Firestore ---
                    Map<String, Object> user = new HashMap<>();
                    user.put("Name", Name);
                    user.put("Email", Email);
                    user.put("Phone", Phone);
                    user.put("createdAt", System.currentTimeMillis());

                    db.collection("users").document(uid)
                            .set(user)
                            .addOnSuccessListener(unused -> {
                                Toast.makeText(this, "Stored safely. Try not to forget your own data now.", Toast.LENGTH_SHORT).show();

//                                No need for this now ..... for debugging purpose only.....
                                // Debug Provider Check
//                                auth.fetchSignInMethodsForEmail(Email)
//                                        .addOnSuccessListener(result ->
//                                                Log.d("SIGNUP_DEBUG", "Providers for " + Email + ": " + result.getSignInMethods()))
//                                        .addOnFailureListener(e ->
//                                                Log.e("SIGNUP_DEBUG", "Provider fetch failed: " + e.getMessage()));

                                clearFields();
                                resetProgress();
                                Toast.makeText(this, "Off you go. Don’t mess up your new account already.", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(this, chooseLoginSignup.class));
                                finish();
                            })
                            .addOnFailureListener(e -> {
                                showError("Firestore took one look and said ‘nope \uD83D\uDC80");
                            });
                })
                .addOnFailureListener(e -> {
                    if (e.getMessage() != null && e.getMessage().contains("already in use")) {
                        showError("That email’s taken. Try adding a random number like everyone else.");
                    } else {
                        showError("Firebase ghosted you. Typical.");
                    }
                });

//        No need for this.... for debugging purpose only.....
//        Extra Debug Log
//        auth.fetchSignInMethodsForEmail(Email)
//                .addOnSuccessListener(r -> Log.d("SIGNUP_CHECK",
//                        "After signup, providers for " + Email + ": " + r.getSignInMethods()))
//                .addOnFailureListener(e ->
//                        Log.e("SIGNUP_CHECK", "Failed to fetch after signup: " + e.getMessage()));
    }

    private void clearFields() {
        name.setText("");
        email.setText("");
        phone.setText("");
        pin.setText("");
    }

    private void showError(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        resetProgress();
    }

    private void resetProgress() {
        progress.setVisibility(View.GONE);
        signupBtn.setEnabled(true);
    }
}
