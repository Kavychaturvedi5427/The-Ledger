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
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class signup extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signup);

        // finding id's of the text group and sign up button....
        AppCompatEditText name = findViewById(R.id.nametext);
        AppCompatEditText aadhar = findViewById(R.id.aadhartxt);
        AppCompatEditText phone = findViewById(R.id.phonetxt);
        AppCompatEditText pin = findViewById(R.id.pintxt);
        AppCompatButton signup = findViewById(R.id.signupbtn);
        ProgressBar progress = findViewById(R.id.progressBar);
        ShapeableImageView backBtn = findViewById(R.id.backbtn);


        backBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backTochoose = new Intent(getApplicationContext(),chooseLoginSignup.class);
                startActivity(backTochoose);
            }
        });

        // connecting to the database....
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                progress.setVisibility(View.VISIBLE);
                String Name = name.getText().toString().trim();
                String Aadhar = aadhar.getText().toString().trim();
                String Phone = phone.getText().toString().trim();
                String Pin = pin.getText().toString().trim();

                // storing the data in the hashmap....
                Map<String, Object> user = new HashMap<>();
                user.put("Name",Name);
                user.put("Aadhar",Aadhar);
                user.put("Phone",Phone);
                user.put("Pin",Pin);

                db.collection("users").document(Aadhar).set(user).addOnSuccessListener(documentReference -> {
                            Toast.makeText(getApplicationContext(),"User Added Successfully",Toast.LENGTH_SHORT).show();

                            // resetting the Entry field after the registration....
                            name.setText("");
                            aadhar.setText("");
                            phone.setText("");
                            pin.setText("");
                            progress.setVisibility(View.GONE);

                        })
                        .addOnFailureListener(documentReference -> {
                            Toast.makeText(getApplicationContext(), "Try Again", Toast.LENGTH_SHORT).show();});
            }
        });
    }
}
