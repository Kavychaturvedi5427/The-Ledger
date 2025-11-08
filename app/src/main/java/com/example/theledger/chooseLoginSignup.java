package com.example.theledger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

public class chooseLoginSignup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.choose_login_signup);

        // UI elements
        ImageView logo = findViewById(R.id.mainimg);
        View appName = findViewById(R.id.appname);
        View tagline = findViewById(R.id.appTagline);
        AppCompatButton login = findViewById(R.id.loginBTN);
        AppCompatButton signup = findViewById(R.id.signupbtn);
        ImageView exit = findViewById(R.id.exit);

        // Load existing animations
        Animation fadeZoom = AnimationUtils.loadAnimation(this, R.anim.fade_zoom);
        Animation fadeInSlow = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // Animate logo first
        logo.startAnimation(fadeZoom);
        logo.setVisibility(View.VISIBLE);

        // Animate name + tagline slightly after logo
        appName.postDelayed(() -> {
            appName.startAnimation(fadeIn);
            tagline.startAnimation(fadeIn);
            appName.setVisibility(View.VISIBLE);
            tagline.setVisibility(View.VISIBLE);
        }, 400);

        // Animate buttons and exit last
        login.postDelayed(() -> {
            login.startAnimation(fadeInSlow);
            signup.startAnimation(fadeInSlow);
            exit.startAnimation(fadeInSlow);
            login.setVisibility(View.VISIBLE);
            signup.setVisibility(View.VISIBLE);
            exit.setVisibility(View.VISIBLE);
        }, 800);

        // Button click effects + transitions
        login.setOnClickListener(v -> {
            Intent move_toLogin = new Intent(chooseLoginSignup.this, Login.class);
            startActivity(move_toLogin);
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);
        });

        signup.setOnClickListener(v -> {
            Intent move_toSignup = new Intent(chooseLoginSignup.this, signup.class);
            startActivity(move_toSignup);
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);
        });

        exit.setOnClickListener(v -> {
            finishAffinity();
            System.exit(0);
        });
    }
}
