package com.example.theledger;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class chooseLoginSignup extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.choose_login_signup);

        AppCompatButton login = findViewById(R.id.loginBTN);
        AppCompatButton signup = findViewById(R.id.signupbtn);
        ImageView exit = findViewById(R.id.exit);


        login.setOnClickListener(v -> {
            Intent move_toLogin = new Intent(chooseLoginSignup.this, Login.class);
            startActivity(move_toLogin);
        });

        signup.setOnClickListener(v -> {
            Intent move_toSignup = new Intent(chooseLoginSignup.this, signup.class);
            startActivity(move_toSignup);
        });

        exit.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                finishAffinity();
                System.exit(0);
            }
        });

    }
}
