package com.pdetector.android.activity;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.text.format.DateFormat;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdetector.android.R;
import com.pdetector.android.models.Pothole;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class Splash extends AppCompatActivity {

    private DatabaseReference PotholeDatabase = FirebaseDatabase.getInstance().getReference("Pothole");
    private List<Pothole> potholeList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        PotholeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    potholeList = new ArrayList<>();

                    File filesDir = getFilesDir();
                    File downDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    // Create a new Excel workbook
                    XSSFWorkbook workbook = new XSSFWorkbook();
                    XSSFWorkbook workbook2 = new XSSFWorkbook();

                    // Create a new sheet
                    XSSFSheet sheet = workbook.createSheet("AllPothole");
                    XSSFSheet sheet2 = workbook2.createSheet("Pothole");


                    Row row = sheet.createRow(0);
                    Row row2 = sheet2.createRow(0);


                    row.createCell(0).setCellValue("Date");
                    row.createCell(1).setCellValue("Time");
                    row.createCell(2).setCellValue("Address");
                    row.createCell(3).setCellValue("Type of Pothole");
                    row.createCell(4).setCellValue("Latitude");
                    row.createCell(5).setCellValue("Longitude");
                    row.createCell(6).setCellValue("Intensity");
                    row.createCell(7).setCellValue("Link");

                    row2.createCell(0).setCellValue("Date");
                    row2.createCell(1).setCellValue("Time");
                    row2.createCell(2).setCellValue("Address");
                    row2.createCell(3).setCellValue("Type of Pothole");
                    row2.createCell(4).setCellValue("Latitude");
                    row2.createCell(5).setCellValue("Longitude");
                    row2.createCell(6).setCellValue("Intensity");
                    row2.createCell(7).setCellValue("Link");

                    int rownum = 1;
                    int rownum2 = 1;
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        // Retrieve data from the snapshot and write it to the sheet
                        Pothole pothole = dataSnapshot.getValue(Pothole.class);
                        row = sheet.createRow(rownum++);

                        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                        cal.setTimeInMillis(pothole.getTimeStamp());
                        String date = DateFormat.format("dd MMM yyyy", cal).toString();
                        String time = DateFormat.format("hh:mm a", cal).toString();

                        row.createCell(0).setCellValue(date);
                        row.createCell(1).setCellValue(time);
                        row.createCell(2).setCellValue(pothole.getAddress());
                        row.createCell(3).setCellValue(pothole.getStatus());
                        row.createCell(4).setCellValue(pothole.getLat());
                        row.createCell(5).setCellValue(pothole.getLon());
                        row.createCell(6).setCellValue(pothole.getIntensity());
                        row.createCell(7).setCellValue(pothole.getGlink());

                        String androidID = android.provider.Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
                        if (androidID.equals(pothole.getDeviceid()))
                        {
                            row2 = sheet2.createRow(rownum2++);

                            Calendar cal2 = Calendar.getInstance(Locale.ENGLISH);
                            cal2.setTimeInMillis(pothole.getTimeStamp());
                            String date2 = DateFormat.format("dd MMM yyyy", cal2).toString();
                            String time2 = DateFormat.format("hh:mm a", cal2).toString();

                            row2.createCell(0).setCellValue(date2);
                            row2.createCell(1).setCellValue(time2);
                            row2.createCell(2).setCellValue(pothole.getAddress());
                            row2.createCell(3).setCellValue(pothole.getStatus());
                            row2.createCell(4).setCellValue(pothole.getLat());
                            row2.createCell(5).setCellValue(pothole.getLon());
                            row2.createCell(6).setCellValue(pothole.getIntensity());
                            row2.createCell(7).setCellValue(pothole.getGlink());
                        }

                    }

                    try {
                        File file = new File(filesDir, "AllPothole.xlsx");
                        if (fileExists("AllPothole.xlsx"))
                            file.delete();
                        FileOutputStream outputStream = new FileOutputStream(file);
                        workbook.write(outputStream);
                        workbook.close();

                        File file2 = new File(downDir, "Pothole.xlsx");
                        if (fileExists2("Pothole.xlsx"))
                            file2.delete();
                        FileOutputStream outputStream2 = new FileOutputStream(file2);
                        workbook2.write(outputStream2);
                        workbook2.close();

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(Splash.this,MainActivity.class));
                finish();
            }
        },5000);
    }

    public boolean fileExists(String fileName) {
        File downloadsDir = getFilesDir();
        File file = new File(downloadsDir,fileName);
        return file.exists();
    }

    public boolean fileExists2(String fileName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir,fileName);
        return file.exists();
    }
}