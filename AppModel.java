package com.example.freeze;

import android.graphics.drawable.Drawable;
import java.io.Serializable;

public class AppModel implements Serializable {
    private String appName;
    private transient Drawable appIcon; // transient prevents crashes during Intent passing
    private String packageName;
    private boolean is_Selected;
    private long targetTime;

    public AppModel(String appName, Drawable appIcon, String packageName) {
        this.appName = appName;
        this.appIcon = appIcon;
        this.packageName = packageName;
        this.is_Selected = false;
    }

    // --- GETTERS AND SETTERS ---

    public String getAppName() { return appName; }
    public void setAppName(String appName) { this.appName = appName; }

    public Drawable getAppIcon() { return appIcon; }
    public void setAppIcon(Drawable appIcon) { this.appIcon = appIcon; }

    public String getPackageName() { return packageName; }
    public void setPackageName(String packageName) { this.packageName = packageName; }

    public boolean is_Selected() {
        return is_Selected;
    }

    // Method 1: Matches setIs_Selected(isChecked)
    public void setIs_Selected(boolean is_Selected) {
        this.is_Selected = is_Selected;
    }

    // Method 2: Matches setSelected(newState)
    public void setSelected(boolean newState) {
        this.is_Selected = newState;
    }
    public long getTargetTime() {
        return targetTime;
    }

    public void setTargetTime(long targetTime) {
        this.targetTime = targetTime;
    }
}