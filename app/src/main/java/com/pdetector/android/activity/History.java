package com.pdetector.android.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdetector.android.R;
import com.pdetector.android.utils.HistoryAdapter;
import com.pdetector.android.models.Pothole;
import com.pdetector.android.utils.Tools;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class History extends AppCompatActivity {

    private DatabaseReference PotholeDatabase = FirebaseDatabase.getInstance().getReference("Pothole");
    private ChildEventListener childEventListener;
    private List<Pothole> potholeList = new ArrayList<>();
    private RecyclerView recyclerView;
    private LinearLayoutManager linearLayoutManager;
    private HistoryAdapter historyAdapter;
    private ImageView BackBtn;
    private LinearLayout MainLayout;
    private TextView ClearAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerView = findViewById(R.id.history_recyclerview);
        BackBtn = findViewById(R.id.history_toolbar_back);
        MainLayout = findViewById(R.id.history_main_layout);
        ClearAll = findViewById(R.id.history_clear_all);

        linearLayoutManager = new LinearLayoutManager(this,LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setHasFixedSize(true);

        if (!Tools.isInternetConnected())
            Tools.ShowNoInternetDialog(this);

        PotholeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    potholeList = new ArrayList<>();

                    for (DataSnapshot snapshot1 : snapshot.getChildren())
                    {
                        Pothole pothole = snapshot1.getValue(Pothole.class);
                        String androidID = android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        if (androidID.equals(pothole.getDeviceid()))
                            potholeList.add(pothole);
                    }
                    Collections.reverse(potholeList);
                    historyAdapter = new HistoryAdapter(History.this,potholeList);
                    recyclerView.setAdapter(historyAdapter);
                    historyAdapter.notifyDataSetChanged();
                    if (potholeList.size()>0)
                    {
                        ClearAll.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        Tools.showSnackbar(History.this, MainLayout, "No History Found..", "Okay");
                        ClearAll.setVisibility(View.GONE);
                    }
                }
                else {
                    Tools.showSnackbar(History.this, MainLayout, "No History Found..", "Okay");
                    ClearAll.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        ClearAll.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ShowDialog();
            }
        });



        BackBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
            }
        });
    }

    private void ShowDialog(){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle("Do you want to Delete?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PotholeDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.exists())
                                {
                                    potholeList = new ArrayList<>();

                                    for (DataSnapshot snapshot1 : snapshot.getChildren())
                                    {
                                        Pothole pothole = snapshot1.getValue(Pothole.class);
                                        String androidID = android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                                        if (androidID.equals(pothole.getDeviceid()))
                                            PotholeDatabase.child(pothole.getId()).setValue(null);
                                    }
                                    Tools.showSnackbar(History.this, MainLayout, "Deleted Successfully..", "Okay");
                                    ClearAll.setVisibility(View.GONE);
                                }
                                else {
                                    Tools.showSnackbar(History.this, MainLayout, "No History Found..", "Okay");
                                    ClearAll.setVisibility(View.GONE);
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}