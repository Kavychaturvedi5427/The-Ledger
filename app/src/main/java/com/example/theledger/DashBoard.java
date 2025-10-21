package com.example.theledger;

import android.app.Dialog;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class DashBoard extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        // Get Aadhaar ID and pin passed from previous activity
        String a_id = getIntent().getStringExtra("id");
        String pin = getIntent().getStringExtra("pin");

        // Reference to TextViews
        TextView username = findViewById(R.id.username);
        TextView balance = findViewById(R.id.balance1);
        TextView expense = findViewById(R.id.expensesValue);

        // Initialize Firestore
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        // Fetch user document
        db.collection("users").document(a_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("Name");
                        String balance1 = documentSnapshot.getString("Balance");
                        String expense1 = documentSnapshot.getString("Expenses");

                        username.setText("Welcome, " + name);
                        balance.setText(balance1);
                        expense.setText(expense1);

                        // ---------------- ADD BALANCE ----------------
                        LinearLayout addbalance = findViewById(R.id.addBalancell);
                        addbalance.setOnClickListener(v -> {
                            Dialog addBal = new Dialog(this);
                            addBal.setContentView(R.layout.addbalance);
                            addBal.show();

                            AppCompatEditText am = addBal.findViewById(R.id.amntinput);
                            AppCompatEditText da = addBal.findViewById(R.id.dateinput);
                            AppCompatButton addBtn = addBal.findViewById(R.id.add);

                            addBtn.setOnClickListener(v2 -> {
                                String balanceAmount = am.getText().toString();
                                String balanceDate = da.getText().toString();

                                if (balanceAmount.isEmpty() || balanceDate.isEmpty()) {
                                    Toast.makeText(DashBoard.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                db.collection("users").document(a_id)
                                        .update("Balance", balanceAmount, "LastUpdated", balanceDate)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(DashBoard.this, "Balance updated successfully", Toast.LENGTH_SHORT).show();
                                            addBal.dismiss();
                                            balance.setText(balanceAmount);
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(DashBoard.this, "Error updating balance: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                        });
                            });
                        });

                        // ---------------- ADD EXPENSE ----------------
                        LinearLayout addExpenses = findViewById(R.id.addexpnsell);
                        addExpenses.setOnClickListener(v -> {
                            Dialog add = new Dialog(this);
                            add.setContentView(R.layout.addexpense);
                            add.show();

                            AppCompatEditText amount = add.findViewById(R.id.amntinput);
                            AppCompatEditText date = add.findViewById(R.id.dateinput);
                            AppCompatEditText desc = add.findViewById(R.id.descinput);
                            AppCompatButton addBtn = add.findViewById(R.id.add);

                            addBtn.setOnClickListener(view -> {
                                String amnt = amount.getText().toString();
                                String D = date.getText().toString();
                                String Desc = desc.getText().toString();

                                if (amnt.isEmpty() || D.isEmpty() || Desc.isEmpty()) {
                                    Toast.makeText(DashBoard.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // Create transaction data
                                Map<String, Object> data = new HashMap<>();
                                data.put("Amount", amnt);
                                data.put("Date", D);
                                data.put("Description", Desc);
                                data.put("Type", "Expense"); // 👈 New attribute

                                db.collection("users").document(a_id)
                                        .collection("transactions").get()
                                        .addOnSuccessListener(queryDocumentSnapshots -> {
                                            int transactionsPresent = queryDocumentSnapshots.size();
                                            String transaction = "transaction" + (transactionsPresent + 1);

                                            db.collection("users")
                                                    .document(a_id)
                                                    .collection("transactions")
                                                    .document(transaction)
                                                    .set(data)
                                                    .addOnSuccessListener(unused -> {
                                                        Toast.makeText(DashBoard.this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                                                        add.dismiss();
                                                    })
                                                    .addOnFailureListener(e -> {
                                                        Toast.makeText(DashBoard.this, "Error adding expense: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                                    });
                                        });
                            });
                        });

                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error fetching user: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
