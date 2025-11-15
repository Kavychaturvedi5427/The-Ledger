package com.example.theledger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

public class Transaction_activity extends AppCompatActivity {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    String uid;

    TextView txn1, txn2, txn3, bal1, bal2, bal3;

    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.activity_transac);

//      ------- Binding the id's of the viewgroups --------
        txn1 = findViewById(R.id.txn1);
        txn2 = findViewById(R.id.txn2);
        txn3 = findViewById(R.id.txn3);
        bal1 = findViewById(R.id.bal1);
        bal2 = findViewById(R.id.bal2);
        bal3 = findViewById(R.id.bal3);

//      ------ clearing old text from the textviews ------
        txn1.setText("");
        txn2.setText("");
        txn3.setText("");
        bal1.setText("");
        bal2.setText("");
        bal3.setText("");


        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        if (auth.getCurrentUser() == null) {
            Toast.makeText(this, "Who are you again? Please log in first.", Toast.LENGTH_SHORT).show();
            return;
        }
        uid = auth.getCurrentUser().getUid();


//      ------ fetching last 3 transactions from the db -------
        db.collection("users").document(uid).collection("transactions")
                .orderBy("Date", Query.Direction.DESCENDING).limit(3)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Zero transactions. You’re really saving the economy.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int i = 1;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        String Category = doc.getString("Description");
                        Double amount = doc.getDouble("Amount");
                        String date = doc.getString("Date");
                        String formatted = Category + " | ₹" + amount + " | " + date;
                        if (i == 1) txn1.setText(formatted);
                        else if (i == 2) txn2.setText(formatted);
                        else if (i == 3) txn3.setText(formatted);
                        i++;
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Wow. Firebase decided to nap again.", Toast.LENGTH_SHORT).show();
                });

//        ----- Fetching last 3 balance top ups ------
        db.collection("users").document(uid).collection("BalanceManagement").orderBy("Date", Query.Direction.DESCENDING).limit(3)
                .get().addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Toast.makeText(this, "Empty wallet detected. Classic.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    int i = 1;
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        Double amount = doc.getDouble("Amount");
                        String date = doc.getString("Date");
                        String formatted = "₹" + amount + " | " + date;
                        if (i == 1) bal1.setText(formatted);
                        else if (i == 2) bal2.setText(formatted);
                        else if (i == 3) bal3.setText(formatted);
                        i++;
                    }
                }).addOnFailureListener(e -> {
                    Toast.makeText(this, "Oops. That wasn’t supposed to happen... probably.", Toast.LENGTH_SHORT).show();
                });

//        ----- Navbar Functionalities ------
        LinearLayout home = findViewById(R.id.homeBtnLayout);
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backToDashboard = new Intent(Transaction_activity.this, DashBoard.class);
                startActivity(backToDashboard);
                finish();
            }
        });
        
        LinearLayout transacBtn = findViewById(R.id.transacBtnLayout);
        transacBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(Transaction_activity.this, "Relax, you’re already drowning in transactions.", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout analyBtn = findViewById(R.id.analyBtnLayout);
        analyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics_section analy = new Analytics_section();
                analy.show(getSupportFragmentManager(), "analytics_section");
            }
        });

        LinearLayout profile = findViewById(R.id.profBtnLayout);
        profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                for opening BottomSheetDialogFragment
                Profile_nav prof = new Profile_nav();
                prof.show(getSupportFragmentManager(), "profile_nav");
            }
        });
    }
}
