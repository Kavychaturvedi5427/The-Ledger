package com.example.theledger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.cardview.widget.CardView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.Firebase;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class Profile_nav extends BottomSheetDialogFragment {
    @Nullable
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.profile_sheet_btm, container, false);
        TextView name = view.findViewById(R.id.NameTxt);
        TextView email = view.findViewById(R.id.mailTxt);
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String uid = auth.getCurrentUser().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(uid).get().addOnSuccessListener(documentSnapshot -> {
            name.setText(documentSnapshot.getString("Name"));
            email.setText(documentSnapshot.getString("Email"));
        });

        // Edit Profile Functionality....
        CardView edit = view.findViewById(R.id.profile_card);


        // logout functionality.....
        CardView logout = view.findViewById(R.id.logout_card);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog logDia = new Dialog(requireActivity());
                logDia.setContentView(R.layout.signout_layout);
                logDia.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                logDia.show();
                logDia.findViewById(R.id.YesBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseAuth.getInstance().signOut();
                        dismiss();
                        Toast.makeText(requireContext(), "All set. Come back soon to balance the books", Toast.LENGTH_SHORT).show();
                        Intent backtolog = new Intent(getActivity().getApplicationContext(), Login.class);
                        startActivity(backtolog);
                    }
                });
                logDia.findViewById(R.id.NoBtn).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        logDia.dismiss();
                    }
                });
            }
        });


        return view;
    }
}
