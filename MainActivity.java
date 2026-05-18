package com.example.freeze;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Process;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat; // Added for Service starting
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import androidx.appcompat.widget.SearchView;
import android.content.Intent;
import android.view.MenuItem;
import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class MainActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private AppAdapter appAdapter;
    private List<AppModel> appModelList;
    private DatabaseHelper dbHelper;
    private SearchView searchview;
    private BottomNavigationView bottomNavigationView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        dbHelper = new DatabaseHelper(this);
        recyclerView = findViewById(R.id.recycler);
        SearchView searchView = findViewById(R.id.searchView);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
/*
        bottomNavigationView.setOnClickListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean
            int id = item.getItemId();

            if(id ==R.id.home)

            {
                Intent intent = new Intent(MainActivity.this, MainActivity.class);
                StartActivity(intent);
                return true;
            } else if(id ==R.id.Add)

            {
                Intent intent = new Intent(MainActivity.this, Clock.class);
                StartActivity(intent);
                return true;

            }
            return false;

        });
    */

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return false; // We don't need to do anything when "Enter" is pressed
            }
            @Override
            public boolean onQueryTextChange(String newText) {
                // This runs every time the user types a letter
                filterList(newText);
                return true;
            }
        });

        recyclerView.setLayoutManager(new LinearLayoutManager(this));


        findViewById(R.id.history).setOnClickListener(v -> startActivity(new Intent(this, Active_Freeze.class)));

        loadApps();
    }


    private void loadApps() {
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> infos = pm.queryIntentActivities(intent, 0);
        appModelList = new ArrayList<>();
        for (ResolveInfo info : infos) {
            String pkg = info.activityInfo.packageName;
            // Filter out our own app and apps already in the freeze database
            if (!pkg.equals(getPackageName()) && !dbHelper.isAppFrozen(pkg)) {
                appModelList.add(new AppModel(info.loadLabel(pm).toString(), info.loadIcon(pm), pkg));
            }
        }
        appAdapter = new AppAdapter(this, appModelList);
        recyclerView.setAdapter(appAdapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Permission check loop: Won't let user proceed until both are granted
        if (!hasUsageStatsPermission()) {
            showPermissionDialog("Usage Access", Settings.ACTION_USAGE_ACCESS_SETTINGS);
        } else if (!hasOverlayPermission()) {
            showPermissionDialog("Overlay Permission", Settings.ACTION_MANAGE_OVERLAY_PERMISSION);
        } else {
            loadApps();
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private boolean hasOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return Settings.canDrawOverlays(this);
        }
        return true;
    }

    private void showPermissionDialog(String title, String action) {
        new AlertDialog.Builder(this)
                .setTitle(title)
                .setMessage("This permission is required for the freeze logic to work effectively.")
                .setPositiveButton("Grant", (d, w) -> {
                    Intent i = new Intent(action);
                    if (action.equals(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)) {
                        i.setData(Uri.parse("package:" + getPackageName()));
                    }
                    startActivity(i);
                }).setCancelable(false).show();
    }

    // Triggered when the user clicks the "Freeze" / Confirm button
    public void TimePicker(View view) {
        ArrayList<AppModel> selected = new ArrayList<>();
        for (AppModel a : appModelList) {
            if (a.is_Selected()) {
                selected.add(new AppModel(a.getAppName(), null, a.getPackageName()));
            }
        }

        if (!selected.isEmpty()) {
            // 1. UPDATE: Start the AppCheckService as a Foreground Service
            Intent serviceIntent = new Intent(this, AppCheckService.class);
            ContextCompat.startForegroundService(this, serviceIntent);

            // 2. Pass selected apps to the Clock activity to set the timer
            Intent i = new Intent(this, Clock.class);
            i.putExtra("SELECTED_APPS", selected);
            startActivity(i);
        } else {
            Toast.makeText(this, "Please select at least one app!", Toast.LENGTH_SHORT).show();
        }
    }
    // Add this method at the end of the class
    private void filterList(String text) {
        List<AppModel> filteredList = new ArrayList<>();

        // Loop through your master list
        for (AppModel app : appModelList) {
            // Check if the app name contains the search text (case-insensitive)
            if (app.getAppName().toLowerCase().contains(text.toLowerCase())) {
                filteredList.add(app);
            }
        }

        // Tell the adapter to show only the matching apps
        if (appAdapter != null) {
            appAdapter.setFilteredList(filteredList);
        }
    }

}