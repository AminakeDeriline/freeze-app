package com.example.freeze;

import android.app.TimePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;

public class Clock extends AppCompatActivity {
    private Button timeButton;
    private int selectedHour = -1, selectedMinute = -1;
    private ArrayList<AppModel> selectedApps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_clock);



        selectedApps = (ArrayList<AppModel>) getIntent().getSerializableExtra("SELECTED_APPS");
        timeButton = findViewById(R.id.timeButton);

        timeButton.setOnClickListener(v -> {
            // FORCE the picker to use the phone's Local Timezone
            Calendar c = Calendar.getInstance(TimeZone.getDefault());

            TimePickerDialog picker = new TimePickerDialog(this, (view, hourOfDay, minute) -> {
                selectedHour = hourOfDay;
                selectedMinute = minute;

                String amPm = (hourOfDay >= 12) ? "PM" : "AM";
                int displayHour = (hourOfDay > 12) ? hourOfDay - 12 : (hourOfDay == 0 ? 12 : hourOfDay);
                timeButton.setText(String.format(Locale.US, "%02d:%02d %s", displayHour, minute, amPm));

            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), false);

            picker.show();
        });
    }

    public void TimeConfirm(View view) {
        if (selectedHour == -1) return;

        // Calendar.getInstance() automatically detects your Cameroon local time
        Calendar target = Calendar.getInstance();
        target.set(Calendar.HOUR_OF_DAY, selectedHour);
        target.set(Calendar.MINUTE, selectedMinute);
        target.set(Calendar.SECOND, 0);
        target.set(Calendar.MILLISECOND, 0);

        // If the time already passed today, set it for tomorrow
        if (target.getTimeInMillis() <= System.currentTimeMillis()) {
            target.add(Calendar.DATE, 1);
        }

        DatabaseHelper db = new DatabaseHelper(this);
        db.addApps(selectedApps,target.getTimeInMillis());

        // Save the absolute moment in time to the database
        db.saveTargetMillis(target.getTimeInMillis());

        startActivity(new Intent(this, Active_Freeze.class));
        finish();
    }

}