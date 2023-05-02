package com.pdetector.android.utils;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.FirebaseDatabase;
import com.pdetector.android.R;
import com.pdetector.android.activity.History;
import com.pdetector.android.models.Pothole;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private Context context;
    private List<Pothole> potholeList;

    public HistoryAdapter(Context context, List<Pothole> potholeList) {
        this.context = context;
        this.potholeList = potholeList;
    }

    @NonNull
    @Override
    public HistoryAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history,parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryAdapter.ViewHolder holder, int position) {
        Pothole pothole = potholeList.get(position);
        if (potholeList.size()>0)
        {
            Calendar cal = Calendar.getInstance(Locale.ENGLISH);
            cal.setTimeInMillis(pothole.getTimeStamp());
            String date = DateFormat.format("dd MMM yyyy - hh:mm a", cal).toString();
            holder.DateTimeText.setText("Time : "+date);
            holder.AddressText.setText("Address : "+pothole.getAddress());
            holder.LatText.setText("Latitude : "+pothole.getLat());
            holder.LonText.setText("Longitude : "+pothole.getLon());
            holder.IntensityText.setText("Intensity : "+pothole.getIntensity());
            holder.StatusText.setText(pothole.getStatus()+"\nPothole");

            holder.itemView.setOnClickListener(v -> {
                String uri = String.format(Locale.ENGLISH, ""+pothole.getGlink());
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                context.startActivity(intent);
            });


            holder.itemView.setOnLongClickListener(v -> {
                ShowDialog(pothole.getId());
                return true;
            });
        }
    }

    private void ShowDialog(String id){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle("Do you want to Delete?")
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FirebaseDatabase.getInstance().getReference("Pothole")
                                .child(id).setValue(null).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void unused) {
                                        Toast.makeText(context, "Deleted Successfully!!", Toast.LENGTH_SHORT).show();
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

    @Override
    public int getItemCount() {
        return potholeList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder{

        private TextView DateTimeText, AddressText, LatText, LonText, IntensityText, StatusText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            DateTimeText = itemView.findViewById(R.id.item_history_date_time);
            AddressText = itemView.findViewById(R.id.item_history_address);
            LatText = itemView.findViewById(R.id.item_history_lat);
            LonText = itemView.findViewById(R.id.item_history_lon);
            IntensityText = itemView.findViewById(R.id.item_history_intensity);
            StatusText = itemView.findViewById(R.id.item_history_status);
        }
    }
}
