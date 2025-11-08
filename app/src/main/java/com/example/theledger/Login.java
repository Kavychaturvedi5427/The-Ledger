package com.example.theledger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;

import java.util.concurrent.Executor;

public class Login extends AppCompatActivity {

    private FirebaseAuth auth;
    private TextInputLayout emailLayout, passwordLayout;
    private TextInputEditText emailEdit, passwordEdit;
    private AppCompatButton loginBtn;
    private ProgressBar progress;
    private ShapeableImageView backBtn;
    private TextView redirectSignup, resetPassword;
    private ImageView fingerprintIcon;

    private BiometricPrompt biometricPrompt;
    private BiometricPrompt.PromptInfo promptInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // --- View Bindings ---
        emailLayout = findViewById(R.id.emailLayout);
        passwordLayout = findViewById(R.id.passwordLayout);
        emailEdit = findViewById(R.id.emailedit);
        passwordEdit = findViewById(R.id.pinInput);
        loginBtn = findViewById(R.id.loginBtn);
        progress = findViewById(R.id.customProgress);
        backBtn = findViewById(R.id.back_btn);
        redirectSignup = findViewById(R.id.redirectSignup);
        resetPassword = findViewById(R.id.reset);
        fingerprintIcon = findViewById(R.id.fingerprintico);

        auth = FirebaseAuth.getInstance();

        // -------------------- ✨ ANIMATIONS --------------------
        Animation fadeZoom = AnimationUtils.loadAnimation(this, R.anim.fade_zoom);
        Animation fadeInSlow = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);

        View logo = findViewById(R.id.mainimg);
        if (logo != null) {
            logo.startAnimation(fadeZoom);
        }

        // Removed animations for email and password fields
        // Keep subtle animation for buttons and icons only
        loginBtn.postDelayed(() -> loginBtn.startAnimation(fadeInSlow), 300);
        fingerprintIcon.postDelayed(() -> fingerprintIcon.startAnimation(fadeInSlow), 400);
        resetPassword.postDelayed(() -> resetPassword.startAnimation(fadeIn), 500);
        redirectSignup.postDelayed(() -> redirectSignup.startAnimation(fadeIn), 600);
        backBtn.postDelayed(() -> backBtn.startAnimation(fadeInSlow), 700);

        // --- Redirect to Signup ---
        redirectSignup.setOnClickListener(v -> {
            startActivity(new Intent(this, signup.class));
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);
            finish();
        });

        // --- Login functionality ---
        loginBtn.setOnClickListener(v -> handleLogin());

        // --- Reset Password Dialog ---
        resetPassword.setOnClickListener(v -> openResetDialog());

        // --- Back Button ---
        backBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, chooseLoginSignup.class));
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);
            finish();
        });

        // --- Setup Biometric ---
        setupBiometricPrompt();

        // --- Fingerprint Tap Trigger ---
        fingerprintIcon.setOnClickListener(v -> {
            String savedEmail = getSharedPreferences("loginPrefs", MODE_PRIVATE).getString("savedEmail", null);
            String savedPass = getSharedPreferences("loginPrefs", MODE_PRIVATE).getString("savedPass", null);

            if (savedEmail != null && savedPass != null) {
                if (isBiometricAvailable()) {
                    biometricPrompt.authenticate(promptInfo);
                } else {
                    Toast.makeText(this, "No biometrics? What is this, 2010?", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Backing out already? Commitment issues?", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // ------------------------- LOGIN HANDLER -------------------------
    private void handleLogin() {
        progress.setVisibility(View.VISIBLE);
        loginBtn.setEnabled(false);

        String enteredEmail = emailEdit.getText() != null ? emailEdit.getText().toString().trim().toLowerCase() : "";
        String enteredPass = passwordEdit.getText() != null ? passwordEdit.getText().toString().trim() : "";

        if (enteredEmail.isEmpty()) {
            Toast.makeText(this, "You forgot your email. Again?", Toast.LENGTH_SHORT).show();
            resetProgress();
            return;
        }

        if (enteredPass.isEmpty()) {
            Toast.makeText(this, "Typing a password would really help.", Toast.LENGTH_SHORT).show();
            resetProgress();
            return;
        }

        auth.signInWithEmailAndPassword(enteredEmail, enteredPass)
                .addOnSuccessListener(authResult -> {
                    resetProgress();
                    Toast.makeText(this, "Well, look who decided to come back.", Toast.LENGTH_SHORT).show();

                    // Save credentials for biometric login
                    getSharedPreferences("loginPrefs", MODE_PRIVATE)
                            .edit()
                            .putString("savedEmail", enteredEmail)
                            .putString("savedPass", enteredPass)
                            .apply();

                    navigateToDashboard();
                })
                .addOnFailureListener(e -> {
                    resetProgress();
                    String msg = e.getMessage() != null ? e.getMessage() : "";
                    if (msg.contains("no user record")) {
                        Toast.makeText(this, "No account? Guess you imagined signing up.", Toast.LENGTH_SHORT).show();
                    } else if (msg.contains("password is invalid")) {
                        Toast.makeText(this, "Nice try. Password’s not even close.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Something broke. Probably not your fault… probably.️", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // ------------------------- RESET PASSWORD HANDLER -------------------------
    private void openResetDialog() {
        Dialog resetDialog = new Dialog(this);
        resetDialog.setContentView(R.layout.reset_dialog);
        resetDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        resetDialog.show();

        AppCompatEditText emailField = resetDialog.findViewById(R.id.emailtxt);
        AppCompatButton confirmBtn = resetDialog.findViewById(R.id.confirmBtn);
        AppCompatButton cancelBtn = resetDialog.findViewById(R.id.cancelBtn);

        cancelBtn.setOnClickListener(view -> resetDialog.dismiss());

        confirmBtn.setOnClickListener(view -> {
            String enteredEmail = emailField.getText() != null
                    ? emailField.getText().toString().trim()
                    .replaceAll("\\s+", "")
                    .replaceAll("[\\u200B-\\u200D\\uFEFF]", "")
                    .toLowerCase()
                    : "";

            if (enteredEmail.isEmpty()) {
                Toast.makeText(this, "Enter your email. Telepathy doesn’t work here.", Toast.LENGTH_SHORT).show();
                return;
            }

            auth.sendPasswordResetEmail(enteredEmail)
                    .addOnSuccessListener(unused -> {
                        Toast.makeText(this, "Reset link sent. Check inbox… or that Spam graveyard.", Toast.LENGTH_LONG).show();
                        resetDialog.dismiss();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(this, "Couldn’t send it. Maybe your email’s in witness protection " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });
    }

    // ------------------------- BIOMETRIC -------------------------
    private void setupBiometricPrompt() {
        Executor executor = ContextCompat.getMainExecutor(this);
        biometricPrompt = new BiometricPrompt(Login.this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        Toast.makeText(Login.this, "Fingerprint accepted. You may pass, oh chosen one.", Toast.LENGTH_SHORT).show();

                        String savedEmail = getSharedPreferences("loginPrefs", MODE_PRIVATE)
                                .getString("savedEmail", null);
                        String savedPass = getSharedPreferences("loginPrefs", MODE_PRIVATE)
                                .getString("savedPass", null);

                        if (savedEmail != null && savedPass != null) {
                            auth.signInWithEmailAndPassword(savedEmail, savedPass)
                                    .addOnSuccessListener(authResult -> navigateToDashboard())
                                    .addOnFailureListener(e ->
                                            Toast.makeText(Login.this, "Re-login failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                        } else {
                            Toast.makeText(Login.this, "No saved login found. Maybe try logging in first, genius.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(Login.this, "Nope. That’s not you. Try the real finger.", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(Login.this, "Biometric system had a meltdown. Try again. " + errString, Toast.LENGTH_SHORT).show();
                    }
                });

        promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Unlock with Fingerprint")
                .setSubtitle("Use biometric authentication to access your Ledger")
                .setNegativeButtonText("Cancel")
                .build();
    }

    private boolean isBiometricAvailable() {
        BiometricManager biometricManager = BiometricManager.from(this);
        return biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG)
                == BiometricManager.BIOMETRIC_SUCCESS;
    }

    private void navigateToDashboard() {
        startActivity(new Intent(this, DashBoard.class));
        overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);
        finish();
    }

    // ------------------------- HELPERS -------------------------
    private void resetProgress() {
        progress.setVisibility(View.GONE);
        loginBtn.setEnabled(true);
    }
}
