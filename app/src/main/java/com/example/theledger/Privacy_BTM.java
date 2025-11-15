package com.example.theledger;

import android.app.AlertDialog;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.SwitchCompat;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.concurrent.Executor;

public class Privacy_BTM extends BottomSheetDialogFragment {

    @Override
    public void onStart() {
        super.onStart();

        View bottomSheet = getDialog().findViewById(
                com.google.android.material.R.id.design_bottom_sheet
        );

        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(R.drawable.bottomsheet_bg);
        }
    }

    private void deleteUserAccount(FirebaseFirestore db, FirebaseAuth auth, FirebaseUser user) {
        // Step 1: Delete Firestore data first
        db.collection("users").document(user.getUid())
                .delete()
                .addOnSuccessListener(aVoid -> Log.d("Delete", "User data deleted"))
                .addOnFailureListener(e -> Log.w("Delete", "Error deleting data", e));

        // Step 2: Delete Firebase Auth account
        user.delete()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        auth.signOut();
                        FirebaseAuth.getInstance().getCurrentUser(); // ensure null locally
                        Toast.makeText(getContext(), "Account deleted successfully", Toast.LENGTH_SHORT).show();
                        dismiss();

                        startActivity(new Intent(getContext(), Login.class)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK));
                    } else {
                        Toast.makeText(getContext(), "Failed to delete account. Try logging in again.", Toast.LENGTH_LONG).show();
                    }
                });
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseAuth auth = FirebaseAuth.getInstance();

        View view = inflater.inflate(R.layout.privacy_setting_bottomsheet, container, false);

        CardView AppLock = view.findViewById(R.id.AppLock_card);
        AppLock.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) {
                Toast.makeText(getContext(), "User not logged in!", Toast.LENGTH_SHORT).show();
                return;
            }
            View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.biometric_dialog_box, null);
            SwitchCompat toggle = dialogView.findViewById(R.id.biometricSwitch);

            AlertDialog dialog = new AlertDialog.Builder(getContext())
                    .setTitle("App Lock")
                    .setView(dialogView)
                    .setCancelable(true)
                    .create();

            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        if (documentSnapshot.exists()) {
                            Boolean biometricEnabled = documentSnapshot.getBoolean("Biometric Functionality");
                            toggle.setChecked(Boolean.TRUE.equals(biometricEnabled));
                        }

                        toggle.setOnCheckedChangeListener((buttonView, isChecked) -> {
                            if (isChecked) {
                                BiometricManager biometricManager = BiometricManager.from(getContext());
                                int canAuth = biometricManager.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_STRONG);

                                if (canAuth == BiometricManager.BIOMETRIC_SUCCESS) {
                                    showBiometricPromptForEnable(user.getUid(), toggle);
                                } else if (canAuth == BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED) {
                                    Toast.makeText(getContext(), "No fingerprint enrolled! Please add one in settings.", Toast.LENGTH_LONG).show();
                                    toggle.setChecked(false);
                                    Intent enrollIntent = new Intent(Settings.ACTION_BIOMETRIC_ENROLL);
                                    enrollIntent.putExtra(Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                                            BiometricManager.Authenticators.BIOMETRIC_STRONG);
                                    startActivity(enrollIntent);
                                } else {
                                    Toast.makeText(getContext(), "Device doesn't support biometrics.", Toast.LENGTH_SHORT).show();
                                    toggle.setChecked(false);
                                }
                            } else {
                                db.collection("users").document(user.getUid())
                                        .update("Biometric Functionality", false)
                                        .addOnSuccessListener(aVoid ->
                                                Toast.makeText(getContext(), "🔓 App Lock turned off", Toast.LENGTH_SHORT).show())
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Failed to update biometric state", Toast.LENGTH_SHORT).show();
                                            toggle.setChecked(true);
                                        });
                            }
                        });
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to load biometric state", Toast.LENGTH_SHORT).show()
                    );

            dialog.show();
        });

        // -------- Privacy Policy Card ----------
        CardView privacyCard = view.findViewById(R.id.privacy_policy_Card);
        privacyCard.setOnClickListener(v -> {
            String url = "https://Kavychaturvedi5427.github.io/The-Ledger-Privacy/privacy_policy.htm";
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        });

        // ------ Beta Features functionality -------
        CardView betaFeatures = view.findViewById(R.id.beta_feature_card);
        betaFeatures.setOnClickListener(v ->
                Toast.makeText(getContext(), "❄ You’re too early. Even the devs haven’t seen these yet.", Toast.LENGTH_SHORT).show()
        );

        // --------- Delete account --------
        CardView delete = view.findViewById(R.id.delete_account_Card);
        delete.setOnClickListener(v -> {
            FirebaseUser user = auth.getCurrentUser();

            if (user == null) {
                Toast.makeText(getContext(), "No user logged in", Toast.LENGTH_SHORT).show();
                return;
            }

            new AlertDialog.Builder(getContext())
                    .setTitle("Delete Account")
                    .setMessage("Are you sure you want to delete your account? This action cannot be undone.")
                    .setPositiveButton("Yes", (dialogInterface, i) -> {

                        // Show password input dialog for re-authentication
                        View passwordView = LayoutInflater.from(getContext()).inflate(R.layout.password_confirm_dialog, null);
                        AppCompatEditText passwordInput = passwordView.findViewById(R.id.passwordEditText);

                        new AlertDialog.Builder(getContext())
                                .setTitle("Confirm Password")
                                .setView(passwordView)
                                .setMessage("Please enter your password to delete your account.")
                                .setPositiveButton("Delete", (d, i2) -> {
                                    String password = passwordInput.getText().toString().trim();
                                    if (password.isEmpty()) {
                                        Toast.makeText(getContext(), "Password required", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    String email = user.getEmail();
                                    if (email == null) {
                                        Toast.makeText(getContext(), "No email found for this account.", Toast.LENGTH_SHORT).show();
                                        return;
                                    }

                                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);
                                    user.reauthenticate(credential)
                                            .addOnSuccessListener(aVoid -> deleteUserAccount(db, auth, user))
                                            .addOnFailureListener(e -> Toast.makeText(getContext(), "Wrong password", Toast.LENGTH_SHORT).show());
                                })
                                .setNegativeButton("Cancel", null)
                                .show();

                    })
                    .setNegativeButton("No", null)
                    .show();
        });

        // -------- BACK BUTTON --------
        ImageView back = view.findViewById(R.id.backbtn);
        back.setOnClickListener(v -> dismiss());

        return view;
    }

    private void showBiometricPrompt(String uid) {
        Executor executor = ContextCompat.getMainExecutor(getContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                super.onAuthenticationSucceeded(result);
                Toast.makeText(getContext(), "\uD83D\uDD12 Congrats, you’ve just proven you’re you. Impressive.", Toast.LENGTH_SHORT).show();
                updateFirestoreDb(uid, true);
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                super.onAuthenticationError(errorCode, errString);
                Toast.makeText(getContext(), " — even your phone lost patience." + errString, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAuthenticationFailed() {
                super.onAuthenticationFailed();
                Toast.makeText(getContext(), "\uD83D\uDE43 Nice try, but that’s not your finger.", Toast.LENGTH_SHORT).show();
            }
        });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Enable App Lock")
                .setSubtitle("Prove you’re not an imposter to activate App Lock")
                .setNegativeButtonText("Cancel, I like living dangerously")
                .build();
        biometricPrompt.authenticate(promptInfo);
    }

    private void updateFirestoreDb(String uid, boolean b) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(uid).update("Biometric Functionality", b)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Biometric Functionality Enabled", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed To update biometric functionality", Toast.LENGTH_SHORT).show());
    }

    private void showBiometricPromptForEnable(String uid, SwitchCompat toggle) {
        Executor executor = ContextCompat.getMainExecutor(getContext());
        BiometricPrompt biometricPrompt = new BiometricPrompt(this, executor,
                new BiometricPrompt.AuthenticationCallback() {
                    @Override
                    public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                        super.onAuthenticationSucceeded(result);
                        FirebaseFirestore.getInstance()
                                .collection("users").document(uid)
                                .update("Biometric Functionality", true)
                                .addOnSuccessListener(aVoid -> {
                                    Toast.makeText(getContext(), "🔐 App Lock enabled", Toast.LENGTH_SHORT).show();
                                    toggle.setChecked(true);
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(getContext(), "Failed to enable App Lock", Toast.LENGTH_SHORT).show();
                                    toggle.setChecked(false);
                                });
                    }

                    @Override
                    public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {
                        super.onAuthenticationError(errorCode, errString);
                        Toast.makeText(getContext(), "❌ " + errString, Toast.LENGTH_SHORT).show();
                        toggle.setChecked(false);
                    }

                    @Override
                    public void onAuthenticationFailed() {
                        super.onAuthenticationFailed();
                        Toast.makeText(getContext(), "🙃 Wrong fingerprint, try again.", Toast.LENGTH_SHORT).show();
                        toggle.setChecked(false);
                    }
                });

        BiometricPrompt.PromptInfo promptInfo = new BiometricPrompt.PromptInfo.Builder()
                .setTitle("Enable App Lock")
                .setSubtitle("Authenticate to activate biometric protection")
                .setNegativeButtonText("Cancel")
                .build();

        biometricPrompt.authenticate(promptInfo);
    }
}
