package com.example.theledger;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.FirebaseFirestore;

public class DashBoard extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Get Aadhaar ID and pin passed from previous activity
        String a_id = getIntent().getStringExtra("id");
        String pin = getIntent().getStringExtra("pin");

        // Reference to TextView in dashboard.xml
        TextView username = findViewById(R.id.username);

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch user document by Aadhaar ID
        db.collection("users").document(a_id).get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                String name = documentSnapshot.getString("Name");
                username.setText("Welcome, " + name);



//                db.collection("users").document(a_id).set()

            } else {
                Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(e -> {
            Toast.makeText(this, "Error fetching user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        });
    }
}
