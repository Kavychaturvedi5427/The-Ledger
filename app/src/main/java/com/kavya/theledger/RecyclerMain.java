package com.kavya.theledger;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class RecyclerMain extends AppCompatActivity {

    FirebaseFirestore db;
    //    FirebaseUser user;
    FirebaseAuth auth;
    String uid;
    TextView Mainheading, Subheading;
    LottieAnimationView lottie;

    private void loadTransac(){
        db.collection("users").document(uid).collection("transactions").orderBy("Timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots ->
        {
            ArrayList<Transaction_model> transacData = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Transaction_model transac = doc.toObject(Transaction_model.class);
                transacData.add(transac);
            }
            RecyclerView recyclerView = findViewById(R.id.recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new Transac_recycler_adapter(this, transacData));

        }).addOnFailureListener(a -> {
            Toast.makeText(getApplicationContext(), "Unexpected Error Occured", Toast.LENGTH_SHORT).show();
        });
    }

    private void loadBalance(){
        db.collection("users").document(uid).collection("BalanceManagement").orderBy("Date", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots ->
        {
            ArrayList<Transaction_model> balanceData = new ArrayList<>();
            for (DocumentSnapshot doc : queryDocumentSnapshots) {
                Transaction_model transac = doc.toObject(Transaction_model.class);
                balanceData.add(transac);
            }
            RecyclerView recyclerView = findViewById(R.id.recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new Transac_recycler_adapter(this, balanceData));

        }).addOnFailureListener(a -> {
            Toast.makeText(getApplicationContext(), "Unexpected Error Occured", Toast.LENGTH_SHORT).show();
        });
    }


    protected void onCreate(Bundle SavedInstanceState) {
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.transac_recycler_main);

        int mode = getIntent().getIntExtra(Transaction_activity.EXTRA_MODE,Transaction_activity.MODE_TRANSACTIONS);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) {
            return;
        }

        uid = auth.getCurrentUser().getUid();

        Mainheading = findViewById(R.id.mainHeading);
        Subheading = findViewById(R.id.subHeading);
        lottie = findViewById(R.id.lottie);

        if(mode == Transaction_activity.MODE_TRANSACTIONS){
            Mainheading.setText("Where My Money Went!");
            Subheading.setText("Still trying to figure this out");
            lottie.setAnimation(R.raw.transactionrecycler);
            loadTransac();
        }
        else{
            Mainheading.setText("Balance history");
            Subheading.setText("Money in, money out.");
            lottie.setAnimation(R.raw.balancerecycler);
            loadBalance();
        }

        ShapeableImageView back = findViewById(R.id.backbtn);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent backTOTransaction = new Intent(getApplicationContext(), Transaction_activity.class);
                startActivity(backTOTransaction);
                finish();
            }
        });
    }
}
