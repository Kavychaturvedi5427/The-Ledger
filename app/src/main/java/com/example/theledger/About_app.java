package com.example.theledger;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.InetAddresses;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;

import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class About_app extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.about_app);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String Name = null;
        String Email = null;

        String uid = auth.getUid();

        ProgressBar progress = findViewById(R.id.customProgress);
//      --------- Insta Linking ----------
        LinearLayout insta = findViewById(R.id.install);
        insta.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String insta_url = "https://www.instagram.com/kavy___17/";
                        Intent inst = new Intent(Intent.ACTION_VIEW, Uri.parse(insta_url));
                        startActivity(inst);
                        progress.setVisibility(View.GONE);
                    }
                }, 500);
            }
        });

//      --------- Linked in linking ----------
        LinearLayout linked = findViewById(R.id.linkedll);
        linked.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String linkedIN_url = "https://www.linkedin.com/in/kavya-chaturvedi-1a181932a/";
                        Intent linked = new Intent(Intent.ACTION_VIEW, Uri.parse(linkedIN_url));
                        startActivity(linked);
                        progress.setVisibility(View.GONE);
                    }
                }, 500);
            }
        });

//      --------- GitHub Linking ----------
        LinearLayout Git = findViewById(R.id.gitll);
        Git.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                v.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        String Git_url = "https://github.com/Kavychaturvedi5427";
                        Intent git = new Intent(Intent.ACTION_VIEW, Uri.parse(Git_url));
                        startActivity(git);
                        progress.setVisibility(View.GONE);
                    }
                }, 500);
            }
        });

//      -------- Storing suggetion in the db (suggestion) ----------
        AppCompatEditText suggestion = findViewById(R.id.suggest_input);
        ImageView send = findViewById(R.id.sendBtn);

        send.setOnClickListener(v -> {
            String suggestText = suggestion.getText().toString().trim();

            if (suggestText.isEmpty()) {
                Toast.makeText(this, "Please enter your suggestion.", Toast.LENGTH_SHORT).show();
                return;
            }

            progress.setVisibility(View.VISIBLE);

            db.collection("users").document(uid).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            String name = documentSnapshot.getString("name");
                            String email = documentSnapshot.getString("Email");

                            Map<String, Object> data = new HashMap<>();
                            data.put("Name", name);
                            data.put("Email", email);
                            data.put("Suggestion", suggestText);
                            data.put("UID", uid);

                            db.collection("Suggestions").add(data)
                                    .addOnSuccessListener(docRef -> {
                                        Toast.makeText(this, "Your wisdom has been recorded.", Toast.LENGTH_SHORT).show();
                                        suggestion.setText(""); // clear input
                                        progress.setVisibility(View.GONE);
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(this, "The void has rejected your thoughts.", Toast.LENGTH_SHORT).show();
                                        progress.setVisibility(View.GONE);
                                    });
                        } else {
                            Toast.makeText(this, "User not found.", Toast.LENGTH_SHORT).show();
                            progress.setVisibility(View.GONE);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(this, "Error fetching user info.", Toast.LENGTH_SHORT).show();
                        progress.setVisibility(View.GONE);
                    });
        });


        ImageView back = findViewById(R.id.backBtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                finish();
                progress.setVisibility(View.GONE);

            }
        });

    }
}
