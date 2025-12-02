package com.kavya.theledger;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

public class chooseLoginSignup extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.choose_login_signup);

        // Get the actual root ScrollView from XML
        View root = findViewById(R.id.rootScroll);

        CardView mainCard = findViewById(R.id.mainCard);
        mainCard.setVisibility(View.VISIBLE);

        // UI elements
        ImageView logo = findViewById(R.id.mainimg);
        View appName = findViewById(R.id.appname);
        View tagline = findViewById(R.id.appTagline);
        AppCompatButton login = findViewById(R.id.loginBTN);
        AppCompatButton signup = findViewById(R.id.signupbtn);
        ImageView exit = findViewById(R.id.exit);

        // Load animations
        Animation fadeZoom = AnimationUtils.loadAnimation(this, R.anim.fade_zoom);
        Animation fadeInSlow = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        // 🌟 Fix: Animate the real root ScrollView (not android.R.id.content)
        root.setVisibility(View.VISIBLE);
        root.setAlpha(0f);
        root.animate()
                .alpha(1f)
                .setDuration(400)
                .withEndAction(() ->
                        startIntroSequence(logo, appName, tagline, login, signup, exit,
                                fadeZoom, fadeIn, fadeInSlow))
                .start();

        // Click listeners
        login.setOnClickListener(v -> {
            startActivity(new Intent(this, Login.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        signup.setOnClickListener(v -> {
            startActivity(new Intent(this, signup.class));
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
        });

        exit.setOnClickListener(v -> {
            finishAffinity();
            System.exit(0);
        });
    }

    private void startIntroSequence(
            ImageView logo, View appName, View tagline,
            AppCompatButton login, AppCompatButton signup, ImageView exit,
            Animation fadeZoom, Animation fadeIn, Animation fadeInSlow) {

        // Logo first
        logo.startAnimation(fadeZoom);
        logo.setVisibility(View.VISIBLE);

        // App name + tagline
        appName.postDelayed(() -> {
            appName.startAnimation(fadeIn);
            tagline.startAnimation(fadeIn);
            appName.setVisibility(View.VISIBLE);
            tagline.setVisibility(View.VISIBLE);
        }, 400);

        // Buttons + exit last
        login.postDelayed(() -> {
            login.startAnimation(fadeInSlow);
            signup.startAnimation(fadeInSlow);
            exit.startAnimation(fadeInSlow);
            login.setVisibility(View.VISIBLE);
            signup.setVisibility(View.VISIBLE);
            exit.setVisibility(View.VISIBLE);
        }, 800);

        ImageView help = findViewById(R.id.help);
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String url = "https://Kavychaturvedi5427.github.io/The-Ledger-Privacy/help_desk.htm";
                Intent sendmail = new Intent(Intent.ACTION_VIEW);
                sendmail.setData(Uri.parse(url));
                startActivity(sendmail);
            }
        });
    }
}
