package com.kavya.theledger;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
    String uid ;
    protected void onCreate(Bundle SavedInstanceState){
        super.onCreate(SavedInstanceState);
        setContentView(R.layout.transac_recycler_main);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if(auth.getCurrentUser() == null){
            return;
        }

        uid = auth.getCurrentUser().getUid();

        db.collection("users").document(uid).collection("transactions").orderBy("Timestamp", Query.Direction.DESCENDING).get().addOnSuccessListener(queryDocumentSnapshots ->
        {
            ArrayList<Transaction_model> transacData = new ArrayList<>();
            for(DocumentSnapshot doc : queryDocumentSnapshots){
                Transaction_model transac = doc.toObject(Transaction_model.class);
                transacData.add(transac);
            }
            RecyclerView recyclerView = findViewById(R.id.recyclerview);
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(new Transac_recycler_adapter(this,transacData));

        }).addOnFailureListener(a->{
            Toast.makeText(getApplicationContext(), "Unexpected Error Occured", Toast.LENGTH_SHORT).show();
        });
    }
}
