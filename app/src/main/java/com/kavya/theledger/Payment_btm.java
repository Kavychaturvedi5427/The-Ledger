package com.kavya.theledger;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.appcompat.widget.AppCompatButton;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.card.MaterialCardView;
import com.google.android.play.core.integrity.IntegrityTokenRequest;

import java.util.zip.Inflater;

import javax.annotation.Nullable;

public class Payment_btm extends BottomSheetDialogFragment {

    MaterialCardView gpay, phonepe, paytm, bhim;
    AppCompatButton chooseBtn;

    @Override
    @Nullable
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle SavedInstanceState) {
        View view = inflater.inflate(R.layout.payment_btm, container, false);

        // binding the variables with the viewgroups
        gpay = view.findViewById(R.id.gpaycard);
        phonepe = view.findViewById(R.id.phonepecard);
        paytm = view.findViewById(R.id.paytmcard);
        bhim = view.findViewById(R.id.bhimcard);
        chooseBtn = view.findViewById(R.id.chooseBtn);

        gpay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openApp("com.google.android.apps.nbu.paisa.user", "GPay");
            }
        });

        phonepe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openApp("com.phonepe.app", "PhonePe");
            }
        });

        paytm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openApp("net.one97.paytm", "Paytm");
            }
        });

        bhim.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openApp("in.org.npci.upiapp", "BHIM");
            }
        });

        chooseBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openchooser();
            }
        });

        return view;
    }

    private void openchooser() {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse("upi://pay"));
        startActivity(Intent.createChooser(intent,"Pay Using"));
    }

    private void openApp(String packagename, String Appname) {
        PackageManager pm = requireContext().getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packagename);
        if (intent != null) {
            startActivity(intent);
            dismiss();
        } else {
            Toast.makeText(requireContext(), Appname + " is not installed", Toast.LENGTH_SHORT).show();

            try {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=" + packagename)));
            } catch (Exception e) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=" + packagename)));
            }
        }
    }


}
