package com.example.freeze;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;

public class Active_Freeze extends AppCompatActivity {

    private DatabaseHelper db;
    private ArrayList<AppModel> frozenApps;
    private ActiveAdapter adapter;
    private TextView emptyText;
    private ImageView boxIcon;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_freeze);
        emptyText = findViewById(R.id.emptyText);
        boxIcon = findViewById(R.id.boxIcon);
        RecyclerView rv = findViewById(R.id.recycler_active_apps);


        db = new DatabaseHelper(this);

        // NAVIGATION FIX: Back button takes you to MainActivity
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(Active_Freeze.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            }
        });

        // Load frozen apps and their icons
        frozenApps = db.getFrozenApps();
        checkEmptyState();

        PackageManager pm = getPackageManager();
        for (AppModel app : frozenApps) {
            try {
                app.setAppIcon(pm.getApplicationIcon(app.getPackageName()));
                app.setAppName(pm.getApplicationLabel(pm.getApplicationInfo(app.getPackageName(), 0)).toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        RecyclerView recyclerView = findViewById(R.id.recycler_active_apps);
        rv.setLayoutManager(new LinearLayoutManager(this));
        adapter = new ActiveAdapter(frozenApps);
        rv.setAdapter(adapter);
    }
    public void checkEmptyState(){
        if (frozenApps.isEmpty()) {
            emptyText.setVisibility(View.VISIBLE);
            boxIcon.setVisibility(View.VISIBLE);
        } else {
            emptyText.setVisibility(View.GONE);
            boxIcon.setVisibility(View.GONE);
        }

    }
}