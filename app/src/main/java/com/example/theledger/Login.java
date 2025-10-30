package com.example.theledger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.method.PasswordTransformationMethod;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class Login extends AppCompatActivity {

    // Move to next box after typing one digit
    private void moveToNext(EditText curr, EditText next) {
        curr.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 1 && next != null) next.requestFocus();
            }
        });
    }

    // Move back on backspace
    private void moveToPrev(EditText curr, EditText prev) {
        curr.setOnKeyListener((v, keyCode, event) -> {
            if (event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_DEL) {
                if (curr.getText().toString().isEmpty()) {
                    prev.requestFocus();
                    prev.setText(""); // optional: clear previous box
                }
            }
            return false;
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.login);

        // Fetching view IDs
        TextView redirectSignup = findViewById(R.id.redirectSignup);
        TextView PinReset = findViewById(R.id.reset);
        EditText pin1 = findViewById(R.id.pin1);
        EditText pin2 = findViewById(R.id.pin2);
        EditText pin3 = findViewById(R.id.pin3);
        EditText pin4 = findViewById(R.id.pin4);
        EditText pin5 = findViewById(R.id.pin5);
        EditText pin6 = findViewById(R.id.pin6);
        AppCompatButton login = findViewById(R.id.loginBtn);
        AppCompatEditText email = findViewById(R.id.emailedit);
        ShapeableImageView back = findViewById(R.id.back_btn);
        ProgressBar progress = findViewById(R.id.customProgress);

        // getting access to the firebase database....
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        // getting the current user for authentication....
        FirebaseAuth auth = FirebaseAuth.getInstance();

        // --- PIN fields setup for 6 digits ---
        pin1.setTransformationMethod(new PasswordTransformationMethod());
        pin2.setTransformationMethod(new PasswordTransformationMethod());
        pin3.setTransformationMethod(new PasswordTransformationMethod());
        pin4.setTransformationMethod(new PasswordTransformationMethod());
        pin5.setTransformationMethod(new PasswordTransformationMethod());
        pin6.setTransformationMethod(new PasswordTransformationMethod());

        // Smooth PIN navigation
        moveToNext(pin1, pin2);
        moveToNext(pin2, pin3);
        moveToNext(pin3, pin4);
        moveToNext(pin4, pin5);
        moveToNext(pin5, pin6);
        moveToNext(pin6, null);

        moveToPrev(pin2, pin1);
        moveToPrev(pin3, pin2);
        moveToPrev(pin4, pin3);
        moveToPrev(pin5, pin4);
        moveToPrev(pin6, pin5);

        // Redirect to signup
        redirectSignup.setOnClickListener(v -> {
            Intent moveToSignup = new Intent(Login.this, signup.class);
            startActivity(moveToSignup);
        });

        // LOGIN BUTTON
        login.setOnClickListener(v -> {
            progress.setVisibility(View.VISIBLE);

            String pin = pin1.getText().toString().trim() + pin2.getText().toString().trim() + pin3.getText().toString().trim() + pin4.getText().toString().trim() + pin5.getText().toString().trim() + pin6.getText().toString().trim();

            String EnterEmail = email.getText().toString().trim();

            // --- VALIDATION ---
            if (EnterEmail.isEmpty()) {
                Toast.makeText(Login.this, "We can’t log you in if you don’t tell us who you are.", Toast.LENGTH_SHORT).show();
                progress.setVisibility(View.GONE);
                return;
            }
            if (pin.length() != 6) {
                Toast.makeText(Login.this, "Your PIN’s having an identity crisis — needs 6 digits.", Toast.LENGTH_SHORT).show();
                progress.setVisibility(View.GONE);
                return;
            }

            login.setEnabled(false);

            // --- FIREBASE LOGIN ---
            auth.signInWithEmailAndPassword(EnterEmail, pin).addOnSuccessListener(authResult -> {
                String uid = authResult.getUser().getUid();

                db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
                    login.setEnabled(true);
                    progress.setVisibility(View.GONE);

                    if (documentSnapshot.exists()) {
                        String storedPin = documentSnapshot.getString("Pin");
                        if (pin.equals(storedPin)) {
                            Toast.makeText(Login.this, "You’re in! The app approves.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Login.this, DashBoard.class);
                            intent.putExtra("uid", uid);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(Login.this, "Invalid PIN", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(Login.this, "User doesn't exist.", Toast.LENGTH_SHORT).show();
                    }
                }).addOnFailureListener(e -> {
                    login.setEnabled(true);
                    progress.setVisibility(View.GONE);
                    Toast.makeText(Login.this, "Error fetching data", Toast.LENGTH_SHORT).show();
                });
            }).addOnFailureListener(e -> {
                login.setEnabled(true);
                progress.setVisibility(View.GONE);
                Toast.makeText(Login.this, "Credentials rejected. Even the app has standards.", Toast.LENGTH_SHORT).show();
            });
        });


        // --- FORGOT PIN RESET ---
        PinReset.setOnClickListener(v1 -> {
            Dialog resetDialog = new Dialog(Login.this);
            resetDialog.setContentView(R.layout.reset_dialog);
            resetDialog.show();

            // New PIN
            AppCompatEditText pin1Reset = resetDialog.findViewById(R.id.pin1);
            AppCompatEditText pin2Reset = resetDialog.findViewById(R.id.pin2);
            AppCompatEditText pin3Reset = resetDialog.findViewById(R.id.pin3);
            AppCompatEditText pin4Reset = resetDialog.findViewById(R.id.pin4);
            AppCompatEditText pin5Reset = resetDialog.findViewById(R.id.pin5);
            AppCompatEditText pin6Reset = resetDialog.findViewById(R.id.pin6);

            // Confirm PIN
            AppCompatEditText pin_1Reset = resetDialog.findViewById(R.id.pin_1);
            AppCompatEditText pin_2Reset = resetDialog.findViewById(R.id.pin_2);
            AppCompatEditText pin_3Reset = resetDialog.findViewById(R.id.pin_3);
            AppCompatEditText pin_4Reset = resetDialog.findViewById(R.id.pin_4);
            AppCompatEditText pin_5Reset = resetDialog.findViewById(R.id.pin_5);
            AppCompatEditText pin_6Reset = resetDialog.findViewById(R.id.pin_6);

            // Phone number
            AppCompatEditText phone = resetDialog.findViewById(R.id.phonetxt);

            AppCompatButton confirmBtn = resetDialog.findViewById(R.id.confirmBtn);
            AppCompatButton cancelBtn = resetDialog.findViewById(R.id.cancelBtn);

            // Hide digits for both new and confirm PIN fields
            pin1Reset.setTransformationMethod(new PasswordTransformationMethod());
            pin2Reset.setTransformationMethod(new PasswordTransformationMethod());
            pin3Reset.setTransformationMethod(new PasswordTransformationMethod());
            pin4Reset.setTransformationMethod(new PasswordTransformationMethod());
            pin5Reset.setTransformationMethod(new PasswordTransformationMethod());
            pin6Reset.setTransformationMethod(new PasswordTransformationMethod());

            pin_1Reset.setTransformationMethod(new PasswordTransformationMethod());
            pin_2Reset.setTransformationMethod(new PasswordTransformationMethod());
            pin_3Reset.setTransformationMethod(new PasswordTransformationMethod());
            pin_4Reset.setTransformationMethod(new PasswordTransformationMethod());
            pin_5Reset.setTransformationMethod(new PasswordTransformationMethod());
            pin_6Reset.setTransformationMethod(new PasswordTransformationMethod());

            // Enable smooth PIN navigation for both new and confirm PINs
            moveToNext(pin1Reset, pin2Reset);
            moveToNext(pin2Reset, pin3Reset);
            moveToNext(pin3Reset, pin4Reset);
            moveToNext(pin4Reset, pin5Reset);
            moveToNext(pin5Reset, pin6Reset);
            moveToPrev(pin2Reset, pin1Reset);
            moveToPrev(pin3Reset, pin2Reset);
            moveToPrev(pin4Reset, pin3Reset);
            moveToPrev(pin5Reset, pin4Reset);
            moveToPrev(pin6Reset, pin5Reset);

            moveToNext(pin_1Reset, pin_2Reset);
            moveToNext(pin_2Reset, pin_3Reset);
            moveToNext(pin_3Reset, pin_4Reset);
            moveToNext(pin_4Reset, pin_5Reset);
            moveToNext(pin_5Reset, pin_6Reset);
            moveToPrev(pin_2Reset, pin_1Reset);
            moveToPrev(pin_3Reset, pin_2Reset);
            moveToPrev(pin_4Reset, pin_3Reset);
            moveToPrev(pin_5Reset, pin_4Reset);
            moveToPrev(pin_6Reset, pin_5Reset);

            // Cancel button
            cancelBtn.setOnClickListener(view -> resetDialog.dismiss());

            // Confirm button
            confirmBtn.setOnClickListener(view -> {
                String phonenum = phone.getText().toString().trim();
                String newpin = pin1Reset.getText().toString().trim() + pin2Reset.getText().toString().trim() + pin3Reset.getText().toString().trim() + pin4Reset.getText().toString().trim() + pin5Reset.getText().toString().trim() + pin6Reset.getText().toString().trim();

                String confirmPin = pin_1Reset.getText().toString().trim() + pin_2Reset.getText().toString().trim() + pin_3Reset.getText().toString().trim() + pin_4Reset.getText().toString().trim() + pin_5Reset.getText().toString().trim() + pin_6Reset.getText().toString().trim();

                if (phonenum.isEmpty()) {
                    Toast.makeText(Login.this, "Empty fields won’t reset anything, pal", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!newpin.equals(confirmPin)) {
                    Toast.makeText(Login.this, "New PINs should match. It’s not a competition.", Toast.LENGTH_SHORT).show();
                    return;
                }

                db.collection("users").whereEqualTo("Phone", phonenum).get().addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        DocumentSnapshot document = querySnapshot.getDocuments().get(0);
                        db.collection("users").document(document.getId()).update("Pin", newpin).addOnSuccessListener(aVoid -> {
                            Toast.makeText(Login.this, "PIN updated! Try not to forget it this time.", Toast.LENGTH_SHORT).show();
                            resetDialog.dismiss();
                        }).addOnFailureListener(e -> Toast.makeText(Login.this, "Try again", Toast.LENGTH_SHORT).show());
                    } else {
                        Toast.makeText(Login.this, "If this is your number, Firestore strongly disagrees.", Toast.LENGTH_SHORT).show();
                    }
                });
            });
        });

        // Back button
        back.setOnClickListener(v2 -> {
            Intent backIntent = new Intent(Login.this, chooseLoginSignup.class);
            startActivity(backIntent);
            finish();
        });
    }
}
