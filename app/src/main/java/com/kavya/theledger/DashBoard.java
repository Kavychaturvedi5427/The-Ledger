package com.kavya.theledger;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatEditText;

import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class DashBoard extends AppCompatActivity {

    TextView username, balance, expense;
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();
    String uid;

    private TextInputLayout aiTextInputLayout;
    private TextInputEditText aiUserInput;
    private LinearLayout aiMessageContainer;
    private ScrollView aiScrollView;
    private TextView aiPlaceholder, advicetxt;
    private OkHttpClient client = new OkHttpClient();

    private FirebaseRemoteConfig firebaseRemoteConfig;
    private String geminiApiKey = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dashboard);

        username = findViewById(R.id.username);
        balance = findViewById(R.id.balance1);
        expense = findViewById(R.id.expensesValue);

        aiTextInputLayout = findViewById(R.id.aiTextInputLayout);
        aiUserInput = findViewById(R.id.aiUserInput);
        aiMessageContainer = findViewById(R.id.aiMessageContainer);
        aiScrollView = findViewById(R.id.aiScrollView);
        aiPlaceholder = findViewById(R.id.aiPlaceholder);
        advicetxt = findViewById(R.id.advicetxt);

        // Animations
        Animation fadeZoom = AnimationUtils.loadAnimation(this, R.anim.fade_zoom);
        Animation fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in);
        Animation fadeInSlow = AnimationUtils.loadAnimation(this, R.anim.fade_in_slow);

        username.startAnimation(fadeZoom);
        balance.startAnimation(fadeIn);
        expense.startAnimation(fadeInSlow);

        if (auth.getCurrentUser() != null) {
            uid = auth.getCurrentUser().getUid();
        } else {
            Toast.makeText(this, "No authenticated user found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        aiTextInputLayout.setEnabled(false);
        aiTextInputLayout.setHint("Please wait for AI to initialize...");

        //  Remote Config for Gemini API Key
        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        FirebaseRemoteConfigSettings configSettings =
                new FirebaseRemoteConfigSettings.Builder()
                        .setMinimumFetchIntervalInSeconds(3600)
                        .build();
        firebaseRemoteConfig.setConfigSettingsAsync(configSettings);

        Map<String, Object> defaults = new HashMap<>();
        defaults.put("gemini_api_key", "YOUR_FALLBACK_GEMINI_KEY");
        firebaseRemoteConfig.setDefaultsAsync(defaults);

        firebaseRemoteConfig.fetchAndActivate()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        geminiApiKey = firebaseRemoteConfig.getString("gemini_api_key");
                        if (geminiApiKey != null && !geminiApiKey.isEmpty() && !geminiApiKey.equals("YOUR_FALLBACK_GEMINI_KEY")) {
                            Toast.makeText(this, "AI Ready ✅", Toast.LENGTH_SHORT).show();
                            aiTextInputLayout.setEnabled(true);
                            aiTextInputLayout.setHint("Ask something...");
                            aiScrollView.startAnimation(fadeInSlow);
                        } else {
                            Toast.makeText(this, "⚠️ Gemini API key missing or invalid", Toast.LENGTH_SHORT).show();
                            aiTextInputLayout.setHint("AI Key Missing");
                        }
                    } else {
                        Toast.makeText(this, "Failed to load Remote Config ❌", Toast.LENGTH_SHORT).show();
                        aiTextInputLayout.setHint("Remote Config Failed");
                    }
                });

        //  Reset
        LinearLayout reset = findViewById(R.id.resetBtn);
        reset.setOnClickListener(v -> {
            v.startAnimation(fadeIn);
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
                            balance.setText("₹0");
                            expense.setText("₹0");
                            Toast.makeText(DashBoard.this, "All data reset successfully", Toast.LENGTH_SHORT).show();
                            sure.dismiss();
                        })
                        .addOnFailureListener(e -> Toast.makeText(DashBoard.this, "Failed to reset: " + e.getMessage(), Toast.LENGTH_SHORT).show());
            });

            no.setOnClickListener(v12 -> sure.dismiss());
        });

        //  Load user
        db.collection("users").document(uid).get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        String name = documentSnapshot.getString("Name");
                        username.setText("Welcome, " + name);
                        listenForBalanceAndExpenses();

                        findViewById(R.id.addBalancell).setOnClickListener(v -> {
                            v.startAnimation(fadeIn);
                            openAddBalanceDialog();
                        });
                        findViewById(R.id.addexpnsell).setOnClickListener(v1 -> {
                            v1.startAnimation(fadeIn);
                            openAddExpenseDialog();
                        });
                    }
                });

        //  Navbar
        LinearLayout homeBTN = findViewById(R.id.homeBtnLayout);
        LinearLayout transacBtn = findViewById(R.id.transacBtnLayout);
        LinearLayout profileBTN = findViewById(R.id.profBtnLayout);
        LinearLayout analyBTN = findViewById(R.id.analyBtnLayout);

        homeBTN.setOnClickListener(v -> Toast.makeText(this, "Already on Home 🏠", Toast.LENGTH_SHORT).show());
        transacBtn.setOnClickListener(v -> {
            startActivity(new Intent(this, Transaction_activity.class));
            overridePendingTransition(R.anim.fade_in_slow, R.anim.fade_out_slow);
        });
        profileBTN.setOnClickListener(v -> {
            Profile_nav profileSheet = new Profile_nav();
            profileSheet.show(getSupportFragmentManager(), "profile_nav");
        });

        analyBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Analytics_section analy = new Analytics_section();
                analy.show(getSupportFragmentManager(), "analytics_section");
            }
        });

        addNavClickEffect(homeBTN);
        addNavClickEffect(transacBtn);
        addNavClickEffect(profileBTN);
        addNavClickEffect(analyBTN);

        // Scroll navbar animation
        ScrollView scrollView = findViewById(R.id.mainScroll);
        View navCard = findViewById(R.id.navigationCard);
        scrollView.getViewTreeObserver().addOnScrollChangedListener(() -> {
            float y = scrollView.getScrollY();
            navCard.animate().translationY(y > 50 ? 10 : 0).setDuration(250).start();
        });

        //  AI Send listener
        aiTextInputLayout.setEndIconOnClickListener(v -> {
            String userMsg = aiUserInput.getText().toString().trim();
            if (userMsg.isEmpty()) return;

            if (!aiTextInputLayout.isEnabled()) {
                Toast.makeText(this, "AI is not ready yet. Please wait.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (aiPlaceholder.getVisibility() == View.VISIBLE)
                aiPlaceholder.setVisibility(View.GONE);

            advicetxt.setVisibility(View.GONE);
            addMessage("You: " + userMsg, true);
            aiUserInput.setText("");
            addTypingIndicator();
            sendToGemini(userMsg);
        });
    }

    //  Listen for balance/expense changes
    private void listenForBalanceAndExpenses() {
        db.collection("users").document(uid).get().addOnSuccessListener(userDoc -> {
            long resetTimestamp = 0;
            if (userDoc.exists() && userDoc.getTimestamp("ResetTimestamp") != null)
                resetTimestamp = userDoc.getTimestamp("ResetTimestamp").toDate().getTime();
            final long finalResetTimestamp = resetTimestamp;

            db.collection("users").document(uid)
                    .collection("transactions")
                    .addSnapshotListener((snap, e) -> {
                        if (e != null || snap == null) return;
                        long totalExpense = 0;
                        for (QueryDocumentSnapshot doc : snap) {
                            Object amtObj = doc.get("Amount");
                            long amnt = (amtObj instanceof Long)
                                    ? (Long) amtObj
                                    : Long.parseLong(String.valueOf(amtObj));
                            if (doc.getTimestamp("Timestamp") != null &&
                                    doc.getTimestamp("Timestamp").toDate().getTime() > finalResetTimestamp)
                                totalExpense += amnt;
                        }
                        expense.setText("₹" + String.valueOf(totalExpense));
                    });

            db.collection("users").document(uid)
                    .addSnapshotListener((snapshot, e) -> {
                        if (snapshot != null && snapshot.exists()) {
                            String bal = snapshot.get("Balance") != null ? String.valueOf(snapshot.get("Balance")) : "0";
                            balance.setText("₹" +bal);
                        }
                    });
        });
    }

    //  Add chat message
    private void addMessage(String text, boolean isUser) {
        TextView msgView = new TextView(this);
        msgView.setText(text);
        msgView.setTextSize(14);
        msgView.setTextColor(getResources().getColor(R.color.black));
        msgView.setPadding(16, 8, 16, 8);
        msgView.setBackgroundResource(isUser ? R.drawable.user_bubble : R.drawable.ai_bubble);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.gravity = isUser ? Gravity.END : Gravity.START;
        params.setMargins(10, 10, 10, 10);
        msgView.setLayoutParams(params);

        int animRes = isUser ? R.anim.slide_in_right : R.anim.slide_in_left;
        msgView.startAnimation(AnimationUtils.loadAnimation(this, animRes));

        aiMessageContainer.addView(msgView);
        scrollToBottom();
    }

    private void addTypingIndicator() {
        TextView typing = new TextView(this);
        typing.setText("AI is typing...");
        typing.setTextColor(getResources().getColor(R.color.gray));
        typing.setPadding(16, 4, 16, 4);
        typing.setGravity(Gravity.START);
        typing.setId(View.generateViewId());
        aiMessageContainer.addView(typing);
        scrollToBottom();
    }

    private void removeTypingIndicator() {
        runOnUiThread(() -> {
            int count = aiMessageContainer.getChildCount();
            if (count > 0) {
                View last = aiMessageContainer.getChildAt(count - 1);
                if (last instanceof TextView && ((TextView) last).getText().toString().contains("typing")) {
                    aiMessageContainer.removeView(last);
                }
            }
        });
    }

    private void scrollToBottom() {
        aiScrollView.post(() -> aiScrollView.smoothScrollTo(0, aiMessageContainer.getBottom()));
    }

    // 🔹 Send to Gemini
    private void sendToGemini(String userInput) {
        if (geminiApiKey.isEmpty()) {
            addMessage("AI key not loaded yet. Please wait ⏳", false);
            return;
        }

        try {
            JSONObject textPart = new JSONObject();
            textPart.put("text", userInput);

            JSONObject content = new JSONObject();
            content.put("parts", new JSONArray().put(textPart));

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("contents", new JSONArray().put(content));

            RequestBody body = RequestBody.create(
                    jsonBody.toString(),
                    MediaType.parse("application/json; charset=utf-8")
            );

            Request request = new Request.Builder()
                    .url("https://generativelanguage.googleapis.com/v1/models/gemini-2.5-flash:generateContent?key=" + geminiApiKey)
                    .post(body)
                    .build();

            client.newCall(request).enqueue(new Callback() {
                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    String responseBody = response.body().string();
                    Log.e("GeminiResponse", "HTTP " + response.code() + ": " + responseBody);

                    runOnUiThread(() -> {
                        try {
                            JSONObject json = new JSONObject(responseBody);

                            if (json.has("error")) {
                                String errorMsg = json.getJSONObject("error").optString("message", "Unknown API Error");
                                removeTypingIndicator();
                                addMessage("AI: API Error: " + errorMsg, false);
                                scrollToBottom();
                                return;
                            }

                            JSONArray candidates = json.optJSONArray("candidates");
                            if (candidates == null || candidates.length() == 0) {
                                removeTypingIndicator();
                                addMessage("AI: No response from model 😕", false);
                                return;
                            }

                            String reply = candidates.getJSONObject(0)
                                    .getJSONObject("content")
                                    .getJSONArray("parts")
                                    .getJSONObject(0)
                                    .optString("text", "(Empty)");

                            removeTypingIndicator();
                            addMessage("AI: " + reply.trim(), false);
                            scrollToBottom();

                        } catch (JSONException e) {
                            removeTypingIndicator();
                            addMessage("AI: Couldn't parse response.", false);
                        }
                    });
                }

                @Override
                public void onFailure(Call call, IOException e) {
                    runOnUiThread(() -> {
                        removeTypingIndicator();

                        // Add timeout as AI message
                        String timeoutMessage = "AI: Timeout. Snitch to the dev in Suggestions.";
                        addMessage(timeoutMessage, false);

                        // Smooth fade-in for realism
                        aiMessageContainer.getChildAt(aiMessageContainer.getChildCount() - 1)
                                .startAnimation(AnimationUtils.loadAnimation(DashBoard.this, R.anim.fade_in));

                        scrollToBottom();
                    });
                }

            });

        } catch (JSONException e) {
            e.printStackTrace();
            removeTypingIndicator();
            addMessage("AI: Internal app error during message preparation.", false);
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private void addNavClickEffect(LinearLayout layout) {
        layout.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    v.animate().scaleX(0.93f).scaleY(0.93f).setDuration(100).start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    v.animate().scaleX(1f).scaleY(1f).setDuration(100).start();
                    break;
            }
            return false;
        });
    }

    //  Add Balance Dialog
    private void openAddBalanceDialog() {
        Dialog addBal = new Dialog(this);
        addBal.setContentView(R.layout.addbalance);
        addBal.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Window window = addBal.getWindow();
        if (window != null)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        addBal.show();

        addBal.findViewById(R.id.maincard).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

        AppCompatEditText am = addBal.findViewById(R.id.amntinput);
        AppCompatEditText da = addBal.findViewById(R.id.dateinput);
        AppCompatButton addBtn = addBal.findViewById(R.id.add);
        AppCompatButton cancel = addBal.findViewById(R.id.Cancel);

        cancel.setOnClickListener(v1 -> addBal.dismiss());

        addBtn.setOnClickListener(v2 -> {
            String balanceAmount = am.getText().toString().trim();
            String balanceDate = da.getText().toString().trim();
            if (balanceAmount.isEmpty() || balanceDate.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            long amount = Long.parseLong(balanceAmount);
            Map<String, Object> balanceData = new HashMap<>();
            balanceData.put("Amount", amount);
            balanceData.put("Date", balanceDate);

            db.collection("users").document(uid)
                    .collection("BalanceManagement")
                    .get()
                    .addOnSuccessListener(q -> {
                        int next = q.size() + 1;
                        String balanceId = "Balance" + next;
                        db.collection("users").document(uid)
                                .collection("BalanceManagement")
                                .document(balanceId)
                                .set(balanceData)
                                .addOnSuccessListener(unused -> {
                                    db.collection("users").document(uid)
                                            .update("Balance", FieldValue.increment(amount))
                                            .addOnSuccessListener(vx -> {
                                                Toast.makeText(this, "Balance added successfully", Toast.LENGTH_SHORT).show();
                                                listenForBalanceAndExpenses();
                                                addBal.dismiss();
                                            });
                                });
                    });
        });
    }

    //  Add Expense Dialog
    private void openAddExpenseDialog() {
        Dialog add = new Dialog(this);
        add.setContentView(R.layout.addexpense);
        add.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        Window window = add.getWindow();
        if (window != null)
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        add.show();

        add.findViewById(R.id.maincard).startAnimation(AnimationUtils.loadAnimation(this, R.anim.fade_in));

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
                Toast.makeText(this, "Please fill all the fields", Toast.LENGTH_SHORT).show();
                return;
            }

            long amnt = Long.parseLong(amntStr);
            Map<String, Object> data = new HashMap<>();
            data.put("Amount", amnt);
            data.put("Date", date);
            data.put("Description", desc);
            data.put("Type", "Expense");
            data.put("Timestamp", FieldValue.serverTimestamp());

            db.collection("users").document(uid)
                    .collection("transactions")
                    .get()
                    .addOnSuccessListener(unused -> {
                        int next = unused.size() + 1;
                        String id = "Transaction" + next;
                        db.collection("users").document(uid)
                                .collection("transactions")
                                .document(id)
                                .set(data)
                                .addOnSuccessListener(query -> {
                                    db.collection("users").document(uid)
                                            .update("Expense", FieldValue.increment(amnt))
                                            .addOnSuccessListener(que -> {
                                                db.collection("users").document(uid)
                                                        .update("Balance", FieldValue.increment(-amnt))
                                                        .addOnSuccessListener(x -> {
                                                            Toast.makeText(this, "Expense added successfully", Toast.LENGTH_SHORT).show();
                                                            listenForBalanceAndExpenses();
                                                            add.dismiss();
                                                        });
                                            });
                                });
                    });
        });
    }
}
