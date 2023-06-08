package com.pdetector.android.utils;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.pdetector.android.R;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

public class Tools {

    public static boolean isInternetConnected() {

        StrictMode.ThreadPolicy gfgPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(gfgPolicy);
        try {
            URL url = new URL("http://www.google.com");
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setConnectTimeout(3000);
            connection.connect();
            return connection.getResponseCode() == 200;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void showSnackbar(Context context, View view, String title, String Btntext)
    {
        Snackbar snackbar = Snackbar.make(view,"",Snackbar.LENGTH_SHORT);
        View customView = ((Activity) context).getLayoutInflater().inflate(R.layout.custom_snackbar,null);
        snackbar.getView().setBackgroundColor(Color.TRANSPARENT);
        Snackbar.SnackbarLayout snackbarLayout = (Snackbar.SnackbarLayout) snackbar.getView();
        snackbarLayout.setPadding(0,0,0,0);

        TextView titleText = customView.findViewById(R.id.custom_snackbar_text);
        Button Btn = customView.findViewById(R.id.custom_snackbar_btn);

        titleText.setText(title);
        Btn.setText(Btntext);

        Btn.setOnClickListener(v -> snackbar.dismiss());

        snackbarLayout.addView(customView,0);
        snackbar.show();
    }

    public static String getString(Context context, String key)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PRE_NAME,Context.MODE_PRIVATE);
        String value = sharedPreferences.getString(key,"");
        return value;
    }

    public static void saveString(Context context, String key, String value)
    {
        SharedPreferences sharedPreferences = context.getSharedPreferences(Constants.SHARED_PRE_NAME,Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public static void ShowNoInternetDialog(Context context)
    {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(context);
        builder.setTitle("No Internet!!")
                .setCancelable(false)
                .setNegativeButton("Okay", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
