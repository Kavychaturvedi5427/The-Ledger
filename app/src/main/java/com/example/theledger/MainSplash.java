package com.example.theledger;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.appcompat.app.AppCompatActivity;

public class MainSplash extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.splash_main);

        // move to home screen after 3 sec
        Intent move_toMain = new Intent(MainSplash.this , chooseLoginSignup.class);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(move_toMain);
                overridePendingTransition(R.anim.slide_left,R.anim.slide_right);
                finish();
            }
        },4000);

        // fade and zoom animation on the main logo
        Animation logoAnim = AnimationUtils.loadAnimation(this , R.anim.fade_zoom);
        findViewById(R.id.mainimg).startAnimation(logoAnim);
    }
}
