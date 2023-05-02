package com.pdetector.android.activity;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.cardview.widget.CardView;
import androidx.core.content.FileProvider;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.pdetector.android.R;
import com.pdetector.android.api.ApiInterface;
import com.pdetector.android.models.Mailer;
import com.pdetector.android.models.MessageVersion;
import com.pdetector.android.models.Pothole;
import com.pdetector.android.models.Sender;
import com.pdetector.android.models.To;
import com.pdetector.android.utils.GpsTracker;
import com.pdetector.android.utils.PermissionUtil;
import com.pdetector.android.utils.Tools;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements PermissionUtil.PermissionsCallBack, SensorEventListener {

    private double latitude, longitude;
    private TextView PotholeText;
    private ImageButton PotholeBtn;
    private CardView HistoryBtn, SettingsBtn, AllPotholeBtn, AboutUsBtn, RateAppBtn, ShareAppBtn;
    private LinearLayout MainLayout;
    private LocationManager locationManager;
    Sensor mySensor;
    SensorManager mySensorManager;
    boolean start = false;
    int numberOfHolesAndBumbs = 0;
    float accel;
    float accelCurrent;
    float accelLast;
    int shakeReset = 10000;
    long timeStamp;
    private DatabaseReference PotholeDatabase;
    private FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
    private String GLink;
    private GpsTracker gpsTracker;
    private List<Pothole> potholeList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        PotholeText = findViewById(R.id.main_pothole_text);
        PotholeBtn = findViewById(R.id.main_pothole_btn);
        HistoryBtn = findViewById(R.id.main_history);
        SettingsBtn = findViewById(R.id.main_settings);
        AllPotholeBtn = findViewById(R.id.main_all_pothole);
        AboutUsBtn = findViewById(R.id.main_about_us);
        RateAppBtn = findViewById(R.id.main_rate_app);
        ShareAppBtn = findViewById(R.id.main_share_app);
        MainLayout = findViewById(R.id.main_layout);


        if (firebaseDatabase==null)
            firebaseDatabase.setPersistenceEnabled(true);

        PotholeDatabase = firebaseDatabase.getReference("Pothole");

        gpsTracker = new GpsTracker(MainActivity.this);
        gpsTracker.getLocation();

        if (gpsTracker.canGetLocation())
        {
            latitude = gpsTracker.getLatitude();
            longitude = gpsTracker.getLongitude();
        }
        else
        {
            gpsTracker.showSettingsAlert();
        }

        // CREATE SENSOR MANAGER
        mySensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
        // CREATE ACCELERATION SENSOR
        mySensor = mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);


        requestPermissions();

        mySensorManager.registerListener(this, mySensor, SensorManager.SENSOR_DELAY_NORMAL);

        // SETTING ACCELERATION VALUES
        accel = 0.00f;
        accelCurrent = SensorManager.GRAVITY_EARTH;
        accelLast = SensorManager.GRAVITY_EARTH;


        PotholeBtn.setOnClickListener(v -> {
            if (!Tools.isInternetConnected())
            {
                Tools.ShowNoInternetDialog(this);
            }
            else
            {
                if(!start) {
                    PotholeBtn.setImageDrawable(getDrawable(R.drawable.ic_off));
                    PotholeText.setText("Turn Off Pothole Detector");
                    Tools.showSnackbar(this,MainLayout,"Pothole Detector Running","Okay");
                    start = true;
                }
                else {
                    PotholeBtn.setImageDrawable(getDrawable(R.drawable.ic_on));
                    PotholeText.setText("Turn On Pothole Detector");
                    Tools.showSnackbar(this,MainLayout,"Pothole Detector Stopped Running","Okay");
                    start = false;
                }
            }
        });

        HistoryBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,History.class)));
        SettingsBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this, com.pdetector.android.activity.Settings.class)));
        AllPotholeBtn.setOnClickListener(v -> startActivity(new Intent(MainActivity.this,MapsActivity.class)));

        testExcel();


        if (!Tools.isInternetConnected())
        {
            Tools.ShowNoInternetDialog(this);
        }

        AboutUsBtn.setOnClickListener(view -> {
            ShowDialog2();
        });

    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {

        if (start) {
            // STORING THE VALUES OF THE AXIS
            float x = sensorEvent.values[0];
            float y = sensorEvent.values[1];
            float z = sensorEvent.values[2];
            // ACCELEROMETER LAST READ EQUAL To THE CURRENT ONE
            accelLast = accelCurrent;
            // QUICK MAFS To CALCULATE THE ACCELERATION
            accelCurrent = (float) Math.sqrt(x * x + y * y + z * z);
            // DELTA BETWEEN THE CURRENT AND THE LAST READ OF THE ACCELEROMETER
            float delta = accelCurrent - accelLast;
            // QUICK MAFS To CALCULATE THE ACCEL THAT WILL DECLARE IF IT SHAKED OR NOT
            accel = accel * 0.9f + delta;
            // DID IT SHAKE??
            int intensity_minor = 10, intensity_moderate = 20, intensity_major = 30;
            String IntensityMinorStr = Tools.getString(this,"intensity_minor");
            String IntensityModerateStr = Tools.getString(this,"intensity_moderate");
            String IntensityMajorStr = Tools.getString(this,"intensity_major");

            if (!IntensityMinorStr.isEmpty())
                intensity_minor = Integer.parseInt(IntensityMinorStr);

            if (!IntensityModerateStr.isEmpty())
                intensity_moderate = Integer.parseInt(IntensityModerateStr);

            if (!IntensityMajorStr.isEmpty())
                intensity_major = Integer.parseInt(IntensityMajorStr);

            gpsTracker.getLocation();
            if (gpsTracker.canGetLocation())
            {
                latitude = gpsTracker.getLatitude();
                longitude = gpsTracker.getLongitude();
            }
            else
            {
                gpsTracker.showSettingsAlert();
            }

            if (accel >= intensity_minor && accel<intensity_moderate) {
                final long timenow = System.currentTimeMillis();
                if(timeStamp + shakeReset  > timenow){
                    return;
                }
                String link = "http://maps.google.com/maps?q=loc:"+latitude+","+longitude;
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(timenow);
                String date = DateFormat.format("dd MMM yyyy", cal).toString();
                String time = DateFormat.format("hh:mm a", cal).toString();
                storeDataExcel(date,time,getAddress(),"Minor",""+latitude,""+longitude,""+accel,link);
                runMain(timenow,accel,"Minor");
            }
            else if (accel >= intensity_moderate && accel<=intensity_major) {
                final long timenow = System.currentTimeMillis();
                if(timeStamp + shakeReset  > timenow){
                    return;
                }
                String link = "http://maps.google.com/maps?q=loc:"+latitude+","+longitude;
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(timenow);
                String date = DateFormat.format("dd MMM yyyy", cal).toString();
                String time = DateFormat.format("hh:mm a", cal).toString();
                storeDataExcel(date,time,getAddress(),"Moderate",""+latitude,""+longitude,""+accel,link);
                runMain(timenow,accel,"Moderate");
            }
            else if (accel >intensity_major) {
                final long timenow = System.currentTimeMillis();
                if(timeStamp + shakeReset  > timenow){
                    return;
                }
                String link = "http://maps.google.com/maps?q=loc:"+latitude+","+longitude;
                Calendar cal = Calendar.getInstance(Locale.ENGLISH);
                cal.setTimeInMillis(timenow);
                String date = DateFormat.format("dd MMM yyyy", cal).toString();
                String time = DateFormat.format("hh:mm a", cal).toString();
                storeDataExcel(date,time,getAddress(),"Major",""+latitude,""+longitude,""+accel,link);
                runMain(timenow,accel,"Major");
            }
        }
    }

    private void runMain(long timenow, float accel, String status) {
        timeStamp = timenow;
        numberOfHolesAndBumbs++;
        String key = PotholeDatabase.push().getKey();
        String androidID = Settings.Secure.getString(getContentResolver(),Settings.Secure.ANDROID_ID);
        String link = "http://maps.google.com/maps?q=loc:"+latitude+","+longitude;
        double lat, lon, inten;
        inten = accel;
        lat = latitude;
        lon = longitude;
        String address = getAddress();
        String tostr = Tools.getString(this,"to");
        if (tostr.isEmpty())
            tostr = "potholesdetector@gmail.com";

        String fromstr = Tools.getString(this,"from");
        if (fromstr.isEmpty())
            fromstr = "potholesdetector@gmail.com";

        HashMap<String, Object> dataMap = new HashMap<>();
        dataMap.put("timeStamp",timeStamp);
        dataMap.put("intensity",inten);
        dataMap.put("lat",lat);
        dataMap.put("lon",lon);
        dataMap.put("address",address);
        dataMap.put("to",tostr);
        dataMap.put("from",fromstr);
        dataMap.put("deviceid",androidID);
        dataMap.put("glink",link);
        dataMap.put("id",key);
        dataMap.put("status",status);
        PotholeDatabase.child(key).updateChildren(dataMap).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void unused) {
            }
        });
        String precision_mode = Tools.getString(MainActivity.this,"precision_mode");
        if (precision_mode.equals("true"))
            ShowDialog(timenow, link, inten, lat, lon,address,status);
        else {
            Tools.showSnackbar(this,MainLayout,"Pothole Detected!!","Okay");
            SendinblueMailer(timenow,link, inten, lat, lon, address,status);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onResume() {
        super.onResume();
        mySensorManager.registerListener(this,mySensor,SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mySensorManager!=null)
            mySensorManager.unregisterListener(this);
    }

    public void requestPermissions() {
        if (PermissionUtil.checkAndRequestPermissions(this,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE)) {
        }
    }

    private String getAddress()
    {
        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
        List<Address> addresses = null;
        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (addresses != null && !addresses.isEmpty()) {
            Address address = addresses.get(0);
            String fullAddress = address.getAddressLine(0); // This will give you the full address
            return fullAddress;
        }
        return "";
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        PermissionUtil.onRequestPermissionsResult(this, requestCode, permissions, grantResults, this);
    }

    @Override
    public void permissionsGranted() {
    }

    @Override
    public void permissionsDenied() {
    }


    private void SendinblueMailer(long timenow, String link, double intensity, double lat, double lon, String address, String status){

        String str = "<!DOCTYPE html>\n" +
                "<html>\n" +
                "  <head>\n" +
                "    <title>Pothole Detector</title>\n" +
                "    <meta charset=\"utf-8\">\n" +
                "    <meta name=\"viewport\" content=\"width=device-width, initial-scale=1\">\n" +
                "    <style>\n" +
                "      /* Put your CSS styles here */\n" +
                "    </style>\n" +
                "  </head>\n" +
                "  <body style=\"background-color: #f1f1f1; font-family: Arial, sans-serif; font-size: 16px;\">\n" +
                "    <table style=\"max-width: 600px; margin: 0 auto; background-color: #ffffff; padding: 20px;\">\n" +
                "      <tr>\n" +
                "        <td>\n" +
                "          <h1 style=\"color: #2d2d2d; font-size: 24px; font-weight: bold; margin: 0;\">Pothole Detected of $$IStaus$$ intensity</h1>\n" +
                "          <p style=\"color: #555555; font-size: 16px; margin-top: 10px;\">Address : $$Address$$</p>\n" +
                "          <p style=\"color: #555555; font-size: 16px; margin-top: 10px;\">Latitude : $$Lat$$</p>\n" +
                "          <p style=\"color: #555555; font-size: 16px; margin-top: 10px;\">Longitude : $$Lon$$</p>\n" +
                "          <p style=\"color: #555555; font-size: 16px; margin-top: 10px;\">Intensity : $$Intensity$$</p>\n" +
                "          <p style=\"color: #555555; font-size: 16px; margin-top: 10px;\">Status : $$IStaus$$</p>\n" +
                "          <p style=\"color: #555555; font-size: 16px; margin-top: 10px;\">Date : $$date$$</p>\n" +
                "          <p style=\"color: #555555; font-size: 16px; margin-top: 10px;\">Time : $$time$$</p>\n" +
                "          <p style=\"color: #555555; font-size: 16px; margin-top: 10px;\">Link : $$link$$</p>\n" +
                "        </td>\n" +
                "      </tr>\n" +
                "    </table>\n" +
                "  </body>\n" +
                "</html>";

        Calendar cal = Calendar.getInstance(Locale.ENGLISH);
        cal.setTimeInMillis(timenow);
        String date = DateFormat.format("dd MMM yyyy", cal).toString();
        String time = DateFormat.format("hh:mm a", cal).toString();


        str=str.replace("$$IStaus$$", status);
        str=str.replace("$$Address$$", address);
        str=str.replace("$$Lat$$", ""+lat);
        str=str.replace("$$Lon$$", ""+lon);
        str=str.replace("$$Intensity$$", ""+intensity);
        str=str.replace("$$date$$", date);
        str=str.replace("$$time$$", time);
        str=str.replace("$$link$$", link);


        String tostr = Tools.getString(this,"to");
        if (tostr.isEmpty())
            tostr = "potholesdetector@gmail.com";

        String fromstr = Tools.getString(this,"from");
        if (fromstr.isEmpty())
            fromstr = "potholesdetector@gmail.com";

        Sender sender = new Sender(fromstr,"Pothole Detector");
        To to = new To(tostr,tostr);
        List<To> toList = new ArrayList<>();
        toList.add(to);
        MessageVersion messageVersion = new MessageVersion(toList);
        List<MessageVersion> messageVersions = new ArrayList<>();
        messageVersions.add(messageVersion);
        String subject = "Pothole Detected "+status;
        Mailer mailer = new Mailer(sender,subject,str,messageVersions);
        ApiInterface.getApiRequestInterface().sendMail(mailer).enqueue(new Callback<Mailer>() {
            @Override
            public void onResponse(Call<Mailer> call, Response<Mailer> response) {

            }

            @Override
            public void onFailure(Call<Mailer> call, Throwable t) {

            }
        });
    }

    private void ShowDialog(long timenow, String link, double intensity, double lat, double lon, String address, String status){
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this,R.style.AlertDialogCustom);
        View customLayout = getLayoutInflater().inflate(R.layout.custom_dialog, null);
        builder.setView(customLayout);
        builder
                .setCancelable(false)
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        SendinblueMailer(timenow, link, intensity, lat, lon,address, status);
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

    private void ShowDialog2(){

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        ViewGroup viewGroup = findViewById(android.R.id.content);

        View customLayout = LayoutInflater.from(this).inflate(R.layout.custom_dialog2, viewGroup, false);
        builder.setView(customLayout);

        EditText editText = customLayout.findViewById(R.id.custom_dialog2_edit);
        AppCompatButton yesbtn = customLayout.findViewById(R.id.custom_dialog2_yes);
        AppCompatButton nobtn = customLayout.findViewById(R.id.custom_dialog2_no);

        AlertDialog mDialog = builder.create();
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setCancelable(false);
        mDialog.show();

        yesbtn.setOnClickListener(view -> {
            if (editText.getText().toString().equals("123456"))
            {
                closeKeyBoard();
                mDialog.dismiss();
                ShowDialog3();
            }
            else {
                Toast.makeText(MainActivity.this, "Wrong Password", Toast.LENGTH_SHORT).show();
                editText.setError("Wrong Password");
            }
        });

        nobtn.setOnClickListener(view -> {
            mDialog.dismiss();
        });
    }

    private void ShowDialog3(){

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        ViewGroup viewGroup = findViewById(android.R.id.content);

        View customLayout = LayoutInflater.from(this).inflate(R.layout.custom_dialog4, viewGroup, false);
        builder.setView(customLayout);

        AlertDialog mDialog = builder.create();
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setCancelable(false);
        mDialog.show();

        PotholeDatabase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    potholeList = new ArrayList<>();

                    File downloadsDir = getFilesDir();
                    // Create a new Excel workbook
                    XSSFWorkbook workbook = new XSSFWorkbook();
                    // Create a new sheet
                    XSSFSheet sheet = workbook.createSheet("AllPothole");

                    Row row = sheet.createRow(0);

                    row.createCell(0).setCellValue("Date");
                    row.createCell(1).setCellValue("Time");
                    row.createCell(2).setCellValue("Address");
                    row.createCell(3).setCellValue("Type of Pothole");
                    row.createCell(4).setCellValue("Latitude");
                    row.createCell(5).setCellValue("Longitude");
                    row.createCell(6).setCellValue("Intensity");
                    row.createCell(7).setCellValue("Link");

                    int rownum = 1;
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
                    }

                    try {
                        File file = new File(downloadsDir, "AllPothole.xlsx");
                        if (fileExists2("AllPothole.xlsx"))
                            file.delete();
                        FileOutputStream outputStream = new FileOutputStream(file);
                        workbook.write(outputStream);
                        workbook.close();
                        mDialog.dismiss();
                        ShowDialog4();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void ShowDialog4(){

        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        ViewGroup viewGroup = findViewById(android.R.id.content);

        View customLayout = LayoutInflater.from(this).inflate(R.layout.custom_dialog3, viewGroup, false);
        builder.setView(customLayout);

        AppCompatButton Viewbtn = customLayout.findViewById(R.id.custom_dialog3_view);
        AppCompatButton Savebtn = customLayout.findViewById(R.id.custom_dialog3_save);

        AlertDialog mDialog = builder.create();
        mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        mDialog.setCancelable(false);
        mDialog.show();

        Viewbtn.setOnClickListener(view -> {
            mDialog.dismiss();
            File file = new File(getFilesDir() + "/AllPothole.xlsx");
            Uri fileUri = FileProvider.getUriForFile(MainActivity.this, "com.pdetector.android.provider", file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(fileUri, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            startActivity(intent);
        });

        Savebtn.setOnClickListener(view -> {
            mDialog.dismiss();
            File sourceFile = new File(getFilesDir() + "/AllPothole.xlsx");
            File destFile = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), "AllPothole.xlsx");

            InputStream in = null;
            try {
                in = new FileInputStream(sourceFile);
                OutputStream out = new FileOutputStream(destFile);

                byte[] buffer = new byte[1024];
                int read;
                while ((read = in.read(buffer)) != -1) {
                    out.write(buffer, 0, read);
                }
                in.close();
                out.close();
                Toast.makeText(this, "Saved, Check download folder (AllPothole.xlsx)", Toast.LENGTH_SHORT).show();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
    }

    private void storeDataExcel(String date, String time, String address, String typeOfHole, String latitude, String longitude, String intensity, String link)
    {
        if (fileExists("Pothole.xlsx"))
        {
            try {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                // Open the Excel file
                File file = new File(downloadsDir,"Pothole.xlsx");
                FileInputStream inputStream = new FileInputStream(file);
                XSSFWorkbook workbook = new XSSFWorkbook(inputStream);

                // Get the first sheet
                XSSFSheet sheet = workbook.getSheetAt(0);

                // Get the last row number
                int lastRowNum = sheet.getLastRowNum();

                // Create a new row after the last row
                XSSFRow newRow = sheet.createRow(lastRowNum);

                newRow.createCell(0).setCellValue(date);
                newRow.createCell(1).setCellValue(time);
                newRow.createCell(2).setCellValue(address);
                newRow.createCell(3).setCellValue(typeOfHole);
                newRow.createCell(4).setCellValue(latitude);
                newRow.createCell(5).setCellValue(longitude);
                newRow.createCell(6).setCellValue(intensity);
                newRow.createCell(7).setCellValue(link);

                // Save the changes to the Excel file
                FileOutputStream outputStream = new FileOutputStream(file);
                workbook.write(outputStream);

                // Close the workbook and output stream
                workbook.close();
                outputStream.close();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        else
        {
            File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

            // Create a new Excel workbook
            XSSFWorkbook workbook = new XSSFWorkbook();

            // Create a new sheet
            XSSFSheet sheet = workbook.createSheet("Pothole");

            // Create a header row
            Row headerRow = sheet.createRow(0);

            // Create header cells
            headerRow.createCell(0).setCellValue("Date");
            headerRow.createCell(1).setCellValue("Time");
            headerRow.createCell(2).setCellValue("Address");
            headerRow.createCell(3).setCellValue("Type of Pothole");
            headerRow.createCell(4).setCellValue("Latitude");
            headerRow.createCell(5).setCellValue("Longitude");
            headerRow.createCell(6).setCellValue("Intensity");
            headerRow.createCell(7).setCellValue("Link");

            // Create data rows
            Row dataRow1 = sheet.createRow(1);

            dataRow1.createCell(0).setCellValue(date);
            dataRow1.createCell(1).setCellValue(time);
            dataRow1.createCell(2).setCellValue(address);
            dataRow1.createCell(3).setCellValue(typeOfHole);
            dataRow1.createCell(4).setCellValue(latitude);
            dataRow1.createCell(5).setCellValue(longitude);
            dataRow1.createCell(6).setCellValue(intensity);
            dataRow1.createCell(7).setCellValue(link);


            // Write the workbook to a file
            try {
                File file = new File(downloadsDir, "Pothole.xlsx");
                FileOutputStream outputStream = new FileOutputStream(file);
                workbook.write(outputStream);
                workbook.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void testExcel(){
        // Get the Downloads folder
        /*File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);

        // Create a new Excel workbook
        XSSFWorkbook workbook = new XSSFWorkbook();

        // Create a new sheet
        XSSFSheet sheet = workbook.createSheet("Data");

        // Create a header row
        Row headerRow = sheet.createRow(0);

        // Create header cells
        Cell headerCell1 = headerRow.createCell(0);
        headerCell1.setCellValue("Name");

        Cell headerCell2 = headerRow.createCell(1);
        headerCell2.setCellValue("Age");

        // Create data rows
        Row dataRow1 = sheet.createRow(1);
        Cell dataCell1 = dataRow1.createCell(0);
        dataCell1.setCellValue("John");

        Cell dataCell2 = dataRow1.createCell(1);
        dataCell2.setCellValue(30);

        Row dataRow2 = sheet.createRow(2);
        Cell dataCell3 = dataRow2.createCell(0);
        dataCell3.setCellValue("Mary");

        Cell dataCell4 = dataRow2.createCell(1);
        dataCell4.setCellValue(25);

        // Write the workbook to a file
        try {
            File file = new File(downloadsDir, "data.xlsx");
            FileOutputStream outputStream = new FileOutputStream(file);
            workbook.write(outputStream);
            workbook.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Toast.makeText(this, "Excel file created in Downloads folder.", Toast.LENGTH_SHORT).show();*/
    }

    public boolean fileExists(String fileName) {
        File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
        File file = new File(downloadsDir,fileName);
        return file.exists();
    }
    public boolean fileExists2(String fileName) {
        File downloadsDir = getFilesDir();
        File file = new File(downloadsDir,fileName);
        return file.exists();
    }

    private void closeKeyBoard(){
        View view = this.getCurrentFocus();
        if (view != null){
            InputMethodManager imm = (InputMethodManager)
                    getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}