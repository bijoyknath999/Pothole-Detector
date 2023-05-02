package com.pdetector.android.activity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import android.os.Bundle;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.pdetector.android.R;
import com.pdetector.android.utils.Tools;

public class Settings extends AppCompatActivity {
    
    private EditText IntensityMinorEdit, IntensityModerateEdit, IntensityMajorEdit, FromEdit, ToEdit;
    private Button SaveBtn;
    private LinearLayout MainLayout;
    private SwitchCompat switchCompat;
    private ImageView BackBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        
        IntensityMinorEdit = findViewById(R.id.settings_intensity_minor);
        IntensityModerateEdit = findViewById(R.id.settings_intensity_moderate);
        IntensityMajorEdit = findViewById(R.id.settings_intensity_major);
        FromEdit = findViewById(R.id.settings_from);
        ToEdit = findViewById(R.id.settings_to);
        SaveBtn = findViewById(R.id.settings_save_btn);
        MainLayout = findViewById(R.id.settings_main_layout);
        switchCompat = findViewById(R.id.settings_precision_mode);
        BackBtn = findViewById(R.id.settings_toolbar_back);

        String IntensityMinor = Tools.getString(this,"intensity_minor");
        String IntensityModerate = Tools.getString(this,"intensity_moderate");
        String IntensityMajor = Tools.getString(this,"intensity_major");
        String From = Tools.getString(this,"from");
        String To = Tools.getString(this,"to");
        String precision_mode = Tools.getString(this,"precision_mode");

        if (precision_mode.equals("true"))
            switchCompat.setChecked(true);
        else
            switchCompat.setChecked(false);

        switchCompat.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                Tools.saveString(Settings.this,"precision_mode",String.valueOf(isChecked));
            }
        });

        if (!IntensityMinor.isEmpty())
            IntensityMinorEdit.setText(IntensityMinor);
        else
            IntensityMinorEdit.setText("10");

        if (!IntensityModerate.isEmpty())
            IntensityModerateEdit.setText(IntensityModerate);
        else
            IntensityModerateEdit.setText("20");

        if (!IntensityMajor.isEmpty())
            IntensityMajorEdit.setText(IntensityMajor);
        else
            IntensityMajorEdit.setText("30");

        if (!From.isEmpty())
            FromEdit.setText(From);
        else
            FromEdit.setText("potholesdetector@gmail.com");
        if (!To.isEmpty())
            ToEdit.setText(To);
        else
            ToEdit.setText("potholesdetector@gmail.com");


        SaveBtn.setOnClickListener(v -> {
            String intensity_minor = IntensityMinorEdit.getText().toString().trim();
            String intensity_moderate = IntensityModerateEdit.getText().toString().trim();
            String intensity_major = IntensityMajorEdit.getText().toString().trim();
            String from = FromEdit.getText().toString().trim();
            String to = ToEdit.getText().toString().trim();

            if (intensity_minor.isEmpty())
                IntensityMinorEdit.setError("Minor Intensity is empty!!!");
            else if (intensity_moderate.isEmpty())
                IntensityModerateEdit.setError("Moderate Intensity is empty!!!");
            else if (intensity_major.isEmpty())
                IntensityMajorEdit.setError("Major Intensity is empty!!!");
            else if (from.isEmpty())
                FromEdit.setError("From is empty!!!");
            else if (to.isEmpty())
                ToEdit.setError("To is empty!!!");
            else
            {
                SaveData(intensity_minor,intensity_moderate,intensity_major,from,to);
            }
        });

        BackBtn.setOnClickListener(v -> onBackPressed());
    }

    private void SaveData(String intensity_minor, String intensity_moderate, String intensity_major, String from, String to) {
        Tools.saveString(this,"intensity_minor", intensity_minor);
        Tools.saveString(this,"intensity_moderate", intensity_moderate);
        Tools.saveString(this,"intensity_major", intensity_major);
        Tools.saveString(this,"from", from);
        Tools.saveString(this,"to", to);
        Tools.showSnackbar(this, MainLayout,"Saved Successfully...","Okay");
    }
}