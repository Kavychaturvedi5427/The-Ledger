package com.kavya.theledger;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.w3c.dom.Text;

import java.util.ArrayList;

public class Transac_recycler_adapter extends RecyclerView.Adapter<Transac_recycler_adapter.ViewHolder> {
    Context context;
    ArrayList<Transaction_model> transacList;

    public Transac_recycler_adapter(Context context, ArrayList<Transaction_model> datamodel) {
        this.context = context;
        this.transacList = datamodel;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView transac_detail;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            transac_detail = itemView.findViewById(R.id.amnt_txt);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(context).inflate(R.layout.transac_recycler, parent, false);
        ViewHolder viewHolder = new ViewHolder(v);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Transaction_model model = transacList.get(position);

        StringBuilder text = new StringBuilder("₹");

        // Amount (always present)
        text.append(model.getAmount()).append("\n");

        // Description (only if present)
        if (model.getDescription() != null && !model.getDescription().trim().isEmpty()) {
            text.append(model.getDescription()).append("\n");
        }

        // Date (always present)
        text.append(model.getDate());

        holder.transac_detail.setText(text.toString());
    }


    @Override
    public int getItemCount() {
        return transacList.size();
    }


}
