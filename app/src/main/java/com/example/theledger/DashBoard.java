package com.example.theledger;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.HashMap;
import java.util.Map;

public class DashBoard extends AppCompatActivity {
    TextView username;
    TextView balance;
    TextView expense;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    String uid;

    // Listen to changes in balance and expense
    private void listenForBalanceAndExpenses() {
        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            long resetTimestamp = 0;
            if (userDoc.exists() && userDoc.getTimestamp("ResetTimestamp") != null) {
                resetTimestamp = userDoc.getTimestamp("ResetTimestamp").toDate().getTime();
            }

            final long finalResetTimestamp = resetTimestamp;

            // Expense listener
            db.collection("users").document(uid)
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

                                // Only count expenses after reset
                                if (doc.getTimestamp("Timestamp") != null &&
                                        doc.getTimestamp("Timestamp").toDate().getTime() > finalResetTimestamp) {
                                    totalExpense += amnt;
                                }
                            }
                            expense.setText(String.valueOf(totalExpense));
                        }
                    });

            // Balance listener
            db.collection("users").document(uid)
                    .addSnapshotListener((snapshot, e) -> {
                        if (e != null) return;
                        if (snapshot != null && snapshot.exists()) {
                            String bal = snapshot.get("Balance") != null ? String.valueOf(snapshot.get("Balance")) : "0";
                            balance.setText(bal);
                        }
                    });
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        username = findViewById(R.id.username);
        balance = findViewById(R.id.balance1);
        expense = findViewById(R.id.expensesValue);

        if (auth.getCurrentUser() != null) {
            uid = auth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        LinearLayout reset = findViewById(R.id.resetBtn);
        reset.setOnClickListener(v -> {
            Dialog sure = new Dialog(DashBoard.this);
            sure.setContentView(R.layout.dialogsure);
            sure.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            sure.show();

            AppCompatButton yes = sure.findViewById(R.id.YesBtn);
            AppCompatButton no = sure.findViewById(R.id.NoBtn);

            yes.setOnClickListener(v1 -> {
                db.collection("users").document(uid)
                        .update("Balance", 0, "Expense", 0, "ResetTimestamp", FieldValue.serverTimestamp())
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

        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("Name");
                        username.setText("Welcome, " + name);
                        listenForBalanceAndExpenses();

                        // -------------------- Add Balance --------------------
                        LinearLayout addbalance = findViewById(R.id.addBalancell);
                        addbalance.setOnClickListener(v -> openAddBalanceDialog());

                        // -------------------- Add Expense --------------------
                        LinearLayout addExpenses = findViewById(R.id.addexpnsell);
                        addExpenses.setOnClickListener(v1 -> openAddExpenseDialog());

                    } else {
                        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "Error fetching user: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        // Navigation buttons
        LinearLayout homeBTN = findViewById(R.id.homeBtnLayout);
        homeBTN.setOnClickListener(v -> {
            Intent home = new Intent(getApplicationContext(), DashBoard.class);
            startActivity(home);
            finish();
        });

        LinearLayout profileBTN = findViewById(R.id.profBtnLayout);
        profileBTN.setOnClickListener(v -> {
            Profile_nav profileSheet = new Profile_nav();
            profileSheet.show(getSupportFragmentManager(), "profile_nav");

        });
    }

    // Dialog for adding balance
    private void openAddBalanceDialog() {
        Dialog addBal = new Dialog(DashBoard.this);
        addBal.setContentView(R.layout.addbalance);
        addBal.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Window window = addBal.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
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

            // Store the record with document id as Balance1, Balance2...
            db.collection("users").document(uid)
                    .collection("BalanceManagement")
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        int next = queryDocumentSnapshots.size() + 1;
                        String balanceId = "Balance" + next;
                        db.collection("users").document(uid)
                                .collection("BalanceManagement")
                                .document(balanceId)
                                .set(balanceData)
                                .addOnSuccessListener(unused -> {
                                    db.collection("users").document(uid)
                                            .update("Balance", FieldValue.increment(amount))
                                            .addOnSuccessListener(vx -> {
                                                Toast.makeText(DashBoard.this, "Balance added successfully", Toast.LENGTH_SHORT).show();
                                                listenForBalanceAndExpenses();
                                                addBal.dismiss();
                                            })
                                            .addOnFailureListener(e -> Toast.makeText(DashBoard.this, "Failed to update balance: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                })
                                .addOnFailureListener(e -> Toast.makeText(DashBoard.this, "Failed to add balance: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                    });
        });
    }

    // Dialog for adding expense
    private void openAddExpenseDialog() {
        Dialog add = new Dialog(DashBoard.this);
        add.setContentView(R.layout.addexpense);
        add.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Window window = add.getWindow();
        if (window != null) {
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        }
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

            // Record expense
            db.collection("users").document(uid)
                    .collection("transactions")
                    .get()
                    .addOnSuccessListener(unused -> {
                        int transac_next = unused.size() + 1;
                        String transac_id = "Transaction" + transac_next;
                        db.collection("users").document(uid)
                                .collection("transactions")
                                .document(transac_id)
                                .set(data)
                                .addOnSuccessListener(query -> {
                                    db.collection("users").document(uid)
                                            .update("Expense", FieldValue.increment(amnt))
                                            .addOnSuccessListener(que -> {
                                                db.collection("users").document(uid)
                                                        .update("Balance", FieldValue.increment(-amnt))
                                                        .addOnSuccessListener(x -> {
                                                            Toast.makeText(DashBoard.this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                                                            listenForBalanceAndExpenses();
                                                            add.dismiss();
                                                        })
                                                        .addOnFailureListener(e -> Toast.makeText(DashBoard.this, "Failed to deduct balance: " + e.getMessage(), Toast.LENGTH_SHORT).show());
                                            })
                                            .addOnFailureListener(ques -> Toast.makeText(DashBoard.this, "Failed to update expense: " + ques.getMessage(), Toast.LENGTH_SHORT).show());
                                });
                    })
                    .addOnFailureListener(e -> Toast.makeText(DashBoard.this, "Error adding expense: " + e.getMessage(), Toast.LENGTH_SHORT).show());
        });


    }
}
