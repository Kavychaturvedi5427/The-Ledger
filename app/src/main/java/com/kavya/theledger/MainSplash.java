package com.kavya.theledger;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class MainSplash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.splash_main);

        // Force light mode
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);

        View logo = findViewById(R.id.mainimg);
        View mainText = findViewById(R.id.Maintext);
        View tagline = findViewById(R.id.tagline);
        View loader = findViewById(R.id.customProgress);

        // 🌀 Logo animation (fade + zoom)
        Animation logoAnim = AnimationUtils.loadAnimation(this, R.anim.fade_zoom);
        logo.startAnimation(logoAnim);

        // 💬 Text fade-in after logo
        new Handler().postDelayed(() -> {
            Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
            mainText.startAnimation(fadeIn);
            tagline.startAnimation(fadeIn);
            mainText.setVisibility(View.VISIBLE);
            tagline.setVisibility(View.VISIBLE);
        }, 600);

        // 🔄 Loader fade-in later
        new Handler().postDelayed(() -> {
            Animation fadeInLoader = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
            loader.startAnimation(fadeInLoader);
            loader.setVisibility(View.VISIBLE);
        }, 1200);

        // 🎬 Smooth exit to next screen
        new Handler().postDelayed(() -> {
            // stop running animations to avoid transition lag
            logo.clearAnimation();
            mainText.clearAnimation();
            tagline.clearAnimation();
            loader.clearAnimation();

            Intent moveToMain = new Intent(MainSplash.this, chooseLoginSignup.class);
            startActivity(moveToMain);

            // use fade for smoother GPU transition instead of slide (less lag)
            overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
            finishAffinity(); // ensures no residual activity in backstack
        }, 2800);

        // 🌓 Theme log (for debugging)
//        int mode = getResources().getConfiguration().uiMode & Configuration.UI_MODE_NIGHT_MASK;
//        Log.d("ThemeCheck", mode == Configuration.UI_MODE_NIGHT_YES ? "Dark mode active" : "Light mode active");
    }
}
