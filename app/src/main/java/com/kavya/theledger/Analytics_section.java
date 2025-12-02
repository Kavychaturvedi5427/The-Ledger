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
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Analytics_section extends BottomSheetDialogFragment {

    FirebaseFirestore db = FirebaseFirestore.getInstance();
    FirebaseAuth auth = FirebaseAuth.getInstance();

    FirebaseUser user = auth.getCurrentUser();
    String uid;

    @Override
    public int getTheme() {
        return R.style.BottomSheetAnimation;  // <-- animation theme
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.analytics_sheet_btm, container, false);

        uid = auth.getCurrentUser().getUid();

        PieChart pie = view.findViewById(R.id.pieChart);
        pie.setNoDataText("No data. Either you’re disciplined or forgetful.");
        pie.setNoDataTextColor(Color.GRAY);
        pie.setNoDataTextTypeface(ResourcesCompat.getFont(getContext(), R.font.lexend));

        // ---- ANIMATIONS ADDED ----
        pie.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_zoom));

        db.collection("users").document(uid).collection("transactions")
                .get().addOnSuccessListener(transData -> {

                    Map<String, Float> categoryTotal = new HashMap<>();

                    for (DocumentSnapshot doc : transData) {

                        String category = doc.getString("Description");
                        Double amount = doc.getDouble("Amount");

                        if (category == null || amount == null) continue;

                        category = category.trim();

                        if (categoryTotal.containsKey(category)) {
                            categoryTotal.put(category,
                                    categoryTotal.get(category) + amount.floatValue());
                        } else {
                            categoryTotal.put(category, amount.floatValue());
                        }
                    }

                    ArrayList<PieEntry> entries = new ArrayList<>();
                    for (String cat : categoryTotal.keySet()) {
                        entries.add(new PieEntry(categoryTotal.get(cat), cat));

                        // ---- Percentage Breakdown ----
                        TextView c1 = view.findViewById(R.id.cat1);
                        TextView c2 = view.findViewById(R.id.cat2);
                        TextView c3 = view.findViewById(R.id.cat3);
                        TextView c4 = view.findViewById(R.id.cat4);

                        double totalAmount = 0;
                        for (float val : categoryTotal.values()) {
                            totalAmount += val;
                        }

                        ArrayList<Map.Entry<String, Float>> sortedList =
                                new ArrayList<>(categoryTotal.entrySet());
                        sortedList.sort((a, b) -> Float.compare(b.getValue(), a.getValue()));

                        java.util.function.BiFunction<Float, Double, String> percentText = (amt, total) ->
                                total == 0 ? "0%" : String.format("%.1f%%", (amt / total) * 100);

                        TextView[] fields = {c1, c2, c3, c4};

                        for (int i = 0; i < fields.length; i++) {
                            if (i < sortedList.size()) {
                                String cate = sortedList.get(i).getKey();
                                float amt = sortedList.get(i).getValue();
                                fields[i].setText(cate + " — " + percentText.apply(amt, totalAmount));
                            } else {
                                fields[i].setText("—");
                            }
                        }
                    }

                    PieDataSet dataSet = new PieDataSet(entries, "Expense Categories");

                    dataSet.setSliceSpace(3f);
                    dataSet.setSelectionShift(6f);

                    dataSet.setValueTextSize(14f);
                    dataSet.setValueTypeface(Typeface.DEFAULT_BOLD);
                    dataSet.setValueTextColor(Color.parseColor("#2E2E2E"));

                    dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
                    dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);

                    dataSet.setValueLinePart1Length(0.85f);
                    dataSet.setValueLinePart2Length(0.65f);
                    dataSet.setValueLinePart1OffsetPercentage(90f);
                    dataSet.setValueLineColor(Color.DKGRAY);

                    ArrayList<Integer> colors = new ArrayList<>();
                    colors.add(Color.parseColor("#B38BFF"));
                    colors.add(Color.parseColor("#FF8FB1"));
                    colors.add(Color.parseColor("#7FA8FF"));
                    colors.add(Color.parseColor("#D28BFF"));
                    colors.add(Color.parseColor("#FF7FA3"));
                    colors.add(Color.parseColor("#6FA8FF"));
                    dataSet.setColors(colors);

                    PieData data = new PieData(dataSet);
                    data.setValueFormatter(new PercentFormatter(pie));

                    pie.setData(data);
                    pie.setUsePercentValues(true);
                    pie.getDescription().setEnabled(false);
                    pie.invalidate();
                    pie.animateY(1200, Easing.EaseInOutQuad);
                });

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

        // ---- Spending Amount ----
        TextView exp_amt = view.findViewById(R.id.Exp_amt);
        TextView amt_head = view.findViewById(R.id.amt_head);

        exp_amt.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.fade_in));

        db.collection("users").document(uid).collection("transactions").get()
                .addOnSuccessListener(ExpenseFetch -> {

                    double amount = 0;
                    for (DocumentSnapshot doc : ExpenseFetch) {
                        double trans_amt = doc.getDouble("Amount");
                        amount += trans_amt;
                    }

                    String formatted = "You blew ₹" + amount + ".";
                    exp_amt.setText(formatted);

                    if (amount > 1000) {
                        amt_head.setVisibility(View.VISIBLE);
                    }
                });

        // ---- Percentage Breakdown Card Animation ----
        View percentCard = view.findViewById(R.id.percent_breakdown);
        percentCard.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_left));

        // ---- Detailed Transactions Button ----
        AppCompatButton detailed_Transac = view.findViewById(R.id.details);
        detailed_Transac.startAnimation(AnimationUtils.loadAnimation(getContext(), R.anim.slide_in_right));

        detailed_Transac.setOnClickListener(v -> {
            dismiss();
            Intent intent = new Intent(getContext(), Transaction_activity.class);
            startActivity(intent);
        });

//        ----- back btn -----
        ImageView back = view.findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }
    @Override
    public void onStart() {
        super.onStart();

        View bottomSheet = getDialog().findViewById(
                com.google.android.material.R.id.design_bottom_sheet
        );

        if (bottomSheet != null) {
            bottomSheet.setBackgroundResource(R.drawable.bottomsheet_bg);
        }
    }

}
