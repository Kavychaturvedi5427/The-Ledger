package com.example.theledger;

import android.app.Dialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class DashBoard extends AppCompatActivity {

    String a_id;
    String pin;

    TextView username;
    TextView balance;
    TextView expense;
    FirebaseFirestore db = FirebaseFirestore.getInstance();

    // Listen to changes in balance and expense
    private void listenForBalanceAndExpenses() {
        // Expense listener
        db.collection("users").document(a_id)
                .collection("transactions")
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Toast.makeText(DashBoard.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (queryDocumentSnapshots != null) {
                        long totalExpense = 0;
                        for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                            Object amtObj = doc.get("Amount");
                            long amnt = 0;
                            if (amtObj instanceof Long) {
                                amnt = (Long) amtObj;
                            } else if (amtObj instanceof String) {
                                try {
                                    amnt = Long.parseLong((String) amtObj);
                                } catch (NumberFormatException ex) {
                                    ex.printStackTrace();
                                }
                            }
                            totalExpense += amnt;
                        }
                        expense.setText(String.valueOf(totalExpense));
                    }
                });

        // Balance listener
        db.collection("users").document(a_id)
                .addSnapshotListener((snapshot, e) -> {
                    if (e != null) return;
                    if (snapshot != null && snapshot.exists()) {
                        String bal = snapshot.get("Balance") != null ? String.valueOf(snapshot.get("Balance")) : "0";
                        balance.setText(bal);
                    }
                });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        a_id = getIntent().getStringExtra("id");
        pin = getIntent().getStringExtra("pin");

        username = findViewById(R.id.username);
        balance = findViewById(R.id.balance1);
        expense = findViewById(R.id.expensesValue);

        LinearLayout reset = findViewById(R.id.resetBtn);
        reset.setOnClickListener(v -> {
            // Fixed Dialog context (should use Activity context)
            Dialog sure = new Dialog(DashBoard.this);
            sure.setContentView(R.layout.dialogsure);
            sure.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            sure.show();

            AppCompatButton yes = sure.findViewById(R.id.YesBtn);
            AppCompatButton no = sure.findViewById(R.id.NoBtn);

            yes.setOnClickListener(v1 -> {
                // Reset values in Firestore too
                db.collection("users").document(a_id)
                        .update("Balance", 0)
                        .addOnSuccessListener(aVoid -> {
                            balance.setText("0");
                            expense.setText("0");
                            Toast.makeText(DashBoard.this, "All data reset successfully", Toast.LENGTH_SHORT).show();
                            sure.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(DashBoard.this, "Failed to reset: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });

            no.setOnClickListener(v12 -> sure.dismiss());
        });

        db.collection("users").document(a_id).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("Name");
                        username.setText("Welcome, " + name);
                        listenForBalanceAndExpenses();

                        // -------------------- Add Balance --------------------
                        LinearLayout addbalance = findViewById(R.id.addBalancell);
                        addbalance.setOnClickListener(v -> {
                            Dialog addBal = new Dialog(DashBoard.this);
                            addBal.setContentView(R.layout.addbalance);
                            addBal.getWindow().setBackgroundDrawableResource(android.R.color.transparent);      // for making the card view look proper in the dialog box
                            addBal.show();

                            AppCompatEditText am = addBal.findViewById(R.id.amntinput);
                            AppCompatEditText da = addBal.findViewById(R.id.dateinput);
                            AppCompatButton addBtn = addBal.findViewById(R.id.add);
                            AppCompatButton cancel = addBal.findViewById(R.id.Cancel);

                            cancel.setOnClickListener(v1 -> addBal.dismiss());

                            addBtn.setOnClickListener(v2 -> {
                                String balanceAmount = am.getText().toString().trim();
                                String balanceDate = da.getText().toString().trim();

                                if (balanceAmount.isEmpty() || balanceDate.isEmpty()) {
                                    Toast.makeText(DashBoard.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                long amount = Long.parseLong(balanceAmount);

                                Map<String, Object> balanceData = new HashMap<>();
                                balanceData.put("Amount", amount);
                                balanceData.put("Date", balanceDate);

                                // Step 1: Record in BalanceManagement
                                db.collection("users")
                                        .document(a_id)
                                        .collection("BalanceManagement")
                                        .add(balanceData)
                                        .addOnSuccessListener(unused -> {
                                            // Step 2: Increment total balance
                                            db.collection("users")
                                                    .document(a_id)
                                                    .update("Balance", FieldValue.increment(amount))        // for updating the amount....
                                                    .addOnSuccessListener(vx -> {
                                                        Toast.makeText(DashBoard.this, "Balance added successfully", Toast.LENGTH_SHORT).show();
                                                        addBal.dismiss();
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(DashBoard.this, "Failed to update balance: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(DashBoard.this, "Balance can't be added: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            });
                        });

                        // -------------------- Add Expense --------------------
                        LinearLayout addExpenses = findViewById(R.id.addexpnsell);
                        addExpenses.setOnClickListener(v1 -> {
                            Dialog add = new Dialog(DashBoard.this);
                            add.setContentView(R.layout.addexpense);
                            add.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                            add.show();

                            AppCompatEditText amountET = add.findViewById(R.id.amntinput);
                            AppCompatEditText dateET = add.findViewById(R.id.dateinput);
                            AppCompatEditText descET = add.findViewById(R.id.descinput);
                            AppCompatButton AddBtn = add.findViewById(R.id.add);
                            AppCompatButton Cancel = add.findViewById(R.id.Cancel);

                            Cancel.setOnClickListener(v2 -> add.dismiss());

                            AddBtn.setOnClickListener(view -> {
                                String amntStr = amountET.getText().toString().trim();
                                String date = dateET.getText().toString().trim();
                                String desc = descET.getText().toString().trim();

                                if (amntStr.isEmpty() || date.isEmpty() || desc.isEmpty()) {
                                    Toast.makeText(DashBoard.this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                long amnt = Long.parseLong(amntStr);

                                Map<String, Object> data = new HashMap<>();
                                data.put("Amount", amnt);
                                data.put("Date", date);
                                data.put("Description", desc);
                                data.put("Type", "Expense");
                                data.put("Timestamp", FieldValue.serverTimestamp());

                                // Step 1: Record expense
                                db.collection("users")
                                        .document(a_id)
                                        .collection("transactions")
                                        .add(data)
                                        .addOnSuccessListener(unused -> {
                                            // Step 2: Deduct from balance
                                            DocumentReference userRef = db.collection("users").document(a_id);
                                            userRef.update("Balance", FieldValue.increment(-amnt))  // for deducting the amount from the balance.....
                                                    .addOnSuccessListener(x -> {
                                                        Toast.makeText(DashBoard.this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                                                        add.dismiss();
                                                    })
                                                    .addOnFailureListener(e -> Toast.makeText(DashBoard.this, "Failed to deduct balance: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                        })
                                        .addOnFailureListener(e -> Toast.makeText(DashBoard.this, "Error adding expense: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                            });
                        });

                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching user: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}
