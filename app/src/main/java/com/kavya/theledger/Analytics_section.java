package com.kavya.theledger;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.widget.AppCompatButton;
import androidx.core.content.res.ResourcesCompat;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Analytics_section extends BottomSheetDialogFragment {

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private String uid;

    // Views
    private PieChart pie;
    private TextView c1, c2, c3, c4;
    private TextView expAmt, amtHead;
    private ImageView resetBtn;

    @Override
    public int getTheme() {
        return R.style.BottomSheetAnimation;
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {

        View view = inflater.inflate(R.layout.analytics_sheet_btm, container, false);

        // Firebase
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        if (auth.getCurrentUser() == null) return view;
        uid = auth.getCurrentUser().getUid();

        // Bind views
        pie = view.findViewById(R.id.pieChart);

        c1 = view.findViewById(R.id.cat1);
        c2 = view.findViewById(R.id.cat2);
        c3 = view.findViewById(R.id.cat3);
        c4 = view.findViewById(R.id.cat4);

        expAmt = view.findViewById(R.id.Exp_amt);
        amtHead = view.findViewById(R.id.amt_head);

        resetBtn = view.findViewById(R.id.resetBtn);

        setupPieChart();

        // 🔥 CHECK RESET FLAG FIRST
        db.collection("users")
                .document(uid)
                .get()
                .addOnSuccessListener(doc -> {
                    Boolean reset = doc.getBoolean("analyticsReset");
                    if (reset != null && reset) {
                        resetAnalyticsUI();
                    } else {
                        loadAnalytics();
                    }
                });

        // Reset button (persistent)
        resetBtn.setOnClickListener(v -> {
            db.collection("users")
                    .document(uid)
                    .update("analyticsReset", true)
                    .addOnSuccessListener(unused -> resetAnalyticsUI());
        });

        // Details button
        AppCompatButton details = view.findViewById(R.id.details);
        details.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right));
        details.setOnClickListener(v -> {
            dismiss();
            startActivity(new Intent(getContext(), Transaction_activity.class));
        });

        // Back button
        ImageView back = view.findViewById(R.id.back);
        back.setOnClickListener(v -> dismiss());

        return view;
    }

    // ---------------- PIE SETUP ----------------

    private void setupPieChart() {
        pie.setNoDataText("No data. Either you’re disciplined or forgetful.");
        pie.setNoDataTextColor(Color.GRAY);
        pie.setNoDataTextTypeface(ResourcesCompat.getFont(getContext(), R.font.lexend));

        pie.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_zoom));

        pie.setDrawHoleEnabled(true);
        pie.setHoleRadius(60f);
        pie.setTransparentCircleRadius(68f);
        pie.setDrawEntryLabels(false);
        pie.setExtraOffsets(20, 25, 20, 25);
        pie.setCenterText("Expenses");
        pie.setCenterTextSize(16f);
        pie.setCenterTextColor(Color.DKGRAY);
        pie.setRotationEnabled(true);
        pie.setHighlightPerTapEnabled(true);

        Legend legend = pie.getLegend();
        legend.setEnabled(true);
        legend.setTextSize(12f);
        legend.setForm(Legend.LegendForm.CIRCLE);
        legend.setFormSize(12f);
        legend.setHorizontalAlignment(Legend.LegendHorizontalAlignment.CENTER);
        legend.setWordWrapEnabled(true);
    }

    // ---------------- LOAD ANALYTICS ----------------

    private void loadAnalytics() {

        db.collection("users")
                .document(uid)
                .collection("transactions")
                .get()
                .addOnSuccessListener(transData -> {

                    Map<String, Float> totals = new HashMap<>();

                    for (DocumentSnapshot doc : transData) {
                        String category = doc.getString("Description");
                        Double amount = doc.getDouble("Amount");

                        if (category == null || amount == null) continue;

                        totals.put(
                                category,
                                totals.getOrDefault(category, 0f) + amount.floatValue()
                        );
                    }

                    if (totals.isEmpty()) {
                        resetAnalyticsUI();
                        return;
                    }

                    ArrayList<PieEntry> entries = new ArrayList<>();
                    float totalAmount = 0;

                    for (Map.Entry<String, Float> e : totals.entrySet()) {
                        entries.add(new PieEntry(e.getValue(), e.getKey()));
                        totalAmount += e.getValue();
                    }

                    // Sort categories
                    ArrayList<Map.Entry<String, Float>> sorted =
                            new ArrayList<>(totals.entrySet());
                    sorted.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

                    TextView[] fields = {c1, c2, c3, c4};

                    for (int i = 0; i < fields.length; i++) {
                        if (i < sorted.size()) {
                            float amt = sorted.get(i).getValue();
                            fields[i].setText(
                                    sorted.get(i).getKey() + " — " +
                                            String.format("%.1f%%", (amt / totalAmount) * 100)
                            );
                        } else {
                            fields[i].setText("—");
                        }
                    }

                    PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");
                    dataSet.setSliceSpace(3f);
                    dataSet.setSelectionShift(6f);
                    dataSet.setValueTextSize(14f);
                    dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
                    dataSet.setValueTextColor(Color.parseColor("#2E2E2E"));
                    dataSet.setColors(
                            Color.parseColor("#B38BFF"),
                            Color.parseColor("#FF8FB1"),
                            Color.parseColor("#7FA8FF"),
                            Color.parseColor("#D28BFF"),
                            Color.parseColor("#FF7FA3")
                    );

                    PieData data = new PieData(dataSet);
                    data.setValueFormatter(new PercentFormatter(pie));

                    pie.setData(data);
                    pie.setUsePercentValues(true);
                    pie.invalidate();
                    pie.animateY(1200, Easing.EaseInOutQuad);

                    expAmt.setText("You blew ₹" + totalAmount);
                    if (totalAmount > 1000) amtHead.setVisibility(View.VISIBLE);
                });
    }

    // ---------------- RESET UI ----------------

    private void resetAnalyticsUI() {
        pie.clear();
        pie.invalidate();
        pie.setCenterText("Expenses");

        c1.setText("—");
        c2.setText("—");
        c3.setText("—");
        c4.setText("—");

        expAmt.setText("₹0");
        amtHead.setVisibility(View.GONE);
    }

    @Override
    public void onStart() {
        super.onStart();
        View sheet = getDialog().findViewById(
                com.google.android.material.R.id.design_bottom_sheet
        );
        if (sheet != null) {
            sheet.setBackgroundResource(R.drawable.bottomsheet_bg);
        }
    }
}
