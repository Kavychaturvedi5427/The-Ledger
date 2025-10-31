package com.example.theledger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class Profile_nav extends BottomSheetDialogFragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.profile_sheet_btm, container, false);

        TextView name = view.findViewById(R.id.NameTxt);
        TextView email = view.findViewById(R.id.mailTxt);
        CardView edit = view.findViewById(R.id.profile_card);
        CardView logout = view.findViewById(R.id.logout_card);

        FirebaseAuth auth = FirebaseAuth.getInstance();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser user = auth.getCurrentUser();

        if (user == null) {
            Toast.makeText(getContext(), "User not found!", Toast.LENGTH_SHORT).show();
            return view;
        }

        String uid = user.getUid();

        // -------- Load Firestore user info --------
        db.collection("users").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        name.setText(documentSnapshot.getString("Name"));
                        email.setText(documentSnapshot.getString("Email"));
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to fetch profile: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );

        // -------- Edit Profile --------
        edit.setOnClickListener(v -> {
            Dialog editDia = new Dialog(requireActivity());
            editDia.setContentView(R.layout.edit_profile);
            editDia.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            editDia.show();

            TextInputEditText NameTxt = editDia.findViewById(R.id.name_txt);
            TextInputEditText EmailTxt = editDia.findViewById(R.id.emailTxt);
            TextInputEditText PhoneTxt = editDia.findViewById(R.id.phoneTxt);
            TextInputEditText PinTxt = editDia.findViewById(R.id.pintxt);
            ImageView back = editDia.findViewById(R.id.backBtn);
            AppCompatButton save = editDia.findViewById(R.id.saveBTN);

            back.setOnClickListener(bv -> editDia.dismiss());

            save.setOnClickListener(sv -> {
                String newName = NameTxt.getText().toString().trim();
                String newEmail = EmailTxt.getText().toString().trim();
                String newPhone = PhoneTxt.getText().toString().trim();
                String currentPin = PinTxt.getText().toString().trim();

                if (newName.isEmpty() || newEmail.isEmpty() || newPhone.isEmpty()) {
                    Toast.makeText(getContext(), "All fields are required", Toast.LENGTH_SHORT).show();
                    return;
                }

                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if (currentUser == null) {
                    Toast.makeText(getContext(), "User not found", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Step 1 → Update Firestore
                Map<String, Object> updates = new HashMap<>();
                updates.put("Name", newName);
                updates.put("Email", newEmail);
                updates.put("Phone", newPhone);

                DocumentReference userRef = db.collection("users").document(uid);
                userRef.update(updates)
                        .addOnSuccessListener(v1 -> {
                            // Update Auth profile name (always)
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(newName)
                                    .build();

                            currentUser.updateProfile(profileUpdates)
                                    .addOnSuccessListener(unused -> {
                                        // Step 2 → Update Firebase Auth email if needed
                                        if (!currentUser.getEmail().equals(newEmail)) {
                                            if (currentPin.isEmpty()) {
                                                Toast.makeText(getContext(), "Enter your current PIN to confirm changes", Toast.LENGTH_SHORT).show();
                                                return;
                                            }

                                            AuthCredential credential = EmailAuthProvider.getCredential(currentUser.getEmail(), currentPin);
                                            currentUser.reauthenticate(credential)
                                                    .addOnSuccessListener(aVoid -> {
                                                        currentUser.updateEmail(newEmail)
                                                                .addOnSuccessListener(aVoid2 -> {
                                                                    db.collection("users").document(uid)
                                                                            .update("Email", newEmail)
                                                                            .addOnSuccessListener(v2 -> {
                                                                                Toast.makeText(getContext(), "Email updated successfully ✅", Toast.LENGTH_SHORT).show();
                                                                                name.setText(newName);
                                                                                email.setText(newEmail);
                                                                                editDia.dismiss();
                                                                            })
                                                                            .addOnFailureListener(e ->
                                                                                    Toast.makeText(getContext(), "Auth updated but Firestore failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                                                                            );
                                                                })
                                                                .addOnFailureListener(e -> {
                                                                    Toast.makeText(getContext(), "Email update failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                                                                });
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(getContext(), "Re-authentication failed — incorrect PIN.", Toast.LENGTH_LONG).show();
                                                    });
                                        } else {
                                            Toast.makeText(getContext(), "Profile updated successfully", Toast.LENGTH_SHORT).show();
                                            name.setText(newName);
                                            email.setText(newEmail);
                                            editDia.dismiss();
                                        }
                                    })
                                    .addOnFailureListener(e -> {
                                        Toast.makeText(getContext(), "Failed to update Auth display name: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    });
                        })
                        .addOnFailureListener(e ->
                                Toast.makeText(getContext(), "Profile update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                        );

            });
        });

        // -------- Logout --------
        logout.setOnClickListener(view1 ->

        {
            Dialog logDia = new Dialog(requireActivity());
            logDia.setContentView(R.layout.signout_layout);
            logDia.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            logDia.show();

            logDia.findViewById(R.id.YesBtn).setOnClickListener(v4 -> {
                FirebaseAuth.getInstance().signOut();
                dismiss();
                Toast.makeText(requireContext(),
                        "All set. Come back soon to balance the books 📚",
                        Toast.LENGTH_SHORT).show();
                startActivity(new Intent(getActivity(), Login.class));
            });

            logDia.findViewById(R.id.NoBtn).setOnClickListener(v3 -> logDia.dismiss());
        });

//      ------------- ABOUT CARD --------------

        CardView about = view.findViewById(R.id.About_card);
        about.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent about_intent = new Intent(getContext() , About_app.class);
                startActivity(about_intent);
            }
        });
        
        
//      ---------- Change theme functionality (will be implemented later) -----------
        CardView theme = view.findViewById(R.id.Theme_Card);
        theme.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(getContext(), "Oh, you really thought that’d change the theme?", Toast.LENGTH_SHORT).show();
            }
        });

//        --------- Privacy and Settings menu ---------
        CardView Privacy = view.findViewById(R.id.privacy_Setting_card);
        Privacy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Privacy_BTM privacyBtm = new Privacy_BTM();
                privacyBtm.show(getParentFragmentManager(), privacyBtm.getTag());

            }
        });



        return view;
    }
}
